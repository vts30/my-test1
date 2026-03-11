package de.vts.monitor.forum.service;

import de.vts.monitor.forum.config.BspConfiguration;
import de.vts.monitor.forum.integration.MinioBackupReader;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern BACKUP_TS_PATTERN = Pattern.compile("backup_(\\d{8}_\\d{6})\\.zip");

    private final BspConfiguration bspConfiguration;
    private final MinioBackupReader backupReader;
    private final MissingBackupService missingBackupService;

    public BackupService(BspConfiguration bspConfiguration, MinioBackupReader backupReader,
                         MissingBackupService missingBackupService) {
        this.bspConfiguration = bspConfiguration;
        this.backupReader = backupReader;
        this.missingBackupService = missingBackupService;
    }

    public Map<Integer, Map<String, String>> getOutdatedMandants() throws Exception {
        MinioClient client = backupReader.createClient();
        List<String> mandants = backupReader.fetchMandantList(client);

        log.info("Found {} mandants.", mandants);

        Map<String, String> latestFiles = new HashMap<>();
        for (String mandant : mandants) {
            if (bspConfiguration.getZam().getMandantList() != null
                    && bspConfiguration.getZam().getMandantList().contains(mandant)) {
                for (String instance : bspConfiguration.getZam().getInstanceList()) {
                    var info = backupReader.getLatestZipInfo(mandant, instance, client);
                    if (info != null) latestFiles.put(info.getMandantId(), info.getBackupFileName());
                    else missingBackupService.processMissing(mandant, instance);
                }
            } else {
                var info = backupReader.getLatestZipInfo(mandant, "forumsuite", client);
                if (info != null) latestFiles.put(info.getMandantId(), info.getBackupFileName());
                else missingBackupService.processMissing(mandant, "forumsuite");
            }
        }

        return getOutdatedMandantsGroupedByAge(latestFiles);
    }

    private SortedMap<Integer, Map<String, String>> getOutdatedMandantsGroupedByAge(Map<String, String> fileMap) {
        record Entry(String mandant, String filename, int age) {}

        return fileMap.entrySet().stream()
                .map(e -> new Entry(e.getKey(), e.getValue(), extractAgeInDays(e.getValue())))
                .filter(e -> e.age() > 0)
                .collect(Collectors.groupingBy(
                        Entry::age,
                        TreeMap::new,
                        Collectors.toMap(
                                Entry::mandant,
                                Entry::filename,
                                (a, b) -> a,
                                LinkedHashMap::new
                        )
                ));
    }

    private int extractAgeInDays(String fileName) {
        try {
            Matcher m = BACKUP_TS_PATTERN.matcher(fileName);
            if (m.find()) {
                String ts = m.group(1); // "yyyyMMdd_HHmmss"
                LocalDateTime backupTime = LocalDateTime.parse(ts, TS_FORMATTER);
                LocalDate backupDate = backupTime.toLocalDate();
                LocalDate referenceDate = LocalDate.now().minusDays(1); // automatically use yesterday
                long days = ChronoUnit.DAYS.between(backupDate, referenceDate);
                return (int) Math.max(days, 0);
            }
        } catch (Exception ex) {
            System.err.println("Could not parse backup timestamp from file: " + fileName + " (" + ex.getMessage() + ")");
        }
        return -1;
    }
}
