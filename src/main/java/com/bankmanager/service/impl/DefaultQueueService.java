package com.bankmanager.service.impl;

import com.bankmanager.dao.QueueDao;
import com.bankmanager.domain.QueueTicket;
import com.bankmanager.service.QueueService;
import com.bankmanager.service.ServiceException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 排队叫号服务：业务规则很薄，主要做参数校验后委托 {@link QueueDao}。
 * 号码生成、先来后到等规则由数据层保证。
 */
public final class DefaultQueueService implements QueueService {

    /** 客户姓名长度上限（按字符数计）。 */
    public static final int MAX_NAME_LENGTH = 32;

    private final QueueDao queueDao;

    public DefaultQueueService(QueueDao queueDao) {
        this.queueDao = Objects.requireNonNull(queueDao, "queueDao");
    }

    @Override
    public QueueTicket take(String customerName, boolean vip) {
        String name = requireName(customerName);
        try {
            return queueDao.enqueue(name, vip);
        } catch (RuntimeException e) {
            throw new ServiceException("取号失败：" + describe(e), e);
        }
    }

    @Override
    public Optional<QueueTicket> callNext(boolean vip, int counterNo) {
        if (counterNo <= 0) {
            throw new ServiceException("窗口号必须大于 0");
        }
        try {
            return queueDao.callNext(vip, counterNo);
        } catch (RuntimeException e) {
            throw new ServiceException("叫号失败：" + describe(e), e);
        }
    }

    @Override
    public void finish(long ticketId) {
        if (ticketId <= 0) {
            throw new ServiceException("票据编号必须大于 0");
        }
        try {
            queueDao.finish(ticketId);
        } catch (RuntimeException e) {
            throw new ServiceException("完成业务失败：" + describe(e), e);
        }
    }

    @Override
    public List<QueueTicket> waiting(boolean vip) {
        try {
            return queueDao.waiting(vip);
        } catch (RuntimeException e) {
            throw new ServiceException("查询排队列表失败：" + describe(e), e);
        }
    }

    /** 去除首尾空白后校验姓名。 */
    private static String requireName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new ServiceException("客户姓名不能为空");
        }
        String name = customerName.trim();
        if (name.length() > MAX_NAME_LENGTH) {
            throw new ServiceException("客户姓名不能超过 " + MAX_NAME_LENGTH + " 个字符");
        }
        return name;
    }

    private static String describe(Throwable e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
