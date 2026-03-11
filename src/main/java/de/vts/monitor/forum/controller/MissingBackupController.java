package de.vts.monitor.forum.controller;

import de.vts.monitor.forum.db.MissingBackupRecord;
import de.vts.monitor.forum.service.MissingBackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missing-backups")
public class MissingBackupController {

    private final MissingBackupService missingBackupService;

    public MissingBackupController(MissingBackupService missingBackupService) {
        this.missingBackupService = missingBackupService;
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
}
