package com.bankmanager.service.impl;

import com.bankmanager.db.Database;
import com.bankmanager.domain.AccountType;
import com.bankmanager.domain.Customer;
import com.bankmanager.service.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 账户与资金业务单元测试：手写 fake DAO + H2 提供事务边界。 */
class DefaultAccountServiceTest {

    private static final String CARD_A = "6222020000000001";
    private static final String CARD_B = "6222020000000002";

    private FakeCustomerDao customerDao;
    private FakeLogDao logDao;
    private Database database;
    private DefaultAccountService service;

    @BeforeEach
    void setUp() {
        customerDao = new FakeCustomerDao();
        logDao = new FakeLogDao();
        database = TestDatabases.inMemory();
        service = new DefaultAccountService(customerDao, logDao, database, () -> "tester");
        customerDao.put(customer(CARD_A, "陈晨", "1000.00", "0.00"));
        customerDao.put(customer(CARD_B, "刘洋", "500.00", "2000.00"));
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    private static Customer customer(String card, String name, String balance, String loan) {
        return new Customer(card, name, BCrypt.hashpw("123456", BCrypt.gensalt()), "13900000000",
                AccountType.NORMAL, new BigDecimal(balance), new BigDecimal(loan));
    }

    // ---------- 存款 ----------

    @Test
    @DisplayName("存款增加余额并写入操作日志")
    void depositIncreasesBalanceAndWritesLog() {
        service.deposit(CARD_A, new BigDecimal("500.00"));

        assertEquals(new BigDecimal("1500.00"), customerDao.get(CARD_A).balance());
        assertEquals(1, logDao.entries().size());
        assertEquals("存款", logDao.entries().get(0).action());
        assertEquals("tester", logDao.entries().get(0).username());
    }

    @Test
    @DisplayName("存款金额必须大于 0")
    void depositRejectsNonPositiveAmount() {
        assertAll(
                () -> assertThrows(ServiceException.class,
                        () -> service.deposit(CARD_A, BigDecimal.ZERO)),
                () -> assertThrows(ServiceException.class,
                        () -> service.deposit(CARD_A, new BigDecimal("-1.00"))),
                () -> assertThrows(ServiceException.class,
                        () -> service.deposit(CARD_A, null)));
        assertEquals(new BigDecimal("1000.00"), customerDao.get(CARD_A).balance());
        assertTrue(logDao.entries().isEmpty());
    }

    @Test
    @DisplayName("金额超过两位小数被拒绝")
    void depositRejectsMoreThanTwoDecimals() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.deposit(CARD_A, new BigDecimal("1.005")));

