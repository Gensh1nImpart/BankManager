package com.bankmanager.service;

/** 业务层异常，message 可直接展示给用户。 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
