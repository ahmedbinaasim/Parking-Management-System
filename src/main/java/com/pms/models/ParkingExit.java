package com.pms.models;

import java.time.LocalDateTime;

public class ParkingExit {
    private final int sessionId;
    private final String receiptNumber;
    private final LocalDateTime exitTime;
    private final double charges;
    private final PaymentStatus paymentStatus;
    private String paymentMethod;
    private String transactionId;

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        WAIVED
    }

    // Constructor for initiating exit
    public ParkingExit(ParkingSession session) {
        this.sessionId = session.getHistoryId();
        this.receiptNumber = session.getReceiptNumber();
        this.exitTime = LocalDateTime.now();
        this.charges = session.getCharges();
        this.paymentStatus = PaymentStatus.PENDING;
    }

    // Constructor for database retrieval
    public ParkingExit(int sessionId, String receiptNumber, LocalDateTime exitTime,
                      double charges, PaymentStatus paymentStatus, 
                      String paymentMethod, String transactionId) {
        this.sessionId = sessionId;
        this.receiptNumber = receiptNumber;
        this.exitTime = exitTime;
        this.charges = charges;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
    }

    // Getters
    public int getSessionId() { return sessionId; }
    public String getReceiptNumber() { return receiptNumber; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getCharges() { return charges; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionId() { return transactionId; }

    // Setters for payment details
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    // Receipt validation
    public boolean validateReceipt(String inputReceipt) {
        return receiptNumber != null && receiptNumber.equals(inputReceipt);
    }

    // Payment processing result
    public static class PaymentResult {
        private final boolean success;
        private final String message;
        private final String transactionId;

        public PaymentResult(boolean success, String message, String transactionId) {
            this.success = success;
            this.message = message;
            this.transactionId = transactionId;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getTransactionId() { return transactionId; }
    }
}