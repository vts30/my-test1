package de.vts.monitor.forum.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @Value("${spring.mail.to}")
    private String toAddress;

    @Value("${spring.mail.subject}")
    private String subject;

    public void sendSimpleMail(String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toAddress.split("\\s*,\\s*"));
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Backup report email sent to {}", toAddress);
        } catch (MessagingException e) {
            // original checked-exception handling
            log.error("Failed to send backup report email (MessagingException): {}", e.getMessage(), e);
        } catch (Exception ex) {
            // handle runtime exceptions (e.g. mocked RuntimeException in tests, unexpected runtime errors)
            log.error("Failed to send backup report email (unexpected): {}", ex.getMessage(), ex);
        }
    }
}
