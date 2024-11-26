package com.pms.models;

import java.time.LocalDateTime;

public class ParkingViolation {
    private int violationId;
    private int historyId;
    private int userId;
    private String violationType;
    private String description;
    private double fineAmount;
    private String status;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private boolean disputed;
    private String disputeReason;

    public ParkingViolation(int violationId, int historyId, int userId, String violationType,
                           String description, double fineAmount, String status,
                           LocalDateTime reportedAt, LocalDateTime resolvedAt,
                           boolean disputed, String disputeReason) {
        this.violationId = violationId;
        this.historyId = historyId;
        this.userId = userId;
        this.violationType = violationType;
        this.description = description;
        this.fineAmount = fineAmount;
        this.status = status;
        this.reportedAt = reportedAt;
        this.resolvedAt = resolvedAt;
        this.disputed = disputed;
        this.disputeReason = disputeReason;
    }

    // Getters and setters
    public int getViolationId() { return violationId; }
    public void setViolationId(int violationId) { this.violationId = violationId; }
    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public boolean isDisputed() { return disputed; }
    public void setDisputed(boolean disputed) { this.disputed = disputed; }
    public String getDisputeReason() { return disputeReason; }
    public void setDisputeReason(String disputeReason) { this.disputeReason = disputeReason; }
}
