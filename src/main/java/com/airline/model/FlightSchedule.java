package com.airline.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

public class FlightSchedule {
    private long shcedule_id;
    private String flight_number;
    private long route_id;
    private String departure_time;
    private String arrival_time;
    private Set<DayOfWeek> days_of_operation;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private String schedule_status;

    public FlightSchedule(
            long shcedule_id, String flight_number, long route_id, String departure_time,
            String arrival_time, Set<DayOfWeek> days_of_operation, LocalDateTime start_date,
            String schedule_status, LocalDateTime end_date
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
            String flight_number, long route_id, String departure_time,
            String arrival_time, Set<DayOfWeek> days_of_operation, LocalDateTime start_date,
            String schedule_status, LocalDateTime end_date
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

    public String getDeparture_time() {
        return departure_time;
    }

    public String getArrival_time() {
        return arrival_time;
    }

    public Set<DayOfWeek> getDays_of_operation() {
        return days_of_operation;
    }

    public LocalDateTime getStart_date() {
        return start_date;
    }

    public LocalDateTime getEnd_date() {
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

    public void setDeparture_time(String departure_time) {
        this.departure_time = departure_time;
    }

    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }

    public void setDays_of_operation(Set<DayOfWeek> days_of_operation) {
        this.days_of_operation = days_of_operation;
    }

    public void setStart_date(LocalDateTime start_date) {
        this.start_date = start_date;
    }

    public void setEnd_date(LocalDateTime end_date) {
        this.end_date = end_date;
    }

    public void setSchedule_status(String schedule_status) {
        this.schedule_status = schedule_status;
    }
}
