package com.neusoft.edu.neullmdev.service.communication;

import com.neusoft.edu.neullmdev.dto.communication.EmailParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SendEmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public SendEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public ToolResult send(EmailParams params, McpCallContext ctx) {
        String recipient = params.getRecipient() != null ? params.getRecipient().trim() : "";
        String subj = params.getSubject();
        String subject = (subj != null && !subj.isBlank()) ? subj.trim() : "无主题";
        String body = params.getContent() != null ? params.getContent() : "";

        if (ctx.commitSideEffects()) {
            String msg = deliver(recipient, subject, body);
            return new ToolResult("send_email", msg, msg);
        }

        JSONObject o = new JSONObject();
        o.put("functionName", "send_email");
        o.put("name", "send_email");
        o.put("recipient", recipient);
        o.put("subject", subject);
        o.put("content", body);
        String json = o.toString();
        return new ToolResult("send_email", "待发邮件预览：" + subject, json);
    }

    public String deliver(String recipient, String subject, String content) {
        if (!StringUtils.hasText(fromEmail)) {
            return "邮件未发送：未配置发件邮箱 spring.mail.username（可设置环境变量 QQ_SMTP_USERNAME）";
        }
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, false);
            javaMailSender.send(mimeMessage);
            return "邮件发送成功";
        } catch (MessagingException e) {
            return "邮件构建失败: " + e.getMessage();
        } catch (MailAuthenticationException e) {
            return "QQ 邮箱 SMTP 登录失败（535）。请检查授权码与 QQ_SMTP_USERNAME/QQ_SMTP_PASSWORD。原始错误: " + e.getMessage();
        } catch (MailSendException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("502") || msg.contains("Invalid input")) {
                return "邮件发送失败（QQ 返回 502）：请检查 mail.smtp.localhost 与 SMTP 授权码。详情: " + msg;
            }
            return "邮件发送失败: " + msg;
        } catch (Exception e) {
            return "邮件发送异常: " + e.getMessage();
        }
    }
}
