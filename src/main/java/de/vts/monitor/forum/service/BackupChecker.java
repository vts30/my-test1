package de.vts.monitor.forum.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BackupChecker {

    private static final Logger log = LoggerFactory.getLogger(BackupChecker.class);
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    private final BackupService backupService;
    private final EmailService emailService;
    private final MissingBackupService missingBackupService;

    public BackupChecker(BackupService backupService, EmailService emailService, MissingBackupService missingBackupService) {
        this.backupService = backupService;
        this.emailService = emailService;
        this.missingBackupService = missingBackupService;
    }

    @Scheduled(cron = "#{@bspConfiguration.zipCheckupCron}")
    public void checkMissingBackups() {
        log.info("###############################################################");
        log.info("Starting backup check...");
        missingBackupService.deleteAllMissing();

        try {
            Map<Integer, Map<String, String>> outdated = backupService.getOutdatedMandants();
            int noOfOutdatedMandant = 0;

            if (!outdated.isEmpty()) {
                StringBuilder logMsg = new StringBuilder();
                StringBuilder emailMsg = new StringBuilder();

                for (Map.Entry<Integer, Map<String, String>> entry : outdated.entrySet()) {
                    int daysOld = entry.getKey();
                    Map<String, String> mandantFiles = entry.getValue();

                    // LOG message with ANSI red
                    logMsg.append(String.format("%s%sMandants with the last backup older than %d days:%s%s\n",
                            ANSI_RED, daysOld > 1 ? "s" : "", daysOld, " : ", ANSI_RESET));

                    for (Map.Entry<String, String> mandantEntry : mandantFiles.entrySet()) {
                        String mandant = mandantEntry.getKey();
                        String file = mandantEntry.getValue();
                        logMsg.append(String.format("  - Mandant: %-30s Last Backup File: %s%n", mandant, file));
                        noOfOutdatedMandant++; // increment for next iteration
                    }

                    logMsg.append("\n");

                    // EMAIL message using HTML
                    emailMsg.append(String.format(
                            "<p><span style='color:red'>Mandants with the last backup older than %d days:</span></p>",
                            daysOld));
                    emailMsg.append("<ul>");
                    mandantFiles.forEach((mandant, file) ->
                            emailMsg.append(String.format("<li>Mandant: %s + Last Backup File: %s</li>", mandant, file)));
                    emailMsg.append("</ul><br/>");
                }

                // Log warning
                log.warn(logMsg.toString());

                // Send email as HTML
                emailService.sendSimpleMail(emailMsg.toString());
            }

            log.info("Backup check completed. Total affected mandants: {}", noOfOutdatedMandant);
        } catch (Exception e) {
            log.error("Unexpected error during backup check: {}", e.getMessage(), e);
        }

        log.info("###############################################################");
    }
}
