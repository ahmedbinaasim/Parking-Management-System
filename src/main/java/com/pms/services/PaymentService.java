package com.pms.services;

import com.pms.dao.UserDAO;
import com.pms.models.*;
import java.time.LocalDateTime;

public class PaymentService {
    private final NotificationService notificationService;
    private final UserDAO userDAO;
    

    public PaymentService() {
        this.notificationService = new NotificationService();
        this.userDAO = new UserDAO();
    }

    public PaymentResult processParkingPayment(ParkingSession session) {
        try {
            double amount = session.getCharges();
            String transactionId = generateTransactionId();
            
            PaymentResult result = processPayment(amount, transactionId);
            if (result.isSuccess()) {
                notificationService.sendParkingExitNotification(
                    session.getUserId(), 
                    amount, 
                    session.getReceiptNumber()
                );
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new PaymentResult(false, "Payment processing failed", null);
        }
    }

    public PaymentResult processViolationPayment(ParkingViolation violation) {
        try {
            double amount = violation.getFineAmount();
            String transactionId = generateTransactionId();
            
            PaymentResult result = processPayment(amount, transactionId);
            if (result.isSuccess()) {
                User user = userDAO.getUserById(violation.getUserId());
                if (user != null) {
                    notificationService.sendEmail(
                        user.getEmail(),  // Now passing the actual email address
                        "Violation Payment Confirmation",
                        String.format("Your violation payment of $%.2f has been processed.", amount)
                    );
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new PaymentResult(false, "Violation payment processing failed", null);
        }
    }

    public PaymentResult processReservationPayment(ParkingReservation reservation) {
        try {
            double amount = reservation.getCharges();
            String transactionId = generateTransactionId();
            
            PaymentResult result = processPayment(amount, transactionId);
            if (result.isSuccess()) {
                notificationService.sendReservationConfirmation(
                    reservation.getUserId(), 
                    reservation
                );
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new PaymentResult(false, "Reservation payment processing failed", null);
        }
    }

    public PaymentResult processCarWashPayment(CarWashRequest request) {
        try {
            double amount = request.getCharges();
            String transactionId = generateTransactionId();
            
            PaymentResult result = processPayment(amount, transactionId);
            if (result.isSuccess()) {
                notificationService.sendCarWashConfirmation(
                    request.getUserId(), 
                    request
                );
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new PaymentResult(false, "Car wash payment processing failed", null);
        }
    }

    private PaymentResult processPayment(double amount, String transactionId) {
        // In a real implementation, this would integrate with a payment gateway
        // For now, we'll simulate successful payment
        boolean success = true; // In real implementation, this would depend on payment gateway response
        
        if (success) {
            return new PaymentResult(true, "Payment processed successfully", transactionId);
        } else {
            return new PaymentResult(false, "Payment processing failed", null);
        }
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + 
               String.format("%04d", (int)(Math.random() * 10000));
    }

    // Inner class for payment results
    public static class PaymentResult {
        private final boolean success;
        private final String message;
        private final String transactionId;
        private final LocalDateTime timestamp;

        public PaymentResult(boolean success, String message, String transactionId) {
            this.success = success;
            this.message = message;
            this.transactionId = transactionId;
            this.timestamp = LocalDateTime.now();
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getTransactionId() { return transactionId; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}