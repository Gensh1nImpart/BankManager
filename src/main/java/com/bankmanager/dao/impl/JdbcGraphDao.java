package com.bankmanager.dao.impl;

import com.bankmanager.dao.GraphDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.db.Database;
import com.bankmanager.domain.BankEdge;
import com.bankmanager.domain.BankNode;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 网点图 {@code bank_node} / {@code bank_edge} 的 JDBC 实现。 */
public final class JdbcGraphDao implements GraphDao {

    private final Database database;

    public JdbcGraphDao(Database database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public List<BankNode> nodes() {
        String sql = "SELECT id, name FROM bank_node ORDER BY id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<BankNode> nodes = new ArrayList<>();
            while (rs.next()) {
                nodes.add(new BankNode(rs.getInt("id"), rs.getString("name")));
            }
            return nodes;
        } catch (SQLException e) {
            throw new DaoException("查询网点节点失败", e);
        }
    }

    @Override
    public List<BankEdge> edges() {
        String sql = "SELECT from_node, to_node, distance FROM bank_edge ORDER BY id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<BankEdge> edges = new ArrayList<>();
            while (rs.next()) {
                edges.add(new BankEdge(
                        rs.getInt("from_node"),
                        rs.getInt("to_node"),
                        rs.getBigDecimal("distance")));
            }
            return edges;
        } catch (SQLException e) {
            throw new DaoException("查询网点边失败", e);
        }
    }

    @Override
    public void insertNode(int id, String name) {
        String sql = "INSERT INTO bank_node (id, name) VALUES (?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("新增网点节点失败：" + id, e);
        }
    }

    @Override
    public void insertEdge(int fromNode, int toNode, BigDecimal distance) {
        String sql = "INSERT INTO bank_edge (from_node, to_node, distance) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, fromNode);
            statement.setInt(2, toNode);
            statement.setBigDecimal(3, distance);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("新增网点边失败：" + fromNode + " -> " + toNode, e);
        }
    }
}
