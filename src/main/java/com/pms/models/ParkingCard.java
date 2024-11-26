package com.pms.models;

import java.time.LocalDateTime;

public class ParkingCard {
    private int cardId;
    private int userId;
    private String cardNumber;
    private String status;
    private LocalDateTime issuedDate;
    private LocalDateTime expiryDate;

    public ParkingCard(int cardId, int userId, String cardNumber, String status, 
                      LocalDateTime issuedDate, LocalDateTime expiryDate) {
        this.cardId = cardId;
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.status = status;
        this.issuedDate = issuedDate;
        this.expiryDate = expiryDate;
    }

    // Getters and setters
    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDateTime issuedDate) { this.issuedDate = issuedDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
}