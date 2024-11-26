package com.pms.dao;

import com.pms.models.Feedback;
import com.pms.utils.DatabaseManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    
    public Feedback submitFeedback(Feedback feedback) {
        String query = "INSERT INTO Feedback (user_id, feedback_type, content, rating, status) " +
                      "VALUES (?, ?, ?, ?, ?)";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, 
                 Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, feedback.getUserId());
            stmt.setString(2, feedback.getFeedbackType());
            stmt.setString(3, feedback.getContent());
            setNullableInt(stmt, 4, feedback.getRating());
            stmt.setString(5, feedback.getStatus());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                feedback.setFeedbackId(rs.getInt(1));
                return feedback;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Feedback> getUserFeedback(int userId) {
        String query = "SELECT * FROM Feedback WHERE user_id = ? ORDER BY created_at DESC";
        
        List<Feedback> feedbackList = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                feedbackList.add(mapResultSetToFeedback(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbackList;
    }

    public List<Feedback> getPendingFeedback() {
        String query = "SELECT * FROM Feedback WHERE status = 'PENDING' " +
                      "ORDER BY created_at ASC";
        
        List<Feedback> feedbackList = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                feedbackList.add(mapResultSetToFeedback(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbackList;
    }

    public boolean acknowledgeFeedback(int feedbackId, int adminId) {
        String query = "UPDATE Feedback SET status = 'ACKNOWLEDGED', " +
                      "acknowledged_at = ?, acknowledged_by = ? " +
                      "WHERE feedback_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, adminId);
            stmt.setInt(3, feedbackId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Feedback getFeedbackById(int feedbackId) {
        String query = "SELECT * FROM Feedback WHERE feedback_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, feedbackId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToFeedback(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        return new Feedback(
            rs.getInt("feedback_id"),
            rs.getInt("user_id"),
            rs.getString("feedback_type"),
            rs.getString("content"),
            getIntegerFromRS(rs, "rating"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            getLocalDateTimeFromRS(rs, "acknowledged_at"),
            getIntegerFromRS(rs, "acknowledged_by")
        );
    }

    private void setNullableInt(PreparedStatement stmt, int parameterIndex, 
                               Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(parameterIndex, value);
        } else {
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
    }

    private Integer getIntegerFromRS(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime getLocalDateTimeFromRS(ResultSet rs, String columnName) 
        throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}