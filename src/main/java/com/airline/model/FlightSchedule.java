package com.airline.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class FlightSchedule {


    private long shcedule_id;
    private String flight_number;
    private long route_id;
    private String days_of_operation;
    private LocalDate start_date;
    private LocalDate end_date;
    private String schedule_status;
    private LocalTime departure_time;
    private LocalTime arrival_time;




    public FlightSchedule(
            long shcedule_id,
            String flight_number,
            long route_id,
            String days_of_operation,
            LocalDate start_date,
            LocalDate end_date,
            String schedule_status,
            LocalTime departure_time,
            LocalTime arrival_time
    ) {
        this.shcedule_id = shcedule_id;
        this.flight_number = flight_number;
        this.route_id = route_id;
        this.departure_time = departure_time;
        this.arrival_time = arrival_time;
        this.days_of_operation = days_of_operation;
        this.start_date = start_date;
        this.schedule_status = schedule_status;
        this.end_date = end_date;
    }
    public FlightSchedule(
            String flight_number,
            long route_id,
            String days_of_operation,
            LocalDate start_date,
            LocalDate end_date,
            String schedule_status,
            LocalTime departure_time,
            LocalTime arrival_time
    ) {
        this.flight_number = flight_number;
        this.route_id = route_id;
        this.departure_time = departure_time;
        this.arrival_time = arrival_time;
        this.days_of_operation = days_of_operation;
        this.start_date = start_date;
        this.schedule_status = schedule_status;
        this.end_date = end_date;
    }

    public long getShcedule_id() {
        return shcedule_id;
    }

    public String getFlight_number() {
        return flight_number;
    }

    public long getRoute_id() {
        return route_id;
    }

    public LocalTime getDeparture_time() {
        return departure_time;
    }

    public LocalTime getArrival_time() {
        return arrival_time;
    }

    public String getDays_of_operation() {
        return days_of_operation;
    }

    public LocalDate getStart_date() {
        return start_date;
    }

    public LocalDate getEnd_date() {
        return end_date;
    }

    public String getSchedule_status() {
        return schedule_status;
    }

    public void setShcedule_id(long shcedule_id) {
        this.shcedule_id = shcedule_id;
    }

    public void setFlight_number(String flight_number) {
        this.flight_number = flight_number;
    }

    public void setRoute_id(long route_id) {
        this.route_id = route_id;
    }

    public void setDeparture_time(LocalTime departure_time) {
        this.departure_time = departure_time;
    }

    public void setArrival_time(LocalTime arrival_time) {
        this.arrival_time = arrival_time;
    }

    public void setDays_of_operation(String days_of_operation) {
        this.days_of_operation = days_of_operation;
    }

    public void setStart_date(LocalDate start_date) {
        this.start_date = start_date;
    }

    public void setEnd_date(LocalDate end_date) {
        this.end_date = end_date;
    }

    public void setSchedule_status(String schedule_status) {
        this.schedule_status = schedule_status;
    }
}
