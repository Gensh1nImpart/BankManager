package com.bankmanager.dao.impl;

import com.bankmanager.dao.GraphDao;
import com.bankmanager.db.DaoException;
import com.bankmanager.domain.BankEdge;
import com.bankmanager.domain.BankNode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link JdbcGraphDao} 的 H2 集成测试。 */
class JdbcGraphDaoTest extends DaoTestSupport {

    private final GraphDao dao = new JdbcGraphDao(DATABASE);

    @Test
    void nodesAreSortedById() {
        dao.insertNode(3, "分行");
        dao.insertNode(1, "大门");
        dao.insertNode(2, "网点1");

        List<BankNode> nodes = dao.nodes();

        assertEquals(List.of(
                new BankNode(1, "大门"),
                new BankNode(2, "网点1"),
                new BankNode(3, "分行")), nodes);
    }

    @Test
    void edgesAreSortedByIdWithBigDecimalDistance() {
        dao.insertNode(1, "大门");
        dao.insertNode(2, "网点1");
        dao.insertNode(3, "网点2");
        dao.insertEdge(2, 3, new BigDecimal("15.50"));
        dao.insertEdge(1, 2, new BigDecimal("20.00"));

        List<BankEdge> edges = dao.edges();

        assertEquals(2, edges.size());
        assertEquals(new BankEdge(2, 3, new BigDecimal("15.50")), edges.get(0));
        assertEquals(1, edges.get(1).fromNode());
        assertEquals(2, edges.get(1).toNode());
        assertEquals(0, new BigDecimal("20.00").compareTo(edges.get(1).distance()));
    }

    @Test
    void insertEdgeWithoutNodeRejected() {
        dao.insertNode(1, "大门");

        assertThrows(DaoException.class, () -> dao.insertEdge(1, 99, new BigDecimal("10.00")));
        assertTrue(dao.edges().isEmpty());
    }
}
