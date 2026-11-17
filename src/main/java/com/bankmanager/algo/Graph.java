package com.bankmanager.algo;

import com.bankmanager.domain.BankEdge;
import com.bankmanager.domain.BankNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 网点无向带权图的邻接表实现。
 *
 * <p>构建规则：
 * <ul>
 *     <li>节点按 {@code nodes} 的顺序编号，{@code BankNode.id()} 只用于把边映射到节点；</li>
 *     <li>每条 {@link BankEdge} 视为双向边，重复边只保留距离更小的一条；</li>
 *     <li>自环（起点与终点相同）直接忽略；</li>
 *     <li>距离必须为正数（{@code > 0}），否则抛出 {@link IllegalArgumentException}。</li>
 * </ul>
 *
 * <p>构建完成后图不可变，可安全地在多线程间共享读取。
 */
public final class Graph {

    /** 邻接表中的一条边：{@code to} 是对端节点序号，{@code distance} 为该边距离。 */
    public record Neighbor(int to, BigDecimal distance) {
        public Neighbor {
            Objects.requireNonNull(distance, "distance");
        }
    }

    private final List<String> names;
    private final List<List<Neighbor>> adjacency;
    private final Map<String, Integer> indexByName;

    /**
     * 由节点与边构建无向图。
     *
     * @param nodes 网点节点（不允许 null；编号不可重复）
     * @param edges 网点边（允许 null，按空集合处理）
     */
    public Graph(List<BankNode> nodes, List<BankEdge> edges) {
        List<BankNode> nodeList = nodes == null ? List.of() : nodes;
        List<BankEdge> edgeList = edges == null ? List.of() : edges;

        List<String> nameList = new ArrayList<>(nodeList.size());
        Map<Integer, Integer> indexById = new HashMap<>();
        Map<String, Integer> byName = new LinkedHashMap<>();

        for (BankNode node : nodeList) {
            if (node == null) {
                throw new IllegalArgumentException("网点节点不能为空");
            }
            if (indexById.containsKey(node.id())) {
                throw new IllegalArgumentException("网点编号重复：" + node.id());
            }
            String name = node.name() == null ? "" : node.name().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("网点名称不能为空（编号 " + node.id() + "）");
            }
            int index = nameList.size();
            nameList.add(name);
            indexById.put(node.id(), index);
            byName.putIfAbsent(name, index);
        }

        List<List<Neighbor>> lists = new ArrayList<>(nameList.size());
        for (int i = 0; i < nameList.size(); i++) {
            lists.add(new ArrayList<>());
        }

        for (BankEdge edge : edgeList) {
            if (edge == null) {
                throw new IllegalArgumentException("网点边不能为空");
            }
            BigDecimal distance = edge.distance();
            if (distance == null) {
                throw new IllegalArgumentException(
                        "网点距离不能为空：" + edge.fromNode() + " - " + edge.toNode());
            }
            if (distance.signum() <= 0) {
                throw new IllegalArgumentException("网点距离必须大于 0：" + distance.toPlainString());
            }
            Integer from = indexById.get(edge.fromNode());
            Integer to = indexById.get(edge.toNode());
            if (from == null) {
                throw new IllegalArgumentException("边的起点不存在：" + edge.fromNode());
            }
            if (to == null) {
                throw new IllegalArgumentException("边的终点不存在：" + edge.toNode());
            }
            if (from.intValue() == to.intValue()) {
                // 自环不影响最短路，直接忽略
                continue;
            }
            // 无向图：两个方向都要建边
            putMin(lists.get(from), to, distance);
            putMin(lists.get(to), from, distance);
        }

        List<List<Neighbor>> immutable = new ArrayList<>(lists.size());
        for (List<Neighbor> list : lists) {
            immutable.add(Collections.unmodifiableList(list));
        }

        this.names = List.copyOf(nameList);
        this.adjacency = List.copyOf(immutable);
        this.indexByName = Collections.unmodifiableMap(byName);
    }

    /** 重复边保留最小值；邻接表规模很小，顺序扫描即可。 */
    private static void putMin(List<Neighbor> list, int to, BigDecimal distance) {
        for (int i = 0; i < list.size(); i++) {
            Neighbor current = list.get(i);
            if (current.to() == to) {
                if (distance.compareTo(current.distance()) < 0) {
                    list.set(i, new Neighbor(to, distance));
                }
                return;
            }
        }
        list.add(new Neighbor(to, distance));
    }

    /** 节点数量。 */
    public int nodeCount() {
        return names.size();
    }

    /** 节点序号对应的网点名称。 */
    public String nameOf(int node) {
        checkNode(node);
        return names.get(node);
    }

    /** 全部网点名称，顺序与节点序号一致。 */
    public List<String> names() {
        return names;
    }

    /** 按名称查节点序号，找不到返回 {@code -1}。同名节点取第一个。 */
    public int indexOf(String name) {
        if (name == null) {
            return -1;
        }
        Integer index = indexByName.get(name.trim());
        return index == null ? -1 : index;
    }

    /** 节点序号对应的邻接边，返回值不可修改。 */
    public List<Neighbor> neighbors(int node) {
        checkNode(node);
        return adjacency.get(node);
    }

    private void checkNode(int node) {
        if (node < 0 || node >= names.size()) {
            throw new IndexOutOfBoundsException("节点序号越界：" + node);
        }
    }
}
