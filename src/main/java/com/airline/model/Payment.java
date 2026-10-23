package com.airline.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private long payment_id;
    private long reservation_id;
    private LocalDateTime payment_date;
    private BigDecimal amount;
    private String currency;
    private String payment_method;
    private String payment_status;
    private String transaction_ref;
    private long recorded_by;

    public Payment(long reservation_id,
                   LocalDateTime payment_date,
                   BigDecimal amount,
                   String currency,
                   String payment_method,
                   String payment_status,
                   String transaction_ref,
                   long recorded_by) {
        this.reservation_id = reservation_id;
        this.payment_date = payment_date;
        this.amount = amount;
        this.currency = currency;
        this.payment_method = payment_method;
        this.payment_status = payment_status;
        this.transaction_ref = transaction_ref;
        this.recorded_by = recorded_by;
    }

    public Payment(long payment_id,
                   long reservation_id,
                   LocalDateTime payment_date,
                   BigDecimal amount,
                   String currency,
                   String payment_method,
                   String payment_status,
                   String transaction_ref,
                   long recorded_by) {
        this.payment_id = payment_id;
        this.reservation_id = reservation_id;
        this.payment_date = payment_date;
        this.amount = amount;
        this.currency = currency;
        this.payment_method = payment_method;
        this.payment_status = payment_status;
        this.transaction_ref = transaction_ref;
        this.recorded_by = recorded_by;
    }

    public long getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(long payment_id) {
        this.payment_id = payment_id;
    }

    public long getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(long reservation_id) {
        this.reservation_id = reservation_id;
    }

    public LocalDateTime getPayment_date() {
        return payment_date;
    }

    public void setPayment_date(LocalDateTime payment_date) {
        this.payment_date = payment_date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getTransaction_ref() {
        return transaction_ref;
    }

    public void setTransaction_ref(String transaction_ref) {
        this.transaction_ref = transaction_ref;
    }

    public long getRecorded_by() {
        return recorded_by;
    }

    public void setRecorded_by(long recorded_by) {
        this.recorded_by = recorded_by;
    }
}