package com.bankmanager.dao.impl;

import com.bankmanager.dao.LogDao;
import com.bankmanager.domain.OperationLog;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link JdbcLogDao} 的 H2 集成测试。 */
class JdbcLogDaoTest extends DaoTestSupport {

    private final LogDao dao = new JdbcLogDao(DATABASE);

    @Test
    void appendStoresAllFields() {
        dao.append("admin", "开户", "为客户 陈晨 开户");

        List<OperationLog> logs = dao.recent(10);
        assertEquals(1, logs.size());
        OperationLog log = logs.get(0);
        assertEquals("admin", log.username());
        assertEquals("开户", log.action());
        assertEquals("为客户 陈晨 开户", log.detail());
        assertNotNull(log.createdAt());
        assertTrue(log.id() > 0);
    }

    @Test
    void recentOrdersByCreatedAtThenIdDescending() {
        dao.append("admin", "第一笔", null);
        dao.append("admin", "第二笔", null);
        dao.append("admin", "第三笔", null);
        // 直接插入一条更早的日志，验证 created_at 优先于 id
        DATABASE.inTransaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO operation_log (created_at, username, action, detail) VALUES (?, ?, ?, ?)")) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
                statement.setString(2, "system");
                statement.setString(3, "旧日志");
                statement.setString(4, "历史数据");
                statement.executeUpdate();
            }
            return null;
        });

        List<OperationLog> logs = dao.recent(10);

        assertEquals(4, logs.size());
        assertEquals("第三笔", logs.get(0).action());
        assertEquals("第二笔", logs.get(1).action());
        assertEquals("第一笔", logs.get(2).action());
        assertEquals("旧日志", logs.get(3).action());
    }

    @Test
    void recentHonoursLimit() {
        for (int i = 0; i < 5; i++) {
            dao.append("admin", "操作" + i, null);
        }

        List<OperationLog> logs = dao.recent(2);

        assertEquals(2, logs.size());
        assertEquals("操作4", logs.get(0).action());
        assertEquals("操作3", logs.get(1).action());
        assertTrue(dao.recent(0).isEmpty());
    }
}
