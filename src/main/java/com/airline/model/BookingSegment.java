package com.airline.model;

public class BookingSegment {
    private long segmentId;
    private long reservationId;
    private long passengerId;
    private long flightId;
    private long seatId;
    private long fareId;
    public BookingSegment(){}
    public BookingSegment(long segmentId, long reservationId,
                              long passengerId, long flightId,
                              long seatId, long fareId) {
        this.segmentId = segmentId;
        this.reservationId = reservationId;
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.seatId = seatId;
        this.fareId = fareId;}
    public BookingSegment(long reservationId,
                              long passengerId, long flightId,
                              long seatId, long fareId) {
        this.reservationId = reservationId;
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.seatId = seatId;
        this.fareId = fareId;
    }

    public long getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(long segmentId) {
        this.segmentId = segmentId;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(long passengerId) {
        this.passengerId = passengerId;
    }

    public long getFlightId() {
        return flightId;
    }

    public void setFlightId(long flightId) {
        this.flightId = flightId;
    }

    public long getSeatId() {
        return seatId;
    }

    public void setSeatId(long seatId) {
        this.seatId = seatId;
    }

    public long getFareId() {
        return fareId;
    }

    public void setFareId(long fareId) {
        this.fareId = fareId;
    }
}
