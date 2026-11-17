package com.bankmanager.dao;

import com.bankmanager.domain.Employee;

import java.util.List;
import java.util.Optional;

/** 职员访问接口。 */
public interface EmployeeDao {

    List<Employee> findAll();

    Optional<Employee> findById(String id);

    void insert(Employee employee);

    void update(Employee employee);

    void deleteById(String id);

    /** 生成下一个未被占用的职员编号（如 001）。 */
    String nextId();
}
