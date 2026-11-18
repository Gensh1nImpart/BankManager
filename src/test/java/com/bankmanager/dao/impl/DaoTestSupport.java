package com.bankmanager.dao.impl;

import com.bankmanager.db.Database;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DAO 测试基类：共享一个 H2 内存库数据源，每个测试方法前重建表结构。
 */
abstract class DaoTestSupport {

    private static final String JDBC_URL =
            "jdbc:h2:mem:bank;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000";

    protected static final Database DATABASE = new Database(createDataSource());

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        config.setMaximumPoolSize(8);
        config.setPoolName("bank-dao-test");
        config.setConnectionTimeout(5_000);
        return new HikariDataSource(config);
    }

    @BeforeEach
    void resetSchema() throws Exception {
        try (Connection connection = DATABASE.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP ALL OBJECTS");
            }
            try (InputStream in = DaoTestSupport.class.getResourceAsStream("/schema-h2.sql")) {
                assertNotNull(in, "测试脚本 schema-h2.sql 未找到");
                RunScript.execute(connection, new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        }
    }
}
