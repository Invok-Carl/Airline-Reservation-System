package com.airline.model;
public class Airport {

    private long airportId;
    private String airportCode;
    private String airportName;
    private String city;
    private String country;
    private String airportTimeZone;

    // Constructor for creating a new airport
    public Airport(
            String airportCode,
            String airportName,
            String city,
            String country,
            String airportTimeZone) {

        this.airportCode = airportCode;
        this.airportName = airportName;
        this.city = city;
        this.country = country;
        this.airportTimeZone = airportTimeZone;
    }

    // Constructor for an existing airport retrieved from the database
    public Airport(
            long airportId,
            String airportCode,
            String airportName,
            String city,
            String country,
            String airportTimeZone) {

        this.airportId = airportId;
        this.airportCode = airportCode;
        this.airportName = airportName;
        this.city = city;
        this.country = country;
        this.airportTimeZone = airportTimeZone;
    }

    public long getAirportId() {
        return airportId;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAirportTimeZone() {
        return airportTimeZone;
    }

    public void setAirportTimeZone(String airportTimeZone) {
        this.airportTimeZone = airportTimeZone;
    }
}