package com.airline.model;

public class Route {

    private long routeId;
    private long originAirportId;
    private long destAirportId;
    private double distanceKm;
    private int estDurationMin;
    private String routeStatus;

    // Constructor for creating a new route
    public Route(
            long originAirportId,
            long destAirportId,
            double distanceKm,
            int estDurationMin,
            String routeStatus) {

        this.originAirportId = originAirportId;
        this.destAirportId = destAirportId;
        this.distanceKm = distanceKm;
        this.estDurationMin = estDurationMin;
        this.routeStatus = routeStatus;
    }

    // Constructor for an existing route retrieved from the database
    public Route(
            long routeId,
            long originAirportId,
            long destAirportId,
            double distanceKm,
            int estDurationMin,
            String routeStatus) {

        this.routeId = routeId;
        this.originAirportId = originAirportId;
        this.destAirportId = destAirportId;
        this.distanceKm = distanceKm;
        this.estDurationMin = estDurationMin;
        this.routeStatus = routeStatus;
    }

    public long getRouteId() {
        return routeId;
    }

    public long getOriginAirportId() {
        return originAirportId;
    }

    public void setOriginAirportId(long originAirportId) {
        this.originAirportId = originAirportId;
    }

    public long getDestAirportId() {
        return destAirportId;
    }

    public void setDestAirportId(long destAirportId) {
        this.destAirportId = destAirportId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public int getEstDurationMin() {
        return estDurationMin;
    }

    public void setEstDurationMin(int estDurationMin) {
        this.estDurationMin = estDurationMin;
    }

    public String getRouteStatus() {
        return routeStatus;
    }

    public void setRouteStatus(String routeStatus) {
        this.routeStatus = routeStatus;
    }
}