package com.pms.dao;

import com.pms.models.ParkingCard;
import com.pms.utils.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingCardDAO {
    
    public ParkingCard createCard(ParkingCard card) {
        String query = "INSERT INTO ParkingCards (user_id, card_number, status, expiry_date) " +
                      "VALUES (?, ?, ?, ?)";
                      
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, card.getUserId());
            stmt.setString(2, card.getCardNumber());
            stmt.setString(3, card.getStatus());
            stmt.setTimestamp(4, Timestamp.valueOf(card.getExpiryDate()));
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                card.setCardId(rs.getInt(1));
                return card;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ParkingCard getCardByNumber(String cardNumber) {
        String query = "SELECT * FROM ParkingCards WHERE card_number = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, cardNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCard(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ParkingCard> getCardsByUser(int userId) {
        String query = "SELECT * FROM ParkingCards WHERE user_id = ?";
        
        List<ParkingCard> cards = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                cards.add(mapResultSetToCard(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    public boolean updateCardStatus(int cardId, String status) {
        String query = "UPDATE ParkingCards SET status = ? WHERE card_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, cardId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ParkingCard mapResultSetToCard(ResultSet rs) throws SQLException {
        return new ParkingCard(
            rs.getInt("card_id"),
            rs.getInt("user_id"),
            rs.getString("card_number"),
            rs.getString("status"),
            rs.getTimestamp("issued_date").toLocalDateTime(),
            rs.getTimestamp("expiry_date").toLocalDateTime()
        );
    }
}
