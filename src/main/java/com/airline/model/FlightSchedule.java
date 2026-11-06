package com.airline.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class FlightSchedule {


    private long shceduleId;
    private String flightNumber;
    private long routeId;
    private String daysOfOperation;
    private LocalDate startDate;
    private LocalDate endDate;
    private String scheduleStatus;
    private LocalTime departureTime;
    private LocalTime arrivalTime;




    public FlightSchedule(
            long shceduleId,
            String flightNumber,
            long routeId,
            String daysOfOperation,
            LocalDate startDate,
            LocalDate endDate,
            String scheduleStatus,
            LocalTime departureTime,
            LocalTime arrivalTime
    ) {
        this.shceduleId = shceduleId;
        this.flightNumber = flightNumber;
        this.routeId = routeId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.daysOfOperation = daysOfOperation;
        this.startDate = startDate;
        this.scheduleStatus = scheduleStatus;
        this.endDate = endDate;
    }
    public FlightSchedule(
            String flightNumber,
            long routeId,
            String daysOfOperation,
            LocalDate startDate,
            LocalDate endDate,
            String scheduleStatus,
            LocalTime departureTime,
            LocalTime arrivalTime
    ) {
        this.flightNumber = flightNumber;
        this.routeId = routeId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.daysOfOperation = daysOfOperation;
        this.startDate = startDate;
        this.scheduleStatus = scheduleStatus;
        this.endDate = endDate;
    }

    public long getShceduleId() {
        return shceduleId;
    }

    public void setShceduleId(long shceduleId) {
        this.shceduleId = shceduleId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public String getDaysOfOperation() {
        return daysOfOperation;
    }

    public void setDaysOfOperation(String daysOfOperation) {
        this.daysOfOperation = daysOfOperation;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getScheduleStatus() {
        return scheduleStatus;
    }

    public void setScheduleStatus(String scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }
}
