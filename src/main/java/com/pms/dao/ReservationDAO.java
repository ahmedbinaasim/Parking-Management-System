package com.pms.dao;

import com.pms.models.*;
import com.pms.utils.DatabaseManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    
    public ParkingReservation createReservation(ParkingReservation reservation) {
        String query = "INSERT INTO Reservations (user_id, spot_id, vehicle_number, " +
                      "reservation_start, reservation_end, status) " +
                      "SELECT ?, ps.spot_id, ?, ?, ?, ? " +
                      "FROM ParkingSpots ps WHERE ps.spot_number = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, reservation.getUserId());
            stmt.setString(2, reservation.getVehicleNumber());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(reservation.getEndTime()));
            stmt.setString(5, reservation.getStatus().toString());
            stmt.setString(6, reservation.getSpotNumber());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                reservation.setReservationId(rs.getInt(1));
                return reservation;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean cancelReservation(int reservationId, String reason) {
        String query = "UPDATE Reservations SET status = 'CANCELLED', " +
                      "cancelled_at = ?, cancellation_reason = ? " +
                      "WHERE reservation_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, reason);
            stmt.setInt(3, reservationId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean extendReservation(int reservationId, LocalDateTime newEndTime) {
        String query = "UPDATE Reservations SET reservation_end = ?, status = 'EXTENDED', " +
                      "extension_count = extension_count + 1 " +
                      "WHERE reservation_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(newEndTime));
            stmt.setInt(2, reservationId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ParkingReservation> getUserReservations(int userId) {
        String query = "SELECT r.*, ps.spot_number FROM Reservations r " +
                      "JOIN ParkingSpots ps ON r.spot_id = ps.spot_id " +
                      "WHERE r.user_id = ? ORDER BY r.reservation_start DESC";
                      
        List<ParkingReservation> reservations = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    public ParkingReservation getActiveReservation(int userId) {
        String query = "SELECT r.*, ps.spot_number FROM Reservations r " +
                      "JOIN ParkingSpots ps ON r.spot_id = ps.spot_id " +
                      "WHERE r.user_id = ? AND r.status IN ('PENDING', 'ACTIVE') " +
                      "AND r.reservation_start <= ? AND r.reservation_end >= ? " +
                      "ORDER BY r.reservation_start ASC LIMIT 1";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            LocalDateTime now = LocalDateTime.now();
            stmt.setInt(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(now));
            stmt.setTimestamp(3, Timestamp.valueOf(now));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isSpotAvailable(String spotNumber, LocalDateTime startTime, LocalDateTime endTime) {
        String query = "SELECT COUNT(*) FROM Reservations r " +
                      "JOIN ParkingSpots ps ON r.spot_id = ps.spot_id " +
                      "WHERE ps.spot_number = ? AND r.status IN ('PENDING', 'ACTIVE', 'EXTENDED') " +
                      "AND ((r.reservation_start BETWEEN ? AND ?) OR " +
                      "(r.reservation_end BETWEEN ? AND ?) OR " +
                      "(r.reservation_start <= ? AND r.reservation_end >= ?))";
                      
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
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ParkingReservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        return new ParkingReservation(
            rs.getInt("reservation_id"),
            rs.getInt("user_id"),
            rs.getString("spot_number"),
            rs.getString("vehicle_number"),
            rs.getTimestamp("reservation_start").toLocalDateTime(),
            rs.getTimestamp("reservation_end").toLocalDateTime(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            getLocalDateTimeFromRS(rs, "cancelled_at"),
            rs.getString("cancellation_reason"),
            ReservationStatus.valueOf(rs.getString("status")),
            rs.getInt("extension_count"),
            rs.getDouble("charges")
        );
    }

    private LocalDateTime getLocalDateTimeFromRS(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }


    public ParkingReservation getReservationById(int reservationId) {
        String query = "SELECT r.*, ps.spot_number FROM Reservations r " +
                      "JOIN ParkingSpots ps ON r.spot_id = ps.spot_id " +
                      "WHERE r.reservation_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new ParkingReservation(
                    rs.getInt("reservation_id"),
                    rs.getInt("user_id"),
                    rs.getString("spot_number"),
                    rs.getString("vehicle_number"),
                    rs.getTimestamp("reservation_start").toLocalDateTime(),
                    rs.getTimestamp("reservation_end").toLocalDateTime(),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    getLocalDateTimeFromRS(rs, "cancelled_at"),
                    rs.getString("cancellation_reason"),
                    ReservationStatus.valueOf(rs.getString("status")),
                    rs.getInt("extension_count"),
                    rs.getDouble("charges")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
