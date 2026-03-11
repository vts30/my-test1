package de.vts.monitor.forum.integration;

import de.vts.monitor.forum.config.BspConfiguration;
import de.vts.monitor.forum.model.BackupFileInfo;
import de.vts.monitor.forum.service.MinioClientFactory;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MinioBackupReader {

    private static final Logger log = LoggerFactory.getLogger(MinioBackupReader.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BspConfiguration bspConfiguration;
    private final MinioClientFactory minioClientFactory;

    public MinioBackupReader(BspConfiguration bspConfiguration, MinioClientFactory minioClientFactory) {
        this.bspConfiguration = bspConfiguration;
        this.minioClientFactory = minioClientFactory;
    }

    public MinioClient createClient() {
        return minioClientFactory.createClient();
    }

    public List<String> fetchMandantList(MinioClient client) {
        List<String> mandants = new ArrayList<>();
        try {
            var args = ListObjectsArgs.builder()
                    .bucket(bspConfiguration.getDih().getS3().getBucket())
                    .prefix(bspConfiguration.getDih().getS3().getPrefix() + "/")
                    .build();

            String fusionList = bspConfiguration.getFusionList();
            Set<String> fusionMandants = Arrays.stream(fusionList.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            for (Result<Item> result : client.listObjects(args)) {
                String objectName = result.get().objectName();
                Arrays.stream(objectName.split("/"))
                        .filter(p -> p.startsWith("mandant="))
                        .map(p -> p.substring("mandant=".length()))
                        .filter(m -> !fusionMandants.contains(m))
                        .findFirst()
                        .ifPresent(mandants::add);
            }
        } catch (Exception e) {
            log.error("Error fetching mandants: {}", e.getMessage(), e);
        }
        return mandants.stream().distinct().collect(Collectors.toList());
    }

    public BackupFileInfo getLatestZipInfo(String mandant, String instance, MinioClient client) {
        try {
            var args = ListObjectsArgs.builder()
                    .bucket(bspConfiguration.getDih().getS3().getBucket())
                    .prefix(String.format("%s/mandant=%s/instance=%s",
                            bspConfiguration.getDih().getS3().getPrefix(), mandant, instance))
                    .recursive(true)
                    .build();

            List<String> filenames = new ArrayList<>();
            String mandantId = null;

            for (Result<Item> result : client.listObjects(args)) {
                String objectName = result.get().objectName();
                String[] parts = objectName.split("/");
                if (parts.length < 3) continue;
                String filename = parts[parts.length - 1];
                filenames.add(filename);
                if (mandantId == null && parts.length >= 3) {
                    mandantId = parts[1] + "_" + parts[2];
                }
            }

            List<String> validFilenames = filenames.stream()
                    .filter(name -> {
                        try {
                            LocalDate.parse(name.substring(7, 15), DATE_FORMATTER);
                            return true;
                        } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                            log.warn("Skipping file due to parse error: {}", name);
                            return false;
                        }
                    })
                    .sorted((a, b) -> {
                        String dateA = a.substring(7, 15);
                        String dateB = b.substring(7, 15);
                        return LocalDate.parse(dateB, DATE_FORMATTER)
                                .compareTo(LocalDate.parse(dateA, DATE_FORMATTER));
                    })
                    .collect(Collectors.toList());

            if (validFilenames.isEmpty()) return null;

            return new BackupFileInfo(mandantId, validFilenames.get(0));
        } catch (Exception e) {
            log.error("Error fetching zip info for mandant={} instance={}: {}", mandant, instance, e.getMessage(), e);
            return null;
        }
    }
}
