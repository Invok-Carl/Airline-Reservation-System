package com.airline.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Flight {
    private long flight_id;
    private String flight_number;
    private LocalDate flight_date;
    private long aircraft_id;
    private LocalDateTime sched_departure;
    private LocalDateTime sched_arrival;
    private LocalDateTime actual_departure;
    private LocalDateTime actual_arrival;
    private String flight_status;
    private long route_id;
    private long schedule_id;

    public Flight(
            long flight_id, String flight_number, LocalDate flight_date,
            long aircraft_id, LocalDateTime sched_departure,
            LocalDateTime sched_arrival, LocalDateTime actual_departure,
            LocalDateTime actual_arrival, String flight_status, long route_id,
            long schedule_id
    ) {
        this.flight_id = flight_id;
        this.flight_number = flight_number;
        this.flight_date = flight_date;
        this.aircraft_id = aircraft_id;
        this.sched_departure = sched_departure;
        this.sched_arrival = sched_arrival;
        this.actual_departure = actual_departure;
        this.actual_arrival = actual_arrival;
        this.flight_status = flight_status;
        this.route_id = route_id;
        this.schedule_id = schedule_id;
    }
    public Flight(
            String flight_number, LocalDate flight_date,long aircraft_id,
            LocalDateTime sched_departure,
            LocalDateTime sched_arrival, LocalDateTime actual_departure,
            LocalDateTime actual_arrival, String flight_status, long route_id,
            long schedule_id
    ) {
        this.flight_number = flight_number;
        this.flight_date = flight_date;
        this.aircraft_id = aircraft_id;
        this.sched_departure = sched_departure;
        this.sched_arrival = sched_arrival;
        this.actual_departure = actual_departure;
        this.actual_arrival = actual_arrival;
        this.flight_status = flight_status;
        this.route_id = route_id;
        this.schedule_id = schedule_id;
    }

    public long getFlight_id() {
        return flight_id;
    }

    public String getFlight_number() {
        return flight_number;
    }

    public LocalDate getFlight_date() {
        return flight_date;
    }

    public long getAircraft_id() {
        return aircraft_id;
    }

    public LocalDateTime getSched_arrival() {
        return sched_arrival;
    }

    public LocalDateTime getSched_departure() {
        return sched_departure;
    }

    public LocalDateTime getActual_departure() {
        return actual_departure;
    }

    public LocalDateTime getActual_arrival() {
        return actual_arrival;
    }

    public String getFlight_status() {
        return flight_status;
    }

    public long getRoute_id() {
        return route_id;
    }

    public long getSchedule_id(){
        return schedule_id;
    }

    public void setFlight_id(long flight_id) {
        this.flight_id = flight_id;
    }

    public void setFlight_number(String flight_number) {
        this.flight_number = flight_number;
    }

    public void setFlight_date(LocalDate flight_date) {
        this.flight_date = flight_date;
    }

    public void setAircraft_id(long aircraft_id) {
        this.aircraft_id = aircraft_id;
    }

    public void setSched_departure(LocalDateTime sched_departure) {
        this.sched_departure = sched_departure;
    }

    public void setSched_arrival(LocalDateTime sched_arrival) {
        this.sched_arrival = sched_arrival;
    }

    public void setActual_departure(LocalDateTime actual_departure) {
        this.actual_departure = actual_departure;
    }

    public void setActaul_arrival(LocalDateTime actual_arrival) {
        this.actual_arrival = actual_arrival;
    }

    public void setFlight_status(String flight_Status) {
        this.flight_status = flight_Status;
    }

    public void setRoute_id(long route_id) {
        this.route_id = route_id;
    }
    public void setSchedule_id(long schedule_id){
        this.schedule_id = schedule_id;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flight_id=" + flight_id +
                ", flight_number='" + flight_number + '\'' +
                ", flight_date=" + flight_date +
                ", aircraft_id=" + aircraft_id +
                ", sched_departure=" + sched_departure +
                ", sched_arrival=" + sched_arrival +
                ", actual_departure=" + actual_departure +
                ", actaul_arrival=" + actual_arrival +
                ", flight_status='" + flight_status + '\'' +
                ", route_id=" + route_id +
                ", schedule_id=" + schedule_id +
                '}';
    }
}
