package com.example.budget_management_app.common.event.service;

import com.example.budget_management_app.common.exception.InternalException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailServiceHelper {

    public String loadTemplate(String templateName) {
        try (InputStream inputStream = getClass().getResourceAsStream("/email/" + templateName)) {
            if (inputStream == null) {
                throw new InternalException("Template not found: " + templateName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InternalException("Failed to load template: " + templateName, e);
        }
    }

    public String buildContent(String template, Map<String, String> placeholders) {
        String content = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return content;
    }
}
