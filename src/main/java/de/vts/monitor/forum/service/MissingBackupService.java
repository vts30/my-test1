package de.vts.monitor.forum.service;

import de.vts.monitor.forum.config.BspConfiguration;
import de.vts.monitor.forum.db.MissingBackupRecord;
import de.vts.monitor.forum.db.MissingBackupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MissingBackupService {

    private static final Logger log = LoggerFactory.getLogger(MissingBackupService.class);

    private final MissingBackupRepository repository;
    private final BspConfiguration bspConfiguration;
    private final RestTemplate restTemplate = new RestTemplate();

    public MissingBackupService(MissingBackupRepository repository, BspConfiguration bspConfiguration) {
        this.repository = repository;
        this.bspConfiguration = bspConfiguration;
    }

    public void processMissing(String mandant, String instance) {
        boolean alreadyMissing = repository.findByMandantAndInstance(mandant, instance)
                .filter(r -> "MISSING".equals(r.getStatus()))
                .isPresent();

        if (!alreadyMissing) {
            MissingBackupRecord record = new MissingBackupRecord(mandant, instance);
            repository.save(record);
            log.warn("Missing backup recorded in DB: mandant={}, instance={}", mandant, instance);
            callExternalService(record);
        }
    }

    public List<MissingBackupRecord> getAllMissing() {
        return repository.findByStatus("MISSING");
    }

    public MissingBackupRecord resolve(Long id) {
        MissingBackupRecord record = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found: " + id));
        record.setStatus("RESOLVED");
        record.setResolvedAt(LocalDateTime.now());
        return repository.save(record);
    }

    private void callExternalService(MissingBackupRecord record) {
        String url = bspConfiguration.getExternalService().getUrl();
        if (url == null || url.isBlank()) {
            log.info("No external service URL configured, skipping notification for mandant={}", record.getMandant());
            return;
        }
        try {
            restTemplate.postForObject(url, record, String.class);
            log.info("External service notified for mandant={}, instance={}", record.getMandant(), record.getInstance());
        } catch (Exception e) {
            log.error("Failed to call external service for mandant={}: {}", record.getMandant(), e.getMessage());
        }
    }
}
