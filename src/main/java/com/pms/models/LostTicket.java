package com.pms.models;

import java.time.LocalDateTime;

public class LostTicket {
    private int lostTicketId;
    private int sessionId;
    private int userId;
    private LocalDateTime reportedTime;
    private double fine;
    private boolean verified;
    private String verificationMethod;
    private LocalDateTime resolvedTime;
    private String status;

    // Constructor for reporting lost ticket
    public LostTicket(int sessionId, int userId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.reportedTime = LocalDateTime.now();
        this.fine = calculateFine();
        this.status = "PENDING";
    }

    // Constructor for database retrieval
    public LostTicket(int lostTicketId, int sessionId, int userId, 
                     LocalDateTime reportedTime, double fine, boolean verified,
                     String verificationMethod, LocalDateTime resolvedTime, 
                     String status) {
        this.lostTicketId = lostTicketId;
        this.sessionId = sessionId;
        this.userId = userId;
        this.reportedTime = reportedTime;
        this.fine = fine;
        this.verified = verified;
        this.verificationMethod = verificationMethod;
        this.resolvedTime = resolvedTime;
        this.status = status;
    }

    private double calculateFine() {
        // Standard fine for lost ticket - can be made configurable
        return 50.0;
    }

    // Getters and setters
    public int getLostTicketId() { return lostTicketId; }
    public void setLostTicketId(int lostTicketId) { this.lostTicketId = lostTicketId; }
    public int getSessionId() { return sessionId; }
    public int getUserId() { return userId; }
    public LocalDateTime getReportedTime() { return reportedTime; }
    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String method) { this.verificationMethod = method; }
    public LocalDateTime getResolvedTime() { return resolvedTime; }
    public void setResolvedTime(LocalDateTime time) { this.resolvedTime = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}