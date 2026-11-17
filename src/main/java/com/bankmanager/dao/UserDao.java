package com.bankmanager.dao;

import com.bankmanager.domain.UserAccount;

import java.util.Optional;

/** 登录账号访问接口。 */
public interface UserDao {

    Optional<UserAccount> findByUsername(String username);

    void insert(String username, String passwordHash, String role);

    boolean exists(String username);
}
