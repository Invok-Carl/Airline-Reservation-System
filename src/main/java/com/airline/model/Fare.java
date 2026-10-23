package com.airline.model;

import java.math.BigDecimal;

public class Fare {

    private long fare_id;
    private long route_id;
    private String fare_class;
    private BigDecimal base_fare;
    private BigDecimal tax;
    private BigDecimal total_fare;
    private String currency;

    public Fare(long fare_id, long route_id, String fare_class,
                BigDecimal base_fare, BigDecimal tax, BigDecimal total_fare, String currency) {
        this.fare_id = fare_id;
        this.route_id = route_id;
        this.fare_class = fare_class;
        this.base_fare = base_fare;
        this.tax = tax;
        this.total_fare = total_fare;
        this.currency = currency;
    }
    public Fare(long route_id, String fare_class,
                BigDecimal base_fare, BigDecimal tax,BigDecimal total_fare,
                String currency) {
        this.route_id = route_id;
        this.fare_class = fare_class;
        this.base_fare = base_fare;
        this.tax = tax;
        this.total_fare = total_fare;
        this.currency = currency;
    }

    public long getFare_id() {
        return fare_id;
    }

    public void setFare_id(long fare_id) {
        this.fare_id = fare_id;
    }

    public long getRoute_id() {
        return route_id;
    }

    public void setRoute_id(long route_id) {
        this.route_id = route_id;
    }

    public String getFare_class() {
        return fare_class;
    }

    public void setFare_class(String fare_class) {
        this.fare_class = fare_class;
    }

    public BigDecimal getBase_fare() {
        return base_fare;
    }

    public void setBase_fare(BigDecimal base_fare) {
        this.base_fare = base_fare;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal_fare() {
        return total_fare;
    }

    public void setTotal_fare(BigDecimal total_fare) {
        this.total_fare = total_fare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
