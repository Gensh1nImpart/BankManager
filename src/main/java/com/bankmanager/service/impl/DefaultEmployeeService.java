package com.bankmanager.service.impl;

import com.bankmanager.dao.EmployeeDao;
import com.bankmanager.domain.Employee;
import com.bankmanager.service.EmployeeService;
import com.bankmanager.service.ServiceException;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 职员管理实现：做字段校验与编号生成，数据访问全部委托 {@link EmployeeDao}。 */
public final class DefaultEmployeeService implements EmployeeService {

    /** 允许的性别取值。 */
    public static final Set<String> SEXES = Set.of("男", "女");
    /** 年龄范围，与数据库 CHECK 约束一致。 */
    public static final int MIN_AGE = 16;
    public static final int MAX_AGE = 100;
    private static final int NAME_MAX_LENGTH = 50;
    private static final int TELE_MAX_LENGTH = 20;
    private static final int ADDRESS_MAX_LENGTH = 100;
    private static final int POSITION_MAX_LENGTH = 30;
    private static final int ID_MAX_LENGTH = 20;

    private final EmployeeDao employeeDao;

    public DefaultEmployeeService(EmployeeDao employeeDao) {
        this.employeeDao = Objects.requireNonNull(employeeDao, "employeeDao");
    }

    @Override
    public List<Employee> employees() {
        try {
            return employeeDao.findAll();
        } catch (RuntimeException e) {
            throw new ServiceException("查询职员列表失败：" + describe(e), e);
        }
    }

    @Override
    public Employee employee(String id) {
        String validId = requireId(id);
        try {
            return employeeDao.findById(validId)
                    .orElseThrow(() -> new ServiceException("职员不存在：" + validId));
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("查询职员失败：" + describe(e), e);
        }
    }

    @Override
    public String create(Employee employee) {
        Objects.requireNonNull(employee, "employee");
        Employee valid = validate(employee);
        try {
            String id = valid.id() == null || valid.id().isBlank()
                    ? employeeDao.nextId()
                    : requireId(valid.id());
            if (employeeDao.findById(id).isPresent()) {
                throw new ServiceException("职员编号已存在：" + id);
            }
            employeeDao.insert(new Employee(id, valid.name(), valid.sex(), valid.age(),
                    valid.tele(), valid.address(), valid.position()));
            return id;
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("新增职员失败：" + describe(e), e);
        }
    }

    @Override
    public void update(Employee employee) {
        Objects.requireNonNull(employee, "employee");
        Employee valid = validate(employee);
        String id = requireId(valid.id());
        try {
            if (employeeDao.findById(id).isEmpty()) {
                throw new ServiceException("职员不存在：" + id);
            }
            employeeDao.update(new Employee(id, valid.name(), valid.sex(), valid.age(),
                    valid.tele(), valid.address(), valid.position()));
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("修改职员失败：" + describe(e), e);
        }
    }

    @Override
    public void delete(String id) {
        String validId = requireId(id);
        try {
            if (employeeDao.findById(validId).isEmpty()) {
                throw new ServiceException("职员不存在：" + validId);
            }
            employeeDao.deleteById(validId);
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("删除职员失败：" + describe(e), e);
        }
    }

    /** 统一做去空白与字段校验，返回规范化后的对象。 */
    private static Employee validate(Employee employee) {
        String name = trim(employee.name());
        if (name.isEmpty()) {
            throw new ServiceException("职员姓名不能为空");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new ServiceException("职员姓名不能超过 " + NAME_MAX_LENGTH + " 个字符");
        }
        String sex = trim(employee.sex());
        if (!sex.isEmpty() && !SEXES.contains(sex)) {
            throw new ServiceException("性别只能是男或女");
        }
        Integer age = employee.age();
        if (age != null && (age < MIN_AGE || age > MAX_AGE)) {
            throw new ServiceException("年龄必须在 " + MIN_AGE + " 到 " + MAX_AGE + " 之间");
        }
        String tele = trim(employee.tele());
        if (tele.length() > TELE_MAX_LENGTH) {
            throw new ServiceException("联系电话不能超过 " + TELE_MAX_LENGTH + " 个字符");
        }
        String address = trim(employee.address());
        if (address.length() > ADDRESS_MAX_LENGTH) {
            throw new ServiceException("地址不能超过 " + ADDRESS_MAX_LENGTH + " 个字符");
        }
        String position = trim(employee.position());
        if (position.length() > POSITION_MAX_LENGTH) {
            throw new ServiceException("职位不能超过 " + POSITION_MAX_LENGTH + " 个字符");
        }
        return new Employee(trim(employee.id()), name, sex, age, tele, address, position);
    }

    private static String requireId(String id) {
        String value = trim(id);
        if (value.isEmpty()) {
            throw new ServiceException("职员编号不能为空");
        }
        if (value.length() > ID_MAX_LENGTH) {
            throw new ServiceException("职员编号不能超过 " + ID_MAX_LENGTH + " 个字符");
        }
        return value;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String describe(Throwable e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
