package com.bankmanager.dao.impl;

import com.bankmanager.dao.CustomerDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.domain.AccountType;
import com.bankmanager.domain.Customer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link JdbcCustomerDao} 的 H2 集成测试。 */
class JdbcCustomerDaoTest extends DaoTestSupport {

    private final CustomerDao dao = new JdbcCustomerDao(DATABASE);

    @Test
    void insertAndFindByCard() {
        dao.insert(customer("6222020000000001", "陈晨", AccountType.NORMAL, "12500.00", "0.00"));

        Customer found = dao.findByCard("6222020000000001").orElseThrow();
        assertEquals("陈晨", found.name());
        assertEquals("hash-6222020000000001", found.passwordHash());
        assertEquals("13900000001", found.tele());
        assertEquals(AccountType.NORMAL, found.type());
        assertEquals(0, new BigDecimal("12500.00").compareTo(found.balance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(found.loan()));
    }

    @Test
    void findMissingCardReturnsEmpty() {
        assertTrue(dao.findByCard("6222029999999999").isEmpty());
    }

    @Test
    void findAllSortedByCardAndCount() {
        dao.insert(customer("6222020000000003", "赵敏", AccountType.VIP, "680000.00", "0.00"));
        dao.insert(customer("6222020000000001", "陈晨", AccountType.NORMAL, "12500.00", "0.00"));

        List<Customer> all = dao.findAll();
        assertEquals(2, all.size());
        assertEquals("6222020000000001", all.get(0).card());
        assertEquals("6222020000000003", all.get(1).card());
        assertEquals(2, dao.count());
    }

    @Test
    void updateChangesProfileFields() {
        dao.insert(customer("6222020000000001", "陈晨", AccountType.NORMAL, "12500.00", "0.00"));

        dao.update(new Customer("6222020000000001", "陈晨（改）", "new-hash", "13700000000",
                AccountType.VIP, new BigDecimal("13000.50"), new BigDecimal("500.00")));

        Customer updated = dao.findByCard("6222020000000001").orElseThrow();
        assertEquals("陈晨（改）", updated.name());
        assertEquals("new-hash", updated.passwordHash());
        assertEquals("13700000000", updated.tele());
        assertEquals(AccountType.VIP, updated.type());
        assertEquals(0, new BigDecimal("13000.50").compareTo(updated.balance()));
        assertEquals(0, new BigDecimal("500.00").compareTo(updated.loan()));
    }

    @Test
    void deleteByCardRemovesRow() {
        dao.insert(customer("6222020000000001", "陈晨", AccountType.NORMAL, "12500.00", "0.00"));

        dao.deleteByCard("6222020000000001");

        assertTrue(dao.findByCard("6222020000000001").isEmpty());
        assertEquals(0, dao.count());
    }

    @Test
    void updateBalanceAndLoanUseBigDecimal() {
        dao.insert(customer("6222020000000001", "陈晨", AccountType.NORMAL, "100.00", "0.00"));

        dao.updateBalance("6222020000000001", new BigDecimal("8888.88"));
        dao.updateLoan("6222020000000001", new BigDecimal("3000.25"));

        Customer updated = dao.findByCard("6222020000000001").orElseThrow();
        assertEquals(0, new BigDecimal("8888.88").compareTo(updated.balance()));
        assertEquals(0, new BigDecimal("3000.25").compareTo(updated.loan()));
    }

    @Test
    void insertDuplicateCardRejected() {
        dao.insert(customer("6222020000000001", "陈晨", AccountType.NORMAL, "1.00", "0.00"));

        assertThrows(DaoException.class, () ->
                dao.insert(customer("6222020000000001", "另一个人", AccountType.NORMAL, "2.00", "0.00")));
        assertEquals(1, dao.count());
    }

    @Test
    void nextCardNumberHasFixedFormatAndIsUnused() {
        String card = dao.nextCardNumber();

        assertEquals(16, card.length());
        assertTrue(card.matches("\\d{16}"), "卡号必须是 16 位数字");
        assertTrue(card.startsWith("622202"), "卡号必须以 622202 开头");
        assertTrue(dao.findByCard(card).isEmpty());

        dao.insert(new Customer(card, "随机客户", "hash", null, AccountType.NORMAL,
                BigDecimal.ZERO, BigDecimal.ZERO));
        String next = dao.nextCardNumber();

        assertEquals(16, next.length());
        assertNotEquals(card, next, "已入库的卡号不应再次生成");
        assertTrue(dao.findByCard(next).isEmpty());
    }

    private static Customer customer(String card, String name, AccountType type, String balance, String loan) {
        return new Customer(card, name, "hash-" + card, "13900000001", type,
                new BigDecimal(balance), new BigDecimal(loan));
    }
}
