package com.bankmanager.domain;

import java.math.BigDecimal;

/** 网点图的无向边（注意：旧库 grap 表每行只存一次，读取时需视为双向）。 */
public record BankEdge(int fromNode, int toNode, BigDecimal distance) {
}
