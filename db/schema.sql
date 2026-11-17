-- BankManager 2.0 数据库结构（MySQL 8.0+）
-- 相比旧版的改动：
--   * 所有表都有主键
--   * 金额使用 DECIMAL(16,2)，不再使用 double
--   * 密码只存 BCrypt 哈希
--   * 队列票据带状态与时间戳，可追溯
--   * 网点图拆成节点表与边表，并加外键

CREATE DATABASE IF NOT EXISTS `bank_manager`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `bank_manager`;

DROP TABLE IF EXISTS bank_edge;
DROP TABLE IF EXISTS bank_node;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS queue_ticket;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS app_user;

-- 系统登录账号
CREATE TABLE app_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          ENUM ('ADMIN', 'TELLER') NOT NULL DEFAULT 'TELLER',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 银行职员
CREATE TABLE employee (
    id         VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    sex        VARCHAR(10),
    age        INT,
    tele       VARCHAR(20),
    address    VARCHAR(100),
    position   VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_employee_name (name),
    CONSTRAINT ck_employee_age CHECK (age IS NULL OR age BETWEEN 16 AND 100)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 客户账户
CREATE TABLE customer (
    card          VARCHAR(19) PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    tele          VARCHAR(20),
    type          ENUM ('NORMAL', 'VIP') NOT NULL DEFAULT 'NORMAL',
    balance       DECIMAL(16, 2) NOT NULL DEFAULT 0.00,
    loan          DECIMAL(16, 2) NOT NULL DEFAULT 0.00,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_customer_name (name),
    KEY idx_customer_type (type),
    CONSTRAINT ck_customer_balance CHECK (balance >= 0),
    CONSTRAINT ck_customer_loan CHECK (loan >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 排队票据
CREATE TABLE queue_ticket (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    number        INT         NOT NULL,
    customer_name VARCHAR(50) NOT NULL,
    vip           BOOLEAN     NOT NULL DEFAULT FALSE,
    status        ENUM ('WAITING', 'CALLED', 'DONE', 'CANCELLED') NOT NULL DEFAULT 'WAITING',
    counter_no    INT,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_at     TIMESTAMP   NULL,
    KEY idx_queue_waiting (status, vip, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 操作日志
CREATE TABLE operation_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    username   VARCHAR(50),
    action     VARCHAR(50)  NOT NULL,
    detail     VARCHAR(500),
    KEY idx_log_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 网点图的节点与边
CREATE TABLE bank_node (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    UNIQUE KEY uk_bank_node_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE bank_edge (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    from_node INT            NOT NULL,
    to_node   INT            NOT NULL,
    distance  DECIMAL(10, 2) NOT NULL,
    UNIQUE KEY uk_bank_edge (from_node, to_node),
    CONSTRAINT fk_bank_edge_from FOREIGN KEY (from_node) REFERENCES bank_node (id),
    CONSTRAINT fk_bank_edge_to FOREIGN KEY (to_node) REFERENCES bank_node (id),
    CONSTRAINT ck_bank_edge_distance CHECK (distance > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
