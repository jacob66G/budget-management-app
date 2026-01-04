package com.example.budget_management_app.common.mail;

import com.example.budget_management_app.common.exception.EmailException;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.constants.ApiPaths;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;


@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender sender;
    private final TemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendVerificationEmail(String userEmail, String userName, String verificationCode, boolean resend) {
        String resourceHtml = resend ? "email/html/resend-verification" : "email/html/verification";
        String resourceText = resend ? "email/text/resend-verification" : "email/text/verification";

        String verificationLink = UriComponentsBuilder
                .fromPath(ApiPaths.CLIENT_BASE_URL)
                .pathSegment(ApiPaths.VERIFY)
                .queryParam("code", verificationCode)
                .build().toUriString();

        Context context = new Context();
        context.setVariable("name", userName);
        context.setVariable("verificationLink", verificationLink);

        String htmlContent = templateEngine.process(resourceHtml, context);
        String textContent = templateEngine.process(resourceText, context);

        send(userEmail, "Account verification", htmlContent, textContent, null, null);
    }

    @Override
    public void sendResetPasswordEmail(String userEmail, String userName, String token) {
        String resetPasswordLink = UriComponentsBuilder
                .fromPath(ApiPaths.CLIENT_BASE_URL)
                .pathSegment(ApiPaths.RESET_PASSWORD)
                .queryParam("code", token)
                .build().toUriString();

        Context context = new Context();
        context.setVariable("name", userName);
        context.setVariable("resetPasswordLink", resetPasswordLink);

        String htmlContent = templateEngine.process("email/html/reset-password", context);
        String textContent = templateEngine.process("email/text/reset-password", context);

        send(userEmail, "Reset password", htmlContent, textContent, null, null);
    }

    @Override
    public void sendFinancialReport(String userEmail, String userName, byte[] pdfData) {
        Context context = new Context();
        context.setVariable("userName", userName);

        String htmlContent = templateEngine.process("email/html/financial-report", context);
        String textContent = templateEngine.process("email/text/financial-report", context);

        send(userEmail, "Your financial report", htmlContent, textContent, pdfData, "Financial_Report_" + LocalDate.now() + ".pdf");
    }

    private void send(String to, String subject, String htmlContent, String textContent, byte[] attachmentData, String attachmentName) {
        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, htmlContent);

            if (attachmentData != null && attachmentData.length > 0) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachmentData));
            }

            sender.send(message);
            log.info("Email send to: {}", to);
        } catch (MessagingException e) {
            throw new EmailException("Failed to send email to " + to, ErrorCode.EMAIL_SEND_FAILED, e);
        }
    }
}
