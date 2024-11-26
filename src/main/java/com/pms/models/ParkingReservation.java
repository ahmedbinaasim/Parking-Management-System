// ParkingReservation.java
package com.pms.models;

import java.time.LocalDateTime;

public class ParkingReservation {
    private int reservationId;
    private int userId;
    private String spotNumber;
    private String vehicleNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private ReservationStatus status;
    private int extensionCount;
    private double charges;       // Added field for reservation charges

    // Constructor for new reservation
    public ParkingReservation(int userId, String spotNumber, String vehicleNumber, 
                             LocalDateTime startTime, LocalDateTime endTime) {
        this.userId = userId;
        this.spotNumber = spotNumber;
        this.vehicleNumber = vehicleNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
        this.extensionCount = 0;
    }

    // Constructor for database retrieval
    public ParkingReservation(int reservationId, int userId, String spotNumber, 
                             String vehicleNumber, LocalDateTime startTime, 
                             LocalDateTime endTime, LocalDateTime createdAt, 
                             LocalDateTime cancelledAt, String cancellationReason, 
                             ReservationStatus status, int extensionCount,
                             double charges) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.spotNumber = spotNumber;
        this.vehicleNumber = vehicleNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.status = status;
        this.extensionCount = extensionCount;
        this.charges = charges;
    }

    // Existing getters and setters
    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
    public int getUserId() { return userId; }
    public String getSpotNumber() { return spotNumber; }
    public String getVehicleNumber() { return vehicleNumber; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String reason) { this.cancellationReason = reason; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public int getExtensionCount() { return extensionCount; }
    public void incrementExtensionCount() { this.extensionCount++; }

    // New getter and setter for charges
    public double getCharges() { return charges; }
    public void setCharges(double charges) { this.charges = charges; }
}
