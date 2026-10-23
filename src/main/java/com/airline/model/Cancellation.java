package com.airline.model;

import java.time.LocalDateTime;

public class Cancellation {

    private long cancellation_id;
    private long reservation_id;
    private LocalDateTime cancel_date;
    private String reason;
    private long employee_id;

    public Cancellation(long reservation_id,
                        LocalDateTime cancel_date,
                        String reason,
                        long employee_id) {
        this.reservation_id = reservation_id;
        this.cancel_date = cancel_date;
        this.reason = reason;
        this.employee_id = employee_id;
    }

    public Cancellation(long cancellation_id,
                        long reservation_id,
                        LocalDateTime cancel_date,
                        String reason,
                        long employee_id) {
        this.cancellation_id = cancellation_id;
        this.reservation_id = reservation_id;
        this.cancel_date = cancel_date;
        this.reason = reason;
        this.employee_id = employee_id;
    }

    public long getCancellation_id() {
        return cancellation_id;
    }

    public void setCancellation_id(long cancellation_id) {
        this.cancellation_id = cancellation_id;
    }

    public long getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(long reservation_id) {
        this.reservation_id = reservation_id;
    }

    public LocalDateTime getCancel_date() {
        return cancel_date;
    }

    public void setCancel_date(LocalDateTime cancel_date) {
        this.cancel_date = cancel_date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(long employee_id) {
        this.employee_id = employee_id;
    }
}