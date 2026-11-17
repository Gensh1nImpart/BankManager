package com.bankmanager.domain;

import java.time.LocalDateTime;

/** 操作日志。 */
public record OperationLog(long id, LocalDateTime createdAt, String username, String action, String detail) {
}
