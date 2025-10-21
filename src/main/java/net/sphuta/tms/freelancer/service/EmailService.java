package net.sphuta.tms.freelancer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


/**
 * ==============================================================
 * ✉️ EmailService
 * ==============================================================
 * Backward-compatible email service with support for:
 *  - simple text emails (existing functionality)
 *  - emails with attachments (PDFs or other files)
 *
 * Uses Spring Boot's JavaMailSender if configured in application.yml,
 * otherwise defaults to simple console logging (dev mode).
 * ==============================================================
 */
@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;  // Optional injection

    /**
     * ==============================================================
     * 📨 Existing method (unchanged behavior)
     * ==============================================================
     * Sends a basic text-only email.
     * For dev mode (no mail config), logs to console.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    plain text body
     * ==============================================================
     */
    public void send(String to, String subject, String body) {
        if (mailSender == null) {
            // Preserve old behavior — just log to console in dev mode
            System.out.println("=== Sending Email ===\nTo: " + to + "\nSubject: " + subject + "\nBody: " + body);
            log.info("📧 [DEV MODE] Simulated email sent to {} with subject '{}'", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);
            log.info("✅ Email successfully sent to {} with subject '{}'", to, subject);

        } catch (MessagingException | MailException e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * ==============================================================
     * 📎 NEW: Send email with attachment (PDF or any file)
     * ==============================================================
     *
     * @param to           recipient email address
     * @param subject      subject of the email
     * @param body         email body text
     * @param attachment   byte[] array of the file to attach
     * @param fileName     name of the file attachment (e.g., "invoice.pdf")
     * ==============================================================
     */
    public void sendWithAttachment(String to,
                                   String subject,
                                   String body,
                                   byte[] attachment,
                                   String fileName) {
        if (mailSender == null) {
            // Fallback: Dev mode → just log instead of sending
            System.out.println("""
                    === Sending Email with Attachment (DEV MODE) ===
                    To: %s
                    Subject: %s
                    Body: %s
                    Attachment: %s (%d bytes)
                    """.formatted(to, subject, body, fileName, (attachment != null ? attachment.length : 0)));
            log.info("📧 [DEV MODE] Simulated email with attachment '{}' sent to {}", fileName, to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            if (attachment != null && fileName != null) {
                helper.addAttachment(fileName, new ByteArrayResource(attachment));
            }

            mailSender.send(message);
            log.info("✅ Email with attachment '{}' successfully sent to {}", fileName, to);

        } catch (MessagingException | MailException e) {
            log.error("❌ Failed to send email with attachment to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * ==============================================================
     * 📄 NEW: Send HTML email with attachment
     * ==============================================================
     * Useful for branded templates or invoice summary emails.
     *
     * @param to         recipient
     * @param subject    subject line
     * @param htmlBody   HTML-formatted content
     * @param attachment PDF or file to attach
     * @param fileName   attachment name
     * ==============================================================
     */
    public void sendHtmlWithAttachment(String to,
                                       String subject,
                                       String htmlBody,
                                       byte[] attachment,
                                       String fileName) {
        if (mailSender == null) {
            System.out.println("""
                    === Sending HTML Email (DEV MODE) ===
                    To: %s
                    Subject: %s
                    HTML Body: %s
                    Attachment: %s
                    """.formatted(to, subject, htmlBody, fileName));
            log.info("📧 [DEV MODE] Simulated HTML email sent to {}", to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (attachment != null && fileName != null) {
                helper.addAttachment(fileName, new ByteArrayResource(attachment));
            }

            mailSender.send(message);
            log.info("✅ HTML email with attachment '{}' successfully sent to {}", fileName, to);

        } catch (MessagingException | MailException e) {
            log.error("❌ Failed to send HTML email to {}: {}", to, e.getMessage(), e);
        }
    }
}
