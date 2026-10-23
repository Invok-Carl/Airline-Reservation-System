package com.airline.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Flight {

    private long flight_id;
    private LocalDate flight_date;
    private String departure_airport;
    private String arrival_airport;
    private long aircraft_id;
    private LocalTime sched_departure;
    private LocalTime sched_arrival;
    private LocalTime actual_departure;
    private LocalTime actual_arrival;
    private String flight_status;
    private long schedule_id;


    public Flight(
            long flight_id, LocalDate flight_date, String departure_airport,
            String arrival_airport , long aircraft_id,
            LocalTime sched_departure,
            LocalTime sched_arrival, LocalTime actual_departure,
            LocalTime actual_arrival, String flight_status,
            long schedule_id
    ) {
        this.flight_id = flight_id;
        this.flight_date = flight_date;
        this.departure_airport = departure_airport;
        this.arrival_airport = arrival_airport;
        this.aircraft_id = aircraft_id;
        this.sched_departure = sched_departure;
        this.sched_arrival = sched_arrival;
        this.actual_departure = actual_departure;
        this.actual_arrival = actual_arrival;
        this.flight_status = flight_status;
        this.schedule_id = schedule_id;
    }
    public Flight(
            LocalDate flight_date,
            long aircraft_id,
            LocalTime actual_departure,
            LocalTime actual_arrival,
            String flight_status,
            Long schedule_id
    ) {
        this.flight_date = flight_date;
        this.aircraft_id = aircraft_id;
        this.actual_departure = actual_departure;
        this.actual_arrival = actual_arrival;
        this.flight_status = flight_status;
        this.schedule_id = schedule_id;
    }


    public long getFlight_id() {
        return flight_id;
    }

    public void setFlight_id(long flight_id) {
        this.flight_id = flight_id;
    }

    public LocalDate getFlight_date() {
        return flight_date;
    }

    public void setFlight_date(LocalDate flight_date) {
        this.flight_date = flight_date;
    }

    public String getDeparture_airport() {
        return departure_airport;
    }

    public void setDeparture_airport(String departure_airport) {
        this.departure_airport = departure_airport;
    }

    public String getArrival_airport() {
        return arrival_airport;
    }

    public void setArrival_airport(String arrival_airport) {
        this.arrival_airport = arrival_airport;
    }

    public long getAircraft_id() {
        return aircraft_id;
    }

    public void setAircraft_id(long aircraft_id) {
        this.aircraft_id = aircraft_id;
    }

    public LocalTime getSched_departure() {
        return sched_departure;
    }

    public void setSched_departure(LocalTime sched_departure) {
        this.sched_departure = sched_departure;
    }

    public LocalTime getSched_arrival() {
        return sched_arrival;
    }

    public void setSched_arrival(LocalTime sched_arrival) {
        this.sched_arrival = sched_arrival;
    }

    public LocalTime getActual_departure() {
        return actual_departure;
    }

    public void setActual_departure(LocalTime actual_departure) {
        this.actual_departure = actual_departure;
    }

    public LocalTime getActual_arrival() {
        return actual_arrival;
    }

    public void setActual_arrival(LocalTime actual_arrival) {
        this.actual_arrival = actual_arrival;
    }

    public String getFlight_status() {
        return flight_status;
    }

    public void setFlight_status(String flight_status) {
        this.flight_status = flight_status;
    }

    public long getSchedule_id() {
        return schedule_id;
    }

    public void setSchedule_id(long schedule_id) {
        this.schedule_id = schedule_id;
    }
}
