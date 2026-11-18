package com.bankmanager.db;

import com.bankmanager.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据源与事务边界。整个应用共用一个实例，禁止在 UI 线程持连接。
 */
public final class Database implements AutoCloseable {

    /** 允许在事务中执行的数据库操作。 */
    @FunctionalInterface
    public interface TxFunction<T> {
        T apply(Connection connection) throws SQLException;
    }

    private final HikariDataSource dataSource;

    public Database(Config config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.dbUrl());
        hikari.setUsername(config.dbUser());
        hikari.setPassword(config.dbPassword());
        hikari.setMaximumPoolSize(config.poolSize());
        hikari.setPoolName("bank-manager");
        hikari.setConnectionTimeout(5_000);
        this.dataSource = new HikariDataSource(hikari);
    }

    /** 仅供测试使用，允许注入现成的数据源。 */
    public Database(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 在单个事务中执行，任何异常都会回滚。
     */
    public <T> T inTransaction(TxFunction<T> function) {
        try (Connection connection = getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = function.apply(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new DaoException("事务执行失败", e);
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
