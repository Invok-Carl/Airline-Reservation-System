package com.airline.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private long paymentId;
    private long reservationId;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionRef;
    private long recordedBy;

    public Payment(
            long reservationId,
            LocalDateTime paymentDate,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String paymentStatus,
            String transactionRef,
            long recordedBy
    ) {
        this.reservationId = reservationId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionRef = transactionRef;
        this.recordedBy = recordedBy;
    }

    public Payment(
            long paymentId,
            long reservationId,
            LocalDateTime paymentDate,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String paymentStatus,
            String transactionRef,
            long recordedBy
    ) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionRef = transactionRef;
        this.recordedBy = recordedBy;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public long getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(long recordedBy) {
        this.recordedBy = recordedBy;
    }
}