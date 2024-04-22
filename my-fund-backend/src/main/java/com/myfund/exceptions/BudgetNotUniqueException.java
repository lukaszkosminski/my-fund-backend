package com.myfund.exceptions;

public class BudgetNotUniqueException extends RuntimeException {
    public BudgetNotUniqueException(String message) {
        super(message);
    }
}
