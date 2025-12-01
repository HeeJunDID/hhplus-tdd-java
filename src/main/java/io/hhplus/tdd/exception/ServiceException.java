package io.hhplus.tdd.exception;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
