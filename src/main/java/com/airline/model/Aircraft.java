package com.airline.model;

public class Aircraft {
    public long aircraftId;
    public String registrationNumber;
    private String aircraftModel;
    private String manufacturer;
    private long capacity;
    private String status;

    public Aircraft(long aircraftId, String registrationNumber,
                    String aircraftModel, String manufacturer,
                    long capacity, String status) {
        this.aircraftId = aircraftId;
        this.registrationNumber = registrationNumber;
        this.aircraftModel = aircraftModel;
        this.manufacturer = manufacturer;
        this.capacity = capacity;
        this.status = status;
    }
    public Aircraft(String registrationNumber,
                    String aircraftModel, String manufacturer,
                    long capacity, String status) {
        this.registrationNumber = registrationNumber;
        this.aircraftModel = aircraftModel;
        this.manufacturer = manufacturer;
        this.capacity = capacity;
        this.status = status;
    }

    public long getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(long aircraftId) {
        this.aircraftId = aircraftId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrantionNumber) {
        this.registrationNumber = registrantionNumber;
    }

    public String getAircraftModel() {
        return aircraftModel;
    }

    public void setAircraftModel(String aircraftModel) {
        this.aircraftModel = aircraftModel;
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
