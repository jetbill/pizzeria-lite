package com.pizzeria.app.order.exception;

import com.pizzeria.app.common.exception.BusinessValidationException;
import com.pizzeria.app.order.entity.OrderStatus;

public class InvalidOrderStatusTransitionException extends BusinessValidationException {

    public InvalidOrderStatusTransitionException(OrderStatus current, OrderStatus requested) {
        super("Cannot move order from " + current + " to " + requested);
    }
}
