package com.airline.model;

import java.time.LocalDateTime;

public class Reservation {
    private long reservation_id;
    private String booking_reference;
    private LocalDateTime  reservation_date;
    private String status;
    private long total_amount;
    private long created_by;

    public Reservation(
            long reservation_id, String booking_reference,
            LocalDateTime reservation_date, String status,
            long total_amount, long created_by
    ) {
        this.reservation_id = reservation_id;
        this.booking_reference = booking_reference;
        this.reservation_date = reservation_date;
        this.status = status;
        this.total_amount = total_amount;
        this.created_by = created_by;
    }
    public Reservation(
            String booking_reference,
            LocalDateTime reservation_date, String status,
            long total_amount, long created_by
    ) {
        this.booking_reference = booking_reference;
        this.reservation_date = reservation_date;
        this.status = status;
        this.total_amount = total_amount;
        this.created_by = created_by;
    }

    public long getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(long reservation_id) {
        this.reservation_id = reservation_id;
    }

    public String getBooking_reference() {
        return booking_reference;
    }

    public void setBooking_reference(String booking_reference) {
        this.booking_reference = booking_reference;
    }

    public LocalDateTime getReservation_date() {
        return reservation_date;
    }

    public void setReservation_date(LocalDateTime reservation_date) {
        this.reservation_date = reservation_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(long total_amount) {
        this.total_amount = total_amount;
    }

    public long getCreated_by() {
        return created_by;
    }

    public void setCreated_by(long created_by) {
        this.created_by = created_by;
    }
}
