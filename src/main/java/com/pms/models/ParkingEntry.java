package com.pms.models;

import java.time.LocalDateTime;

public class ParkingEntry {
    private int historyId;
    private int userId;
    private String vehicleNumber;
    private String spotNumber;
    private Integer reservationId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private ParkingStatus status;
    private double charges;
    private String receiptNumber;
    private String cardNumber;
    private String plateNumber;
    private Integer duration;

    // Constructor for new entry
    public ParkingEntry(int userId, String vehicleNumber, String spotNumber, LocalDateTime checkIn) {
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.spotNumber = spotNumber;
        this.checkIn = checkIn;
        this.status = ParkingStatus.ACTIVE;
        this.receiptNumber = generateReceiptNumber();
    }

    // Constructor for database retrieval
    public ParkingEntry(int historyId, int userId, String vehicleNumber, String spotNumber, 
                       Integer reservationId, LocalDateTime checkIn, LocalDateTime checkOut, 
                       ParkingStatus status, double charges, String receiptNumber,
                       String cardNumber, String plateNumber, Integer duration) {
        this.historyId = historyId;
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.spotNumber = spotNumber;
        this.reservationId = reservationId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.charges = charges;
        this.receiptNumber = receiptNumber;
        this.cardNumber = cardNumber;
        this.plateNumber = plateNumber;
        this.duration = duration;
    }

    private String generateReceiptNumber() {
        return "RCP" + System.currentTimeMillis();
    }

    // Existing getters and setters
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
    public double getCharges() { return charges; }
    public void setCharges(double charges) { this.charges = charges; }

    // New getters and setters
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}