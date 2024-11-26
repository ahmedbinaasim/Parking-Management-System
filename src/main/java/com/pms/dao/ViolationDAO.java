package com.pms.dao;

import com.pms.models.*;
import com.pms.utils.DatabaseManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ViolationDAO {
    
    public ParkingViolation reportViolation(ParkingViolation violation) {
        String query = "INSERT INTO ParkingViolations (history_id, user_id, violation_type, " +
                      "description, fine_amount, status) VALUES (?, ?, ?, ?, ?, ?)";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, violation.getHistoryId());
            stmt.setInt(2, violation.getUserId());
            stmt.setString(3, violation.getViolationType());
            stmt.setString(4, violation.getDescription());
            stmt.setDouble(5, violation.getFineAmount());
            stmt.setString(6, violation.getStatus());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                violation.setViolationId(rs.getInt(1));
                return violation;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean disputeViolation(int violationId, String disputeReason) {
        String query = "UPDATE ParkingViolations SET disputed = true, dispute_reason = ? " +
                      "WHERE violation_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, disputeReason);
            stmt.setInt(2, violationId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ParkingViolation> getViolationsByUser(int userId) {
        String query = "SELECT * FROM ParkingViolations WHERE user_id = ?";
        
        List<ParkingViolation> violations = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                violations.add(mapResultSetToViolation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return violations;
    }

    private ParkingViolation mapResultSetToViolation(ResultSet rs) throws SQLException {
        return new ParkingViolation(
            rs.getInt("violation_id"),
            rs.getInt("history_id"),
            rs.getInt("user_id"),
            rs.getString("violation_type"),
            rs.getString("description"),
            rs.getDouble("fine_amount"),
            rs.getString("status"),
            rs.getTimestamp("reported_at").toLocalDateTime(),
            getLocalDateTimeFromRS(rs, "resolved_at"),
            rs.getBoolean("disputed"),
            rs.getString("dispute_reason")
        );
    }

    // In ViolationDAO.java
public List<ParkingViolation> getAllViolations() {
    String query = "SELECT * FROM ParkingViolations ORDER BY reported_at DESC";
    List<ParkingViolation> violations = new ArrayList<>();
    
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            violations.add(mapResultSetToViolation(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return violations;
}

public boolean updateViolationStatus(int violationId, String status) {
    String query = "UPDATE ParkingViolations SET status = ?, resolved_at = ? " +
                  "WHERE violation_id = ?";
    
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, status);
        stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        stmt.setInt(3, violationId);
        
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    public ParkingViolation getViolationById(int violationId) {
        String query = "SELECT * FROM ParkingViolations WHERE violation_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, violationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new ParkingViolation(
                    rs.getInt("violation_id"),
                    rs.getInt("history_id"),
                    rs.getInt("user_id"),
                    rs.getString("violation_type"),
                    rs.getString("description"),
                    rs.getDouble("fine_amount"),
                    rs.getString("status"),
                    rs.getTimestamp("reported_at").toLocalDateTime(),
                    getLocalDateTimeFromRS(rs, "resolved_at"),
                    rs.getBoolean("disputed"),
                    rs.getString("dispute_reason")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private LocalDateTime getLocalDateTimeFromRS(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}