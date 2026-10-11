package com.airline.model;

public class Seat {
    private long seat_id;
    private long aircraft_id;
    private String seat_number;
    private String seat_class;
    private String seat_type;

    public Seat(long seat_id, long aircraft_id, String seat_class, String seat_number, String seat_type) {
        this.seat_id = seat_id;
        this.aircraft_id = aircraft_id;
        this.seat_class = seat_class;
        this.seat_number = seat_number;
        this.seat_type = seat_type;
    }

    public Seat(long aircraft_id, String seat_number, String seat_class, String seat_type) {
        this.aircraft_id = aircraft_id;
        this.seat_number = seat_number;
        this.seat_class = seat_class;
        this.seat_type = seat_type;
    }

    public long getSeat_id() {
        return seat_id;
    }

    public long getAircraft_id() {
        return aircraft_id;
    }

    public String getSeat_number() {
        return seat_number;
    }

    public String getSeat_class() {
        return seat_class;
    }

    public String getSeat_type() {
        return seat_type;
    }

    public void setSeat_id(long seat_id) {
        this.seat_id = seat_id;
    }

    public void setAircraft_id(long aircraft_id) {
        this.aircraft_id = aircraft_id;
    }

    public void setSeat_number(String seat_number) {
        this.seat_number = seat_number;
    }

    public void setSeat_type(String seat_type) {
        this.seat_type = seat_type;
    }

    public void setSeat_class(String seat_class) {
        this.seat_class = seat_class;
    }
}

