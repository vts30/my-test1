package de.vts.monitor.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "de.vts.monitor.forum")
@EnableScheduling
public class BspForumBackupMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BspForumBackupMonitorApplication.class, args);
    }

}
