package com.pms.models;

import java.time.LocalDateTime;

public class ParkingSession {
    private int historyId;
    private int userId;
    private String vehicleNumber;
    private String spotNumber;
    private Integer reservationId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private ParkingStatus status;
    private String receiptNumber;
    private String cardNumber;
    private String plateNumber;
    private double charges;
    private int duration; // in minutes

    // Constructor for creating new session
    public ParkingSession(int userId, String vehicleNumber, String spotNumber, 
                         String cardNumber, String plateNumber) {
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.spotNumber = spotNumber;
        this.cardNumber = cardNumber;
        this.plateNumber = plateNumber;
        this.checkIn = LocalDateTime.now();
        this.status = ParkingStatus.ACTIVE;
        this.receiptNumber = generateReceiptNumber();
    }

    // Constructor for database retrieval
    public ParkingSession(int historyId, int userId, String vehicleNumber, 
                         String spotNumber, Integer reservationId, LocalDateTime checkIn,
                         LocalDateTime checkOut, ParkingStatus status, String receiptNumber,
                         String cardNumber, String plateNumber, double charges, int duration) {
        this.historyId = historyId;
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.spotNumber = spotNumber;
        this.reservationId = reservationId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.receiptNumber = receiptNumber;
        this.cardNumber = cardNumber;
        this.plateNumber = plateNumber;
        this.charges = charges;
        this.duration = duration;
    }

    private String generateReceiptNumber() {
        return "RCP" + System.currentTimeMillis() + "-" + userId;
    }

    // Getters and setters
    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }
    public int getUserId() { return userId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getSpotNumber() { return spotNumber; }
    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }
    public ParkingStatus getStatus() { return status; }
    public void setStatus(ParkingStatus status) { this.status = status; }
    public String getReceiptNumber() { return receiptNumber; }
    public String getCardNumber() { return cardNumber; }
    public String getPlateNumber() { return plateNumber; }
    public double getCharges() { return charges; }
    public void setCharges(double charges) { this.charges = charges; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    
    // Utility methods
    public boolean isActive() {
        return status == ParkingStatus.ACTIVE;
    }

    public boolean hasValidReceipt() {
        return receiptNumber != null && !receiptNumber.isEmpty();
    }

    public void endSession() {
        this.checkOut = LocalDateTime.now();
        this.status = ParkingStatus.COMPLETED;
        this.duration = calculateDuration();
        this.charges = calculateCharges();
    }

    private int calculateDuration() {
        if (checkOut == null) return 0;
        return (int) java.time.Duration.between(checkIn, checkOut).toMinutes();
    }

    public double calculateCharges() {
        // Basic rate calculation - can be made more complex based on requirements
        int hours = (int) Math.ceil(duration / 60.0);
        return hours * 2.0; // $2 per hour
    }
}
