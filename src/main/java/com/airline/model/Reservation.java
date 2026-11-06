package com.airline.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reservation {
    private long reservationId;
    private String bookingReference;
    private LocalDateTime  reservationDate;
    private String status;
    private BigDecimal totalAmount;
    private long createdBy;


    public Reservation(){}
    public Reservation(
            long reservationId, String bookingReference,
            LocalDateTime reservationDate, String status,
            BigDecimal totalAmount, long createdBy
    ) {
        this.reservationId = reservationId;
        this.bookingReference = bookingReference;
        this.reservationDate = reservationDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdBy = createdBy;
    }
    public Reservation(
            String bookingReference,
            LocalDateTime reservationDate, String status,
            BigDecimal totalAmount, long createdBy
    ) {
        this.bookingReference = bookingReference;
        this.reservationDate = reservationDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdBy = createdBy;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }
}
