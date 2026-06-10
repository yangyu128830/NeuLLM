package com.neusoft.edu.neullmdev.service.communication;

import com.neusoft.edu.neullmdev.dto.communication.SmsParams;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class SmsService {

    @Value("${sms.api-url:}")
    private String smsApiUrl;

    @Value("${sms.api-key:}")
    private String smsApiKey;

    public ToolResult send(SmsParams params) {
        String phone = params.getPhoneNumber() != null ? params.getPhoneNumber().trim() : "";
        String message = params.getMessage() != null ? params.getMessage().trim() : "";
        return McpToolSupport.fromJsonString("send_sms", sendSms(phone, message));
    }

    public String sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "手机号不能为空";
        }
        if (message == null || message.trim().isEmpty()) {
            return "短信内容不能为空";
        }
        if (smsApiUrl == null || smsApiUrl.isBlank()) {
            return simulateSendSms(phoneNumber, message);
        }
        try {
            return callSmsApi(phoneNumber, message);
        } catch (Exception e) {
            return "短信发送失败：" + e.getMessage();
        }
    }

    private String simulateSendSms(String phoneNumber, String message) {
        log.info("模拟短信发送 - 时间: {}, 目标号码: {}, 内容: {}",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                phoneNumber, message);
        return "短信发送成功（模拟模式）：" + message;
    }

    private String callSmsApi(String phoneNumber, String message) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(smsApiUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + smsApiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        String jsonPayload = String.format("{\"phone\":\"%s\",\"message\":\"%s\"}", phoneNumber, message);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return "短信发送成功：" + response;
            }
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            return "短信发送失败，响应码：" + responseCode + "，错误信息：" + error;
        } catch (Exception e) {
            return "短信发送失败，响应码：" + responseCode;
        }
    }
}
