package com.neusoft.edu.neullmdev.service.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.auth.TeachingScopeItem;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskTargeting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TeacherTeachingScopeUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TeacherTeachingScopeUtil() {
    }

    public static List<String> parseSubjects(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> list = MAPPER.readValue(json, new TypeReference<>() {
            });
            return list == null ? List.of() : list.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public static String serializeSubjects(List<String> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return null;
        }
        try {
            List<String> cleaned = subjects.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();
            return cleaned.isEmpty() ? null : MAPPER.writeValueAsString(cleaned);
        } catch (Exception e) {
            throw new IllegalArgumentException("所教课程格式无效");
        }
    }

    public static List<TeachingScopeItem> parseScopes(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<TeachingScopeItem> list = MAPPER.readValue(json, new TypeReference<>() {
            });
            if (list == null) {
                return List.of();
            }
            List<TeachingScopeItem> cleaned = new ArrayList<>();
            for (TeachingScopeItem item : list) {
                if (item == null) {
                    continue;
                }
                String major = trimOrNull(item.getMajor());
                String grade = trimOrNull(item.getGrade());
                String className = trimOrNull(item.getClassName());
                if (major == null || grade == null || className == null) {
                    continue;
                }
                TeachingScopeItem row = new TeachingScopeItem();
                row.setMajor(major);
                row.setGrade(grade);
                row.setClassName(className);
                cleaned.add(row);
            }
            return cleaned;
        } catch (Exception e) {
            return List.of();
        }
    }

    public static String serializeScopes(List<TeachingScopeItem> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return null;
        }
        List<TeachingScopeItem> cleaned = parseScopesFromRequest(scopes);
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(cleaned);
        } catch (Exception e) {
            throw new IllegalArgumentException("教学板块格式无效");
        }
    }

    public static List<TeachingScopeItem> parseScopesFromRequest(List<TeachingScopeItem> scopes) {
        if (scopes == null) {
            return List.of();
        }
        List<TeachingScopeItem> cleaned = new ArrayList<>();
        for (TeachingScopeItem item : scopes) {
            if (item == null) {
                continue;
            }
            String major = trimOrNull(item.getMajor());
            String grade = trimOrNull(item.getGrade());
            String className = trimOrNull(item.getClassName());
            if (major == null || grade == null || className == null) {
                continue;
            }
            TeachingScopeItem row = new TeachingScopeItem();
            row.setMajor(major);
            row.setGrade(grade);
            row.setClassName(className);
            cleaned.add(row);
        }
        return cleaned;
    }

    public static boolean studentAccessible(SysUserEntity student, List<TeachingScopeItem> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return true;
        }
        for (TeachingScopeItem scope : scopes) {
            ClassroomTaskEntity probe = new ClassroomTaskEntity();
            probe.setTargetMajor(scope.getMajor());
            probe.setTargetGrade(scope.getGrade());
            probe.setTargetClassName(scope.getClassName());
            if (ClassroomTaskTargeting.matches(probe, student)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> subjectsVo(String json) {
        return Collections.unmodifiableList(parseSubjects(json));
    }

    public static List<TeachingScopeItem> scopesVo(String json) {
        return Collections.unmodifiableList(parseScopes(json));
    }

    private static String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
