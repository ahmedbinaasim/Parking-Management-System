package com.pms.dao;

import com.pms.models.*;
import com.pms.utils.DatabaseManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpotDAO {
    
    public ParkingSpot createSpot(ParkingSpot spot) {
        String query = "INSERT INTO ParkingSpots (spot_number, floor_number, type, status) " +
                      "VALUES (?, ?, ?, ?)";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, spot.getSpotNumber());
            stmt.setInt(2, spot.getFloorNumber());
            stmt.setString(3, spot.getType());
            stmt.setString(4, spot.getStatus().toString());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                spot.setSpotId(rs.getInt(1));
                return spot;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ParkingSpot> getAllSpots() {
        String query = "SELECT * FROM ParkingSpots";
        List<ParkingSpot> spots = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                spots.add(mapResultSetToSpot(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spots;
    }

    public List<ParkingSpot> getAvailableSpots() {
        String query = "SELECT * FROM ParkingSpots WHERE status = 'AVAILABLE'";
        List<ParkingSpot> spots = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                spots.add(mapResultSetToSpot(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spots;
    }

    public List<ParkingSpot> getSpotsByFloor(int floorNumber) {
        String query = "SELECT * FROM ParkingSpots WHERE floor_number = ?";
        List<ParkingSpot> spots = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, floorNumber);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                spots.add(mapResultSetToSpot(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spots;
    }

    public boolean updateSpotStatus(String spotNumber, SpotStatus status) {
        String query = "UPDATE ParkingSpots SET status = ? WHERE spot_number = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status.toString());
            stmt.setString(2, spotNumber);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setSpotReservation(String spotNumber, Integer userId) {
        String query = "UPDATE ParkingSpots SET reserved_by = ? WHERE spot_number = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, spotNumber);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getAvailableSpotCount() {
        String query = "SELECT COUNT(*) FROM ParkingSpots WHERE status = 'AVAILABLE'";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ParkingSpot getSpotByNumber(String spotNumber) {
        String query = "SELECT * FROM ParkingSpots WHERE spot_number = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, spotNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSpot(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ParkingSpot mapResultSetToSpot(ResultSet rs) throws SQLException {
        return new ParkingSpot(
            rs.getInt("spot_id"),
            rs.getString("spot_number"),
            SpotStatus.valueOf(rs.getString("status")),
            rs.getInt("floor_number"),
            rs.getString("type"),
            getIntegerFromRS(rs, "reserved_by")
        );
    }

    // In ParkingSpotDAO.java
public int getTotalSpotCount() {
    String query = "SELECT COUNT(*) FROM ParkingSpots";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}


    public boolean isSpotAvailableForTimeRange(String spotNumber, LocalDateTime startTime, LocalDateTime endTime) {
    String query = "SELECT COUNT(*) FROM ParkingHistory ph " +
                  "JOIN ParkingSpots ps ON ph.spot_id = ps.spot_id " +
                  "WHERE ps.spot_number = ? AND ph.status = 'ACTIVE' " +
                  "AND ((check_in BETWEEN ? AND ?) OR " +
                  "(check_out BETWEEN ? AND ?) OR " +
                  "(check_in <= ? AND (check_out IS NULL OR check_out >= ?)))";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, spotNumber);
        stmt.setTimestamp(2, Timestamp.valueOf(startTime));
        stmt.setTimestamp(3, Timestamp.valueOf(endTime));
        stmt.setTimestamp(4, Timestamp.valueOf(startTime));
        stmt.setTimestamp(5, Timestamp.valueOf(endTime));
        stmt.setTimestamp(6, Timestamp.valueOf(startTime));
        stmt.setTimestamp(7, Timestamp.valueOf(endTime));

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            // If count is 0, spot is available
            return rs.getInt(1) == 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;  // In case of error, assume spot is not available
}
    private Integer getIntegerFromRS(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}