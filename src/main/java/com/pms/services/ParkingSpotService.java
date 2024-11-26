package com.pms.services;

import com.pms.dao.ParkingSpotDAO;
import com.pms.models.ParkingSpot;
import com.pms.models.SpotStatus;
import com.pms.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ParkingSpotService {
    private final ParkingSpotDAO spotDAO;

    public ParkingSpotService() {
        this.spotDAO = new ParkingSpotDAO();
    }

    public List<ParkingSpot> getAvailableSpots(int floor, String type, 
                                              LocalDateTime startTime, LocalDateTime endTime) {
        List<ParkingSpot> allSpots = spotDAO.getSpotsByFloor(floor);
        
        return allSpots.stream()
            .filter(spot -> isSpotAvailable(spot, type, startTime, endTime))
            .collect(Collectors.toList());
    }

    private boolean isSpotAvailable(ParkingSpot spot, String type, 
                                  LocalDateTime startTime, LocalDateTime endTime) {
        // Check if spot matches type
        if (!spot.getType().equalsIgnoreCase(type)) {
            return false;
        }

        // Check if spot is available
        if (spot.getStatus() != SpotStatus.AVAILABLE) {
            return false;
        }

        // Check if spot is not reserved during the requested time
        return spotDAO.isSpotAvailableForTimeRange(spot.getSpotNumber(), startTime, endTime);
    }

    public List<ParkingSpot> getSpotsByFloor(int floor) {
        return spotDAO.getSpotsByFloor(floor);
    }

    public ParkingSpot getSpotByNumber(String spotNumber) {
        return spotDAO.getSpotByNumber(spotNumber);
    }

    public boolean updateSpotStatus(String spotNumber, SpotStatus status) {
        return spotDAO.updateSpotStatus(spotNumber, status);
    }

    public boolean setSpotReservation(String spotNumber, Integer userId) {
        return spotDAO.setSpotReservation(spotNumber, userId);
    }

    public int getAvailableSpotCount() {
        String query = "SELECT COUNT(*) FROM ParkingSpots " +
                      "WHERE status = 'AVAILABLE' AND " +
                      "reserved_by IS NULL";
        
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
}