        assertTrue(error.getMessage().contains("两位小数"), error.getMessage());
    }

    @Test
    @DisplayName("单笔金额不能超过 1 亿元，正好 1 亿元可以通过")
    void depositEnforcesMaximumAmount() {
        assertThrows(ServiceException.class,
                () -> service.deposit(CARD_A, new BigDecimal("100000000.01")));

        service.deposit(CARD_A, new BigDecimal("100000000"));
        assertEquals(new BigDecimal("100001000.00"), customerDao.get(CARD_A).balance());
    }

    @Test
    @DisplayName("存款到不存在的卡号提示卡号不存在")
    void depositRejectsUnknownCard() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.deposit("6222020000009999", new BigDecimal("10.00")));

        assertTrue(error.getMessage().contains("卡号不存在"), error.getMessage());
    }

    @Test
    @DisplayName("卡号必须是 16 位数字")
    void rejectsInvalidCardFormat() {
        assertAll(
                () -> assertThrows(ServiceException.class,
                        () -> service.deposit("622202", new BigDecimal("10.00"))),
                () -> assertThrows(ServiceException.class,
                        () -> service.withdraw("6222-0200-0000-0001", new BigDecimal("10.00"))),
                () -> assertThrows(ServiceException.class,
                        () -> service.customer("abc")));
    }

    // ---------- 取款 ----------

    @Test
    @DisplayName("取款减少余额并写日志")
    void withdrawReducesBalance() {
        service.withdraw(CARD_A, new BigDecimal("250.50"));

        assertEquals(new BigDecimal("749.50"), customerDao.get(CARD_A).balance());
        assertTrue(logDao.hasAction("取款"));
    }

    @Test
    @DisplayName("余额不足时取款失败且余额不变")
    void withdrawRejectsInsufficientBalance() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.withdraw(CARD_A, new BigDecimal("1000.01")));

        assertTrue(error.getMessage().contains("余额不足"), error.getMessage());
        assertEquals(new BigDecimal("1000.00"), customerDao.get(CARD_A).balance());
        assertTrue(logDao.entries().isEmpty());
    }

    @Test
    @DisplayName("余额可以全部取出，余额变为 0")
    void withdrawAllBalance() {
        service.withdraw(CARD_A, new BigDecimal("1000.00"));

        assertEquals(0, customerDao.get(CARD_A).balance().compareTo(BigDecimal.ZERO));
    }

    // ---------- 转账 ----------

    @Test
    @DisplayName("转账成功后双方余额正确并写一条转账日志")
    void transferMovesMoneyBetweenAccounts() {
        service.transfer(CARD_A, CARD_B, new BigDecimal("300.25"));

        assertAll(
                () -> assertEquals(new BigDecimal("699.75"), customerDao.get(CARD_A).balance()),
                () -> assertEquals(new BigDecimal("800.25"), customerDao.get(CARD_B).balance()),
                () -> assertEquals(1, logDao.entries().size()),
                () -> assertEquals("转账", logDao.entries().get(0).action()));
    }

    @Test
    @DisplayName("不允许向自己转账")
    void transferRejectsSelfTransfer() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.transfer(CARD_A, CARD_A, new BigDecimal("10.00")));

        assertTrue(error.getMessage().contains("自己"), error.getMessage());
        assertEquals(new BigDecimal("1000.00"), customerDao.get(CARD_A).balance());
    }

    @Test
    @DisplayName("转入卡号不存在时转账失败，转出方余额不变")
    void transferRejectsUnknownTarget() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.transfer(CARD_A, "6222020000009999", new BigDecimal("10.00")));

        assertTrue(error.getMessage().contains("卡号不存在"), error.getMessage());
        assertEquals(new BigDecimal("1000.00"), customerDao.get(CARD_A).balance());
    }

    @Test
    @DisplayName("转账金额超过余额时失败且双方余额不变")
    void transferRejectsInsufficientBalance() {
        assertThrows(ServiceException.class,
                () -> service.transfer(CARD_A, CARD_B, new BigDecimal("1000.01")));

        assertAll(
                () -> assertEquals(new BigDecimal("1000.00"), customerDao.get(CARD_A).balance()),
                () -> assertEquals(new BigDecimal("500.00"), customerDao.get(CARD_B).balance()),
                () -> assertTrue(logDao.entries().isEmpty()));
    }

    @Test
    @DisplayName("转账金额同样受两位小数与上限约束")
    void transferValidatesAmount() {
        assertThrows(ServiceException.class,
                () -> service.transfer(CARD_A, CARD_B, new BigDecimal("1.001")));
        assertThrows(ServiceException.class,
                () -> service.transfer(CARD_A, CARD_B, new BigDecimal("0")));
    }

    // ---------- 贷款 / 还款 ----------

    @Test
    @DisplayName("贷款同时增加余额与贷款余额")
    void takeLoanIncreasesBalanceAndLoan() {
        service.takeLoan(CARD_A, new BigDecimal("5000.00"));

        assertAll(
                () -> assertEquals(new BigDecimal("6000.00"), customerDao.get(CARD_A).balance()),
                () -> assertEquals(new BigDecimal("5000.00"), customerDao.get(CARD_A).loan()),
                () -> assertTrue(logDao.hasAction("贷款")));
    }

    @Test
    @DisplayName("贷款总额不能超过 1 亿元")
    void takeLoanEnforcesTotalLimit() {
        customerDao.updateLoan(CARD_A, new BigDecimal("90000000.00"));

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.takeLoan(CARD_A, new BigDecimal("20000000.00")));

        assertTrue(error.getMessage().contains("贷款总额"), error.getMessage());
        assertEquals(new BigDecimal("90000000.00"), customerDao.get(CARD_A).loan());
    }

    @Test
    @DisplayName("还款同时减少余额与贷款余额")
    void repayLoanReducesBalanceAndLoan() {
        service.repayLoan(CARD_B, new BigDecimal("300.00"));

        assertAll(
                () -> assertEquals(new BigDecimal("200.00"), customerDao.get(CARD_B).balance()),
                () -> assertEquals(new BigDecimal("1700.00"), customerDao.get(CARD_B).loan()),
                () -> assertTrue(logDao.hasAction("还款")));
    }

    @Test
    @DisplayName("还款金额不能超过贷款余额")
    void repayLoanRejectsAmountOverLoan() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.repayLoan(CARD_B, new BigDecimal("2000.01")));

        assertTrue(error.getMessage().contains("不能超过贷款余额"), error.getMessage());
        assertEquals(new BigDecimal("2000.00"), customerDao.get(CARD_B).loan());
    }

    @Test
    @DisplayName("还款时余额不足则失败")
    void repayLoanRejectsInsufficientBalance() {
        customerDao.put(customer(CARD_B, "刘洋", "100.00", "2000.00"));

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.repayLoan(CARD_B, new BigDecimal("500.00")));

        assertTrue(error.getMessage().contains("余额不足"), error.getMessage());
        assertEquals(new BigDecimal("2000.00"), customerDao.get(CARD_B).loan());
    }

    @Test
    @DisplayName("没有贷款时不能还款")
    void repayLoanRejectsWhenNoLoan() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.repayLoan(CARD_A, new BigDecimal("100.00")));

        assertTrue(error.getMessage().contains("没有"), error.getMessage());
    }

    // ---------- 利息 ----------

    @Test
    @DisplayName("利息按本金×月利率×月数计算并四舍五入两位")
    void interestRoundsHalfUpToTwoDecimals() {
        customerDao.put(customer(CARD_A, "陈晨", "1234.56", "0.00"));

        BigDecimal interest = service.interest(CARD_A, new BigDecimal("0.0325"), 7);

        assertEquals(new BigDecimal("280.86"), interest);
    }

    @Test
    @DisplayName("利息试算不修改余额")
    void interestDoesNotChangeBalance() {
        service.interest(CARD_A, new BigDecimal("0.01"), 12);

        assertEquals(new BigDecimal("1000.00"), customerDao.get(CARD_A).balance());
    }

    @Test
    @DisplayName("极小的利息结果按 HALF_UP 进位到分")
    void interestRoundsSmallResult() {
        customerDao.put(customer(CARD_A, "陈晨", "0.03", "0.00"));

        assertEquals(new BigDecimal("0.01"), service.interest(CARD_A, new BigDecimal("0.333"), 1));
    }

    @Test
    @DisplayName("月利率与月数非法时拒绝计算")
    void interestValidatesInputs() {
        assertAll(
                () -> assertThrows(ServiceException.class,
                        () -> service.interest(CARD_A, new BigDecimal("-0.01"), 1)),
                () -> assertThrows(ServiceException.class,
                        () -> service.interest(CARD_A, new BigDecimal("1.5"), 1)),
                () -> assertThrows(ServiceException.class,
                        () -> service.interest(CARD_A, new BigDecimal("0.01"), 0)),
                () -> assertThrows(ServiceException.class,
                        () -> service.interest(CARD_A, null, 1)));
    }

    // ---------- 开户 / 修改 / 删除 ----------

    @Test
    @DisplayName("开户自动生成 16 位卡号且密码以 BCrypt 存储")
    void createCustomerGeneratesCardAndHashesPassword() {
        service.createCustomer(new Customer("", "新客户", "", "13800000000",
                AccountType.VIP, new BigDecimal("100.00"), BigDecimal.ZERO), "abc123");

        Customer created = customerDao.findAll().stream()
                .filter(customer -> customer.name().equals("新客户"))
                .findFirst().orElseThrow();
        assertAll(
                () -> assertEquals(16, created.card().length()),
                () -> assertTrue(created.card().matches("\\d{16}")),
                () -> assertNotEquals("abc123", created.passwordHash()),
                () -> assertTrue(BCrypt.checkpw("abc123", created.passwordHash())),
                () -> assertEquals(AccountType.VIP, created.type()),
                () -> assertTrue(logDao.hasAction("开户")));
    }

    @Test
    @DisplayName("开户时卡号重复被拒绝")
    void createCustomerRejectsDuplicateCard() {
        Customer duplicate = new Customer(CARD_A, "重复卡号", "", "13800000000",
                AccountType.NORMAL, BigDecimal.ZERO, BigDecimal.ZERO);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.createCustomer(duplicate, "abc123"));

        assertTrue(error.getMessage().contains("卡号已存在"), error.getMessage());
    }

    @Test
    @DisplayName("开户密码少于 6 位被拒绝")
    void createCustomerRejectsShortPassword() {
        Customer customer = new Customer("", "新客户", "", "13800000000",
                AccountType.NORMAL, BigDecimal.ZERO, BigDecimal.ZERO);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.createCustomer(customer, "12345"));

        assertTrue(error.getMessage().contains("至少"), error.getMessage());
        assertEquals(2, customerDao.size());
    }

    @Test
    @DisplayName("修改客户忽略传入的余额与贷款，余额只能由业务方法变动")
    void updateCustomerKeepsBalanceAndLoan() {
        service.updateCustomer(new Customer(CARD_A, "陈晨改名", "", "13911112222",
                AccountType.VIP, new BigDecimal("999999.00"), new BigDecimal("88888.00")));

        Customer updated = customerDao.get(CARD_A);
        assertAll(
                () -> assertEquals("陈晨改名", updated.name()),
                () -> assertEquals(AccountType.VIP, updated.type()),
                () -> assertEquals(new BigDecimal("1000.00"), updated.balance()),
                () -> assertEquals(new BigDecimal("0.00"), updated.loan()),
                () -> assertTrue(logDao.hasAction("修改客户")));
    }

    @Test
    @DisplayName("修改客户时留空密码表示不变，填写新密码则重新哈希")
    void updateCustomerPasswordHandling() {
        String originalHash = customerDao.get(CARD_A).passwordHash();

        service.updateCustomer(new Customer(CARD_A, "陈晨", "", "13900000000",
                AccountType.NORMAL, BigDecimal.ZERO, BigDecimal.ZERO));
        assertEquals(originalHash, customerDao.get(CARD_A).passwordHash());

        service.updateCustomer(new Customer(CARD_A, "陈晨", "newpass1", "13900000000",
                AccountType.NORMAL, BigDecimal.ZERO, BigDecimal.ZERO));
        String newHash = customerDao.get(CARD_A).passwordHash();
        assertNotEquals(originalHash, newHash);
        assertTrue(BCrypt.checkpw("newpass1", newHash));
    }

    @Test
    @DisplayName("删除客户后列表为空并写日志")
    void deleteCustomerRemovesRecord() {
        service.deleteCustomer(CARD_A);

        assertTrue(customerDao.findByCard(CARD_A).isEmpty());
        assertEquals(1, customerDao.size());
        assertTrue(logDao.hasAction("删除客户"));
    }

    @Test
    @DisplayName("删除不存在的客户提示卡号不存在")
    void deleteCustomerRejectsUnknownCard() {
        assertThrows(ServiceException.class, () -> service.deleteCustomer("6222020000009999"));
    }
}
