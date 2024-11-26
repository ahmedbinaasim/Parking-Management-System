package com.pms.dao;

import com.pms.models.*;
import com.pms.utils.DatabaseManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParkingSessionDAO {
    
    public ParkingSession createSession(ParkingSession session) {
        String query = "INSERT INTO ParkingHistory (user_id, spot_id, vehicle_number, check_in, " +
                      "card_number, plate_number, status, receipt_number, reservation_id) " +
                      "SELECT ?, ps.spot_id, ?, ?, ?, ?, ?, ?, ? " +
                      "FROM ParkingSpots ps WHERE ps.spot_number = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, session.getVehicleNumber());
            stmt.setTimestamp(3, Timestamp.valueOf(session.getCheckIn()));
            stmt.setString(4, session.getCardNumber());
            stmt.setString(5, session.getPlateNumber());
            stmt.setString(6, session.getStatus().toString());
            stmt.setString(7, session.getReceiptNumber());
            setNullableInt(stmt, 8, session.getReservationId());
            stmt.setString(9, session.getSpotNumber());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                session.setHistoryId(rs.getInt(1));
                return session;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateSession(ParkingSession session) {
        String query = "UPDATE ParkingHistory SET check_out = ?, status = ?, " +
                      "charges = ?, duration = ? WHERE history_id = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(session.getCheckOut()));
            stmt.setString(2, session.getStatus().toString());
            stmt.setDouble(3, session.getCharges());
            stmt.setInt(4, session.getDuration());
            stmt.setInt(5, session.getHistoryId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ParkingSession getSessionByReceipt(String receiptNumber) {
        String query = "SELECT ph.*, ps.spot_number FROM ParkingHistory ph " +
                      "JOIN ParkingSpots ps ON ph.spot_id = ps.spot_id " +
                      "WHERE ph.receipt_number = ?";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, receiptNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSession(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ParkingSession> getActiveSessionsByUser(int userId) {
        String query = "SELECT ph.*, ps.spot_number FROM ParkingHistory ph " +
                      "JOIN ParkingSpots ps ON ph.spot_id = ps.spot_id " +
                      "WHERE ph.user_id = ? AND ph.status = 'ACTIVE'";
                      
        List<ParkingSession> sessions = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    private ParkingSession mapResultSetToSession(ResultSet rs) throws SQLException {
        return new ParkingSession(
            rs.getInt("history_id"),
            rs.getInt("user_id"),
            rs.getString("vehicle_number"),
            rs.getString("spot_number"),
            getIntegerFromRS(rs, "reservation_id"),
            rs.getTimestamp("check_in").toLocalDateTime(),
            getLocalDateTimeFromRS(rs, "check_out"),
            ParkingStatus.valueOf(rs.getString("status")),
            rs.getString("receipt_number"),
            rs.getString("card_number"),
            rs.getString("plate_number"),
            rs.getDouble("charges"),
            rs.getInt("duration")
        );
    }

    private LocalDateTime getLocalDateTimeFromRS(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private Integer getIntegerFromRS(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private void setNullableInt(PreparedStatement stmt, int parameterIndex, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(parameterIndex, value);
        } else {
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
    }

public ParkingSession getActiveSession(int userId) {
   String query = "SELECT ph.*, ps.spot_number " +
                 "FROM ParkingHistory ph " +
                 "JOIN ParkingSpots ps ON ph.spot_id = ps.spot_id " +
                 "WHERE ph.user_id = ? " +
                 "AND ph.status = 'ACTIVE' " +
                 "AND ph.check_out IS NULL " +
                 "ORDER BY ph.check_in DESC LIMIT 1";  // Gets most recent active session if multiple exist

   try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

       stmt.setInt(1, userId);
       ResultSet rs = stmt.executeQuery();

       if (rs.next()) {
           return new ParkingSession(
               rs.getInt("history_id"),
               rs.getInt("user_id"),
               rs.getString("vehicle_number"),
               rs.getString("spot_number"),
               getIntegerFromRS(rs, "reservation_id"),
               rs.getTimestamp("check_in").toLocalDateTime(),
               getLocalDateTimeFromRS(rs, "check_out"),
               ParkingStatus.valueOf(rs.getString("status")),
               rs.getString("receipt_number"),
               rs.getString("card_number"),
               rs.getString("plate_number"),
               rs.getDouble("charges"),
               rs.getInt("duration")
           );
       }
   } catch (SQLException e) {
       e.printStackTrace();
   }
   return null;
}

    // In ParkingSessionDAO.java
public int getActiveSessionCount() {
    String query = "SELECT COUNT(*) FROM ParkingHistory WHERE status = 'ACTIVE'";
    
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

public boolean updateSessionEndTime(int sessionId, LocalDateTime endTime, 
                                  double charges, String status) {
    String query = "UPDATE ParkingHistory SET check_out = ?, charges = ?, " +
                  "status = ? WHERE history_id = ?";
    
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setTimestamp(1, Timestamp.valueOf(endTime));
        stmt.setDouble(2, charges);
        stmt.setString(3, status);
        stmt.setInt(4, sessionId);
        
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

public double calculateTotalRevenue() {
    String query = "SELECT SUM(charges) FROM ParkingHistory " +
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
    public ParkingSession getSessionById(int historyId) {
        String query = "SELECT ph.*, ps.spot_number " +
                      "FROM ParkingHistory ph " +
                      "JOIN ParkingSpots ps ON ph.spot_id = ps.spot_id " +
                      "WHERE ph.history_id = ?";
    
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, historyId);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return new ParkingSession(
                    rs.getInt("history_id"),
                    rs.getInt("user_id"),
                    rs.getString("vehicle_number"),
                    rs.getString("spot_number"),
                    getIntegerFromRS(rs, "reservation_id"),
                    rs.getTimestamp("check_in").toLocalDateTime(),
                    getLocalDateTimeFromRS(rs, "check_out"),
                    ParkingStatus.valueOf(rs.getString("status")),
                    rs.getString("receipt_number"),
                    rs.getString("card_number"),
                    rs.getString("plate_number"),
                    rs.getDouble("charges"),
                    rs.getInt("duration")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ParkingSession getActiveSessionByVehicle(int userId, String vehicleNumber) {
        String query = "SELECT ph.*, ps.spot_number " +
                      "FROM ParkingHistory ph " +
                      "JOIN ParkingSpots ps ON ph.spot_id = ps.spot_id " +
                      "WHERE ph.user_id = ? " +
                      "AND ph.vehicle_number = ? " +
                      "AND ph.status = 'ACTIVE' " +
                      "AND ph.check_out IS NULL";
    
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, userId);
            stmt.setString(2, vehicleNumber);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return new ParkingSession(
                    rs.getInt("history_id"),
                    rs.getInt("user_id"),
                    rs.getString("vehicle_number"),
                    rs.getString("spot_number"),
                    getIntegerFromRS(rs, "reservation_id"),
                    rs.getTimestamp("check_in").toLocalDateTime(),
                    getLocalDateTimeFromRS(rs, "check_out"),
                    ParkingStatus.valueOf(rs.getString("status")),
                    rs.getString("receipt_number"),
                    rs.getString("card_number"),
                    rs.getString("plate_number"),
                    rs.getDouble("charges"),
                    rs.getInt("duration")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // In ParkingSessionDAO.java
public List<ParkingSession> getUserParkingHistory(int userId, LocalDateTime fromDate, 
LocalDateTime toDate) {
StringBuilder query = new StringBuilder(
"SELECT ph.*, ps.spot_number FROM ParkingHistory ph " +
"JOIN ParkingSpots ps ON ph.spot_id = ps.spot_id " +
"WHERE ph.user_id = ?"
);

// Add date filters if provided
if (fromDate != null) {
query.append(" AND ph.check_in >= ?");
}
if (toDate != null) {
query.append(" AND ph.check_in <= ?");
}

query.append(" ORDER BY ph.check_in DESC");

List<ParkingSession> sessions = new ArrayList<>();
try (Connection conn = DatabaseManager.getConnection();
PreparedStatement stmt = conn.prepareStatement(query.toString())) {

int paramIndex = 1;
stmt.setInt(paramIndex++, userId);

if (fromDate != null) {
stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fromDate));
}
if (toDate != null) {
stmt.setTimestamp(paramIndex, Timestamp.valueOf(toDate));
}

ResultSet rs = stmt.executeQuery();
while (rs.next()) {
sessions.add(mapResultSetToSession(rs));
}
} catch (SQLException e) {
e.printStackTrace();
}
return sessions;
}




}
