package com.bankmanager.db;

/** 数据访问层统一异常，包装 {@link java.sql.SQLException}。 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
