package de.vts.monitor.forum.db;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "missing_backup")
public class MissingBackupRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mandant;

    @Column(nullable = false)
    private String instance;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @Column(nullable = false)
    private String status; // MISSING, RESOLVED

    private LocalDateTime resolvedAt;

    private String lastBackupFile;

    private Integer daysOld;

    public MissingBackupRecord() {}

    public MissingBackupRecord(String mandant, String instance) {
        this.mandant = mandant;
        this.instance = instance;
        this.detectedAt = LocalDateTime.now();
        this.status = "MISSING";
    }

    public MissingBackupRecord(String mandant, String instance, String lastBackupFile, int daysOld) {
        this(mandant, instance);
        this.lastBackupFile = lastBackupFile;
        this.daysOld = daysOld;
    }

    public Long getId() { return id; }
    public String getMandant() { return mandant; }
    public String getInstance() { return instance; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public String getStatus() { return status; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public String getLastBackupFile() { return lastBackupFile; }
    public Integer getDaysOld() { return daysOld; }

    public void setStatus(String status) { this.status = status; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setLastBackupFile(String lastBackupFile) { this.lastBackupFile = lastBackupFile; }
    public void setDaysOld(Integer daysOld) { this.daysOld = daysOld; }
}
