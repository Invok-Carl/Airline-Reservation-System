package com.airline.model;


import java.time.LocalDateTime;

public class CheckIn {

    private long checkinId;
    private long segmentId;
    private LocalDateTime checkinTime;
    private Long seatId;
    private String status;
    private long employeeId;

    public CheckIn(long segmentId,
                   LocalDateTime checkinTime,
                   Long seatId,
                   String status,
                   long employeeId) {
        this.segmentId = segmentId;
        this.checkinTime = checkinTime;
        this.seatId = seatId;
        this.status = status;
        this.employeeId = employeeId;
    }

    public CheckIn(long checkinId,
                   long segmentId,
                   LocalDateTime checkinTime,
                   Long seatId,
                   String status,
                   long employeeId) {
        this.checkinId = checkinId;
        this.segmentId = segmentId;
        this.checkinTime = checkinTime;
        this.seatId = seatId;
        this.status = status;
        this.employeeId = employeeId;
    }

    public long getCheckinId() {
        return checkinId;
    }

    public void setCheckinId(long checkinId) {
        this.checkinId = checkinId;
    }

    public long getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(long segmentId) {
        this.segmentId = segmentId;
    }

    public LocalDateTime getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(LocalDateTime checkinTime) {
        this.checkinTime = checkinTime;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }
}