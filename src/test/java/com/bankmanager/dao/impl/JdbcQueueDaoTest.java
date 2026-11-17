package com.bankmanager.dao.impl;

import com.bankmanager.dao.QueueDao;
import com.bankmanager.domain.QueueTicket;
import com.bankmanager.domain.TicketStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link JdbcQueueDao} 的 H2 集成测试。 */
class JdbcQueueDaoTest extends DaoTestSupport {

    private final QueueDao dao = new JdbcQueueDao(DATABASE);

    @Test
    void enqueueNumbersIncreasePerQueueAndVipIsIsolated() {
        QueueTicket normal1 = dao.enqueue("普通甲", false);
        QueueTicket normal2 = dao.enqueue("普通乙", false);
        QueueTicket vip1 = dao.enqueue("VIP甲", true);
        QueueTicket vip2 = dao.enqueue("VIP乙", true);

        assertEquals(1, normal1.number());
        assertEquals(2, normal2.number());
        assertEquals(1, vip1.number());
        assertEquals(2, vip2.number());
        assertEquals(TicketStatus.WAITING, normal1.status());
        assertNotNull(normal1.createdAt());
        assertNull(normal1.calledAt());
        assertNull(normal1.counterNo());
        assertTrue(vip1.vip());

        List<QueueTicket> normalQueue = dao.waiting(false);
        assertEquals(2, normalQueue.size());
        assertEquals("普通甲", normalQueue.get(0).customerName());
        assertEquals(2, dao.waiting(true).size());
    }

    @Test
    void callNextTakesOldestWaitingTicket() {
        QueueTicket first = dao.enqueue("普通甲", false);
        QueueTicket second = dao.enqueue("普通乙", false);

        Optional<QueueTicket> called = dao.callNext(false, 3);

        assertTrue(called.isPresent());
        assertEquals(first.id(), called.get().id());
        assertEquals(TicketStatus.CALLED, called.get().status());
        assertEquals(3, called.get().counterNo());
        assertNotNull(called.get().calledAt());

        Optional<QueueTicket> next = dao.callNext(false, 4);
        assertTrue(next.isPresent());
        assertEquals(second.id(), next.get().id());
        assertEquals(4, next.get().counterNo());

        assertTrue(dao.callNext(false, 5).isEmpty());
        assertTrue(dao.waiting(false).isEmpty());
    }

    @Test
    void callNextOnlyServesMatchingQueue() {
        dao.enqueue("普通甲", false);
        QueueTicket vip = dao.enqueue("VIP甲", true);

        assertTrue(dao.callNext(false, 1).isPresent());

        Optional<QueueTicket> calledVip = dao.callNext(true, 2);
        assertTrue(calledVip.isPresent());
        assertEquals(vip.id(), calledVip.get().id());
        assertEquals(2, calledVip.get().counterNo());
    }

    @Test
    void callNextOnEmptyQueueReturnsEmpty() {
        assertTrue(dao.callNext(true, 1).isEmpty());
    }

    @Test
    void finishMarksTicketDone() {
        QueueTicket ticket = dao.enqueue("普通甲", false);
        dao.callNext(false, 1);

        dao.finish(ticket.id());

        QueueTicket stored = dao.recent(1).get(0);
        assertEquals(ticket.id(), stored.id());
        assertEquals(TicketStatus.DONE, stored.status());
        assertTrue(dao.waiting(false).isEmpty());
    }

    @Test
    void recentReturnsNewestFirstWithLimit() {
        dao.enqueue("普通甲", false);
        QueueTicket second = dao.enqueue("普通乙", false);
        QueueTicket third = dao.enqueue("普通丙", false);

        List<QueueTicket> recent = dao.recent(2);

        assertEquals(2, recent.size());
        assertEquals(third.id(), recent.get(0).id());
        assertEquals(second.id(), recent.get(1).id());
        assertEquals(3, dao.recent(10).size());
        assertTrue(dao.recent(0).isEmpty());
    }

    @Test
    void concurrentCallNextNeverCallsSameTicketTwice() throws Exception {
        int ticketCount = 8;
        int workers = 4;
        for (int i = 0; i < ticketCount; i++) {
            dao.enqueue("并发客户" + i, false);
        }

        ExecutorService pool = Executors.newFixedThreadPool(workers);
        CountDownLatch start = new CountDownLatch(1);
        ConcurrentLinkedQueue<QueueTicket> called = new ConcurrentLinkedQueue<>();
        try {
            List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < workers; i++) {
                int counterNo = i + 1;
                futures.add(pool.submit(() -> {
                    start.await();
                    dao.callNext(false, counterNo).ifPresent(called::add);
                    return null;
                }));
            }
            start.countDown();
            for (java.util.concurrent.Future<?> future : futures) {
                future.get(20, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(workers, called.size(), "每个并发线程都应叫到一张票");
        Set<Long> ids = new HashSet<>();
        for (QueueTicket ticket : called) {
            assertEquals(TicketStatus.CALLED, ticket.status());
            assertNotNull(ticket.counterNo());
            ids.add(ticket.id());
        }
        assertEquals(workers, ids.size(), "同一张票不能被叫到两次");
        assertEquals(ticketCount - workers, dao.waiting(false).size());
    }
}
