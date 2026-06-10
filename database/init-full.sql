-- =============================================================================
-- NeuLLM / 智学伴 — MySQL 8.0+ 全库初始化脚本
-- 与 application.yml 中库名一致: neusoftllm
--
-- 用法（任选其一）:
--   mysql -u root -p < database/init-full.sql
--   mysql -u root -p -e "source D:/AAAA-AAAA/NeuLLM/database/init-full.sql"
-- =============================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS neusoftllm
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE neusoftllm;

-- ---------------------------------------------------------------------------
-- 学习提醒（定时任务扫描、到点发邮件依赖 email 等字段）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS travel_reminder (
    id VARCHAR(36) PRIMARY KEY COMMENT '提醒ID',
    event_name VARCHAR(100) NOT NULL COMMENT '事件名称',
    event_date VARCHAR(20) NOT NULL COMMENT '事件日期(yyyy-MM-dd)',
    event_time VARCHAR(20) NOT NULL COMMENT '事件时间(HH:mm)',
    phone_number VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    email VARCHAR(100) DEFAULT NULL COMMENT '通知邮箱',
    description TEXT COMMENT '备注信息',
    reminder_minutes INT DEFAULT 15 COMMENT '提前提醒分钟数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_event_date (event_date),
    INDEX idx_event_time (event_time),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习/日程提醒表';

-- ---------------------------------------------------------------------------
-- 酒店预订订单
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hotel_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    order_id VARCHAR(32) NOT NULL COMMENT '订单号',
    hotel_name VARCHAR(255) DEFAULT NULL COMMENT '酒店名称',
    hotel_address VARCHAR(500) DEFAULT NULL COMMENT '酒店地址',
    hotel_phone VARCHAR(50) DEFAULT NULL COMMENT '酒店电话',
    check_in_date DATE DEFAULT NULL COMMENT '入住日期',
    check_out_date DATE DEFAULT NULL COMMENT '退房日期',
    nights INT DEFAULT NULL COMMENT '入住晚数',
    guests INT DEFAULT NULL COMMENT '入住人数',
    contact_name VARCHAR(100) DEFAULT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
    contact_email VARCHAR(100) DEFAULT NULL COMMENT '联系人邮箱',
    special_requests TEXT COMMENT '特殊需求',
    status VARCHAR(20) DEFAULT '待确认' COMMENT '订单状态',
    total_amount DOUBLE DEFAULT NULL COMMENT '订单金额(估算)',
    payment_deadline DATETIME DEFAULT NULL COMMENT '支付截止时间',
    cancellation_policy TEXT COMMENT '取消政策',
    map_link VARCHAR(500) DEFAULT NULL COMMENT '地图链接',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_id (order_id),
    INDEX idx_contact_phone (contact_phone),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店订单表';

-- ---------------------------------------------------------------------------
-- 人名 → 邮箱（历史功能；与 EmailAdressMapper 对应）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS emailaddress (
    email_id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '主键',
    email_name VARCHAR(100) NOT NULL COMMENT '联系人/别名',
    address VARCHAR(255) NOT NULL COMMENT '邮箱地址',
    UNIQUE KEY uk_email_name (email_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱别名表';

-- ---------------------------------------------------------------------------
-- 地名 → 经纬度（路线工具 getLocationInfo 用；GCJ-02 约值）
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS locations (
    location_id VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '地点名称',
    longitude VARCHAR(32) NOT NULL COMMENT '经度',
    latitude VARCHAR(32) NOT NULL COMMENT '纬度',
    UNIQUE KEY uk_locations_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地点坐标表';

-- ---------------------------------------------------------------------------
-- 用户基础资料（邮箱、电话、地址等；应用层可绑定当前用户或默认联系人）
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- 可选示例数据（INSERT IGNORE 可重复执行）
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO emailaddress (email_id, email_name, address) VALUES
('ea-001', '张三', 'zhangsan@example.com'),
('ea-002', '李四', 'lisi@example.com');

INSERT IGNORE INTO user_profile (external_id, display_name, real_name, email, phone, address) VALUES
('demo', '演示用户', '张三', 'zhangsan@example.com', '13900000001', '辽宁省大连市');

INSERT IGNORE INTO locations (location_id, name, longitude, latitude) VALUES
('loc-beijing', '北京', '116.407526', '39.904030'),
('loc-shanghai', '上海', '121.473701', '31.230416'),
('loc-dalian', '大连', '121.614786', '38.913962'),
('loc-shenyang', '沈阳', '123.431383', '41.805698'),
('loc-xian', '西安', '108.939645', '34.343207'),
('loc-guangzhou', '广州', '113.264499', '23.129110'),
('loc-shenzhen', '深圳', '114.057868', '22.543099'),
('loc-hangzhou', '杭州', '120.155070', '30.274084'),
('loc-chengdu', '成都', '104.066541', '30.572269'),
('loc-wuhan', '武汉', '114.305392', '30.593098');

-- 完成
SELECT 'neusoftllm 初始化完成' AS message;
