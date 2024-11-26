package com.pms.services;

import com.pms.dao.*;
import com.pms.models.*;
import com.pms.utils.DatabaseManager;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// ParkingSessionService.java
public class ParkingSessionService {
    private final ParkingSessionDAO sessionDAO;
    private final ParkingCardDAO cardDAO;
    private final ReservationDAO reservationDAO;
    private final ParkingSpotDAO spotDAO;
    private final NotificationService notificationService;

    public ParkingSessionService() {
        this.sessionDAO = new ParkingSessionDAO();
        this.cardDAO = new ParkingCardDAO();
        this.reservationDAO = new ReservationDAO();
        this.spotDAO = new ParkingSpotDAO();
        this.notificationService = new NotificationService();
    }

    public ParkingResult validateParkingCard(String cardNumber) {
        ParkingCard card = cardDAO.getCardByNumber(cardNumber);
        
        if (card == null) {
            return new ParkingResult(false, "Invalid parking card");
        }

        if (!card.getStatus().equals("ACTIVE")) {
            return new ParkingResult(false, "Card is not active");
        }

        if (card.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new ParkingResult(false, "Card has expired");
        }

        return new ParkingResult(true, "Card validated successfully", card);
    }

    public ParkingReservation getActiveReservation(int userId) {
        return reservationDAO.getActiveReservation(userId);
    }

    // In ParkingSessionService.java
public ParkingResult startSession(String cardNumber, String vehicleNumber, 
String plateNumber, Integer reservationId) {
// Validate card again
ParkingCard card = cardDAO.getCardByNumber(cardNumber);
if (card == null || !card.getStatus().equals("ACTIVE")) {
return new ParkingResult(false, "Invalid or inactive card");
}

// Check for active session
List<ParkingSession> activeSessions = sessionDAO.getActiveSessionsByUser(card.getUserId());
if (!activeSessions.isEmpty()) {
return new ParkingResult(false, "User already has an active parking session");
}

// Find spot if no reservation
String spotNumber;
if (reservationId != null) {
ParkingReservation reservation = reservationDAO.getReservationById(reservationId);
if (reservation == null) {
return new ParkingResult(false, "Invalid reservation");
}
spotNumber = reservation.getSpotNumber();
} else {
// Get available spot
List<ParkingSpot> availableSpots = spotDAO.getAvailableSpots();
if (availableSpots.isEmpty()) {
return new ParkingResult(false, "No parking spots available");
}
spotNumber = availableSpots.get(0).getSpotNumber();
}

// Create new session using the correct constructor
ParkingSession session = new ParkingSession(
card.getUserId(),
vehicleNumber,
spotNumber,
cardNumber,
plateNumber
);

// Set reservation ID if exists
if (reservationId != null) {
session.setReservationId(reservationId);
}

// Record session
session = sessionDAO.createSession(session);
if (session != null) {
// Update spot status
spotDAO.updateSpotStatus(session.getSpotNumber(), SpotStatus.OCCUPIED);

// Send notification
notificationService.sendParkingEntryNotification(
card.getUserId(),
session.getSpotNumber(),
session.getReceiptNumber()
);

return new ParkingResult(true, "Session started successfully", 
session.getReceiptNumber());
}

return new ParkingResult(false, "Failed to start parking session");
}

    public ParkingResult endSession(String receiptNumber) {
        ParkingSession session = sessionDAO.getSessionByReceipt(receiptNumber);
        
        if (session == null) {
            return new ParkingResult(false, "Invalid receipt number");
        }

        if (!session.isActive()) {
            return new ParkingResult(false, "Session is not active");
        }

        // Calculate charges and end session
        session.endSession();
        
        if (sessionDAO.updateSession(session)) {
            // Free up the spot
            spotDAO.updateSpotStatus(session.getSpotNumber(), SpotStatus.AVAILABLE);
            
            // Send notification
            notificationService.sendParkingExitNotification(
                session.getUserId(),
                session.getCharges(),
                session.getReceiptNumber()
            );

            return new ParkingResult(true, "Session ended successfully", session);
        }

        return new ParkingResult(false, "Failed to end parking session");
    }

    public int getActiveSessionCount() {
        return sessionDAO.getActiveSessionCount();
    }

    public double getTotalRevenue() {
        return sessionDAO.calculateTotalRevenue();
    }

    public ParkingSession getSessionByReceipt(String receiptNumber) {
        return sessionDAO.getSessionByReceipt(receiptNumber);
    }

    public ParkingSession getActiveSession(int userId) {
        List<ParkingSession> activeSessions = sessionDAO.getActiveSessionsByUser(userId);
        return activeSessions.isEmpty() ? null : activeSessions.get(0);
    }

    // In ParkingSessionService.java
public List<ParkingSession> getUserParkingHistory(int userId, LocalDateTime fromDate, 
                                                LocalDateTime toDate) {
    return sessionDAO.getUserParkingHistory(userId, fromDate, toDate);
}


public double getTotalReservationRevenue() {
    String query = "SELECT COALESCE(SUM(charges), 0.0) FROM Reservations " +
                  "WHERE status IN ('COMPLETED', 'ACTIVE')";
    
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getDouble(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0.0;
}

public boolean exportParkingHistory(int userId, String filePath) {
    try {
        List<ParkingSession> sessions = sessionDAO.getUserParkingHistory(userId, null, null);
        return exportToCSV(sessions, filePath);
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

private boolean exportToCSV(List<ParkingSession> sessions, String filePath) {
    try (FileWriter writer = new FileWriter(filePath)) {
        // Write CSV header
        writer.write("Date,Spot,Vehicle,Duration,Status,Charges,Receipt Number\n");

        // Write session data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ParkingSession session : sessions) {
            String line = String.format("%s,%s,%s,%s,%s,%.2f,%s\n",
                session.getCheckIn().format(formatter),
                session.getSpotNumber(),
                session.getVehicleNumber(),
                formatDuration(session.getDuration()),
                session.getStatus(),
                session.getCharges(),
                session.getReceiptNumber()
            );
            writer.write(line);
        }
        return true;
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
}

private String formatDuration(int minutes) {
    int hours = minutes / 60;
    int mins = minutes % 60;
    return String.format("%dh %dm", hours, mins);
}
}