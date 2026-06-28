CREATE DATABASE IF NOT EXISTS battery_passport DEFAULT CHARSET utf8mb4;

USE battery_passport;

-- 电池护照主表
CREATE TABLE IF NOT EXISTS battery_passport (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    passport_id     VARCHAR(64)  NOT NULL COMMENT '护照编号',
    serial_number   VARCHAR(128) NOT NULL COMMENT '电池序列号',
    product_model   VARCHAR(64)  NULL COMMENT '产品型号',
    manufacturer    VARCHAR(128) NULL COMMENT '制造商',
    production_date DATE         NULL COMMENT '生产日期',
    rated_capacity  DECIMAL(10,2) NULL COMMENT '额定容量(Ah)',
    rated_voltage   DECIMAL(10,2) NULL COMMENT '额定电压(V)',
    initial_soh     DECIMAL(5,2) NULL COMMENT '初始健康度(%)',
    chemistry_type  VARCHAR(32)  NULL COMMENT '电化学类型',
    weight          DECIMAL(10,2) NULL COMMENT '重量(kg)',
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    raw_json        TEXT         NULL COMMENT '原始数据JSON',
    tenant_id       VARCHAR(32)  NULL COMMENT '租户ID(SaaS模式)',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_passport_id (passport_id),
    INDEX idx_serial (serial_number),
    INDEX idx_tenant (tenant_id),
    INDEX idx_manufacturer (manufacturer)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电池护照表';

-- 溯源事件表
CREATE TABLE IF NOT EXISTS trace_event (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    passport_id     VARCHAR(64)  NOT NULL COMMENT '关联护照',
    event_type      VARCHAR(32)  NOT NULL COMMENT '事件类型',
    operator        VARCHAR(64)  NULL COMMENT '操作人',
    location        VARCHAR(128) NULL COMMENT '位置',
    occurred_at     DATETIME     NOT NULL COMMENT '发生时间',
    details         JSON         NULL COMMENT '事件详情',
    tenant_id       VARCHAR(32)  NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_passport (passport_id),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源事件表';

-- 租户表(SaaS模式)
CREATE TABLE IF NOT EXISTS sys_tenant (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_code     VARCHAR(32)  NOT NULL COMMENT '租户编码',
    tenant_name     VARCHAR(128) NOT NULL COMMENT '租户名称',
    contact_name    VARCHAR(64)  NULL,
    contact_phone   VARCHAR(32)  NULL,
    contact_email   VARCHAR(128) NULL,
    plan            VARCHAR(32)  NULL COMMENT '套餐',
    expire_date     DATE         NULL COMMENT '过期日期',
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_tenant_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    config_key      VARCHAR(64)  NOT NULL,
    config_value    TEXT         NOT NULL,
    description     VARCHAR(256) NULL,
    tenant_id       VARCHAR(32)  NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config (tenant_id, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置表';

-- 插入初始数据
INSERT INTO sys_tenant (tenant_code, tenant_name, plan, status)
VALUES ('trina', '天合储能', 'enterprise', 'ACTIVE'),
       ('tuobang', '托邦股份', 'enterprise', 'ACTIVE')
ON DUPLICATE KEY UPDATE tenant_name = VALUES(tenant_name);

INSERT INTO sys_config (config_key, config_value, description)
VALUES ('battery.validation.capacity_deviation', '2.0', '容量偏差阈值(百分比)');
