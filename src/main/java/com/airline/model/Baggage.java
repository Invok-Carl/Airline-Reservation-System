package com.airline.model;

import java.math.BigDecimal;

public class Baggage {

    private long baggage_id;
    private String tag_number;
    private long segment_id;
    private BigDecimal weight_kg;
    private String status;

    public Baggage(String tag_number,
                   long segment_id,
                   BigDecimal weight_kg,
                   String status) {
        this.tag_number = tag_number;
        this.segment_id = segment_id;
        this.weight_kg = weight_kg;
        this.status = status;
    }

    public Baggage(long baggage_id,
                   String tag_number,
                   long segment_id,
                   BigDecimal weight_kg,
                   String status) {
        this.baggage_id = baggage_id;
        this.tag_number = tag_number;
        this.segment_id = segment_id;
        this.weight_kg = weight_kg;
        this.status = status;
    }

    public long getBaggage_id() {
        return baggage_id;
    }

    public void setBaggage_id(long baggage_id) {
        this.baggage_id = baggage_id;
    }

    public String getTag_number() {
        return tag_number;
    }

    public void setTag_number(String tag_number) {
        this.tag_number = tag_number;
    }

    public long getSegment_id() {
        return segment_id;
    }

    public void setSegment_id(long segment_id) {
        this.segment_id = segment_id;
    }

    public BigDecimal getWeight_kg() {
        return weight_kg;
    }

    public void setWeight_kg(BigDecimal weight_kg) {
        this.weight_kg = weight_kg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}