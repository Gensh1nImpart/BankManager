package com.bankmanager.service.impl;

import com.bankmanager.config.Config;
import com.zaxxer.hikari.HikariDataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务感知数据源：把同一个线程在事务期间拿到的连接复用给各 DAO。
 *
 * <p>背景：DAO 接口只持有 {@link com.bankmanager.db.Database}，每次调用都会自己
 * {@code getConnection()}。若服务层只是把 DAO 调用包进
 * {@link com.bankmanager.db.Database#inTransaction}，各 DAO 会各自取到不同连接，
 * 余额更新与操作日志就无法真正同事务提交。
 *
 * <p>本类在 {@code getConnection()} 上做线程级绑定：
 * <ol>
 *     <li>外层先取连接并绑定到当前线程；</li>
 *     <li>事务内的 DAO 取到的是同一物理连接的包装，{@code close()} 在
 *         {@code autoCommit = false} 时不真正归还连接；</li>
 *     <li>{@code inTransaction} 结束把 {@code autoCommit} 恢复为 true 后关闭，
 *         包装的 {@code close()} 才把连接归还连接池并解绑。</li>
 * </ol>
 * 因此 DAO 层无需改动，{@code Database.inTransaction} 内的多条语句即为同一事务。
 *
 * <p>数据库地址、账号等仍完全来自 {@link Config}，不在这里硬编码。
 * 之所以不直接用 {@code new Database(config)}，是因为该入口内部会新建普通
 * {@link HikariDataSource}，服务层无法替换成事务感知版本（db 包已定型）。
 */
public final class TransactionAwareDataSource extends HikariDataSource {

    /** 与 Database 保持一致的取连接超时（毫秒）。 */
    private static final int CONNECTION_TIMEOUT_MS = 5_000;
    private static final String POOL_NAME = "bank-manager";

    /** 当前线程已绑定的物理连接（仅在事务期间长期存在）。 */
    private final transient ThreadLocal<Connection> bound = new ThreadLocal<>();

    public TransactionAwareDataSource(Config config) {
        setJdbcUrl(config.dbUrl());
        setUsername(config.dbUser());
        setPassword(config.dbPassword());
        setMaximumPoolSize(config.poolSize());
        setPoolName(POOL_NAME);
        setConnectionTimeout(CONNECTION_TIMEOUT_MS);
    }

    /** 仅供测试：直接使用现成的连接池配置（例如 H2 内存库）。 */
    public TransactionAwareDataSource(com.zaxxer.hikari.HikariConfig config) {
        super(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection existing = bound.get();
        if (existing != null) {
            return guard(existing);
        }
        Connection connection = super.getConnection();
        bound.set(connection);
        return guard(connection);
    }

    /** 用动态代理包一层，只改写 {@code close()} 的语义，其余全部透传。 */
    private Connection guard(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "close" -> {
                            releaseIfIdle(real);
                            return null;
                        }
                        case "equals" -> {
                            return proxy == args[0];
                        }
                        case "hashCode" -> {
                            return System.identityHashCode(proxy);
                        }
                        case "toString" -> {
                            return "TxBoundConnection[" + real + "]";
                        }
                        default -> {
                            // 其余方法直接委托给真实连接
                        }
                    }
                    try {
                        return method.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause() == null ? e : e.getCause();
                    }
                });
    }

    /** 事务未结束时保留线程绑定；空闲连接关闭并解绑。 */
    private void releaseIfIdle(Connection real) {
        try {
            if (!real.getAutoCommit()) {
                return;
            }
        } catch (SQLException e) {
            // 连接已不可用，按可释放处理
        }
        bound.remove();
        try {
            real.close();
        } catch (SQLException ignored) {
            // 关闭失败无需向上抛出，连接池会自行回收
        }
    }
}
