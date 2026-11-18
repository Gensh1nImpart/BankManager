package com.bankmanager.domain;

/** 客户等级，对应旧库 custom.Posi 字段。 */
public enum AccountType {
    NORMAL("普通用户"),
    VIP("VIP用户");

    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static AccountType fromLabel(String value) {
        return "VIP用户".equals(value) || "VIP".equalsIgnoreCase(value) ? VIP : NORMAL;
    }
}
