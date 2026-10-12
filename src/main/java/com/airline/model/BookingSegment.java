package com.airline.model;

public class BookingSegment {
    private long segment_id;
    private long reservation_id;
    private long passenger_id;
    private long flight_id;
    private long seat_id;
    private long fare_id;
    private long aircraft_id;

    public BookingSegment(long segment_id, long reservation_id,
                              long passenger_id, long flight_id,
                              long seat_id, long fare_id, long aircraft_id) {
        this.segment_id = segment_id;
        this.reservation_id = reservation_id;
        this.passenger_id = passenger_id;
        this.flight_id = flight_id;
        this.seat_id = seat_id;
        this.fare_id = fare_id;
        this.aircraft_id = aircraft_id;
    }
    public BookingSegment(long reservation_id,
                              long passenger_id, long flight_id,
                              long seat_id, long fare_id, long aircraft_id) {
        this.reservation_id = reservation_id;
        this.passenger_id = passenger_id;
        this.flight_id = flight_id;
        this.seat_id = seat_id;
        this.fare_id = fare_id;
        this.aircraft_id = aircraft_id;
    }

    public long getSegment_id() {
        return segment_id;
    }

    public void setSegment_id(long segment_id) {
        this.segment_id = segment_id;
    }

    public long getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(long reservation_id) {
        this.reservation_id = reservation_id;
    }

    public long getPassenger_id() {
        return passenger_id;
    }

    public void setPassenger_id(long passenger_id) {
        this.passenger_id = passenger_id;
    }

    public long getFlight_id() {
        return flight_id;
    }

    public void setFlight_id(long flight_id) {
        this.flight_id = flight_id;
    }

    public long getFare_id() {
        return fare_id;
    }

    public void setFare_id(long fare_id) {
        this.fare_id = fare_id;
    }

    public long getSeat_id() {
        return seat_id;
    }

    public void setSeat_id(long seat_id) {
        this.seat_id = seat_id;
    }

    public long getAircraft_id() {
        return aircraft_id;
    }

    public void setAircraft_id(long aircraft_id) {
        this.aircraft_id = aircraft_id;
    }
}
