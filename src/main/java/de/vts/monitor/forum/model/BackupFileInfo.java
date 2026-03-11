package de.vts.monitor.forum.model;

public class BackupFileInfo {

    private String mandantId;
    private String backupFileName;

    public BackupFileInfo(String mandantId, String backupFileName) {
        this.mandantId = mandantId;
        this.backupFileName = backupFileName;
    }

    public String getMandantId() {
        return mandantId;
    }

    public void setMandantId(String mandantId) {
        this.mandantId = mandantId;
    }

    public String getBackupFileName() {
        return backupFileName;
    }

    public void setBackupFileName(String backupFileName) {
        this.backupFileName = backupFileName;
    }
}
