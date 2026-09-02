package com.pizzeria.app.order.service;

import java.math.BigDecimal;

public record PricingResult(BigDecimal subtotal, BigDecimal discountAmount, BigDecimal total) {
}
