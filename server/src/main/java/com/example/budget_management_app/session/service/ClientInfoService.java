package com.example.budget_management_app.session.service;

import com.example.budget_management_app.session.domain.DeviceType;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class ClientInfoService {

    private UserAgentAnalyzer userAgentAnalyzer;

    @PostConstruct
    public void init() {
        long start = System.currentTimeMillis();
        this.userAgentAnalyzer = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
        log.info("UserAgentAnalyzer initialized in {} ms", System.currentTimeMillis() - start);
    }

    public ClientMetadata extract(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgentString = request.getHeader("User-Agent");

        UserAgent agent = userAgentAnalyzer.parse(userAgentString);

        String deviceClass = agent.getValue(UserAgent.DEVICE_CLASS);
        String deviceName = agent.getValue(UserAgent.DEVICE_NAME);
        String osName = agent.getValue(UserAgent.OPERATING_SYSTEM_NAME);
        String browserName = agent.getValue(UserAgent.AGENT_NAME);

        DeviceType type = mapToDeviceType(deviceClass);

        String formattedInfo = formatDeviceInfo(type, deviceName, osName, browserName);

        return new ClientMetadata(ipAddress, formattedInfo, type);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private DeviceType mapToDeviceType(String yauaaDeviceClass) {
        return switch (yauaaDeviceClass) {
            case "Desktop" -> DeviceType.DESKTOP;
            case "Phone", "Mobile" -> DeviceType.MOBILE;
            case "Tablet" -> DeviceType.TABLET;
            default -> DeviceType.UNKNOWN;
        };
    }

    private String formatDeviceInfo(DeviceType type, String deviceName, String osName, String browserName) {
        if (type == DeviceType.DESKTOP) {
            return String.format("%s on %s", browserName, osName);
        } else {
            if (deviceName.contains("Device") || deviceName.equals("Unknown")) {
                return String.format("%s - %s", osName, browserName);
            }
            return String.format("%s - %s", deviceName, browserName);
        }
    }

    public record ClientMetadata(String ip, String deviceInfo, DeviceType type) {
    }
}
