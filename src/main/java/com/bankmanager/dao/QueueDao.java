package com.bankmanager.dao;

import com.bankmanager.domain.QueueTicket;

import java.util.List;
import java.util.Optional;

/** 排队票据访问接口。 */
public interface QueueDao {

    /** 取号：同一天同一队列（VIP/普通）内号码递增。 */
    QueueTicket enqueue(String customerName, boolean vip);

    /** 按先来后到列出等待中的票据。 */
    List<QueueTicket> waiting(boolean vip);

    /** 叫号：把队首票据置为 CALLED，返回该票据；队列为空返回 empty。 */
    Optional<QueueTicket> callNext(boolean vip, int counterNo);

    /** 完成业务。 */
    void finish(long ticketId);

    /** 最近票据（含已完成），用于历史展示。 */
    List<QueueTicket> recent(int limit);
}
