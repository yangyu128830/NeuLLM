-- 兼容已存在的旧表结构（CREATE TABLE IF NOT EXISTS 不会为旧表补列）
-- 每条语句在列已存在时执行 SELECT 1，避免重复启动报错

-- password_hash：旧表可能为 password 列
SET @sql_pw = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'password_hash') > 0,
        'SELECT 1',
        IF(
            (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'password') > 0,
            'ALTER TABLE sys_user CHANGE COLUMN password password_hash VARCHAR(128) NOT NULL COMMENT ''密码哈希''',
            'ALTER TABLE sys_user ADD COLUMN password_hash VARCHAR(128) NOT NULL DEFAULT '''' COMMENT ''密码哈希'''
        )
    )
);
PREPARE stmt FROM @sql_pw;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_role = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'role') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT ''STUDENT'' COMMENT ''STUDENT|TEACHER'''
    )
);
PREPARE stmt FROM @sql_role;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_dn = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'display_name') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN display_name VARCHAR(100) NOT NULL DEFAULT '''' COMMENT ''显示名'''
    )
);
PREPARE stmt FROM @sql_dn;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_sn = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'student_no') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN student_no VARCHAR(32) DEFAULT NULL COMMENT ''学号(学生)'''
    )
);
PREPARE stmt FROM @sql_sn;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_cid = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'class_id') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN class_id VARCHAR(32) DEFAULT ''CLASS-01'' COMMENT ''班级'''
    )
);
PREPARE stmt FROM @sql_cid;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_ca = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'created_at') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP'
    )
);
PREPARE stmt FROM @sql_ca;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_ua = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'updated_at') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP'
    )
);
PREPARE stmt FROM @sql_ua;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_su_major = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'major') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN major VARCHAR(64) DEFAULT NULL COMMENT ''专业'''
    )
);
PREPARE stmt FROM @sql_su_major;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_su_grade = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'grade') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN grade VARCHAR(16) DEFAULT NULL COMMENT ''年级'''
    )
);
PREPARE stmt FROM @sql_su_grade;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_su_class_name = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'class_name') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN class_name VARCHAR(64) DEFAULT NULL COMMENT ''班级名称'''
    )
);
PREPARE stmt FROM @sql_su_class_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_su_email = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'email') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN email VARCHAR(255) DEFAULT NULL COMMENT ''邮箱'''
    )
);
PREPARE stmt FROM @sql_su_email;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_su_phone = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'phone') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN phone VARCHAR(32) DEFAULT NULL COMMENT ''手机'''
    )
);
PREPARE stmt FROM @sql_su_phone;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_su_taught = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'taught_subjects') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN taught_subjects TEXT DEFAULT NULL COMMENT ''教师所教课程 JSON'''
    )
);
PREPARE stmt FROM @sql_su_taught;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_su_scopes = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'teaching_scopes') > 0,
        'SELECT 1',
        'ALTER TABLE sys_user ADD COLUMN teaching_scopes TEXT DEFAULT NULL COMMENT ''教师教学板块 JSON'''
    )
);
PREPARE stmt FROM @sql_su_scopes;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_ct_subject = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'classroom_task' AND COLUMN_NAME = 'subject') > 0,
        'SELECT 1',
        'ALTER TABLE classroom_task ADD COLUMN subject VARCHAR(64) DEFAULT NULL COMMENT ''科目'''
    )
);
PREPARE stmt FROM @sql_ct_subject;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_ct_start = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'classroom_task' AND COLUMN_NAME = 'start_time') > 0,
        'SELECT 1',
        'ALTER TABLE classroom_task ADD COLUMN start_time DATETIME DEFAULT NULL COMMENT ''开始时间'''
    )
);
PREPARE stmt FROM @sql_ct_start;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_ct_end = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'classroom_task' AND COLUMN_NAME = 'end_time') > 0,
        'SELECT 1',
        'ALTER TABLE classroom_task ADD COLUMN end_time DATETIME DEFAULT NULL COMMENT ''截至时间'''
    )
);
PREPARE stmt FROM @sql_ct_end;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_ct_target_major = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'classroom_task' AND COLUMN_NAME = 'target_major') > 0,
        'SELECT 1',
        'ALTER TABLE classroom_task ADD COLUMN target_major VARCHAR(64) DEFAULT NULL COMMENT ''布置专业'''
    )
);
PREPARE stmt FROM @sql_ct_target_major;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_ct_target_grade = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'classroom_task' AND COLUMN_NAME = 'target_grade') > 0,
        'SELECT 1',
        'ALTER TABLE classroom_task ADD COLUMN target_grade VARCHAR(16) DEFAULT NULL COMMENT ''布置年级'''
    )
);
PREPARE stmt FROM @sql_ct_target_grade;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_ct_target_class = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'classroom_task' AND COLUMN_NAME = 'target_class_name') > 0,
        'SELECT 1',
        'ALTER TABLE classroom_task ADD COLUMN target_class_name VARCHAR(64) DEFAULT NULL COMMENT ''布置班级'''
    )
);
PREPARE stmt FROM @sql_ct_target_class;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_tr_repeat = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'travel_reminder' AND COLUMN_NAME = 'repeat_daily') > 0,
        'SELECT 1',
        'ALTER TABLE travel_reminder ADD COLUMN repeat_daily TINYINT(1) DEFAULT 0 COMMENT ''是否每天重复'''
    )
);
PREPARE stmt FROM @sql_tr_repeat;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_tr_notified = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'travel_reminder' AND COLUMN_NAME = 'notified_at') > 0,
        'SELECT 1',
        'ALTER TABLE travel_reminder ADD COLUMN notified_at DATETIME DEFAULT NULL COMMENT ''已发送提醒时间'''
    )
);
PREPARE stmt FROM @sql_tr_notified;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
