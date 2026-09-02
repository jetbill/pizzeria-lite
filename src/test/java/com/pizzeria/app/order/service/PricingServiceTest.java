package com.pizzeria.app.order.service;

import com.pizzeria.app.order.config.PricingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        PricingProperties properties = new PricingProperties();
        properties.setCouponCode("PIZZA10");
        properties.setCouponDiscount(new BigDecimal("0.10"));
        properties.setHighSubtotalThreshold(new BigDecimal("100"));
        properties.setHighSubtotalDiscount(new BigDecimal("0.10"));
        properties.setMidSubtotalThreshold(new BigDecimal("50"));
        properties.setMidSubtotalDiscount(new BigDecimal("0.05"));
        properties.setBulkQuantityThreshold(5);
        properties.setBulkQuantityDiscount(new BigDecimal("0.05"));

        pricingService = new PricingService(properties);
    }

    @Test
    void sinCuponNiUmbrales_noAplicaDescuento() {
        PricingResult result = pricingService.calculate(new BigDecimal("30"), 2, null);

        assertThat(result.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(result.total()).isEqualByComparingTo("30.00");
    }

    @Test
    void cuponYSubtotalAlto_acumulaAmbosDescuentos() {
        PricingResult result = pricingService.calculate(new BigDecimal("120"), 2, "pizza10");

        assertThat(result.discountAmount()).isEqualByComparingTo("24.00");
        assertThat(result.total()).isEqualByComparingTo("96.00");
    }

    @Test
    void cantidadAltaSumaDescuentoAdicional() {
        PricingResult result = pricingService.calculate(new BigDecimal("60"), 5, null);

        assertThat(result.discountAmount()).isEqualByComparingTo("6.00");
        assertThat(result.total()).isEqualByComparingTo("54.00");
    }
}
