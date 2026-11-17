package com.bankmanager.dao;

import com.bankmanager.domain.OperationLog;

import java.util.List;

/** 操作日志访问接口。 */
public interface LogDao {

    void append(String username, String action, String detail);

    List<OperationLog> recent(int limit);
}
