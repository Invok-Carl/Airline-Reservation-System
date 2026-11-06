package com.airline.model;

import java.time.LocalDateTime;

public class Cancellation {

    private long cancellationId;
    private long reservationId;
    private LocalDateTime cancelDate;
    private String reason;
    private long employeeId;

    public Cancellation(long reservationId,
                        LocalDateTime cancelDate,
                        String reason,
                        long employeeId) {
        this.reservationId = reservationId;
        this.cancelDate = cancelDate;
        this.reason = reason;
        this.employeeId = employeeId;
    }

    public Cancellation(long cancellationId,
                        long reservationId,
                        LocalDateTime cancelDate,
                        String reason,
                        long employeeId) {
        this.cancellationId = cancellationId;
        this.reservationId = reservationId;
        this.cancelDate = cancelDate;
        this.reason = reason;
        this.employeeId = employeeId;
    }

    public long getCancellationId() {
        return cancellationId;
    }

    public void setCancellationId(long cancellationId) {
        this.cancellationId = cancellationId;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public LocalDateTime getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(LocalDateTime cancelDate) {
        this.cancelDate = cancelDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }
}