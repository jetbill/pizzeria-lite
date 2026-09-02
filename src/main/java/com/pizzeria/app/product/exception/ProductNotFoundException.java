package com.pizzeria.app.product.exception;

import com.pizzeria.app.common.exception.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long id) {
        super("Product not found: " + id);
    }
}
