package com.bankmanager.dao.impl;

import com.bankmanager.dao.UserDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.db.Database;
import com.bankmanager.domain.UserAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

/** 登录账号表 {@code app_user} 的 JDBC 实现。 */
public final class JdbcUserDao implements UserDao {

    private final Database database;

    public JdbcUserDao(Database database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role FROM app_user WHERE username = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UserAccount(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")));
            }
        } catch (SQLException e) {
            throw new DaoException("按用户名查询账号失败：" + username, e);
        }
    }

    @Override
    public void insert(String username, String passwordHash, String role) {
        String sql = "INSERT INTO app_user (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, role);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("新增账号失败：" + username, e);
        }
    }

    @Override
    public boolean exists(String username) {
        String sql = "SELECT 1 FROM app_user WHERE username = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("检查账号是否存在失败：" + username, e);
        }
    }
}
