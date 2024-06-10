package com.myfund.exceptions;

public class EmailThrottleException extends RuntimeException {
    public EmailThrottleException(String message) {
        super(message);
    }
}
