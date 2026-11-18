package com.bankmanager.service;

import com.bankmanager.domain.Employee;

import java.util.List;

/** 职员管理。校验规则集中在这一层，界面只负责收集输入。 */
public interface EmployeeService {

    List<Employee> employees();

    Employee employee(String id);

    /** 新增职员，编号为空时自动生成；返回最终使用的编号。 */
    String create(Employee employee);

    void update(Employee employee);

    void delete(String id);
}
