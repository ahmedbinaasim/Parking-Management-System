// LoginController.java
package com.pms.controllers;

import com.pms.services.UserService;
import com.pms.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;
    @FXML private VBox loginContainer;
    @FXML private ProgressIndicator loadingIndicator;
    
    private final UserService userService;

    public LoginController() {
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        // Add input validation
        setupInputValidation();
        
        // Hide loading indicator initially
        loadingIndicator.setVisible(false);
        
        // Add enter key support
        setupEnterKeySupport();
    }

    private void setupInputValidation() {
        // Enable login button only when both fields have content
        loginButton.disableProperty().bind(
            usernameField.textProperty().isEmpty()
            .or(passwordField.textProperty().isEmpty())
        );

        // Add CSS classes for validation
        usernameField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                usernameField.getStyleClass().add("error");
            } else {
                usernameField.getStyleClass().remove("error");
            }
        });

        passwordField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                passwordField.getStyleClass().add("error");
            } else {
                passwordField.getStyleClass().remove("error");
            }
        });
    }

    private void setupEnterKeySupport() {
        // Allow enter key to trigger login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !loginButton.isDisabled()) {
                handleLogin();
            }
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Show loading indicator
        loadingIndicator.setVisible(true);
        if (loginButton.disableProperty().isBound()) {
            loginButton.disableProperty().unbind();
        }
        loginButton.setDisable(true);

        // Create a new thread for login process
        new Thread(() -> {
            try {
                User user = userService.loginUser(username, password);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (user != null) {
                        handleSuccessfulLogin(user);
                    } else {
                        handleFailedLogin();
                    }
                    loadingIndicator.setVisible(false);
                    if (loginButton.disableProperty().isBound()) {
                        loginButton.disableProperty().unbind();
                    }
                    loginButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("An error occurred during login. Please try again.");
                    loadingIndicator.setVisible(false);
                    if (loginButton.disableProperty().isBound()) {
                        loginButton.disableProperty().unbind();
                    }
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }

    private void handleSuccessfulLogin(User user) {
        // Set session information
        Session.setCurrentUserId(user.getUserId());
        Session.setUsername(user.getUsername());
        Session.setUserRole(user.getRole());

        // Show success animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loginContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Switch to appropriate dashboard based on role
            if ("ADMIN".equals(user.getRole())) {
                SceneManager.switchScene("AdminDashboard.fxml");
            } else {
                
                SceneManager.switchScene("MainMenu.fxml");
            }
        });
        fadeOut.play();
    }

    private void handleFailedLogin() {
        statusLabel.setText("Invalid username or password");
        statusLabel.getStyleClass().add("error-label");
        
        // Shake animation for error
        shakeNode(loginContainer);
        
        // Clear password field
        passwordField.clear();
        passwordField.requestFocus();
    }

    private void shakeNode(javafx.scene.Node node) {
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(Duration.millis(100), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    @FXML
    private void goToRegister() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loginContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> SceneManager.switchScene("Registration.fxml"));
        fadeOut.play();
    }

    @FXML
    private void goToPasswordReset() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loginContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> SceneManager.switchScene("PasswordReset.fxml"));
        fadeOut.play();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("@com/pms/css/dialog.css").toExternalForm()
        );
        alert.showAndWait();
    }
}