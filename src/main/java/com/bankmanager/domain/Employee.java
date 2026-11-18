package com.bankmanager.domain;

/** 银行职员，对应旧库 person 表。 */
public record Employee(
        String id,
        String name,
        String sex,
        Integer age,
        String tele,
        String address,
        String position) {

    /** 用于 UI 表格展示的行数据。 */
    public Object[] toRow() {
        return new Object[] {id, name, sex, age, tele, address, position};
    }
}
