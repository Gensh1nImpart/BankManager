package com.bankmanager.service.impl;

import com.bankmanager.dao.CustomerDao;
import com.bankmanager.domain.Customer;
import com.bankmanager.db.DaoException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 手写内存版 {@link CustomerDao}，用于服务层单元测试。 */
final class FakeCustomerDao implements CustomerDao {

    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private int cardSequence;

    /** 直接写入测试数据。 */
    void put(Customer customer) {
        customers.put(customer.card(), customer);
    }

    /** 当前库中的客户（按卡号顺序）。 */
    Customer get(String card) {
        return customers.get(card);
    }

    int size() {
        return customers.size();
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    @Override
    public Optional<Customer> findByCard(String card) {
        return Optional.ofNullable(customers.get(card));
    }

    @Override
    public void insert(Customer customer) {
        if (customers.containsKey(customer.card())) {
            throw new DaoException("新增客户失败：主键冲突 " + customer.card());
        }
        customers.put(customer.card(), customer);
    }

    @Override
    public void update(Customer customer) {
        customers.put(customer.card(), customer);
    }

    @Override
    public void deleteByCard(String card) {
        customers.remove(card);
    }

    @Override
    public void updateBalance(String card, BigDecimal balance) {
        Customer customer = require(card);
        customers.put(card, new Customer(customer.card(), customer.name(), customer.passwordHash(),
                customer.tele(), customer.type(), balance, customer.loan()));
    }

    @Override
    public void updateLoan(String card, BigDecimal loan) {
        Customer customer = require(card);
        customers.put(card, new Customer(customer.card(), customer.name(), customer.passwordHash(),
                customer.tele(), customer.type(), customer.balance(), loan));
    }

    @Override
    public long count() {
        return customers.size();
    }

    @Override
    public String nextCardNumber() {
        String card;
        do {
            card = "622202" + String.format("%010d", ++cardSequence);
        } while (customers.containsKey(card));
        return card;
    }

    private Customer require(String card) {
        Customer customer = customers.get(card);
        if (customer == null) {
            throw new DaoException("客户不存在：" + card);
        }
        return customer;
    }
}
