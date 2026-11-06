package com.airline.model;

public class Aircraft {
    public long aircraftId;
    public String registrantionNumber;
    private String aircraftModel;
    private String manufacturer;
    private long capacity;
    private String status;

    public Aircraft(long aircraftId, String registrantionNumber,
                    String aircraftModel, String manufacturer,
                    long capacity, String status) {
        this.aircraftId = aircraftId;
        this.registrantionNumber = registrantionNumber;
        this.aircraftModel = aircraftModel;
        this.manufacturer = manufacturer;
        this.capacity = capacity;
        this.status = status;
    }
    public Aircraft(String registrantionNumber,
                    String aircraftModel, String manufacturer,
                    long capacity, String status) {
        this.registrantionNumber = registrantionNumber;
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

    public String getRegistrantionNumber() {
        return registrantionNumber;
    }

    public void setRegistrantionNumber(String registrantionNumber) {
        this.registrantionNumber = registrantionNumber;
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
