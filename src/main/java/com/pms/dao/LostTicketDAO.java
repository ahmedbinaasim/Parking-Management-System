package com.pms.dao;

import com.pms.models.LostTicket;
import com.pms.utils.DatabaseManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LostTicketDAO {
    
    public LostTicket reportLostTicket(LostTicket ticket) {
        String query = "INSERT INTO LostTickets (session_id, user_id, reported_time, fine, status) " +
                      "VALUES (?, ?, ?, ?, ?)";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, ticket.getSessionId());
            stmt.setInt(2, ticket.getUserId());
            stmt.setTimestamp(3, Timestamp.valueOf(ticket.getReportedTime()));
            stmt.setDouble(4, ticket.getFine());
            stmt.setString(5, ticket.getStatus());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                ticket.setLostTicketId(rs.getInt(1));
                return ticket;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifyLostTicket(int lostTicketId, String verificationMethod) {
        String query = "UPDATE LostTickets SET verified = true, verification_method = ?, " +
                      "status = 'VERIFIED' WHERE lost_ticket_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, verificationMethod);
            stmt.setInt(2, lostTicketId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resolveLostTicket(int lostTicketId) {
        String query = "UPDATE LostTickets SET resolved_time = ?, status = 'RESOLVED' " +
                      "WHERE lost_ticket_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, lostTicketId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public LostTicket getLostTicketById(int lostTicketId) {
        String query = "SELECT * FROM LostTickets WHERE lost_ticket_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, lostTicketId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToLostTicket(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private LostTicket mapResultSetToLostTicket(ResultSet rs) throws SQLException {
        return new LostTicket(
            rs.getInt("lost_ticket_id"),
            rs.getInt("session_id"),
            rs.getInt("user_id"),
            rs.getTimestamp("reported_time").toLocalDateTime(),
            rs.getDouble("fine"),
            rs.getBoolean("verified"),
            rs.getString("verification_method"),
            getLocalDateTimeFromRS(rs, "resolved_time"),
            rs.getString("status")
        );
    }

    private LocalDateTime getLocalDateTimeFromRS(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}