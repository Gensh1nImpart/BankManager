package com.bankmanager.service;

/** 登录与注册。密码以 BCrypt 存储，任何地方都不得保存明文。 */
public interface AuthService {

    /** 登录成功后返回会话信息；失败抛出 {@link ServiceException}。 */
    Session login(String username, String password);

    /** 注册新账号；用户名重复或密码过短时抛出 {@link ServiceException}。 */
    void register(String username, String password);

    record Session(long id, String username, String role) {
    }
}
