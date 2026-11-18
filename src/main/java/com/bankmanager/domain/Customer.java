package com.bankmanager.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** 客户账户。金额一律使用 {@link BigDecimal}，不使用 double。 */
public record Customer(
        String card,
        String name,
        String passwordHash,
        String tele,
        AccountType type,
        BigDecimal balance,
        BigDecimal loan) {

    public Customer {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        balance = balance == null ? BigDecimal.ZERO : balance;
        loan = loan == null ? BigDecimal.ZERO : loan;
    }

    /** 用于 UI 展示的中文账户类型。 */
    public String typeLabel() {
        return type == AccountType.VIP ? "VIP用户" : "普通用户";
    }
}
