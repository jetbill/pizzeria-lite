package com.pizzeria.app.order.service;

import com.pizzeria.app.order.config.PricingProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    private final PricingProperties pricingProperties;

    public PricingService(PricingProperties pricingProperties) {
        this.pricingProperties = pricingProperties;
    }

    public PricingResult calculate(BigDecimal subtotal, int totalQuantity, String couponCode) {
        BigDecimal discount = BigDecimal.ZERO;

        if (couponCode != null && couponCode.equalsIgnoreCase(pricingProperties.getCouponCode())) {
            discount = discount.add(subtotal.multiply(pricingProperties.getCouponDiscount()));
        }

        if (subtotal.compareTo(pricingProperties.getHighSubtotalThreshold()) > 0) {
            discount = discount.add(subtotal.multiply(pricingProperties.getHighSubtotalDiscount()));
        } else if (subtotal.compareTo(pricingProperties.getMidSubtotalThreshold()) > 0) {
            discount = discount.add(subtotal.multiply(pricingProperties.getMidSubtotalDiscount()));
        }

        if (totalQuantity >= pricingProperties.getBulkQuantityThreshold()) {
            discount = discount.add(subtotal.multiply(pricingProperties.getBulkQuantityDiscount()));
        }

        discount = discount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal roundedSubtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = roundedSubtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        return new PricingResult(roundedSubtotal, discount, total);
    }
}
