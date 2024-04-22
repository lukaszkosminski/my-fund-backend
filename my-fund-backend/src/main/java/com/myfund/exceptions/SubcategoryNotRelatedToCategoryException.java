package com.myfund.exceptions;

public class SubcategoryNotRelatedToCategoryException   extends RuntimeException {
    public SubcategoryNotRelatedToCategoryException(String message) {
        super(message);
    }
}
