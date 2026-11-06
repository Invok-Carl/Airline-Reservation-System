package com.airline.model;

import java.math.BigDecimal;

public class Baggage {

    private long baggageId;
    private String tagNumber;
    private long segmentId;
    private BigDecimal weightKg;
    private String status;

    public Baggage(String tagNumber,
                   long segmentId,
                   BigDecimal weightKg,
                   String status) {
        this.tagNumber = tagNumber;
        this.segmentId = segmentId;
        this.weightKg = weightKg;
        this.status = status;
    }

    public Baggage(long baggageId,
                   String tagNumber,
                   long segmentId,
                   BigDecimal weightKg,
                   String status) {
        this.baggageId = baggageId;
        this.tagNumber = tagNumber;
        this.segmentId = segmentId;
        this.weightKg = weightKg;
        this.status = status;
    }

    public long getBaggageId() {
        return baggageId;
    }

    public void setBaggageId(long baggageId) {
        this.baggageId = baggageId;
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public long getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(long segmentId) {
        this.segmentId = segmentId;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}