package com.bankmanager.dao;

import com.bankmanager.domain.BankEdge;
import com.bankmanager.domain.BankNode;

import java.util.List;

/** 网点图访问接口。边的方向在服务层按无向图处理。 */
public interface GraphDao {

    List<BankNode> nodes();

    List<BankEdge> edges();

    void insertNode(int id, String name);

    void insertEdge(int fromNode, int toNode, java.math.BigDecimal distance);
}
