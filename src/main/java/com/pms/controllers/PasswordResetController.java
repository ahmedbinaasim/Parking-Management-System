package com.pms.controllers;

import com.pms.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class PasswordResetController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final UserService userService = new UserService();

    @FXML
    private void resetPassword() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Input validation
        if (username.isEmpty() || email.isEmpty() || 
            newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        // Password match validation
        if (!newPassword.equals(confirmPassword)) {
            showError("New passwords do not match.");
            return;
        }

        // Password strength validation
        if (newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || 
            !newPassword.matches(".*[^a-zA-Z0-9].*")) {
            showError("Password must be at least 8 characters long, contain an uppercase letter " +
                     "and a special character.");
            return;
        }

        // Attempt password reset
        if (userService.resetPassword(username, email, newPassword)) {
            showSuccess("Password has been reset successfully.");
            SceneManager.switchScene("Login.fxml");
        } else {
            showError("Invalid username or email combination.");
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene("Login.fxml");
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}