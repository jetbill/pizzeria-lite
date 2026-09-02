package com.pizzeria.app.order.service;

import com.pizzeria.app.order.entity.OrderStatus;
import com.pizzeria.app.order.exception.InvalidOrderStatusTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusTransitionValidatorTest {

    private final OrderStatusTransitionValidator validator = new OrderStatusTransitionValidator();

    @Test
    void permiteLaTransicionDeCreatedAInPreparation() {
        assertThatCode(() -> validator.validateTransition(OrderStatus.CREATED, OrderStatus.IN_PREPARATION))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaSaltarseEstados() {
        assertThatThrownBy(() -> validator.validateTransition(OrderStatus.CREATED, OrderStatus.DELIVERED))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED"})
    void losEstadosFinalesNoPermitenNingunaTransicion(OrderStatus finalStatus) {
        assertThatThrownBy(() -> validator.validateTransition(finalStatus, OrderStatus.IN_PREPARATION))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }
}
