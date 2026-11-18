package com.bankmanager.dao.impl;

import com.bankmanager.dao.EmployeeDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.db.Database;
import com.bankmanager.domain.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** 职员表 {@code employee} 的 JDBC 实现。 */
public final class JdbcEmployeeDao implements EmployeeDao {

    private static final String COLUMNS = "id, name, sex, age, tele, address, position";

    private final Database database;

    public JdbcEmployeeDao(Database database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public List<Employee> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM employee ORDER BY id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Employee> employees = new ArrayList<>();
            while (rs.next()) {
                employees.add(map(rs));
            }
            return employees;
        } catch (SQLException e) {
            throw new DaoException("查询职员列表失败", e);
        }
    }

    @Override
    public Optional<Employee> findById(String id) {
        String sql = "SELECT " + COLUMNS + " FROM employee WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("按编号查询职员失败：" + id, e);
        }
    }

    @Override
    public void insert(Employee employee) {
        String sql = "INSERT INTO employee (id, name, sex, age, tele, address, position) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.id());
            statement.setString(2, employee.name());
            statement.setString(3, employee.sex());
            setInteger(statement, 4, employee.age());
            statement.setString(5, employee.tele());
            statement.setString(6, employee.address());
            statement.setString(7, employee.position());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("新增职员失败：" + employee.id(), e);
        }
    }

    @Override
    public void update(Employee employee) {
        String sql = "UPDATE employee SET name = ?, sex = ?, age = ?, tele = ?, address = ?, position = ? "
                + "WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.name());
            statement.setString(2, employee.sex());
            setInteger(statement, 3, employee.age());
            statement.setString(4, employee.tele());
            statement.setString(5, employee.address());
            statement.setString(6, employee.position());
            statement.setString(7, employee.id());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("更新职员失败：" + employee.id(), e);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM employee WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("删除职员失败：" + id, e);
        }
    }

    /**
     * 取现有数字编号的最大值 +1，三位补零；非数字编号（历史脏数据）忽略。
     * 编号由调用方负责避免并发冲突，主键约束兜底。
     */
    @Override
    public String nextId() {
        String sql = "SELECT id FROM employee";
        int max = 0;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                if (id == null) {
                    continue;
                }
                try {
                    max = Math.max(max, Integer.parseInt(id.trim()));
                } catch (NumberFormatException ignored) {
                    // 非数字编号不参与递增，保持与旧数据兼容
                }
            }
        } catch (SQLException e) {
            throw new DaoException("生成职员编号失败", e);
        }
        return String.format("%03d", max + 1);
    }

    private static void setInteger(PreparedStatement statement, int index, Integer value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private static Employee map(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("sex"),
                JdbcRows.integer(rs, "age"),
                rs.getString("tele"),
                rs.getString("address"),
                rs.getString("position"));
    }
}
