package com.bankmanager.dao.impl;

import com.bankmanager.dao.LogDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.db.Database;
import com.bankmanager.domain.OperationLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 操作日志表 {@code operation_log} 的 JDBC 实现。 */
public final class JdbcLogDao implements LogDao {

    private final Database database;

    public JdbcLogDao(Database database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public void append(String username, String action, String detail) {
        String sql = "INSERT INTO operation_log (username, action, detail) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, action);
            statement.setString(3, detail);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("写入操作日志失败：" + action, e);
        }
    }

    @Override
    public List<OperationLog> recent(int limit) {
        String sql = "SELECT id, created_at, username, action, detail FROM operation_log "
                + "ORDER BY created_at DESC, id DESC LIMIT ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(limit, 0));
            try (ResultSet rs = statement.executeQuery()) {
                List<OperationLog> logs = new ArrayList<>();
                while (rs.next()) {
                    logs.add(new OperationLog(
                            rs.getLong("id"),
                            JdbcRows.localDateTime(rs, "created_at"),
                            rs.getString("username"),
                            rs.getString("action"),
                            rs.getString("detail")));
                }
                return logs;
            }
        } catch (SQLException e) {
            throw new DaoException("查询操作日志失败", e);
        }
    }
}
