package com.bankmanager.service.impl;

import com.bankmanager.dao.LogDao;
import com.bankmanager.dao.impl.JdbcCustomerDao;
import com.bankmanager.dao.impl.JdbcLogDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.db.Database;
import com.bankmanager.domain.AccountType;
import com.bankmanager.domain.Customer;
import com.bankmanager.domain.OperationLog;
import com.bankmanager.service.ServiceException;
import com.zaxxer.hikari.HikariConfig;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 真库事务测试（H2）：验证 {@link TransactionAwareDataSource} 让
 * {@code Database.inTransaction} 内的多个 DAO 共用同一连接，
 * 日志写入失败时余额更新会整体回滚。
 */
class AccountTransactionTest {

    private static final String CARD_A = "6222020000000001";
    private static final String CARD_B = "6222020000000002";

    private TransactionAwareDataSource dataSource;
    private Database database;
    private JdbcCustomerDao customerDao;
    private JdbcLogDao logDao;

    @BeforeEach
    void setUp() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:tx-" + System.nanoTime()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        config.setMaximumPoolSize(4);
        config.setPoolName("tx-test");
        config.setConnectionTimeout(5_000);
        dataSource = new TransactionAwareDataSource(config);
        database = new Database(dataSource);
        customerDao = new JdbcCustomerDao(database);
        logDao = new JdbcLogDao(database);

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP ALL OBJECTS");
            }
            try (InputStream in = AccountTransactionTest.class.getResourceAsStream("/schema-h2.sql")) {
                assertNotNull(in, "测试脚本 schema-h2.sql 未找到");
                RunScript.execute(connection, new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        }

        customerDao.insert(customer(CARD_A, "陈晨", "1000.00", "0.00"));
        customerDao.insert(customer(CARD_B, "刘洋", "500.00", "0.00"));
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private static Customer customer(String card, String name, String balance, String loan) {
        return new Customer(card, name, BCrypt.hashpw("123456", BCrypt.gensalt()), "13900000000",
                AccountType.NORMAL, new BigDecimal(balance), new BigDecimal(loan));
    }

    @Test
    @DisplayName("转账成功：两个账户余额与操作日志一起提交")
    void transferCommitsBalancesAndLog() {
        DefaultAccountService service = new DefaultAccountService(customerDao, logDao, database, () -> "tester");

        service.transfer(CARD_A, CARD_B, new BigDecimal("300.00"));

        assertEquals(new BigDecimal("700.00"), balanceOf(CARD_A));
        assertEquals(new BigDecimal("800.00"), balanceOf(CARD_B));
        List<OperationLog> logs = logDao.recent(10);
        assertEquals(1, logs.size());
        assertEquals("转账", logs.get(0).action());
        assertEquals("tester", logs.get(0).username());
    }

    @Test
    @DisplayName("日志写入失败时转账整体回滚，双方余额不变")
    void transferRollsBackWhenLogFails() {
        LogDao failingLog = new LogDao() {
            @Override
            public void append(String username, String action, String detail) {
                throw new DaoException("模拟日志表故障");
            }

            @Override
            public List<OperationLog> recent(int limit) {
                return List.of();
            }
        };
        DefaultAccountService service = new DefaultAccountService(customerDao, failingLog, database, () -> "tester");

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.transfer(CARD_A, CARD_B, new BigDecimal("300.00")));

        assertTrue(error.getMessage().contains("转账失败"), error.getMessage());
        assertEquals(0, balanceOf(CARD_A).compareTo(new BigDecimal("1000.00")));
        assertEquals(0, balanceOf(CARD_B).compareTo(new BigDecimal("500.00")));
        assertTrue(logDao.recent(10).isEmpty());
    }

    @Test
    @DisplayName("存款成功时余额与日志一起提交")
    void depositCommitsBalanceAndLog() {
        DefaultAccountService service = new DefaultAccountService(customerDao, logDao, database, () -> "tester");

        service.deposit(CARD_A, new BigDecimal("123.45"));

        assertEquals(new BigDecimal("1123.45"), balanceOf(CARD_A));
        assertEquals(1, logDao.recent(10).size());
    }

    @Test
    @DisplayName("余额不足时既不写余额也不写日志")
    void withdrawRejectsWithoutSideEffects() {
        DefaultAccountService service = new DefaultAccountService(customerDao, logDao, database, () -> "tester");

        assertThrows(ServiceException.class, () -> service.withdraw(CARD_A, new BigDecimal("99999.00")));

        assertEquals(0, balanceOf(CARD_A).compareTo(new BigDecimal("1000.00")));
        assertTrue(logDao.recent(10).isEmpty());
    }

    private BigDecimal balanceOf(String card) {
        return customerDao.findByCard(card).orElseThrow().balance();
    }
}
