package com.airline.model;


import java.time.LocalDateTime;

public class CheckIn {

    private long checkin_id;
    private long segment_id;
    private LocalDateTime checkin_time;
    private Long seat_id;
    private String status;
    private long employee_id;

    public CheckIn(long segment_id,
                   LocalDateTime checkin_time,
                   Long seat_id,
                   String status,
                   long employee_id) {
        this.segment_id = segment_id;
        this.checkin_time = checkin_time;
        this.seat_id = seat_id;
        this.status = status;
        this.employee_id = employee_id;
    }

    public CheckIn(long checkin_id,
                   long segment_id,
                   LocalDateTime checkin_time,
                   Long seat_id,
                   String status,
                   long employee_id) {
        this.checkin_id = checkin_id;
        this.segment_id = segment_id;
        this.checkin_time = checkin_time;
        this.seat_id = seat_id;
        this.status = status;
        this.employee_id = employee_id;
    }

    public long getCheckin_id() {
        return checkin_id;
    }

    public void setCheckin_id(long checkin_id) {
        this.checkin_id = checkin_id;
    }

    public long getSegment_id() {
        return segment_id;
    }

    public void setSegment_id(long segment_id) {
        this.segment_id = segment_id;
    }

    public LocalDateTime getCheckin_time() {
        return checkin_time;
    }

    public void setCheckin_time(LocalDateTime checkin_time) {
        this.checkin_time = checkin_time;
    }

    public Long getSeat_id() {
        return seat_id;
    }

    public void setSeat_id(Long seat_id) {
        this.seat_id = seat_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(long employee_id) {
        this.employee_id = employee_id;
    }
}