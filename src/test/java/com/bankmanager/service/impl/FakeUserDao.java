package com.bankmanager.service.impl;

import com.bankmanager.dao.UserDao;
import com.bankmanager.domain.UserAccount;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** 手写内存版 {@link UserDao}，用于服务层单元测试。 */
final class FakeUserDao implements UserDao {

    private final Map<String, UserAccount> users = new LinkedHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public void insert(String username, String passwordHash, String role) {
        long id = ids.getAndIncrement();
        users.put(username, new UserAccount(id, username, passwordHash, role));
    }

    @Override
    public boolean exists(String username) {
        return users.containsKey(username);
    }

    /** 直接写入一条账号，便于构造测试前置数据。 */
    void put(String username, String passwordHash, String role) {
        insert(username, passwordHash, role);
    }

    String hashOf(String username) {
        return users.get(username).passwordHash();
    }

    String roleOf(String username) {
        return users.get(username).role();
    }
}
