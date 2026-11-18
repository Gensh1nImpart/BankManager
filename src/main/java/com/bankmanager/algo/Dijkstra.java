package com.bankmanager.algo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * 单源最短路径：Dijkstra + 优先队列（懒惰删除）。
 *
 * <p>复杂度：O((V + E) log V)，空间 O(V + E)。
 * 与旧实现 {@code Dijsrta} 每轮线性扫描找最小点（O(V²)）相比，
 * 在稀疏的网点图上明显更快，且使用 {@link BigDecimal} 避免浮点误差。
 */
public final class Dijkstra {

    /** 距离统一保留两位小数。 */
    private static final int SCALE = 2;

    /**
     * 最短路结果。
     *
     * @param nodes    节点序号序列，包含起点与终点
     * @param distance 总距离，保留两位小数
     */
    public record Path(List<Integer> nodes, BigDecimal distance) {
        public Path {
            nodes = List.copyOf(nodes);
            Objects.requireNonNull(distance, "distance");
        }

        /** 把节点序号序列翻译成网点名称序列。 */
        public List<String> namePath(Graph graph) {
            Objects.requireNonNull(graph, "graph");
            List<String> result = new ArrayList<>(nodes.size());
            for (int node : nodes) {
                result.add(graph.nameOf(node));
            }
            return List.copyOf(result);
        }
    }

    /** 优先队列元素：候选距离 + 节点序号。 */
    private record Candidate(int node, BigDecimal distance) {
    }

    private Dijkstra() {
    }

    /**
     * 求 {@code from} 到 {@code to} 的最短路径。
     *
     * <p>起点等于终点时返回只含一个节点、距离为 {@code 0.00} 的路径；
     * 不可达时返回 {@link Optional#empty()}。
     *
     * @throws IllegalArgumentException 节点序号越界
     */
    public static Optional<Path> shortest(Graph graph, int from, int to) {
        Objects.requireNonNull(graph, "graph");
        int count = graph.nodeCount();
        if (from < 0 || from >= count) {
            throw new IllegalArgumentException("起点序号越界：" + from);
        }
        if (to < 0 || to >= count) {
            throw new IllegalArgumentException("终点序号越界：" + to);
        }
        if (from == to) {
            return Optional.of(new Path(List.of(from), BigDecimal.ZERO.setScale(SCALE)));
        }

        BigDecimal[] dist = new BigDecimal[count];
        int[] previous = new int[count];
        boolean[] settled = new boolean[count];
        Arrays.fill(previous, -1);
        dist[from] = BigDecimal.ZERO;

        // 按距离升序、同距离按序号升序，保证结果稳定
        PriorityQueue<Candidate> queue = new PriorityQueue<>(
                Comparator.comparing(Candidate::distance).thenComparingInt(Candidate::node));
        queue.add(new Candidate(from, BigDecimal.ZERO));

        while (!queue.isEmpty()) {
            Candidate current = queue.poll();
            int u = current.node();
            if (settled[u]) {
                // 惰性删除：旧条目直接跳过
                continue;
            }
            settled[u] = true;
            if (u == to) {
                break;
            }
            for (Graph.Neighbor neighbor : graph.neighbors(u)) {
                int v = neighbor.to();
                if (settled[v]) {
                    continue;
                }
                BigDecimal candidate = current.distance().add(neighbor.distance());
                if (dist[v] == null || candidate.compareTo(dist[v]) < 0) {
                    dist[v] = candidate;
                    previous[v] = u;
                    queue.add(new Candidate(v, candidate));
                }
            }
        }

        if (dist[to] == null) {
            return Optional.empty();
        }

        List<Integer> path = new ArrayList<>();
        for (int node = to; node != -1; node = previous[node]) {
            path.add(node);
        }
        java.util.Collections.reverse(path);

        return Optional.of(new Path(path, dist[to].setScale(SCALE, RoundingMode.HALF_UP)));
    }
}
