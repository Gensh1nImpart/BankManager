package com.bankmanager.service.impl;

import com.bankmanager.db.Database;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.UUID;

/**
 * 服务层测试用的空 H2 数据源：只提供 {@link Database#inTransaction} 所需的事务边界，
 * 业务数据全部由手写 fake DAO 维护，不建表、不执行 SQL。
 */
final class TestDatabases {

    private TestDatabases() {
    }

    static Database inMemory() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:svc-" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        config.setMaximumPoolSize(4);
        config.setPoolName("svc-test");
        config.setConnectionTimeout(5_000);
        return new Database(new HikariDataSource(config));
    }
}
