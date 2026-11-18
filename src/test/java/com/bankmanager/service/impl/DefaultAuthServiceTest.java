package com.bankmanager.service.impl;

import com.bankmanager.service.AuthService;
import com.bankmanager.service.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 登录/注册服务单元测试：全部使用手写内存 DAO，不依赖数据库。 */
class DefaultAuthServiceTest {

    private FakeUserDao userDao;
    private DefaultAuthService service;

    @BeforeEach
    void setUp() {
        userDao = new FakeUserDao();
        service = new DefaultAuthService(userDao);
        userDao.put("admin", BCrypt.hashpw("admin123", BCrypt.gensalt()), "ADMIN");
    }

    @Test
    @DisplayName("正确的用户名和密码可以登录并返回角色")
    void loginSucceedsWithCorrectPassword() {
        AuthService.Session session = service.login("admin", "admin123");

        assertEquals("admin", session.username());
        assertEquals("ADMIN", session.role());
        assertTrue(session.id() > 0);
    }

    @Test
    @DisplayName("密码错误时返回与用户不存在完全相同的提示")
    void loginRejectsWrongPasswordWithGenericMessage() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.login("admin", "wrong-password"));

        assertEquals(DefaultAuthService.INVALID_CREDENTIALS, error.getMessage());
    }

    @Test
    @DisplayName("用户不存在时不暴露账号是否存在")
    void loginRejectsUnknownUserWithSameMessage() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.login("nobody", "admin123"));

        assertEquals(DefaultAuthService.INVALID_CREDENTIALS, error.getMessage());
    }

    @Test
    @DisplayName("空用户名或空密码直接拒绝且不查库")
    void loginRejectsBlankInput() {
        assertThrows(ServiceException.class, () -> service.login("  ", "admin123"));
        assertThrows(ServiceException.class, () -> service.login("admin", ""));
        assertThrows(ServiceException.class, () -> service.login(null, null));
    }

    @Test
    @DisplayName("库中哈希损坏时按密码错误处理")
    void loginRejectsCorruptedHash() {
        userDao.put("broken", "not-a-bcrypt-hash", "TELLER");

        assertThrows(ServiceException.class, () -> service.login("broken", "whatever"));
    }

    @Test
    @DisplayName("注册只保存 BCrypt 哈希，不保存明文")
    void registerStoresBcryptHashNotPlaintext() {
        service.register("teller01", "secret123");

        String hash = userDao.hashOf("teller01");
        assertNotEquals("secret123", hash);
        assertTrue(hash.startsWith("$2"), "应为 BCrypt 哈希：" + hash);
        assertTrue(BCrypt.checkpw("secret123", hash));
        assertFalse(hash.contains("secret123"));
    }

    @Test
    @DisplayName("注册用户可以立即用明文密码登录")
    void registeredUserCanLogin() {
        service.register("teller02", "secret123");

        assertEquals("teller02", service.login("teller02", "secret123").username());
    }

    @Test
    @DisplayName("注册的账号默认为柜员角色，用户名首尾空格会被去掉")
    void registerDefaultsToTellerRole() {
        service.register("  teller03  ", "secret123");

        assertEquals("TELLER", userDao.roleOf("teller03"));
        assertTrue(userDao.exists("teller03"));
    }

    @Test
    @DisplayName("用户名重复时拒绝注册")
    void registerRejectsDuplicateUsername() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.register("admin", "another123"));

        assertTrue(error.getMessage().contains("已被注册"), error.getMessage());
    }

    @Test
    @DisplayName("密码过短时拒绝注册")
    void registerRejectsShortPassword() {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.register("teller04", "12345"));

        assertTrue(error.getMessage().contains("至少"), error.getMessage());
        assertFalse(userDao.exists("teller04"));
    }

    @Test
    @DisplayName("用户名含中文、空格或长度不足时拒绝注册")
    void registerRejectsInvalidUsername() {
        assertThrows(ServiceException.class, () -> service.register("ab", "secret123"));
        assertThrows(ServiceException.class, () -> service.register("张三三", "secret123"));
        assertThrows(ServiceException.class, () -> service.register("user name", "secret123"));
        assertThrows(ServiceException.class, () -> service.register("a".repeat(33), "secret123"));
        assertFalse(userDao.exists("ab"));
    }

    @Test
    @DisplayName("注册密码可以为 6 位或 32 位边界值")
    void registerAcceptsBoundaryPasswordLengths() {
        service.register("teller05", "123456");
        service.register("teller06", "x".repeat(32));

        assertTrue(userDao.exists("teller05"));
        assertTrue(userDao.exists("teller06"));
    }
}
