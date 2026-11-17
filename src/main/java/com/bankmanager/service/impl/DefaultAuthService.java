package com.bankmanager.service.impl;

import com.bankmanager.dao.UserDao;
import com.bankmanager.domain.UserAccount;
import com.bankmanager.service.AuthService;
import com.bankmanager.service.ServiceException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 登录与注册服务。
 *
 * <p>安全约定：
 * <ul>
 *     <li>用户名不存在与密码错误返回完全相同的提示，防止账号枚举；</li>
 *     <li>数据库只保存 BCrypt 哈希，任何路径都不会把明文写库或写日志；</li>
 *     <li>注册用户统一为 TELLER 角色，管理员账号只能由数据库脚本创建。</li>
 * </ul>
 */
public final class DefaultAuthService implements AuthService {

    /** 用户名长度下限。 */
    public static final int USERNAME_MIN_LENGTH = 3;
    /** 用户名长度上限。 */
    public static final int USERNAME_MAX_LENGTH = 32;
    /** 密码长度下限。 */
    public static final int PASSWORD_MIN_LENGTH = 6;
    /** 密码长度上限（BCrypt 只取前 72 字节，这里按字符数保守限制）。 */
    public static final int PASSWORD_MAX_LENGTH = 64;

    /** 登录失败的统一提示。 */
    public static final String INVALID_CREDENTIALS = "用户名或密码错误";

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{" + USERNAME_MIN_LENGTH + "," + USERNAME_MAX_LENGTH + "}$");
    private static final String DEFAULT_ROLE = "TELLER";
    private static final int BCRYPT_ROUNDS = 10;

    private final UserDao userDao;

    public DefaultAuthService(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
    }

    @Override
    public Session login(String username, String password) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty() || password == null || password.isEmpty()) {
            // 空输入不查库，但仍返回统一提示
            throw new ServiceException(INVALID_CREDENTIALS);
        }

        UserAccount user;
        try {
            user = userDao.findByUsername(name).orElse(null);
        } catch (RuntimeException e) {
            throw new ServiceException("登录失败：" + describe(e), e);
        }
        if (user == null || !matches(password, user.passwordHash())) {
            throw new ServiceException(INVALID_CREDENTIALS);
        }
        return new Session(user.id(), user.username(), user.role());
    }

    @Override
    public void register(String username, String password) {
        String name = requireUsername(username);
        requirePassword(password);
        try {
            if (userDao.exists(name)) {
                throw new ServiceException("用户名已被注册：" + name);
            }
            userDao.insert(name, BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS)), DEFAULT_ROLE);
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("注册失败：" + describe(e), e);
        }
    }

    /** 校验 BCrypt 哈希；库中的脏数据（非法哈希）按密码错误处理。 */
    private static boolean matches(String rawPassword, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, passwordHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String requireUsername(String username) {
        String name = username == null ? "" : username.trim();
        if (!USERNAME_PATTERN.matcher(name).matches()) {
            throw new ServiceException("用户名须为 " + USERNAME_MIN_LENGTH + "–" + USERNAME_MAX_LENGTH
                    + " 位字母、数字或下划线");
        }
        return name;
    }

    private static void requirePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ServiceException("密码不能为空");
        }
        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new ServiceException("密码至少 " + PASSWORD_MIN_LENGTH + " 位");
        }
        if (password.length() > PASSWORD_MAX_LENGTH) {
            throw new ServiceException("密码不能超过 " + PASSWORD_MAX_LENGTH + " 位");
        }
    }

    private static String describe(Throwable e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
