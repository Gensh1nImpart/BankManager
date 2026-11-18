-- 演示数据（密码均为 123456，teller 账号密码为 teller123）
-- 导入顺序：先 schema.sql，再本文件。

USE `bank_manager`;

INSERT INTO app_user (username, password_hash, role) VALUES
    ('admin',  '$2a$10$GBvOVkELS8etm4H3739D7e5JT/1U5iwivq5pN.BEfOivcnq3wgfUq', 'ADMIN'),
    ('teller', '$2a$10$isG/Z8OCSx5QWlzmmkfc5.wGCZV68pCHx6iLvy5Axse2cd3AzjtTm', 'TELLER');

INSERT INTO employee (id, name, sex, age, tele, address, position) VALUES
    ('001', '张伟', '男', 32, '13800000001', '济南市历下区经十路 100 号', '柜员'),
    ('002', '李娜', '女', 28, '13800000002', '济南市市中区舜耕路 8 号', '大堂经理'),
    ('003', '王强', '男', 41, '13800000003', '济南市高新区齐鲁软件园', '支行行长');

INSERT INTO customer (card, name, password_hash, tele, type, balance, loan) VALUES
    ('6222020000000001', '陈晨', '$2a$10$GBvOVkELS8etm4H3739D7e5JT/1U5iwivq5pN.BEfOivcnq3wgfUq', '13900000001', 'NORMAL',  12500.00,      0.00),
    ('6222020000000002', '刘洋', '$2a$10$GBvOVkELS8etm4H3739D7e5JT/1U5iwivq5pN.BEfOivcnq3wgfUq', '13900000002', 'NORMAL',   8600.50,   2000.00),
    ('6222020000000003', '赵敏', '$2a$10$GBvOVkELS8etm4H3739D7e5JT/1U5iwivq5pN.BEfOivcnq3wgfUq', '13900000003', 'VIP',    680000.00,      0.00),
    ('6222020000000004', '孙浩', '$2a$10$GBvOVkELS8etm4H3739D7e5JT/1U5iwivq5pN.BEfOivcnq3wgfUq', '13900000004', 'VIP',    215000.75,  50000.00),
    ('6222020000000005', '周琳', '$2a$10$GBvOVkELS8etm4H3739D7e5JT/1U5iwivq5pN.BEfOivcnq3wgfUq', '13900000005', 'NORMAL',   3200.00,      0.00),
    ('6222020000000006', '吴迪', '$2a$10$GBvOVkELS8etm4H3739D7e5JT/1U5iwivq5pN.BEfOivcnq3wgfUq', '13900000006', 'VIP',     98000.00,  12000.00);

INSERT INTO bank_node (id, name) VALUES
    (1, '大门'),
    (2, '网点1'),
    (3, '网点2'),
    (4, '分行'),
    (5, '自助银行'),
    (6, '大学城支行');

INSERT INTO bank_edge (from_node, to_node, distance) VALUES
    (1, 2, 20.00),
    (1, 3, 40.00),
    (2, 3, 15.00),
    (2, 4, 30.00),
    (3, 4, 25.00),
    (4, 5, 10.00),
    (3, 6, 18.00),
    (6, 4, 12.00);
