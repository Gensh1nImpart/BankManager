package com.bankmanager.domain;

import java.time.LocalDateTime;

/** 排队票据。 */
public record QueueTicket(
        long id,
        int number,
        String customerName,
        boolean vip,
        TicketStatus status,
        Integer counterNo,
        LocalDateTime createdAt,
        LocalDateTime calledAt) {

    public String vipLabel() {
        return vip ? "VIP" : "普通";
    }
}
