package com.pms.services;

import com.pms.dao.UserDAO;
import com.pms.models.*;
import com.pms.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final UserDAO userDAO;
    private static final String EMAIL_SENDER = "noreply@parkingsystem.com";
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public NotificationService() {
        this.userDAO = new UserDAO();
    }

    public void sendEmail(int userId, String subject, String message) {
        User user = userDAO.getUserById(userId);
        if (user != null && user.getEmail() != null) {
            sendEmail(user.getEmail(), subject, message);
        }
    }

    public void sendParkingEntryNotification(int userId, String spotNumber, String receiptNumber) {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            String subject = "Parking Entry Confirmation";
            String message = String.format(
                "Your vehicle has been parked successfully.\n" +
                "Spot Number: %s\n" +
                "Receipt Number: %s\n" +
                "Entry Time: %s",
                spotNumber, receiptNumber, LocalDateTime.now()
            );
            
            sendEmail(user.getEmail(), subject, message);
            sendSMS(user.getPhoneNumber(), "Your vehicle has been parked in spot " + spotNumber);
        }
    }

    public void sendParkingExitNotification(int userId, double charges, String receiptNumber) {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            String subject = "Parking Exit Confirmation";
            String message = String.format(
                "Thank you for using our parking service.\n" +
                "Receipt Number: %s\n" +
                "Total Charges: $%.2f\n" +
                "Exit Time: %s",
                receiptNumber, charges, LocalDateTime.now()
            );
            
            sendEmail(user.getEmail(), subject, message);
            sendSMS(user.getPhoneNumber(), 
                String.format("Parking charges: $%.2f. Thank you for using our service.", charges));
        }
    }

    public void sendViolationNotification(int userId, ParkingViolation violation) {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            String subject = "Parking Violation Notice";
            String message = String.format(
                "A parking violation has been recorded.\n" +
                "Violation Type: %s\n" +
                "Description: %s\n" +
                "Fine Amount: $%.2f\n" +
                "Please address this violation as soon as possible.",
                violation.getViolationType(),
                violation.getDescription(),
                violation.getFineAmount()
            );
            
            sendEmail(user.getEmail(), subject, message);
            sendSMS(user.getPhoneNumber(), 
                "Parking violation recorded. Fine amount: $" + violation.getFineAmount());
        }
    }

    public void sendReservationConfirmation(int userId, ParkingReservation reservation) {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            String subject = "Parking Reservation Confirmation";
            String message = String.format(
                "Your parking reservation has been confirmed.\n" +
                "Spot Number: %s\n" +
                "Start Time: %s\n" +
                "End Time: %s\n" +
                "Reservation ID: %d",
                reservation.getSpotNumber(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getReservationId()
            );
            
            sendEmail(user.getEmail(), subject, message);
            sendSMS(user.getPhoneNumber(), 
                "Parking reserved: " + reservation.getSpotNumber() + 
                " from " + reservation.getStartTime().toLocalTime());
        }
    }

    public void sendCarWashConfirmation(int userId, CarWashRequest request) {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            String subject = "Car Wash Service Confirmation";
            String message = String.format(
                "Your car wash service has been scheduled.\n" +
                "Request ID: %d\n" +
                "Scheduled Time: %s\n" +
                "Charges: $%.2f",
                request.getRequestId(),
                request.getScheduledTime(),
                request.getCharges()
            );
            
            sendEmail(user.getEmail(), subject, message);
            sendSMS(user.getPhoneNumber(), 
                "Car wash service scheduled for " + request.getScheduledTime().toLocalTime());
        }
    }

    public void sendAdminNotification(String subject, String message) {
        List<User> admins = userDAO.getUsersByRole("ADMIN");
        for (User admin : admins) {
            sendEmail(admin.getEmail(), subject, message);
        }
    }

    void sendEmail(String email, String subject, String message) {
        // In a real implementation, this would use JavaMail or similar
        System.out.println("Sending email to: " + email);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
    }

    private void sendSMS(String phoneNumber, String message) {
        // In a real implementation, this would use Twilio or similar
        System.out.println("Sending SMS to: " + phoneNumber);
        System.out.println("Message: " + message);
    }

    public List<String> getRecentNotifications(int userId) {
        List<String> notifications = new ArrayList<>();
        
        String query = "SELECT * FROM (" +
            // Parking sessions
            "SELECT 'SESSION' as type, check_in as timestamp, " +
            "CONCAT('Parking session started for spot ', spot_number) as message " +
            "FROM ParkingHistory ph " +
            "JOIN ParkingSpots ps ON ph.spot_id = ps.spot_id " +
            "WHERE user_id = ? AND status = 'ACTIVE' " +
            "UNION ALL " +
            // Feedback acknowledgments
            "SELECT 'FEEDBACK' as type, acknowledged_at as timestamp, " +
            "'Your feedback has been acknowledged' as message " +
            "FROM Feedback " +
            "WHERE user_id = ? AND status = 'ACKNOWLEDGED' " +
            "UNION ALL " +
            // Violations
            "SELECT 'VIOLATION' as type, reported_at as timestamp, " +
            "CONCAT('Violation reported: ', violation_type) as message " +
            "FROM ParkingViolations " +
            "WHERE user_id = ? AND status = 'PENDING' " +
            ") notifications " +
            "ORDER BY timestamp DESC LIMIT 5";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                String formattedTime = timestamp.format(FORMATTER);
                String message = rs.getString("message");
                
                notifications.add(formattedTime + " - " + message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Add a fallback notification in case of error
            notifications.add("Unable to load recent notifications");
        }
        
        return notifications;
    }


}