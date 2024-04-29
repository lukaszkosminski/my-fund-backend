package com.myfund.exceptions;

public class SubcategoryNotFoundException extends RuntimeException {
    public SubcategoryNotFoundException(String message) {
        super(message);
    }
}
