-- Spring Boot sql.init 启动时执行（CREATE IF NOT EXISTS，可重复）
-- 完整建库+示例数据请执行仓库根目录 database/init-full.sql

CREATE TABLE IF NOT EXISTS travel_reminder (
    id VARCHAR(36) PRIMARY KEY COMMENT '提醒ID',
    event_name VARCHAR(100) NOT NULL COMMENT '事件名称',
    event_date VARCHAR(20) NOT NULL COMMENT '事件日期(yyyy-MM-dd)',
    event_time VARCHAR(20) NOT NULL COMMENT '事件时间(HH:mm)',
    phone_number VARCHAR(20) COMMENT '手机号码',
    email VARCHAR(100) COMMENT '通知邮箱',
    description TEXT COMMENT '备注信息',
    reminder_minutes INT DEFAULT 15 COMMENT '提前提醒分钟数',
    repeat_daily TINYINT(1) DEFAULT 0 COMMENT '是否每天重复',
    notified_at DATETIME DEFAULT NULL COMMENT '已发送提醒时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_event_date (event_date),
    INDEX idx_event_time (event_time),
    INDEX idx_email (email),
    INDEX idx_notified_at (notified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习/日程提醒表';

CREATE TABLE IF NOT EXISTS hotel_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    order_id VARCHAR(32) NOT NULL COMMENT '订单号',
    hotel_name VARCHAR(255) COMMENT '酒店名称',
    hotel_address VARCHAR(500) COMMENT '酒店地址',
    hotel_phone VARCHAR(50) COMMENT '酒店电话',
    check_in_date DATE COMMENT '入住日期',
    check_out_date DATE COMMENT '退房日期',
    nights INT COMMENT '入住晚数',
    guests INT COMMENT '入住人数',
    contact_name VARCHAR(100) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系人电话',
    contact_email VARCHAR(100) COMMENT '联系人邮箱',
    special_requests TEXT COMMENT '特殊需求',
    status VARCHAR(20) DEFAULT '待确认' COMMENT '订单状态',
    total_amount DOUBLE COMMENT '订单金额(估算)',
    payment_deadline DATETIME COMMENT '支付截止时间',
    cancellation_policy TEXT COMMENT '取消政策',
    map_link VARCHAR(500) COMMENT '地图链接',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_id (order_id),
    INDEX idx_contact_phone (contact_phone),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店订单表';

CREATE TABLE IF NOT EXISTS emailaddress (
    email_id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '主键',
    email_name VARCHAR(100) NOT NULL COMMENT '联系人/别名',
    address VARCHAR(255) NOT NULL COMMENT '邮箱地址',
    UNIQUE KEY uk_email_name (email_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱别名表';

CREATE TABLE IF NOT EXISTS locations (
    location_id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '地点名称',
    longitude VARCHAR(32) NOT NULL COMMENT '经度',
    latitude VARCHAR(32) NOT NULL COMMENT '纬度',
    UNIQUE KEY uk_locations_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地点坐标表';

-- 当前登录用户/常用联系人基础信息（邮箱、电话等；后续可由接口读写）
CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户主键',
    external_id VARCHAR(64) DEFAULT NULL COMMENT '外部登录唯一标识，可选',
    display_name VARCHAR(100) DEFAULT NULL COMMENT '昵称/显示名',
    real_name VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(128) DEFAULT NULL COMMENT '常用邮箱',
    phone VARCHAR(32) DEFAULT NULL COMMENT '常用手机号',
    address VARCHAR(255) DEFAULT NULL COMMENT '联系地址',
    gender TINYINT DEFAULT NULL COMMENT '0未知 1男 2女',
    avatar_url VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_profile_external_id (external_id),
    UNIQUE KEY uk_user_profile_email (email),
    UNIQUE KEY uk_user_profile_phone (phone),
    INDEX idx_user_profile_display_name (display_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础资料表';

-- ========== 登录与双角色权限 ==========
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '登录名',
    password_hash VARCHAR(128) NOT NULL COMMENT '密码哈希',
    role VARCHAR(16) NOT NULL COMMENT 'STUDENT|TEACHER',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名',
    student_no VARCHAR(32) DEFAULT NULL COMMENT '学号(学生)',
    class_id VARCHAR(32) DEFAULT 'CLASS-01' COMMENT '班级',
    major VARCHAR(64) DEFAULT NULL COMMENT '专业',
    grade VARCHAR(16) DEFAULT NULL COMMENT '年级',
    class_name VARCHAR(64) DEFAULT NULL COMMENT '班级名称',
    email VARCHAR(255) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(32) DEFAULT NULL COMMENT '手机',
    taught_subjects TEXT DEFAULT NULL COMMENT '教师所教课程 JSON',
    teaching_scopes TEXT DEFAULT NULL COMMENT '教师教学板块 JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_student_no (student_no),
    INDEX idx_sys_user_role_class (role, class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS user_session (
    token VARCHAR(64) PRIMARY KEY COMMENT '会话令牌',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_session_user (user_id),
    INDEX idx_user_session_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录会话';

-- ========== 站内消息（学生/教师均可扩展） ==========
CREATE TABLE IF NOT EXISTS user_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '接收用户 sys_user.id',
    type VARCHAR(32) NOT NULL COMMENT 'TASK_REMIND|TASK_PUBLISHED|GRADE_RESULT|ACTIVITY|SYSTEM',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '正文',
    link_path VARCHAR(128) DEFAULT '/messages' COMMENT '前端跳转路径',
    ref_id VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID如 taskId',
    read_flag TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_user_read (user_id, read_flag),
    INDEX idx_notification_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户站内消息';

-- ========== 课堂作业 ==========
CREATE TABLE IF NOT EXISTS classroom_task (
    task_id VARCHAR(32) PRIMARY KEY COMMENT '任务ID',
    class_id VARCHAR(32) NOT NULL COMMENT '班级ID',
    teacher_id BIGINT NOT NULL COMMENT '发布教师',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    description TEXT COMMENT '说明',
    subject VARCHAR(64) DEFAULT NULL COMMENT '科目',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '截至时间',
    published TINYINT(1) DEFAULT 0 COMMENT '是否已发布',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_classroom_task_class (class_id),
    INDEX idx_classroom_task_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂任务';

CREATE TABLE IF NOT EXISTS classroom_sub_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(32) NOT NULL COMMENT '任务ID',
    sub_task_id VARCHAR(32) NOT NULL COMMENT '子任务ID',
    order_no INT NOT NULL COMMENT '顺序',
    title VARCHAR(255) NOT NULL,
    description TEXT,
    UNIQUE KEY uk_sub_task (task_id, sub_task_id),
    INDEX idx_sub_task_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂子任务';

CREATE TABLE IF NOT EXISTS classroom_submission (
    submission_id VARCHAR(32) PRIMARY KEY COMMENT '提交ID',
    task_id VARCHAR(32) NOT NULL,
    sub_task_id VARCHAR(32) NOT NULL,
    student_user_id BIGINT NOT NULL COMMENT '学生用户ID',
    file_name VARCHAR(255) NOT NULL,
    content MEDIUMTEXT COMMENT '提取的文本',
    artifact_path VARCHAR(512) DEFAULT NULL COMMENT '文件路径',
    status VARCHAR(24) DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED|GRADED|REJECTED',
    score DECIMAL(5,2) DEFAULT NULL COMMENT '分数',
    teacher_comment TEXT COMMENT '教师评语',
    graded_by BIGINT DEFAULT NULL COMMENT '批改教师',
    graded_at DATETIME DEFAULT NULL,
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_submission_cell (task_id, sub_task_id, student_user_id),
    INDEX idx_submission_task (task_id),
    INDEX idx_submission_student (student_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交';

-- 演示账号由 AuthDataInitializer 在首次启动时写入（默认密码 123456）
