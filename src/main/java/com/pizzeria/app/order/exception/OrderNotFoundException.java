package com.pizzeria.app.order.exception;

import com.pizzeria.app.common.exception.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {
        super("Order not found: " + id);
    }
}
