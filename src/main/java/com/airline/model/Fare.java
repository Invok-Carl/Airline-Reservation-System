package com.airline.model;

import java.math.BigDecimal;

public class Fare {

    private long fareId;
    private long routeId;
    private String fareClass;
    private BigDecimal baseFare;
    private BigDecimal tax;
    private BigDecimal totalFare;
    private String currency;

    public Fare(long fareId, long routeId, String fareClass,
                BigDecimal baseFare, BigDecimal tax, BigDecimal totalFare, String currency) {
        this.fareId = fareId;
        this.routeId = routeId;
        this.fareClass = fareClass;
        this.baseFare = baseFare;
        this.tax = tax;
        this.totalFare = totalFare;
        this.currency = currency;
    }
    public Fare( long routeId, String fareClass,
                BigDecimal baseFare, BigDecimal tax, BigDecimal totalFare, String currency) {
        this.routeId = routeId;
        this.fareClass = fareClass;
        this.baseFare = baseFare;
        this.tax = tax;
        this.totalFare = totalFare;
        this.currency = currency;
    }

    public long getFareId() {
        return fareId;
    }

    public void setFareId(long fareId) {
        this.fareId = fareId;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public String getFareClass() {
        return fareClass;
    }

    public void setFareClass(String fareClass) {
        this.fareClass = fareClass;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
