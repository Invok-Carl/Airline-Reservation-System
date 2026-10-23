package com.airline.model;

import java.time.LocalDateTime;

public class Ticket {

    private long ticketId;
    private String ticketNumber;
    private long segmentId;
    private LocalDateTime issueDate;
    private String status;

    public Ticket(String ticketNumber, long segmentId,
                  LocalDateTime issueDate, String status) {
        this.ticketNumber = ticketNumber;
        this.segmentId = segmentId;
        this.issueDate = issueDate;
        this.status = status;
    }

    public Ticket(long ticketId, String ticketNumber, long segmentId,
                  LocalDateTime issueDate, String status) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.segmentId = segmentId;
        this.issueDate = issueDate;
        this.status = status;
    }

    public long getTicketId() {
        return ticketId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public long getSegmentId() {
        return segmentId;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setTicketId(long ticketId) {
        this.ticketId = ticketId;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public void setSegmentId(long segmentId) {
        this.segmentId = segmentId;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}