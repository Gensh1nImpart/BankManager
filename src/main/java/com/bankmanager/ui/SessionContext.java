package com.bankmanager.ui;

import com.bankmanager.service.AuthService;

/**
 * 当前登录会话（仅保存在内存中）。用于给操作日志带上操作员用户名，
 * 登出时清空，不做任何持久化。
 */
public final class SessionContext {

    private static volatile AuthService.Session current;

    private SessionContext() {
    }

    public static void set(AuthService.Session session) {
        current = session;
    }

    public static AuthService.Session get() {
        return current;
    }

    public static void clear() {
        current = null;
    }

    /** 未登录时统一记为「柜员」，保证日志字段非空。 */
    public static String username() {
        AuthService.Session session = current;
        return session == null ? "柜员" : session.username();
    }
}
