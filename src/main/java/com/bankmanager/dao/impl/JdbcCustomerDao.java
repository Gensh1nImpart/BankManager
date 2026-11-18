package com.bankmanager.dao.impl;

import com.bankmanager.dao.CustomerDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.db.Database;
import com.bankmanager.domain.AccountType;
import com.bankmanager.domain.Customer;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** 客户表 {@code customer} 的 JDBC 实现。 */
public final class JdbcCustomerDao implements CustomerDao {

    /** 卡号前缀，配合 10 位随机数字组成 16 位卡号。 */
    private static final String CARD_PREFIX = "622202";
    /** 随机段上限（10 位数字）。 */
    private static final long CARD_RANDOM_BOUND = 10_000_000_000L;
    /** 生成卡号的最大重试次数，避免库内卡号高度密集时死循环。 */
    private static final int CARD_MAX_ATTEMPTS = 1_000;

    private static final String COLUMNS =
            "card, name, password_hash, tele, type, balance, loan";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Database database;

    public JdbcCustomerDao(Database database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM customer ORDER BY card";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(map(rs));
            }
            return customers;
        } catch (SQLException e) {
            throw new DaoException("查询客户列表失败", e);
        }
    }

    @Override
    public Optional<Customer> findByCard(String card) {
        String sql = "SELECT " + COLUMNS + " FROM customer WHERE card = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, card);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("按卡号查询客户失败：" + card, e);
        }
    }

    @Override
    public void insert(Customer customer) {
        String sql = "INSERT INTO customer (card, name, password_hash, tele, type, balance, loan) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.card());
            statement.setString(2, customer.name());
            statement.setString(3, customer.passwordHash());
            statement.setString(4, customer.tele());
            statement.setString(5, customer.type().name());
            statement.setBigDecimal(6, customer.balance());
            statement.setBigDecimal(7, customer.loan());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("新增客户失败：" + customer.card(), e);
        }
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customer SET name = ?, password_hash = ?, tele = ?, type = ?, "
                + "balance = ?, loan = ?, updated_at = CURRENT_TIMESTAMP WHERE card = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.name());
            statement.setString(2, customer.passwordHash());
            statement.setString(3, customer.tele());
            statement.setString(4, customer.type().name());
            statement.setBigDecimal(5, customer.balance());
            statement.setBigDecimal(6, customer.loan());
            statement.setString(7, customer.card());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("更新客户失败：" + customer.card(), e);
        }
    }

    @Override
    public void deleteByCard(String card) {
        String sql = "DELETE FROM customer WHERE card = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, card);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("删除客户失败：" + card, e);
        }
    }

    @Override
    public void updateBalance(String card, BigDecimal balance) {
        String sql = "UPDATE customer SET balance = ?, updated_at = CURRENT_TIMESTAMP WHERE card = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, balance);
            statement.setString(2, card);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("更新客户余额失败：" + card, e);
        }
    }

    @Override
    public void updateLoan(String card, BigDecimal loan) {
        String sql = "UPDATE customer SET loan = ?, updated_at = CURRENT_TIMESTAMP WHERE card = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, loan);
            statement.setString(2, card);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("更新客户贷款失败：" + card, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM customer";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DaoException("统计客户数量失败", e);
        }
    }

    /**
     * 生成 {@code 622202} + 10 位随机数字的 16 位卡号，并保证库中不存在。
     * 卡号空间远大于客户数量，重试次数仅用于防御异常数据。
     */
    @Override
    public String nextCardNumber() {
        for (int attempt = 0; attempt < CARD_MAX_ATTEMPTS; attempt++) {
            String card = CARD_PREFIX + String.format("%010d", RANDOM.nextLong(CARD_RANDOM_BOUND));
            if (findByCard(card).isEmpty()) {
                return card;
            }
        }
        throw new DaoException("生成卡号失败：连续 " + CARD_MAX_ATTEMPTS + " 次命中已存在的卡号");
    }

    private static Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("card"),
                rs.getString("name"),
                rs.getString("password_hash"),
                rs.getString("tele"),
                AccountType.fromLabel(rs.getString("type")),
                rs.getBigDecimal("balance"),
                rs.getBigDecimal("loan"));
    }
}
