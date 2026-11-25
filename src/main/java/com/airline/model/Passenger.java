package com.airline.model;

import java.time.LocalDate;

public class Passenger {

    private Long passengerId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationality;
    private String phone;
    private String email;
    private String address;
    private String passportNumber;
    private LocalDate passportExpiry;
    private String passportCountry;
    private long createdBy;

    // Constructor
    public Passenger(Long passengerId, String firstName, String lastName,
                     LocalDate dateOfBirth, String gender, String nationality,
                     String phone, String email, String address,
                     String passportNumber, LocalDate passportExpiry,
                     String passportCountry, long createdBy) {

        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationality = nationality;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.passportNumber = passportNumber;
        this.passportExpiry = passportExpiry;
        this.passportCountry = passportCountry;
        this.createdBy = createdBy;
    }

    // Getters
    public long getCreatedBy(){ return createdBy; }
    public void setCreatedBy(long createdBy){ this.createdBy = createdBy; }

    public Long getPassengerId() {
        return passengerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getNationality() {
        return nationality;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public LocalDate getPassportExpiry() {
        return passportExpiry;
    }

    public String getPassportCountry() {
        return passportCountry;
    }

    // Setters

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void setPassportExpiry(LocalDate passportExpiry) {
        this.passportExpiry = passportExpiry;
    }

    public void setPassportCountry(String passportCountry) {
        this.passportCountry = passportCountry;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "passengerId=" + passengerId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", nationality='" + nationality + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", passportNumber='" + passportNumber + '\'' +
                ", passportExpiry=" + passportExpiry +
                ", passportCountry='" + passportCountry + '\'' +
                '}';
    }
}
