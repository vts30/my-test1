package de.vts.monitor.forum.controller;

import de.vts.monitor.forum.db.MissingBackupRecord;
import de.vts.monitor.forum.service.BackupChecker;
import de.vts.monitor.forum.service.MissingBackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missing-backups")
public class MissingBackupController {

    private final MissingBackupService missingBackupService;
    private final BackupChecker backupChecker;

    public MissingBackupController(MissingBackupService missingBackupService, BackupChecker backupChecker) {
        this.missingBackupService = missingBackupService;
        this.backupChecker = backupChecker;
    }

    // GET /api/missing-backups
    @GetMapping
    public List<MissingBackupRecord> listMissing() {
        return missingBackupService.getAllMissing();
    }

    // PUT /api/missing-backups/{id}/resolve
    @PutMapping("/{id}/resolve")
    public ResponseEntity<MissingBackupRecord> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(missingBackupService.resolve(id));
    }

    // POST /api/missing-backups/trigger
    @PostMapping("/trigger")
    public ResponseEntity<String> trigger() {
        backupChecker.checkMissingBackups();
        return ResponseEntity.ok("Backup check triggered");
    }

    // POST /api/missing-backups/trigger/{mandant}?instance=forumsuite
    @PostMapping("/trigger/{mandant}")
    public ResponseEntity<String> triggerForMandant(
            @PathVariable String mandant,
            @RequestParam(defaultValue = "forumsuite") String instance) {
        missingBackupService.notifyExternalService(mandant, instance);
        return ResponseEntity.ok("External service notified for mandant=" + mandant + ", instance=" + instance);
    }
}
