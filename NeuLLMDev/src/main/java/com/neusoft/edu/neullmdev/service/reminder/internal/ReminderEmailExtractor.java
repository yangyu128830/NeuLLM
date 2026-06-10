package com.neusoft.edu.neullmdev.service.reminder.internal;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从用户自然语言中提取邮箱。
 */
@Component
public class ReminderEmailExtractor {

    private static final Pattern GENERIC = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9]+");
    private static final Pattern PREFIX_WEI = Pattern.compile("邮箱为([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9]+)");
    private static final Pattern PREFIX_SHI = Pattern.compile("邮箱是([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9]+)");
    private static final Pattern QQ = Pattern.compile("[0-9]+@qq\\.com");

    public String extractFrom(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Matcher m = GENERIC.matcher(text);
        if (m.find()) {
            return m.group();
        }
        m = PREFIX_WEI.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        m = PREFIX_SHI.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        m = QQ.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return null;
    }
}
