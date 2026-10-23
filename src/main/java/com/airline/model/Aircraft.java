package com.airline.model;

public class Aircraft {
    public long aircraft_id;
    public String registrantion_number;
    private String aircraft_model;
    private String manufacturer;
    private long capacity;
    private String status;

    public Aircraft(long aircraft_id, String registrantion_number,
                    String aircraft_model, String manufacturer,
                    long capacity, String status) {
        this.aircraft_id = aircraft_id;
        this.registrantion_number = registrantion_number;
        this.aircraft_model = aircraft_model;
        this.manufacturer = manufacturer;
        this.capacity = capacity;
        this.status = status;
    }
    public Aircraft(String registrantion_number,
                    String aircraft_model, String manufacturer,
                    long capacity, String status) {
        this.registrantion_number = registrantion_number;
        this.aircraft_model = aircraft_model;
        this.manufacturer = manufacturer;
        this.capacity = capacity;
        this.status = status;
    }

    public long getAircraft_id() {
        return aircraft_id;
    }

    public void setAircraft_id(long aircraft_id) {
        this.aircraft_id = aircraft_id;
    }

    public String getRegistrantion_number() {
        return registrantion_number;
    }

    public void setRegistrantion_number(String registrantion_number) {
        this.registrantion_number = registrantion_number;
    }

    public String getAircraft_model() {
        return aircraft_model;
    }

    public void setAircraft_model(String aircraft_model) {
        this.aircraft_model = aircraft_model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
