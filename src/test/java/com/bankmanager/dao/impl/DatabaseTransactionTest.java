package com.bankmanager.dao.impl;

import com.bankmanager.db.DaoException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** {@link com.bankmanager.db.Database#inTransaction} 的提交与回滚测试。 */
class DatabaseTransactionTest extends DaoTestSupport {

    @Test
    void inTransactionCommitsOnSuccess() {
        DATABASE.inTransaction(connection -> {
            insertCustomer(connection, "6222020000000001");
            return null;
        });

        assertEquals(1, countCustomers());
    }

    @Test
    void inTransactionRollsBackOnRuntimeException() {
        assertThrows(IllegalStateException.class, () -> DATABASE.inTransaction(connection -> {
            insertCustomer(connection, "6222020000000001");
            throw new IllegalStateException("业务校验失败");
        }));

        assertEquals(0, countCustomers());
    }

    @Test
    void inTransactionRollsBackOnSqlExceptionAndWrapsIt() {
        assertThrows(DaoException.class, () -> DATABASE.inTransaction(connection -> {
            insertCustomer(connection, "6222020000000001");
            throw new SQLException("模拟数据库故障");
        }));

        assertEquals(0, countCustomers());
    }

    private static void insertCustomer(Connection connection, String card) throws SQLException {
        String sql = "INSERT INTO customer (card, name, password_hash, tele, type, balance, loan) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, card);
            statement.setString(2, "事务客户");
            statement.setString(3, "hash");
            statement.setString(4, "13900000001");
            statement.setString(5, "NORMAL");
            statement.setBigDecimal(6, new java.math.BigDecimal("100.00"));
            statement.setBigDecimal(7, java.math.BigDecimal.ZERO);
            statement.executeUpdate();
        }
    }

    private static long countCustomers() {
        return new JdbcCustomerDao(DATABASE).count();
    }
}
