// CarWashRequest.java
package com.pms.models;

import java.time.LocalDateTime;

public class CarWashRequest {
    private int requestId;
    private int historyId;
    private int userId;
    private String status;
    private LocalDateTime requestedTime;
    private LocalDateTime scheduledTime;
    private LocalDateTime completedTime;
    private double charges;
    private String vehicleNumber;

    // Constructor for new request
    public CarWashRequest(int userId, int historyId, String vehicleNumber, LocalDateTime scheduledTime) {
        this.userId = userId;
        this.historyId = historyId;
        this.vehicleNumber = vehicleNumber;
        this.scheduledTime = scheduledTime;
        this.requestedTime = LocalDateTime.now();
        this.status = "PENDING";
        this.charges = 25.0; // Standard charge
    }

    // Constructor for database retrieval
    public CarWashRequest(int requestId, int historyId, int userId, String status,
                         LocalDateTime requestedTime, LocalDateTime scheduledTime,
                         LocalDateTime completedTime, double charges, String vehicleNumber) {
        this.requestId = requestId;
        this.historyId = historyId;
        this.userId = userId;
        this.status = status;
        this.requestedTime = requestedTime;
        this.scheduledTime = scheduledTime;
        this.completedTime = completedTime;
        this.charges = charges;
        this.vehicleNumber = vehicleNumber;
    }

    // Getters and setters
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public int getHistoryId() { return historyId; }
    public int getUserId() { return userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestedTime() { return requestedTime; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    public double getCharges() { return charges; }
    public void setCharges(double charges) { this.charges = charges; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
}