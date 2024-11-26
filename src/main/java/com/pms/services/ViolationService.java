package com.pms.services;

import com.pms.dao.*;
import com.pms.models.*;
import com.pms.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ViolationService {
    private final ViolationDAO violationDAO;
    private final ParkingSessionDAO sessionDAO;
    private final UserDAO userDAO;
    private final NotificationService notificationService;

    public ViolationService() {
        this.violationDAO = new ViolationDAO();
        this.sessionDAO = new ParkingSessionDAO();
        this.userDAO = new UserDAO();
        this.notificationService = new NotificationService();

    }
// In ViolationService.java
public List<ParkingViolation> getAllViolations() {
    return violationDAO.getAllViolations();
}

public double getTotalFinesCollected() {
    String query = "SELECT COALESCE(SUM(fine_amount), 0.0) FROM ParkingViolations " +
                  "WHERE status = 'RESOLVED'";
    
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

public boolean resolveViolation(int violationId) {
    return violationDAO.updateViolationStatus(violationId, "RESOLVED");
}
    public ParkingResult reportViolation(int historyId, String violationType, 
                                       String description, double fineAmount) {
        ParkingSession session = sessionDAO.getSessionById(historyId);
        if (session == null) {
            return new ParkingResult(false, "Invalid parking session");
        }

        ParkingViolation violation = new ParkingViolation(
            0, historyId, session.getUserId(), violationType,
            description, fineAmount, "PENDING",
            LocalDateTime.now(), null, false, null
        );

        violation = violationDAO.reportViolation(violation);
        if (violation != null) {
            // Update session status
            session.setStatus(ParkingStatus.VIOLATION);
            sessionDAO.updateSession(session);

            // Send notification (implement notification service)
            notifyUserOfViolation(session.getUserId(), violation);

            return new ParkingResult(true, "Violation reported successfully", 
                                   violation.getViolationId());
        }

        return new ParkingResult(false, "Failed to report violation");
    }

    public ParkingResult disputeViolation(int violationId, String disputeReason) {
        ParkingViolation violation = violationDAO.getViolationById(violationId);
        if (violation == null) {
            return new ParkingResult(false, "Violation not found");
        }

        if (violation.isDisputed()) {
            return new ParkingResult(false, "Violation already disputed");
        }

        if (violationDAO.disputeViolation(violationId, disputeReason)) {
            // Notify admin of dispute
            notifyAdminOfDispute(violation, disputeReason);
            return new ParkingResult(true, "Dispute submitted successfully");
        }

        return new ParkingResult(false, "Failed to submit dispute");
    }

    public List<ParkingViolation> getUserViolations(int userId) {
        return violationDAO.getViolationsByUser(userId);
    }

    private void notifyUserOfViolation(int userId, ParkingViolation violation) {
        notificationService.sendViolationNotification(userId, violation);
    }
    
    private void notifyAdminOfDispute(ParkingViolation violation, String disputeReason) {
        notificationService.sendAdminNotification(
            "Violation Dispute",
            String.format(
                "Violation ID: %d\nUser ID: %d\nDispute Reason: %s",
                violation.getViolationId(),
                violation.getUserId(),
                disputeReason
            )
        );
    }
}