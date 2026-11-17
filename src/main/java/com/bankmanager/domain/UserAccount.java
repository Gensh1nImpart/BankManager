package com.bankmanager.domain;

/** 系统登录账号。 */
public record UserAccount(long id, String username, String passwordHash, String role) {
}
