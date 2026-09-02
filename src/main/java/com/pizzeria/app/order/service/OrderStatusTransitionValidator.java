package com.pizzeria.app.order.service;

import com.pizzeria.app.order.entity.OrderStatus;
import com.pizzeria.app.order.exception.InvalidOrderStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusTransitionValidator {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.IN_PREPARATION, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.IN_PREPARATION, EnumSet.of(OrderStatus.ON_THE_WAY, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.ON_THE_WAY, EnumSet.of(OrderStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public void validateTransition(OrderStatus current, OrderStatus requested) {
        if (!ALLOWED_TRANSITIONS.get(current).contains(requested)) {
            throw new InvalidOrderStatusTransitionException(current, requested);
        }
    }
}
