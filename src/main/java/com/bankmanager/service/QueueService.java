package com.bankmanager.service;

import com.bankmanager.domain.QueueTicket;

import java.util.List;
import java.util.Optional;

/** 排队叫号。 */
public interface QueueService {

    QueueTicket take(String customerName, boolean vip);

    Optional<QueueTicket> callNext(boolean vip, int counterNo);

    void finish(long ticketId);

    List<QueueTicket> waiting(boolean vip);
}
