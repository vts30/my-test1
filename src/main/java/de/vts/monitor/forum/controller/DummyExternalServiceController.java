package de.vts.monitor.forum.controller;

import de.vts.monitor.forum.db.MissingBackupRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dummy-external-service")
public class DummyExternalServiceController {

    private static final Logger log = LoggerFactory.getLogger(DummyExternalServiceController.class);

    @PostMapping
    public ResponseEntity<String> receive(@RequestBody MissingBackupRecord record) {
        log.info("Dummy external service received: mandant={}, instance={}, lastBackupFile={}, daysOld={}",
                record.getMandant(), record.getInstance(), record.getLastBackupFile(), record.getDaysOld());
        return ResponseEntity.ok("Backup creation triggered for mandant=" + record.getMandant() + ", instance=" + record.getInstance());
    }
}
