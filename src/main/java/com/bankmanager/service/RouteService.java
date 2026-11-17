package com.bankmanager.service;

import java.math.BigDecimal;
import java.util.List;

/** 网点导航：在无向带权图上求最短路径。 */
public interface RouteService {

    /** 返回包含起终点的路径与总距离；不可达时抛出 {@link ServiceException}。 */
    Route shortestPath(String fromName, String toName);

    List<String> nodeNames();

    record Route(List<String> path, BigDecimal distance) {
        public String pathText() {
            return String.join(" → ", path);
        }
    }
}
