package com.neusoft.edu.neullmdev.config.classroom;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 为已存在的 classroom_task 表补列。
 */
@Component
@Order(2)
public class ClassroomSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public ClassroomSchemaMigrationRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("classroom_task")) {
            return;
        }
        addColumnIfMissing("classroom_task", "target_major",
                "ALTER TABLE classroom_task ADD COLUMN target_major VARCHAR(64) DEFAULT NULL COMMENT '布置专业'");
        addColumnIfMissing("classroom_task", "target_grade",
                "ALTER TABLE classroom_task ADD COLUMN target_grade VARCHAR(16) DEFAULT NULL COMMENT '布置年级'");
        addColumnIfMissing("classroom_task", "target_class_name",
                "ALTER TABLE classroom_task ADD COLUMN target_class_name VARCHAR(64) DEFAULT NULL COMMENT '布置班级'");
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
