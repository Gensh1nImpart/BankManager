package com.bankmanager.service.impl;

import com.bankmanager.algo.Dijkstra;
import com.bankmanager.algo.Graph;
import com.bankmanager.dao.GraphDao;
import com.bankmanager.domain.BankEdge;
import com.bankmanager.domain.BankNode;
import com.bankmanager.service.RouteService;
import com.bankmanager.service.ServiceException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 网点导航服务：每次调用都从 {@link GraphDao} 重新加载图，
 * 保证新增/删除网点或道路后立即生效。
 */
public final class DefaultRouteService implements RouteService {

    private final GraphDao graphDao;

    public DefaultRouteService(GraphDao graphDao) {
        this.graphDao = Objects.requireNonNull(graphDao, "graphDao");
    }

    @Override
    public Route shortestPath(String fromName, String toName) {
        String from = requireName(fromName, "起点");
        String to = requireName(toName, "终点");

        Graph graph = loadGraph();

        int fromIndex = graph.indexOf(from);
        if (fromIndex < 0) {
            throw new ServiceException("网点不存在：" + from);
        }
        int toIndex = graph.indexOf(to);
        if (toIndex < 0) {
            throw new ServiceException("网点不存在：" + to);
        }

        Optional<Dijkstra.Path> path = Dijkstra.shortest(graph, fromIndex, toIndex);
        if (path.isEmpty()) {
            throw new ServiceException("无法从 " + from + " 到达 " + to);
        }

        Dijkstra.Path result = path.get();
        return new Route(result.namePath(graph), result.distance());
    }

    @Override
    public List<String> nodeNames() {
        return loadGraph().names();
    }

    /** 从数据层加载并构建图；数据异常统一转换为面向用户的提示。 */
    private Graph loadGraph() {
        List<BankNode> nodes;
        List<BankEdge> edges;
        try {
            nodes = graphDao.nodes();
            edges = graphDao.edges();
        } catch (RuntimeException e) {
            throw new ServiceException("读取网点数据失败：" + describe(e), e);
        }
        try {
            return new Graph(nodes, edges);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("网点图数据有误：" + describe(e), e);
        }
    }

    /** 校验并规范化网点名称。 */
    private static String requireName(String name, String role) {
        if (name == null || name.trim().isEmpty()) {
            throw new ServiceException(role + "网点名称不能为空");
        }
        return name.trim();
    }

    private static String describe(Throwable e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
