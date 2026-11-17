package com.bankmanager.dao;

import com.bankmanager.domain.Customer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** 客户账户访问接口。所有实现必须使用 PreparedStatement，禁止拼接 SQL。 */
public interface CustomerDao {

    List<Customer> findAll();

    Optional<Customer> findByCard(String card);

    void insert(Customer customer);

    void update(Customer customer);

    void deleteByCard(String card);

    void updateBalance(String card, BigDecimal balance);

    void updateLoan(String card, BigDecimal loan);

    long count();

    /** 生成一个库中不存在的 19 位卡号。 */
    String nextCardNumber();
}
