package com.airline.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Flight {

    private long flightId;
    private LocalDate flightDate;
    private String departureAirport;
    private String arrivalAirport;
    private long aircraftId;
    private LocalTime schedDeparture;
    private LocalTime schedArrival;
    private LocalTime actualDeparture;
    private LocalTime actualArrival;
    private String flightStatus;
    private long scheduleId;


    public Flight(
            long flightId, LocalDate flightDate, String departureAirport,
            String arrivalAirport , long aircraftId,
            LocalTime schedDeparture,
            LocalTime schedArrival, LocalTime actualDeparture,
            LocalTime actualArrival, String flightStatus,
            long scheduleId
    ) {
        this.flightId = flightId;
        this.flightDate = flightDate;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.aircraftId = aircraftId;
        this.schedDeparture = schedDeparture;
        this.schedArrival = schedArrival;
        this.actualDeparture = actualDeparture;
        this.actualArrival = actualArrival;
        this.flightStatus = flightStatus;
        this.scheduleId = scheduleId;
    }
    public Flight(
            LocalDate flightDate,
            long aircraftId,
            LocalTime actualDeparture,
            LocalTime actualArrival,
            String flightStatus,
            Long schedule_id
    ) {
        this.flightDate = flightDate;
        this.aircraftId = aircraftId;
        this.actualDeparture = actualDeparture;
        this.actualArrival = actualArrival;
        this.flightStatus = flightStatus;
        this.scheduleId = scheduleId;
    }

    public long getFlightId() {
        return flightId;
    }

    public void setFlightId(long flightId) {
        this.flightId = flightId;
    }

    public LocalDate getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(LocalDate flightDate) {
        this.flightDate = flightDate;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public long getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(long aircraftId) {
        this.aircraftId = aircraftId;
    }

    public LocalTime getSchedDeparture() {
        return schedDeparture;
    }

    public void setSchedDeparture(LocalTime schedDeparture) {
        this.schedDeparture = schedDeparture;
    }

    public LocalTime getSchedArrival() {
        return schedArrival;
    }

    public void setSchedArrival(LocalTime schedArrival) {
        this.schedArrival = schedArrival;
    }

    public LocalTime getActualDeparture() {
        return actualDeparture;
    }

    public void setActualDeparture(LocalTime actualDeparture) {
        this.actualDeparture = actualDeparture;
    }

    public String getFlightStatus() {
        return flightStatus;
    }

    public void setFlightStatus(String flightStatus) {
        this.flightStatus = flightStatus;
    }

    public LocalTime getActualArrival() {
        return actualArrival;
    }

    public void setActualArrival(LocalTime actualArrival) {
        this.actualArrival = actualArrival;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }
}
