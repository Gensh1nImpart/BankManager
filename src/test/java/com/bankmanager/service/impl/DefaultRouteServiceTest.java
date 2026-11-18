package com.bankmanager.service.impl;

import com.bankmanager.dao.GraphDao;
import com.bankmanager.domain.BankEdge;
import com.bankmanager.domain.BankNode;
import com.bankmanager.service.RouteService;
import com.bankmanager.service.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link DefaultRouteService} 的单元测试，使用手写 GraphDao 桩，不连接数据库。 */
class DefaultRouteServiceTest {

    private StubGraphDao dao;
    private DefaultRouteService service;

    @BeforeEach
    void setUp() {
        dao = new StubGraphDao();
        dao.insertNode(1, "大门");
        dao.insertNode(2, "一号网点");
        dao.insertNode(3, "二号网点");
        dao.insertNode(4, "三号网点");
        dao.insertEdge(1, 2, new BigDecimal("100"));
        dao.insertEdge(2, 3, new BigDecimal("50"));
        dao.insertEdge(3, 4, new BigDecimal("40"));
        dao.insertEdge(1, 3, new BigDecimal("300"));
        service = new DefaultRouteService(dao);
    }

    @Test
    @DisplayName("返回中文路径文本与两位小数距离")
    void shortestPathReturnsChinesePathText() {
        RouteService.Route route = service.shortestPath("大门", "三号网点");

        assertEquals(List.of("大门", "一号网点", "二号网点", "三号网点"), route.path());
        assertEquals("大门 → 一号网点 → 二号网点 → 三号网点", route.pathText());
        assertEquals(new BigDecimal("190.00"), route.distance());
    }

    @Test
    @DisplayName("名称两端的空白会被忽略")
    void trimsWhitespaceInNames() {
        RouteService.Route route = service.shortestPath("  大门 ", " 二号网点\t");

        assertEquals("大门 → 一号网点 → 二号网点", route.pathText());
    }

    @Test
    @DisplayName("起点网点不存在时给出中文提示")
    void unknownStartNodeGivesChineseMessage() {
        ServiceException e = assertThrows(ServiceException.class,
                () -> service.shortestPath("不存在的网点", "大门"));

        assertEquals("网点不存在：不存在的网点", e.getMessage());
    }

    @Test
    @DisplayName("终点网点不存在时给出中文提示")
    void unknownEndNodeGivesChineseMessage() {
        ServiceException e = assertThrows(ServiceException.class,
                () -> service.shortestPath("大门", "火星分行"));

        assertEquals("网点不存在：火星分行", e.getMessage());
    }

    @Test
    @DisplayName("网点存在但不可达时给出中文提示")
    void unreachableNodesGiveChineseMessage() {
        dao.insertNode(5, "孤岛网点");

        ServiceException e = assertThrows(ServiceException.class,
                () -> service.shortestPath("大门", "孤岛网点"));

        assertEquals("无法从 大门 到达 孤岛网点", e.getMessage());
    }

    @Test
    @DisplayName("空名称被拒绝")
    void blankNamesRejected() {
        ServiceException start = assertThrows(ServiceException.class,
                () -> service.shortestPath("  ", "大门"));
        assertEquals("起点网点名称不能为空", start.getMessage());

        ServiceException end = assertThrows(ServiceException.class,
                () -> service.shortestPath("大门", null));
        assertEquals("终点网点名称不能为空", end.getMessage());
    }

    @Test
    @DisplayName("每次调用都重新加载图，新增网点后立即生效")
    void reloadsGraphOnEveryCall() {
        assertThrows(ServiceException.class, () -> service.shortestPath("大门", "新网点"));

        dao.insertNode(6, "新网点");
        dao.insertEdge(1, 6, new BigDecimal("42"));

        RouteService.Route route = service.shortestPath("大门", "新网点");
        assertEquals("大门 → 新网点", route.pathText());
        assertEquals(new BigDecimal("42.00"), route.distance());
    }

    @Test
    @DisplayName("nodeNames 按数据层顺序返回全部网点")
    void nodeNamesListsAllNodes() {
        assertEquals(List.of("大门", "一号网点", "二号网点", "三号网点"), service.nodeNames());
    }

    @Test
    @DisplayName("空图时 nodeNames 为空，查询任意网点提示不存在")
    void emptyGraphHandled() {
        DefaultRouteService empty = new DefaultRouteService(new StubGraphDao());

        assertTrue(empty.nodeNames().isEmpty());
        ServiceException e = assertThrows(ServiceException.class,
                () -> empty.shortestPath("大门", "一号网点"));
        assertEquals("网点不存在：大门", e.getMessage());
    }

    @Test
    @DisplayName("起点与终点相同时返回单节点零距离")
    void sameNodeReturnsZeroDistance() {
        RouteService.Route route = service.shortestPath("大门", "大门");

        assertEquals(List.of("大门"), route.path());
        assertEquals("大门", route.pathText());
        assertEquals(new BigDecimal("0.00"), route.distance());
    }

    /** 手写 GraphDao 桩：内存中的节点与边。 */
    private static final class StubGraphDao implements GraphDao {
        private final List<BankNode> nodes = new ArrayList<>();
        private final List<BankEdge> edges = new ArrayList<>();

        @Override
        public List<BankNode> nodes() {
            return List.copyOf(nodes);
        }

        @Override
        public List<BankEdge> edges() {
            return List.copyOf(edges);
        }

        @Override
        public void insertNode(int id, String name) {
            nodes.add(new BankNode(id, name));
        }

        @Override
        public void insertEdge(int fromNode, int toNode, BigDecimal distance) {
            edges.add(new BankEdge(fromNode, toNode, distance));
        }
    }
}
