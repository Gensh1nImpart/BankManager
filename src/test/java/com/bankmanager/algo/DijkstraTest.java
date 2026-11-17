package com.bankmanager.algo;

import com.bankmanager.domain.BankEdge;
import com.bankmanager.domain.BankNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link Dijkstra} 与 {@link Graph} 的单元测试，不依赖数据库。 */
class DijkstraTest {

    private static BankNode node(int id) {
        return new BankNode(id, "网点" + id);
    }

    private static List<BankNode> nodes(int count) {
        List<BankNode> nodes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            nodes.add(node(i));
        }
        return nodes;
    }

    private static BankEdge edge(int from, int to, String distance) {
        return new BankEdge(from, to, new BigDecimal(distance));
    }

    @Test
    @DisplayName("链式图上找到完整路径，距离保留两位小数")
    void chainGraphReturnsWholePath() {
        Graph graph = new Graph(nodes(4), List.of(
                edge(0, 1, "1.5"),
                edge(1, 2, "2.5"),
                edge(2, 3, "3")));

        Optional<Dijkstra.Path> path = Dijkstra.shortest(graph, 0, 3);

        assertTrue(path.isPresent());
        assertEquals(List.of(0, 1, 2, 3), path.get().nodes());
        assertEquals(new BigDecimal("7.00"), path.get().distance());
        assertEquals(2, path.get().distance().scale());
        assertEquals(List.of("网点0", "网点1", "网点2", "网点3"), path.get().namePath(graph));
    }

    @Test
    @DisplayName("三角图选择经由中间点的更短路径，而不是直连边")
    void trianglePrefersShorterDetour() {
        Graph graph = new Graph(nodes(3), List.of(
                edge(0, 1, "2"),
                edge(1, 2, "3"),
                edge(0, 2, "10")));

        Dijkstra.Path path = Dijkstra.shortest(graph, 0, 2).orElseThrow();

        assertEquals(List.of(0, 1, 2), path.nodes());
        assertEquals(new BigDecimal("5.00"), path.distance());
    }

    @Test
    @DisplayName("不可达时返回 empty")
    void unreachableReturnsEmpty() {
        Graph graph = new Graph(nodes(4), List.of(
                edge(0, 1, "1"),
                edge(2, 3, "1")));

        assertTrue(Dijkstra.shortest(graph, 0, 3).isEmpty());
    }

    @Test
    @DisplayName("起点等于终点返回单节点、距离 0.00")
    void sameStartAndEndReturnsSingleNode() {
        Graph graph = new Graph(nodes(3), List.of(edge(0, 1, "1")));

        Dijkstra.Path path = Dijkstra.shortest(graph, 2, 2).orElseThrow();

        assertEquals(List.of(2), path.nodes());
        assertEquals(new BigDecimal("0.00"), path.distance());
    }

    @Test
    @DisplayName("重复边（含反向重复）只保留最小距离")
    void duplicateEdgesKeepMinimum() {
        Graph graph = new Graph(nodes(2), List.of(
                edge(0, 1, "8"),
                edge(0, 1, "3.5"),
                edge(1, 0, "6.5")));

        assertEquals(1, graph.neighbors(0).size());
        assertEquals(new BigDecimal("3.5"), graph.neighbors(0).get(0).distance());
        assertEquals(new BigDecimal("3.50"), Dijkstra.shortest(graph, 0, 1).orElseThrow().distance());
    }

    @Test
    @DisplayName("自环被忽略，不影响邻接表")
    void selfLoopIgnored() {
        Graph graph = new Graph(nodes(2), List.of(
                edge(0, 0, "1"),
                edge(0, 1, "4")));

        assertEquals(1, graph.neighbors(0).size());
        assertEquals(new BigDecimal("4.00"), Dijkstra.shortest(graph, 0, 1).orElseThrow().distance());
    }

    @Test
    @DisplayName("零权与负权边在构建图时被拒绝")
    void nonPositiveDistanceRejected() {
        IllegalArgumentException zero = assertThrows(IllegalArgumentException.class,
                () -> new Graph(nodes(2), List.of(edge(0, 1, "0"))));
        assertTrue(zero.getMessage().contains("大于 0"));

        IllegalArgumentException negative = assertThrows(IllegalArgumentException.class,
                () -> new Graph(nodes(2), List.of(edge(0, 1, "-2"))));
        assertTrue(negative.getMessage().contains("大于 0"));
    }

    @Test
    @DisplayName("引用不存在节点的边被拒绝")
    void edgeWithUnknownNodeRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Graph(nodes(2), List.of(edge(0, 9, "1"))));

        assertTrue(e.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("越界的节点序号被拒绝")
    void outOfRangeNodeRejected() {
        Graph graph = new Graph(nodes(2), List.of());

        assertThrows(IllegalArgumentException.class, () -> Dijkstra.shortest(graph, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.shortest(graph, 0, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> graph.nameOf(2));
    }

    @Test
    @DisplayName("较大随机图与朴素 O(V^2) 实现结果一致")
    void randomGraphMatchesNaiveImplementation() {
        Random random = new Random(20261117L);
        int n = 45;
        List<BankEdge> edges = new ArrayList<>();
        // 先连成一条链保证大多数点可达
        for (int i = 0; i + 1 < n; i++) {
            edges.add(randomEdge(random, i, i + 1));
        }
        for (int i = 0; i < 260; i++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            if (a == b) {
                continue;
            }
            edges.add(randomEdge(random, a, b));
        }
        Graph graph = new Graph(nodes(n), edges);

        int reachable = 0;
        for (int from = 0; from < n; from++) {
            for (int to = 0; to < n; to++) {
                BigDecimal expected = naiveDistance(n, edges, from, to);
                Optional<Dijkstra.Path> actual = Dijkstra.shortest(graph, from, to);
                if (expected == null) {
                    assertTrue(actual.isEmpty(), "应从 " + from + " 不可达 " + to);
                } else {
                    reachable++;
                    assertTrue(actual.isPresent(), "应能从 " + from + " 到达 " + to);
                    assertEquals(0, expected.setScale(2, RoundingMode.HALF_UP)
                                    .compareTo(actual.get().distance()),
                            "距离不一致：" + from + " -> " + to);
                }
            }
        }
        assertEquals(n * n, reachable, "链式结构下全图应两两可达");
    }

    /** 随机生成 0.01 ~ 99.00 的正权边。 */
    private static BankEdge randomEdge(Random random, int from, int to) {
        return new BankEdge(from, to, BigDecimal.valueOf(1 + random.nextInt(9900), 2));
    }

    /** 独立的朴素 Dijkstra 实现（邻接矩阵 + 线性扫描），用于交叉验证。 */
    private static BigDecimal naiveDistance(int n, List<BankEdge> edges, int from, int to) {
        BigDecimal[][] weight = new BigDecimal[n][n];
        for (BankEdge e : edges) {
            if (e.fromNode() == e.toNode()) {
                continue;
            }
            weight[e.fromNode()][e.toNode()] = min(weight[e.fromNode()][e.toNode()], e.distance());
            weight[e.toNode()][e.fromNode()] = min(weight[e.toNode()][e.fromNode()], e.distance());
        }

        BigDecimal[] dist = new BigDecimal[n];
        boolean[] used = new boolean[n];
        dist[from] = BigDecimal.ZERO;

        for (int step = 0; step < n; step++) {
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (!used[i] && dist[i] != null && (u < 0 || dist[i].compareTo(dist[u]) < 0)) {
                    u = i;
                }
            }
            if (u < 0) {
                break;
            }
            used[u] = true;
            if (u == to) {
                break;
            }
            for (int v = 0; v < n; v++) {
                if (weight[u][v] == null || used[v]) {
                    continue;
                }
                BigDecimal candidate = dist[u].add(weight[u][v]);
                dist[v] = min(dist[v], candidate);
            }
        }
        return dist[to];
    }

    private static BigDecimal min(BigDecimal current, BigDecimal candidate) {
        if (current == null || candidate.compareTo(current) < 0) {
            return candidate;
        }
        return current;
    }

    @Test
    @DisplayName("不可达的两个连通分量各自内部仍可计算")
    void separateComponentsWorkIndependently() {
        Graph graph = new Graph(nodes(5), List.of(
                edge(0, 1, "1"),
                edge(1, 2, "1"),
                edge(3, 4, "2")));

        assertFalse(Dijkstra.shortest(graph, 0, 2).isEmpty());
        assertEquals(new BigDecimal("2.00"), Dijkstra.shortest(graph, 0, 2).orElseThrow().distance());
        assertEquals(new BigDecimal("2.00"), Dijkstra.shortest(graph, 3, 4).orElseThrow().distance());
        assertTrue(Dijkstra.shortest(graph, 2, 3).isEmpty());
    }
}
