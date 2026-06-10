package com.neusoft.edu.neullmdev.config.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 为已存在的旧版 sys_user 等表补列（CREATE IF NOT EXISTS 不会更新旧表）。
 */
@Component
@Order(1)
public class SchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public SchemaMigrationRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("sys_user")) {
            return;
        }
        if (!columnExists("sys_user", "password_hash")) {
            if (columnExists("sys_user", "password")) {
                jdbc.execute("ALTER TABLE sys_user CHANGE COLUMN password password_hash VARCHAR(128) NOT NULL COMMENT '密码哈希'");
            } else {
                jdbc.execute("ALTER TABLE sys_user ADD COLUMN password_hash VARCHAR(128) NOT NULL DEFAULT '' COMMENT '密码哈希'");
            }
        }
        addColumnIfMissing("sys_user", "role",
                "ALTER TABLE sys_user ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'STUDENT' COMMENT 'STUDENT|TEACHER'");
        addColumnIfMissing("sys_user", "display_name",
                "ALTER TABLE sys_user ADD COLUMN display_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '显示名'");
        addColumnIfMissing("sys_user", "student_no",
                "ALTER TABLE sys_user ADD COLUMN student_no VARCHAR(32) DEFAULT NULL COMMENT '学号(学生)'");
        addColumnIfMissing("sys_user", "class_id",
                "ALTER TABLE sys_user ADD COLUMN class_id VARCHAR(32) DEFAULT 'CLASS-01' COMMENT '班级'");
        addColumnIfMissing("sys_user", "major",
                "ALTER TABLE sys_user ADD COLUMN major VARCHAR(64) DEFAULT NULL COMMENT '专业(学生)'");
        addColumnIfMissing("sys_user", "grade",
                "ALTER TABLE sys_user ADD COLUMN grade VARCHAR(16) DEFAULT NULL COMMENT '年级(学生)'");
        addColumnIfMissing("sys_user", "class_name",
                "ALTER TABLE sys_user ADD COLUMN class_name VARCHAR(64) DEFAULT NULL COMMENT '班级名称(学生)'");
        addColumnIfMissing("sys_user", "email",
                "ALTER TABLE sys_user ADD COLUMN email VARCHAR(128) DEFAULT NULL COMMENT '邮箱'");
        addColumnIfMissing("sys_user", "phone",
                "ALTER TABLE sys_user ADD COLUMN phone VARCHAR(32) DEFAULT NULL COMMENT '手机号'");
        addColumnIfMissing("sys_user", "taught_subjects",
                "ALTER TABLE sys_user ADD COLUMN taught_subjects TEXT DEFAULT NULL COMMENT '教师所教课程JSON'");
        addColumnIfMissing("sys_user", "teaching_scopes",
                "ALTER TABLE sys_user ADD COLUMN teaching_scopes TEXT DEFAULT NULL COMMENT '教师教学板块JSON'");
        jdbc.update("UPDATE sys_user SET major = '软件工程' "
                + "WHERE role = 'STUDENT' AND (major IS NULL OR TRIM(major) = '')");
        jdbc.update("UPDATE sys_user SET grade = '2024级' "
                + "WHERE role = 'STUDENT' AND (grade IS NULL OR TRIM(grade) = '')");
        jdbc.update("UPDATE sys_user SET class_name = '软工2401班' "
                + "WHERE role = 'STUDENT' AND (class_name IS NULL OR TRIM(class_name) = '')");
        jdbc.update("UPDATE sys_user SET major = '计算机学院', grade = '副教授', student_no = 'T2024001', "
                + "email = 'teacher@example.com', phone = '13800138000' "
                + "WHERE role = 'TEACHER' AND username = 'teacher' "
                + "AND (major IS NULL OR TRIM(major) = '')");
        jdbc.update("UPDATE sys_user SET taught_subjects = '[\"人工智能\",\"软件工程\"]', "
                + "teaching_scopes = '[{\"major\":\"软件工程\",\"grade\":\"2024级\",\"className\":\"软工2401班\"},"
                + "{\"major\":\"人工智能\",\"grade\":\"2024级\",\"className\":\"智能2401班\"}]' "
                + "WHERE role = 'TEACHER' AND username = 'teacher' "
                + "AND (taught_subjects IS NULL OR TRIM(taught_subjects) = '')");
        addColumnIfMissing("sys_user", "created_at",
                "ALTER TABLE sys_user ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP");
        addColumnIfMissing("sys_user", "updated_at",
                "ALTER TABLE sys_user ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
    }

    private void addColumnIfMissing(String table, String column, String ddl) {
        if (!columnExists(table, column)) {
            jdbc.execute(ddl);
        }
    }

    private boolean tableExists(String table) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class, table);
        return n != null && n > 0;
    }

    private boolean columnExists(String table, String column) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, table, column);
        return n != null && n > 0;
    }
}
