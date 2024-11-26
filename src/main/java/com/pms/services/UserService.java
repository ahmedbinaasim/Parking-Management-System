package com.pms.services;

import com.pms.dao.UserDAO;
import com.pms.dao.ParkingCardDAO;
import com.pms.models.User;
import com.pms.models.ParkingCard;
import com.pms.utils.PasswordUtils;
import java.time.LocalDateTime;
import java.util.List;

public class UserService {
    private final UserDAO userDAO;
    private final ParkingCardDAO parkingCardDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.parkingCardDAO = new ParkingCardDAO();
    }

    public boolean registerUser(String username, String email, String phoneNumber, 
                              String password, String role) {
        // Validate input
        if (!isValidRegistrationInput(username, email, phoneNumber, password)) {
            return false;
        }

        // Check if username or email already exists
        if (!userDAO.isUsernameAvailable(username) || !userDAO.isEmailAvailable(email)) {
            return false;
        }

        // Create new user
        User user = new User(username, PasswordUtils.hashPassword(password), 
                           email, phoneNumber, role);
        user.setStatus("PENDING"); // New users start with pending status

        return userDAO.createUser(user);
    }

    // public boolean approveRegistration(int userId) {
    //     User user = userDAO.getUserById(userId);
    //     if (user == null || !user.getStatus().equals("PENDING")) {
    //         return false;
    //     }
    
    //     // Update user status
    //     if (!userDAO.updateUserStatus(userId, "ACTIVE")) {
    //         return false;
    //     }
    
    //     // Generate random cardId between 1000 and 9999
    //     int randomCardId = 1000 + (int)(Math.random() * 9000);
        
    //     // Create parking card for the user
    //     ParkingCard card = new ParkingCard(
    //         randomCardId,  // Added random cardId
    //         userId, 
    //         generateCardNumber(), 
    //         "ACTIVE",
    //         LocalDateTime.now(),
    //         LocalDateTime.now().plusYears(1)
    //     );
    //     return parkingCardDAO.createCard(card) != null;
    // }

    public boolean resetPassword(String username, String email, String newPassword) {
        if (!userDAO.verifyUserEmailCombination(username, email)) {
            return false;
        }

        if (!isValidPassword(newPassword)) {
            return false;
        }

        String hashedPassword = PasswordUtils.hashPassword(newPassword);
        return userDAO.updatePassword(username, hashedPassword);
    }

    public List<User> getPendingRegistrations() {
        return userDAO.getPendingRegistrations();
    }

    public List<User> getActiveUsers() {
        return userDAO.getUsersByStatus("ACTIVE");
    }

    private boolean isValidRegistrationInput(String username, String email, 
                                           String phoneNumber, String password) {
        return username != null && !username.trim().isEmpty() &&
               email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$") &&
               phoneNumber != null && phoneNumber.matches("\\d{10,12}") &&
               isValidPassword(password);
    }

    private boolean isValidPassword(String password) {
        return password != null && 
               password.length() >= 8 && 
               password.matches(".*[A-Z].*") && 
               password.matches(".*[^a-zA-Z0-9].*");
    }

    public boolean rejectRegistration(int userId, String reason) {
        User user = userDAO.getUserById(userId);
        if (user == null || !user.getStatus().equals("PENDING")) {
            return false;
        }
        return userDAO.updateUserStatus(userId, "REJECTED");
    }
    
    private String generateCardNumber() {
        return "PMS-" + System.currentTimeMillis() + "-" + 
               String.format("%04d", (int)(Math.random() * 10000));
    }

    public User loginUser(String username, String password) {
        User user = userDAO.getUserByUsername(username);
        if (user != null && 
            user.getStatus().equals("ACTIVE") && 
            verifyPassword(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        // Simple comparison for now - in production use proper hashing
        return password.equals(hashedPassword);
    }

    public boolean approveRegistration(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null || !user.getStatus().equals("PENDING")) {
            return false;
        }
    
        // Update user status
        if (userDAO.updateUserStatus(userId, "ACTIVE")) {
            // Create parking card for approved user
            ParkingCard card = new ParkingCard(
                0, userId, 
                generateCardNumber(), 
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now().plusYears(1)
            );
            return parkingCardDAO.createCard(card) != null;
        }
        return false;
    }
}