package com.pizzeria.app.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "pricing")
public class PricingProperties {

    private String couponCode;
    private BigDecimal couponDiscount;
    private BigDecimal highSubtotalThreshold;
    private BigDecimal highSubtotalDiscount;
    private BigDecimal midSubtotalThreshold;
    private BigDecimal midSubtotalDiscount;
    private int bulkQuantityThreshold;
    private BigDecimal bulkQuantityDiscount;

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public BigDecimal getHighSubtotalThreshold() {
        return highSubtotalThreshold;
    }

    public void setHighSubtotalThreshold(BigDecimal highSubtotalThreshold) {
        this.highSubtotalThreshold = highSubtotalThreshold;
    }

    public BigDecimal getHighSubtotalDiscount() {
        return highSubtotalDiscount;
    }

    public void setHighSubtotalDiscount(BigDecimal highSubtotalDiscount) {
        this.highSubtotalDiscount = highSubtotalDiscount;
    }

    public BigDecimal getMidSubtotalThreshold() {
        return midSubtotalThreshold;
    }

    public void setMidSubtotalThreshold(BigDecimal midSubtotalThreshold) {
        this.midSubtotalThreshold = midSubtotalThreshold;
    }

    public BigDecimal getMidSubtotalDiscount() {
        return midSubtotalDiscount;
    }

    public void setMidSubtotalDiscount(BigDecimal midSubtotalDiscount) {
        this.midSubtotalDiscount = midSubtotalDiscount;
    }

    public int getBulkQuantityThreshold() {
        return bulkQuantityThreshold;
    }

    public void setBulkQuantityThreshold(int bulkQuantityThreshold) {
        this.bulkQuantityThreshold = bulkQuantityThreshold;
    }

    public BigDecimal getBulkQuantityDiscount() {
        return bulkQuantityDiscount;
    }

    public void setBulkQuantityDiscount(BigDecimal bulkQuantityDiscount) {
        this.bulkQuantityDiscount = bulkQuantityDiscount;
    }
}
