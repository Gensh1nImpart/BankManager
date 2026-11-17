package com.bankmanager.service;

import com.bankmanager.domain.Customer;

import java.math.BigDecimal;
import java.util.List;

/** 账户与资金业务。所有金额校验与日志记录都在这一层完成。 */
public interface AccountService {

    List<Customer> customers();

    Customer customer(String card);

    /** 开户，plainPassword 会在服务层做 BCrypt 哈希。 */
    void createCustomer(Customer customer, String plainPassword);

    void updateCustomer(Customer customer);

    void deleteCustomer(String card);

    void deposit(String card, BigDecimal amount);

    void withdraw(String card, BigDecimal amount);

    void takeLoan(String card, BigDecimal amount);

    void repayLoan(String card, BigDecimal amount);

    void transfer(String fromCard, String toCard, BigDecimal amount);

    /** 简单利息：本金 * 月利率 * 月数。 */
    BigDecimal interest(String card, BigDecimal monthlyRate, int months);
}
