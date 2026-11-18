package com.bankmanager.service.impl;

import com.bankmanager.dao.QueueDao;
import com.bankmanager.domain.QueueTicket;
import com.bankmanager.domain.TicketStatus;
import com.bankmanager.service.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link DefaultQueueService} 的单元测试，使用手写 QueueDao 桩，不连接数据库。 */
class DefaultQueueServiceTest {

    private StubQueueDao dao;
    private DefaultQueueService service;

    @BeforeEach
    void setUp() {
        dao = new StubQueueDao();
        service = new DefaultQueueService(dao);
    }

    @Test
    @DisplayName("取号时去掉姓名首尾空白并委托数据层")
    void takeDelegatesTrimmedName() {
        QueueTicket ticket = service.take("  张三  ", true);

        assertEquals("张三", dao.lastEnqueueName);
        assertTrue(dao.lastEnqueueVip);
        assertEquals(1, dao.enqueueCalls);
        assertEquals("张三", ticket.customerName());
        assertTrue(ticket.vip());
        assertEquals(TicketStatus.WAITING, ticket.status());
    }

    @Test
    @DisplayName("姓名为 null 或空白时拒绝取号")
    void takeRejectsBlankName() {
        ServiceException nullName = assertThrows(ServiceException.class,
                () -> service.take(null, false));
        assertEquals("客户姓名不能为空", nullName.getMessage());

        ServiceException blank = assertThrows(ServiceException.class,
                () -> service.take("   ", false));
        assertEquals("客户姓名不能为空", blank.getMessage());

        assertEquals(0, dao.enqueueCalls, "校验失败时不应调用数据层");
    }

    @Test
    @DisplayName("姓名长度上限为 32 个字符，超长拒绝、边界通过")
    void takeChecksNameLength() {
        String max = "客".repeat(DefaultQueueService.MAX_NAME_LENGTH);
        assertEquals(max, service.take(max, false).customerName());

        String tooLong = "客".repeat(DefaultQueueService.MAX_NAME_LENGTH + 1);
        ServiceException e = assertThrows(ServiceException.class,
                () -> service.take(tooLong, false));
        assertEquals("客户姓名不能超过 32 个字符", e.getMessage());
        assertEquals(1, dao.enqueueCalls, "超长姓名不应传到数据层");
    }

    @Test
    @DisplayName("叫号直接委托数据层并返回票号")
    void callNextDelegates() {
        dao.queueSize = 2;

        Optional<QueueTicket> ticket = service.callNext(true, 3);

        assertTrue(ticket.isPresent());
        assertTrue(dao.lastCallNextVip);
        assertEquals(3, dao.lastCounterNo);
        assertEquals(1, dao.callNextCalls);
        assertTrue(ticket.get().vip());
    }

    @Test
    @DisplayName("队列为空时叫号返回 empty")
    void callNextOnEmptyQueueReturnsEmpty() {
        assertTrue(service.callNext(false, 1).isEmpty());
        assertEquals(1, dao.callNextCalls);
    }

    @Test
    @DisplayName("窗口号非正数时拒绝叫号")
    void callNextRejectsNonPositiveCounter() {
        ServiceException zero = assertThrows(ServiceException.class,
                () -> service.callNext(false, 0));
        assertEquals("窗口号必须大于 0", zero.getMessage());

        ServiceException negative = assertThrows(ServiceException.class,
                () -> service.callNext(false, -1));
        assertEquals("窗口号必须大于 0", negative.getMessage());

        assertEquals(0, dao.callNextCalls);
    }

    @Test
    @DisplayName("完成业务委托数据层")
    void finishDelegates() {
        service.finish(88L);

        assertEquals(88L, dao.finishedId);
        assertEquals(1, dao.finishCalls);
    }

    @Test
    @DisplayName("票据编号非正数时拒绝")
    void finishRejectsNonPositiveId() {
        ServiceException zero = assertThrows(ServiceException.class, () -> service.finish(0));
        assertEquals("票据编号必须大于 0", zero.getMessage());

        ServiceException negative = assertThrows(ServiceException.class, () -> service.finish(-5));
        assertEquals("票据编号必须大于 0", negative.getMessage());

        assertEquals(0, dao.finishCalls);
    }

    @Test
    @DisplayName("等待列表按 VIP 标记委托并原样返回")
    void waitingDelegates() {
        dao.queueSize = 2;

        List<QueueTicket> vip = service.waiting(true);
        List<QueueTicket> normal = service.waiting(false);

        assertEquals(2, vip.size());
        vip.forEach(ticket -> assertTrue(ticket.vip()));
        assertFalse(normal.isEmpty());
        assertFalse(dao.lastWaitingVip, "最后一次查询为普通队列");
        assertEquals(2, dao.waitingCalls);
    }

    @Test
    @DisplayName("数据层异常转换为面向用户的 ServiceException")
    void daoFailureWrapped() {
        dao.failEnqueue = true;

        ServiceException e = assertThrows(ServiceException.class, () -> service.take("李四", false));

        assertTrue(e.getMessage().startsWith("取号失败："));
        assertTrue(e.getCause() instanceof IllegalStateException);
    }

    /** 手写 QueueDao 桩：记录调用参数，可选地抛出异常。 */
    private static final class StubQueueDao implements QueueDao {
        private int queueSize;
        private int enqueueCalls;
        private int callNextCalls;
        private int finishCalls;
        private int waitingCalls;
        private String lastEnqueueName;
        private boolean lastEnqueueVip;
        private boolean lastCallNextVip;
        private int lastCounterNo;
        private long finishedId;
        private boolean lastWaitingVip;
        private boolean failEnqueue;
        private long nextId = 1;

        @Override
        public QueueTicket enqueue(String customerName, boolean vip) {
            if (failEnqueue) {
                throw new IllegalStateException("连接已关闭");
            }
            enqueueCalls++;
            lastEnqueueName = customerName;
            lastEnqueueVip = vip;
            return new QueueTicket(nextId++, enqueueCalls, customerName, vip,
                    TicketStatus.WAITING, null, LocalDateTime.now(), null);
        }

        @Override
        public List<QueueTicket> waiting(boolean vip) {
            waitingCalls++;
            lastWaitingVip = vip;
            List<QueueTicket> tickets = new ArrayList<>();
            for (int i = 0; i < queueSize; i++) {
                tickets.add(new QueueTicket(nextId++, i + 1, "客户" + i, vip,
                        TicketStatus.WAITING, null, LocalDateTime.now(), null));
            }
            return tickets;
        }

        @Override
        public Optional<QueueTicket> callNext(boolean vip, int counterNo) {
            callNextCalls++;
            lastCallNextVip = vip;
            lastCounterNo = counterNo;
            if (queueSize <= 0) {
                return Optional.empty();
            }
            queueSize--;
            return Optional.of(new QueueTicket(nextId++, 1, "客户", vip,
                    TicketStatus.CALLED, counterNo, LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public void finish(long ticketId) {
            finishCalls++;
            finishedId = ticketId;
        }

        @Override
        public List<QueueTicket> recent(int limit) {
            return List.of();
        }
    }
}
