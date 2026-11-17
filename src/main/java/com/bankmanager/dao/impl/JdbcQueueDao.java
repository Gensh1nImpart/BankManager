package com.bankmanager.dao.impl;

import com.bankmanager.dao.QueueDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.db.Database;
import com.bankmanager.domain.QueueTicket;
import com.bankmanager.domain.TicketStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** 排队票据表 {@code queue_ticket} 的 JDBC 实现。 */
public final class JdbcQueueDao implements QueueDao {

    private static final String COLUMNS =
            "id, number, customer_name, vip, status, counter_no, created_at, called_at";
    /** 叫号遇到并发抢占时的最大重试次数。 */
    private static final int CALL_MAX_ATTEMPTS = 1_000;

    private final Database database;

    public JdbcQueueDao(Database database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    /**
     * 取号。号码取同一天、同一队列（VIP/普通）当前最大值 +1，从 1 开始；
     * 最大值查询与插入放在同一事务中，避免并发取号拿到重复号码。
     */
    @Override
    public QueueTicket enqueue(String customerName, boolean vip) {
        LocalDate today = LocalDate.now();
        Timestamp dayStart = Timestamp.valueOf(today.atStartOfDay());
        Timestamp dayEnd = Timestamp.valueOf(today.plusDays(1).atStartOfDay());
        String maxSql = "SELECT COALESCE(MAX(number), 0) FROM queue_ticket "
                + "WHERE vip = ? AND created_at >= ? AND created_at < ?";
        String insertSql = "INSERT INTO queue_ticket (number, customer_name, vip, status) "
                + "VALUES (?, ?, ?, ?)";
        return database.inTransaction(connection -> {
            int number = 1;
            try (PreparedStatement statement = connection.prepareStatement(maxSql)) {
                statement.setBoolean(1, vip);
                statement.setTimestamp(2, dayStart);
                statement.setTimestamp(3, dayEnd);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        number = rs.getInt(1) + 1;
                    }
                }
            }
            long id;
            try (PreparedStatement statement = connection.prepareStatement(
                    insertSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, number);
                statement.setString(2, customerName);
                statement.setBoolean(3, vip);
                statement.setString(4, TicketStatus.WAITING.name());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("取号后数据库未返回自增主键");
                    }
                    id = keys.getLong(1);
                }
            }
            return findById(connection, id)
                    .orElseThrow(() -> new SQLException("取号后回读票据失败：" + id));
        });
    }

    @Override
    public List<QueueTicket> waiting(boolean vip) {
        String sql = "SELECT " + COLUMNS + " FROM queue_ticket "
                + "WHERE vip = ? AND status = ? ORDER BY id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, vip);
            statement.setString(2, TicketStatus.WAITING.name());
            try (ResultSet rs = statement.executeQuery()) {
                List<QueueTicket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(map(rs));
                }
                return tickets;
            }
        } catch (SQLException e) {
            throw new DaoException(vip ? "查询 VIP 等待队列失败" : "查询普通等待队列失败", e);
        }
    }

    /**
     * 叫号。事务内用 {@code SELECT ... FOR UPDATE} 锁住队首等待票据，
     * 更新为 CALLED 并回读；条件更新（status = WAITING）防止并发下重复叫号。
     */
    @Override
    public Optional<QueueTicket> callNext(boolean vip, int counterNo) {
        String selectSql = "SELECT id FROM queue_ticket WHERE vip = ? AND status = ? "
                + "ORDER BY id LIMIT 1 FOR UPDATE";
        String updateSql = "UPDATE queue_ticket SET status = ?, counter_no = ?, "
                + "called_at = CURRENT_TIMESTAMP WHERE id = ? AND status = ?";
        return database.inTransaction(connection -> {
            for (int attempt = 0; attempt < CALL_MAX_ATTEMPTS; attempt++) {
                long id;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setBoolean(1, vip);
                    statement.setString(2, TicketStatus.WAITING.name());
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            return Optional.<QueueTicket>empty();
                        }
                        id = rs.getLong(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setString(1, TicketStatus.CALLED.name());
                    statement.setInt(2, counterNo);
                    statement.setLong(3, id);
                    statement.setString(4, TicketStatus.WAITING.name());
                    if (statement.executeUpdate() == 0) {
                        // 该票据已被并发事务叫走，重新取队首
                        continue;
                    }
                }
                return findById(connection, id);
            }
            throw new SQLException("叫号失败：并发冲突重试次数超过上限");
        });
    }

    @Override
    public void finish(long ticketId) {
        String sql = "UPDATE queue_ticket SET status = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, TicketStatus.DONE.name());
            statement.setLong(2, ticketId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("完成票据失败：" + ticketId, e);
        }
    }

    @Override
    public List<QueueTicket> recent(int limit) {
        String sql = "SELECT " + COLUMNS + " FROM queue_ticket ORDER BY id DESC LIMIT ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(limit, 0));
            try (ResultSet rs = statement.executeQuery()) {
                List<QueueTicket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(map(rs));
                }
                return tickets;
            }
        } catch (SQLException e) {
            throw new DaoException("查询最近票据失败", e);
        }
    }

    private static Optional<QueueTicket> findById(Connection connection, long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM queue_ticket WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private static QueueTicket map(ResultSet rs) throws SQLException {
        return new QueueTicket(
                rs.getLong("id"),
                rs.getInt("number"),
                rs.getString("customer_name"),
                rs.getBoolean("vip"),
                TicketStatus.valueOf(rs.getString("status")),
                JdbcRows.integer(rs, "counter_no"),
                JdbcRows.localDateTime(rs, "created_at"),
                JdbcRows.localDateTime(rs, "called_at"));
    }
}
