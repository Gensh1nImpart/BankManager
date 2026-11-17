-- BankManager 测试用 H2 结构（H2 2.2，MODE=MySQL）
-- 与 db/schema.sql 的差异：
--   * MySQL 的 ENUM 改为 VARCHAR + CHECK
--   * 不支持 ON UPDATE CURRENT_TIMESTAMP，updated_at 由 DAO 显式写入
--   * 去掉 ENGINE / CHARSET，索引改为独立 CREATE INDEX 语句
--   * 其余字段类型与约束保持不变

DROP TABLE IF EXISTS bank_edge;
DROP TABLE IF EXISTS bank_node;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS queue_ticket;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'TELLER',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_user_username UNIQUE (username),
    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'TELLER'))
);

CREATE TABLE employee (
    id         VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    sex        VARCHAR(10),
    age        INT,
    tele       VARCHAR(20),
    address    VARCHAR(100),
    position   VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_employee_age CHECK (age IS NULL OR age BETWEEN 16 AND 100)
);

CREATE INDEX idx_employee_name ON employee (name);

CREATE TABLE customer (
    card          VARCHAR(19) PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    tele          VARCHAR(20),
    type          VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    balance       DECIMAL(16, 2) NOT NULL DEFAULT 0.00,
    loan          DECIMAL(16, 2) NOT NULL DEFAULT 0.00,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_customer_type CHECK (type IN ('NORMAL', 'VIP')),
    CONSTRAINT ck_customer_balance CHECK (balance >= 0),
    CONSTRAINT ck_customer_loan CHECK (loan >= 0)
);

CREATE INDEX idx_customer_name ON customer (name);
CREATE INDEX idx_customer_type ON customer (type);

CREATE TABLE queue_ticket (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    number        INT         NOT NULL,
    customer_name VARCHAR(50) NOT NULL,
    vip           BOOLEAN     NOT NULL DEFAULT FALSE,
    status        VARCHAR(10) NOT NULL DEFAULT 'WAITING',
    counter_no    INT,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_at     TIMESTAMP   NULL,
    CONSTRAINT ck_queue_status CHECK (status IN ('WAITING', 'CALLED', 'DONE', 'CANCELLED'))
);

CREATE INDEX idx_queue_waiting ON queue_ticket (status, vip, id);

CREATE TABLE operation_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    username   VARCHAR(50),
    action     VARCHAR(50)  NOT NULL,
    detail     VARCHAR(500)
);

CREATE INDEX idx_log_created ON operation_log (created_at);

CREATE TABLE bank_node (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_bank_node_name UNIQUE (name)
);

CREATE TABLE bank_edge (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    from_node INT            NOT NULL,
    to_node   INT            NOT NULL,
    distance  DECIMAL(10, 2) NOT NULL,
    CONSTRAINT uk_bank_edge UNIQUE (from_node, to_node),
    CONSTRAINT fk_bank_edge_from FOREIGN KEY (from_node) REFERENCES bank_node (id),
    CONSTRAINT fk_bank_edge_to FOREIGN KEY (to_node) REFERENCES bank_node (id),
    CONSTRAINT ck_bank_edge_distance CHECK (distance > 0)
);
