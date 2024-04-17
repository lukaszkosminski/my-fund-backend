package com.myfund.exceptions;

public class CategoryNotUniqueException  extends RuntimeException {
    public CategoryNotUniqueException(String message) {
        super(message);
    }
}
