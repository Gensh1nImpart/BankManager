package com.bankmanager.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** 读取结果集时的空值安全工具，供各 JDBC 实现共用。 */
final class JdbcRows {

    private JdbcRows() {
    }

    /** 读取可空时间列，统一转成 {@link LocalDateTime}。 */
    static LocalDateTime localDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    /** 读取可空整数列（如 counter_no、age）。 */
    static Integer integer(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
