package com.bankmanager.service.impl;

import com.bankmanager.dao.LogDao;
import com.bankmanager.domain.OperationLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 手写内存版 {@link LogDao}，记录服务层写入的操作日志。 */
final class FakeLogDao implements LogDao {

    record Entry(String username, String action, String detail) {
    }

    private final List<Entry> entries = new ArrayList<>();
    private final List<OperationLog> store = new ArrayList<>();

    @Override
    public void append(String username, String action, String detail) {
        entries.add(new Entry(username, action, detail));
        store.add(new OperationLog(store.size() + 1L, LocalDateTime.now(), username, action, detail));
    }

    @Override
    public List<OperationLog> recent(int limit) {
        List<OperationLog> copy = new ArrayList<>(store);
        Collections.reverse(copy);
        return copy.subList(0, Math.min(Math.max(limit, 0), copy.size()));
    }

    List<Entry> entries() {
        return entries;
    }

    boolean hasAction(String action) {
        return entries.stream().anyMatch(entry -> entry.action().equals(action));
    }
}
