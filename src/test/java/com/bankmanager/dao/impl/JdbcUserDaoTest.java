package com.bankmanager.dao.impl;

import com.bankmanager.dao.UserDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.domain.UserAccount;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link JdbcUserDao} 的 H2 集成测试。 */
class JdbcUserDaoTest extends DaoTestSupport {

    private final UserDao dao = new JdbcUserDao(DATABASE);

    @Test
    void insertAndFindByUsername() {
        dao.insert("admin", "$2a$10$hash", "ADMIN");

        UserAccount account = dao.findByUsername("admin").orElseThrow();
        assertEquals("admin", account.username());
        assertEquals("$2a$10$hash", account.passwordHash());
        assertEquals("ADMIN", account.role());
        assertTrue(account.id() > 0);
        assertTrue(dao.exists("admin"));
    }

    @Test
    void missingUsernameReturnsEmptyAndExistsIsFalse() {
        Optional<UserAccount> account = dao.findByUsername("nobody");

        assertTrue(account.isEmpty());
        assertFalse(dao.exists("nobody"));
    }

    @Test
    void duplicateUsernameRejected() {
        dao.insert("teller", "$2a$10$hash", "TELLER");

        assertThrows(DaoException.class, () -> dao.insert("teller", "$2a$10$other", "ADMIN"));
        assertEquals("TELLER", dao.findByUsername("teller").orElseThrow().role());
    }
}
