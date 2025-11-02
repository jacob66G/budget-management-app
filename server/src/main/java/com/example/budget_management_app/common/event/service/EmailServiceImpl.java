package com.example.budget_management_app.common.event.service;

import com.example.budget_management_app.common.exception.EmailException;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.constants.ApiPaths;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender sender;
    private final EmailServiceHelper emailServiceHelper;
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendVerificationEmail(String userEmail, String userName, String verificationCode, boolean resend) {
        String resourceHtml = resend ? "html/resend-verification.html" : "html/verification.html";
        String resourceText = resend ? "text/resend-verification.txt" : "text/verification.txt";

        String htmlTemplate = emailServiceHelper.loadTemplate(resourceHtml);
        String textTemplate = emailServiceHelper.loadTemplate(resourceText);

        String verificationLink = UriComponentsBuilder
                .fromPath(ApiPaths.BASE_URL)
                .pathSegment(ApiPaths.BASE_API, ApiPaths.AUTH, ApiPaths.VERIFY)
                .queryParam("email", userEmail)
                .queryParam("code", verificationCode)
                .build().toUriString();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", userName);
        placeholders.put("verificationLink", verificationLink);

        String htmlContent = emailServiceHelper.buildContent(htmlTemplate, placeholders);
        String textContent = emailServiceHelper.buildContent(textTemplate, placeholders);

        send(userEmail, "Account verification", htmlContent, textContent);
    }

    @Override
    public void sendResetPasswordEmail(String userEmail, String userName, String token) {
        String resourceHtml = "html/reset-password.html";
        String resourceText = "text/reset-password.txt";

        String htmlTemplate = emailServiceHelper.loadTemplate(resourceHtml);
        String textTemplate = emailServiceHelper.loadTemplate(resourceText);

        String resetPasswordLink = UriComponentsBuilder
                .fromPath(ApiPaths.BASE_URL)
                .pathSegment(ApiPaths.BASE_API, ApiPaths.AUTH, ApiPaths.RESET_PASSWORD)
                .queryParam("email", userEmail)
                .queryParam("code", token)
                .build().toUriString();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", userName);
        placeholders.put("resetPasswordLink", resetPasswordLink);

        String htmlContent = emailServiceHelper.buildContent(htmlTemplate, placeholders);
        String textContent = emailServiceHelper.buildContent(textTemplate, placeholders);

        send(userEmail, "Reset password", htmlContent, textContent);
    }

    private void send(String to, String subject, String htmlContent, String textContent) {
        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, htmlContent);
            sender.send(message);
            log.info("Email send to: {}", to);
        } catch (MessagingException e) {
            throw new EmailException("Failed to send email to " + to, ErrorCode.EMAIL_SEND_FAIL, e);
        }
    }
}
