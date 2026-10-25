package com.airline.model;

import java.time.LocalDateTime;

public class PassengerAccount {

    private long accountId;
    private long passengerId;
    private String email;
    private String passwordHash;
    private String accountStatus;
    private LocalDateTime createdAt;

    public PassengerAccount(long accountId, long passengerId,
                            String email, String passwordHash,
                            String accountStatus, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.passengerId = passengerId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }
    public PassengerAccount(long passengerId,
                            String email, String passwordHash,
                            String accountStatus, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.passengerId = passengerId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public PassengerAccount() {
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(long passengerId) {
        this.passengerId = passengerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}