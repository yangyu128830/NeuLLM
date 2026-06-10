package com.neusoft.edu.neullmdev.service.notification;

import com.neusoft.edu.neullmdev.auth.AuthContext;
import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;
import com.neusoft.edu.neullmdev.entity.notification.UserNotificationEntity;
import com.neusoft.edu.neullmdev.mapper.auth.SysUserMapper;
import com.neusoft.edu.neullmdev.mapper.notification.UserNotificationMapper;
import com.neusoft.edu.neullmdev.model.notification.NotificationType;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomTaskTargeting;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final int DEFAULT_LIST_LIMIT = 50;

    private final UserNotificationMapper notificationMapper;
    private final SysUserMapper sysUserMapper;

    public NotificationService(UserNotificationMapper notificationMapper, SysUserMapper sysUserMapper) {
        this.notificationMapper = notificationMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public UserNotificationEntity notifyUser(Long userId, String type, String title, String content,
                                             String linkPath, String refId) {
        if (userId == null) {
            return null;
        }
        UserNotificationEntity row = new UserNotificationEntity();
        row.setUserId(userId);
        row.setType(type != null ? type : NotificationType.SYSTEM);
        row.setTitle(title != null ? title : "通知");
        row.setContent(content != null ? content : "");
        row.setLinkPath(linkPath != null ? linkPath : "/messages");
        row.setRefId(refId);
        notificationMapper.insert(row);
        return row;
    }

    public int notifyTargetStudents(String classId, String targetMajor, String targetGrade, String targetClassName,
                                    String type, String title, String content,
                                    String linkPath, String refId) {
        int n = 0;
        ClassroomTaskEntity scope = new ClassroomTaskEntity();
        scope.setTargetMajor(targetMajor);
        scope.setTargetGrade(targetGrade);
        scope.setTargetClassName(targetClassName);
        for (SysUserEntity s : sysUserMapper.listStudentsByClass(classId)) {
            if (!ClassroomTaskTargeting.matches(scope, s)) {
                continue;
            }
            if (notifyUser(s.getId(), type, title, content, linkPath, refId) != null) {
                n++;
            }
        }
        return n;
    }

    public int notifyClassStudents(String classId, String type, String title, String content,
                                   String linkPath, String refId) {
        int n = 0;
        for (SysUserEntity s : sysUserMapper.listStudentsByClass(classId)) {
            if (notifyUser(s.getId(), type, title, content, linkPath, refId) != null) {
                n++;
            }
        }
        return n;
    }

    public List<Map<String, Object>> listForCurrentUser(int limit) {
        AuthUser user = AuthContext.require();
        List<UserNotificationEntity> rows = notificationMapper.listByUser(user.id(), Math.max(1, limit));
        List<Map<String, Object>> out = new ArrayList<>();
        for (UserNotificationEntity e : rows) {
            out.add(toVo(e));
        }
        return out;
    }

    public int unreadCountForCurrentUser() {
        return notificationMapper.countUnread(AuthContext.require().id());
    }

    public boolean markRead(Long id) {
        return notificationMapper.markRead(id, AuthContext.require().id()) > 0;
    }

    public int markAllRead() {
        return notificationMapper.markAllRead(AuthContext.require().id());
    }

    private Map<String, Object> toVo(UserNotificationEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("type", e.getType());
        m.put("typeLabel", typeLabel(e.getType()));
        m.put("title", e.getTitle());
        m.put("content", e.getContent());
        m.put("linkPath", e.getLinkPath());
        m.put("refId", e.getRefId());
        m.put("read", Boolean.TRUE.equals(e.getReadFlag()));
        m.put("createdAt", e.getCreatedAt());
        return m;
    }

    private static String typeLabel(String type) {
        if (type == null) {
            return "通知";
        }
        return switch (type) {
            case NotificationType.TASK_REMIND -> "作业催交";
            case NotificationType.TASK_PUBLISHED -> "新作业";
            case NotificationType.GRADE_RESULT -> "批改结果";
            case NotificationType.ACTIVITY -> "活动";
            default -> "系统";
        };
    }
}
