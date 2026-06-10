package com.neusoft.edu.neullmdev.service.reminder.internal;

import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自然语言与模型参数中的日期时间解析、规范化（独立能力，供提醒智能体复用）。
 */
@Slf4j
@Component
public class ReminderDatetimeParsing {

    private static final Pattern DOT_TIME = Pattern.compile("(\\d{1,2})[\\.．](\\d{2})");
    private static final Pattern COLON_TIME = Pattern.compile("(\\d{1,2}:\\d{2})");
    private static final Pattern CN_DOT_MINUTE = Pattern.compile("(\\d{1,2})\\s*[:点]\\s*(\\d{2})\\s*分?");
    private static final Pattern CN_OCLOCK = Pattern.compile("(早上|上午|下午|晚上|凌晨)?\\s*(\\d{1,2})\\s*点\\s*(\\d{1,2})?\\s*分?");
    private static final Pattern CN_HOUR_ONLY = Pattern.compile("(早上|上午|下午|晚上|凌晨)?\\s*(\\d{1,2})\\s*点");

    /**
     * 将模型传入的 datetime 写入 params 的 eventDate / eventTime。
     */
    public void applyExplicitDatetime(TravelReminderParams params) {
        String dt = params.getDatetime();
        if (dt == null || dt.isBlank()) {
            return;
        }
        String raw = dt.trim().replace(' ', 'T');
        try {
            if (raw.contains("T")) {
                String[] parts = raw.split("T", 2);
                if (parts.length == 2) {
                    params.setEventDate(fixYear(parts[0].trim()));
                    params.setEventTime(cleanTime(parts[1].trim()));
                }
            } else if (raw.matches("\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{2}")) {
                String[] sp = raw.split("\\s+", 2);
                params.setEventDate(fixYear(sp[0].trim()));
                params.setEventTime(cleanTime(sp[1].trim()));
            }
        } catch (Exception e) {
            log.warn("解析 datetime 参数失败: {} — {}", dt, e.getMessage());
        }
    }

    public String extractDateFromNaturalLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        if (text.contains("今天") || text.contains("今日")) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (text.contains("明天") || text.contains("明日")) {
            return LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (text.contains("后天")) {
            return LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        Matcher m = Pattern.compile("(\\d{4}年\\d+月\\d+日)").matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        m = Pattern.compile("(\\d+月\\d+日)").matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        m = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})").matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public String extractTimeFromNaturalLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Matcher m = DOT_TIME.matcher(text);
        if (m.find()) {
            int hour = Integer.parseInt(m.group(1));
            int minute = Integer.parseInt(m.group(2));
            return String.format("%02d:%02d", hour, minute);
        }
        m = COLON_TIME.matcher(text);
        if (m.find()) {
            return formatTime(m.group(1));
        }
        m = CN_DOT_MINUTE.matcher(text);
        if (m.find()) {
            int hour = Integer.parseInt(m.group(1));
            int minute = Integer.parseInt(m.group(2));
            return String.format("%02d:%02d", hour, minute);
        }
        m = CN_OCLOCK.matcher(text);
        if (m.find()) {
            String period = m.group(1);
            int hour = Integer.parseInt(m.group(2));
            int minute = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
            if ("下午".equals(period) || "晚上".equals(period)) {
                if (hour < 12) {
                    hour += 12;
                }
            } else if ("凌晨".equals(period) && hour >= 12) {
                hour -= 12;
            }
            return String.format("%02d:%02d", hour, minute);
        }
        m = CN_HOUR_ONLY.matcher(text);
        if (m.find()) {
            String period = m.group(1);
            int hour = Integer.parseInt(m.group(2));
            if ("下午".equals(period) || "晚上".equals(period)) {
                if (hour < 12) {
                    hour += 12;
                }
            } else if ("凌晨".equals(period) && hour >= 12) {
                hour -= 12;
            }
            return String.format("%02d:00", hour);
        }
        return null;
    }

    public String normalizeChineseOrLooseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return dateStr;
        }
        dateStr = dateStr.trim();
        int currentYear = LocalDateTime.now().getYear();

        if (dateStr.matches("\\d{4}年\\d+月\\d+日")) {
            String yearStr = dateStr.replaceAll("年.*", "");
            int year = Integer.parseInt(yearStr);
            String month = dateStr.replaceAll("\\d+年(\\d+)月.*", "$1");
            String day = dateStr.replaceAll(".*月(\\d+)日", "$1");
            if (Math.abs(year - currentYear) > 1) {
                year = currentYear;
            }
            return String.format("%d-%02d-%02d", year, Integer.parseInt(month), Integer.parseInt(day));
        }
        if (dateStr.matches("\\d+月\\d+日")) {
            String month = dateStr.replaceAll("月.*", "");
            String day = dateStr.replaceAll(".*月(\\d+)日", "$1");
            return String.format("%d-%02d-%02d", currentYear, Integer.parseInt(month), Integer.parseInt(day));
        }
        if (dateStr.matches("\\d+/\\d+/\\d+")) {
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[2]);
                if (year < 100) {
                    year += 2000;
                }
                return String.format("%d-%02d-%02d", year, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        }
        if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            if (Math.abs(year - currentYear) > 1) {
                year = currentYear;
            }
            return String.format("%d-%02d-%02d", year, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
        return dateStr;
    }

    public LocalDateTime parseEventDateTime(String dateIso, String timeHm) throws DateTimeParseException {
        return LocalDateTime.parse(dateIso + "T" + timeHm,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    public String fixYear(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return dateStr;
        }
        int currentYear = LocalDateTime.now().getYear();
        if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}T.*")) {
            String datePart = dateStr.substring(0, 10);
            String[] parts = datePart.split("-");
            int year = Integer.parseInt(parts[0]);
            if (Math.abs(year - currentYear) > 1) {
                return currentYear + dateStr.substring(4);
            }
        }
        if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            if (Math.abs(year - currentYear) > 1) {
                return String.format("%d-%02d-%02d", currentYear, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
        }
        return dateStr;
    }

    public String cleanTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return timeStr;
        }
        String cleaned = timeStr.replaceAll("[Zz]", "");
        int colonIndex = cleaned.indexOf(":");
        if (colonIndex > 0 && colonIndex + 3 <= cleaned.length()) {
            String timePart = cleaned.substring(0, colonIndex + 3);
            if (timePart.matches("\\d{1,2}:\\d{2}")) {
                return formatTime(timePart);
            }
        }
        Matcher m = Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(cleaned);
        if (m.find()) {
            return String.format("%02d:%02d", Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        }
        return timeStr;
    }

    private static String formatTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return time;
        }
        String[] parts = time.split(":");
        if (parts.length == 2) {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return String.format("%02d:%02d", hour, minute);
        }
        return time;
    }
}
