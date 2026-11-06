package com.airline.model;

import java.util.Comparator;

public class Seat  {
    private long seatId;
    private long aircraftId;
    private String seatNumber;
    private String seatClass;
    private String seatType;


    public Seat(
            long seatId, long aircraftId, String seatNumber, String seatClass,
             String seatType
    ) {
        this.seatId = seatId;
        this.aircraftId = aircraftId;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.seatType = seatType;
    }

    public Seat(
            long aircraftId, String seatNumber, String seatClass,
            String seatType
    ) {
        this.aircraftId = aircraftId;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.seatType = seatType;
    }

    public long getSeatId() {
        return seatId;
    }

    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }

    public long getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(long aircraftId) {
        this.aircraftId = aircraftId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }
}

