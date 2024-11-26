package com.pms.dao;

import com.pms.models.CarWashRequest;
import com.pms.utils.DatabaseManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CarWashRequestDAO {
    
    public CarWashRequest createRequest(CarWashRequest request) {
        String query = "INSERT INTO CarWashRequests (history_id, user_id, status, " +
                      "scheduled_time, vehicle_number, charges, requested_time) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, request.getHistoryId());
            stmt.setInt(2, request.getUserId());
            stmt.setString(3, request.getStatus());
            setNullableTimestamp(stmt, 4, request.getScheduledTime());
            stmt.setString(5, request.getVehicleNumber());
            stmt.setDouble(6, request.getCharges());
            stmt.setTimestamp(7, Timestamp.valueOf(request.getRequestedTime()));
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                request.setRequestId(rs.getInt(1));
                return request;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateRequestStatus(int requestId, String status) {
        String query = "UPDATE CarWashRequests SET status = ?, " +
                      "completed_time = ? WHERE request_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            if (status.equals("COMPLETED")) {
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            stmt.setInt(3, requestId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public CarWashRequest getRequestById(int requestId) {
        String query = "SELECT * FROM CarWashRequests WHERE request_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRequest(rs);  // Using the existing mapper method
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
     

    public List<CarWashRequest> getPendingRequests() {
        String query = "SELECT * FROM CarWashRequests WHERE status = 'PENDING'";
        
        List<CarWashRequest> requests = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    private CarWashRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        return new CarWashRequest(
            rs.getInt("request_id"),
            rs.getInt("history_id"),
            rs.getInt("user_id"),
            rs.getString("status"),
            rs.getTimestamp("requested_time").toLocalDateTime(),
            getLocalDateTimeFromRS(rs, "scheduled_time"),
            getLocalDateTimeFromRS(rs, "completed_time"),
            rs.getDouble("charges"),
            rs.getString("vehicle_number")
        );
    }

    private void setNullableTimestamp(PreparedStatement stmt, int parameterIndex, LocalDateTime value) throws SQLException {
        if (value != null) {
            stmt.setTimestamp(parameterIndex, Timestamp.valueOf(value));
        } else {
            stmt.setNull(parameterIndex, Types.TIMESTAMP);
        }
    }

    private LocalDateTime getLocalDateTimeFromRS(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}