package com.bankmanager.dao.impl;

import com.bankmanager.dao.EmployeeDao;
import com.bankmanager.domain.Employee;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link JdbcEmployeeDao} 的 H2 集成测试。 */
class JdbcEmployeeDaoTest extends DaoTestSupport {

    private final EmployeeDao dao = new JdbcEmployeeDao(DATABASE);

    @Test
    void insertFindUpdateAndDelete() {
        dao.insert(new Employee("001", "张伟", "男", 32, "13800000001", "济南市历下区", "柜员"));

        Employee inserted = dao.findById("001").orElseThrow();
        assertEquals("张伟", inserted.name());
        assertEquals("男", inserted.sex());
        assertEquals(32, inserted.age());
        assertEquals("柜员", inserted.position());

        dao.update(new Employee("001", "张伟", "男", 35, "13800000009", "济南市市中区", "大堂经理"));

        Employee updated = dao.findById("001").orElseThrow();
        assertEquals(35, updated.age());
        assertEquals("13800000009", updated.tele());
        assertEquals("济南市市中区", updated.address());
        assertEquals("大堂经理", updated.position());

        dao.deleteById("001");
        assertTrue(dao.findById("001").isEmpty());
        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void nullAgeIsStoredAsNull() {
        dao.insert(new Employee("001", "李娜", "女", null, null, null, null));

        Employee found = dao.findById("001").orElseThrow();
        assertNull(found.age());
        assertNull(found.tele());
    }

    @Test
    void findAllSortedById() {
        dao.insert(new Employee("002", "李娜", "女", 28, null, null, "柜员"));
        dao.insert(new Employee("001", "张伟", "男", 32, null, null, "柜员"));

        List<Employee> all = dao.findAll();
        assertEquals(2, all.size());
        assertEquals("001", all.get(0).id());
        assertEquals("002", all.get(1).id());
    }

    @Test
    void nextIdStartsAt001AndIncrements() {
        assertEquals("001", dao.nextId());

        dao.insert(new Employee("001", "张伟", "男", 32, null, null, "柜员"));
        assertEquals("002", dao.nextId());

        dao.insert(new Employee("002", "李娜", "女", 28, null, null, "柜员"));
        assertEquals("003", dao.nextId());

        // 非数字编号是历史脏数据，不参与递增
        dao.insert(new Employee("临时工", "王强", "男", 41, null, null, "柜员"));
        assertEquals("003", dao.nextId());
        assertFalse(dao.findAll().isEmpty());
    }

    @Test
    void nextIdPadsToThreeDigitsAndGrowsBeyond999() {
        dao.insert(new Employee("009", "张伟", "男", 32, null, null, "柜员"));
        assertEquals("010", dao.nextId());

        dao.insert(new Employee("999", "李娜", "女", 28, null, null, "柜员"));
        assertEquals("1000", dao.nextId());
    }
}
