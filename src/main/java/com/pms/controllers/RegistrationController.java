// RegistrationController.java
package com.pms.controllers;

import com.pms.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.animation.*;
import java.util.regex.Pattern;

public class RegistrationController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private VBox registrationContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox strengthIndicator;
    @FXML private Label strengthLabel;

    private final UserService userService;
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("\\d{10,12}");

    public RegistrationController() {
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        setupValidation();
        setupPasswordStrengthIndicator();
        loadingIndicator.setVisible(false);
    }

    private void setupValidation() {
        // Real-time validation for username
        usernameField.textProperty().addListener((obs, old, newVal) -> {
            validateField(usernameField, 
                newVal.length() >= 4, 
                "Username must be at least 4 characters");
        });

        // Real-time validation for email
        emailField.textProperty().addListener((obs, old, newVal) -> {
            validateField(emailField,
                EMAIL_PATTERN.matcher(newVal).matches(),
                "Enter a valid email address");
        });

        // Real-time validation for phone
        phoneField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                phoneField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            validateField(phoneField,
                PHONE_PATTERN.matcher(newVal).matches(),
                "Enter a valid phone number");
        });

        // Password match validation
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> {
            validateField(confirmPasswordField,
                newVal.equals(passwordField.getText()),
                "Passwords must match");
        });

        // Enable/disable register button based on all validations
        registerButton.disableProperty().bind(
            usernameField.textProperty().isEmpty()
            .or(emailField.textProperty().isEmpty())
            .or(phoneField.textProperty().isEmpty())
            .or(passwordField.textProperty().isEmpty())
            .or(confirmPasswordField.textProperty().isEmpty())
            .or(statusLabel.textProperty().isNotEmpty())
        );
    }

    private void setupPasswordStrengthIndicator() {
        passwordField.textProperty().addListener((obs, old, newVal) -> {
            updatePasswordStrength(newVal);
        });
    }

    private void updatePasswordStrength(String password) {
        int strength = 0;
        String message = "";

        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*].*")) strength++;

        switch (strength) {
            case 0:
            case 1:
                message = "Weak";
                strengthIndicator.setStyle("-fx-background-color: #e74c3c;");
                break;
            case 2:
            case 3:
                message = "Medium";
                strengthIndicator.setStyle("-fx-background-color: #f1c40f;");
                break;
            case 4:
            case 5:
                message = "Strong";
                strengthIndicator.setStyle("-fx-background-color: #27ae60;");
                break;
        }

        strengthLabel.setText(message);
        strengthIndicator.setPrefWidth((strength * 20));
    }

    @FXML
    private void handleRegistration() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();

        loadingIndicator.setVisible(true);
        if (registerButton.disableProperty().isBound()) {
            registerButton.disableProperty().unbind();
        }
        registerButton.setDisable(true);

        new Thread(() -> {
            try {
                boolean success = userService.registerUser(
                    username, email, phone, password, "USER");

                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        handleSuccessfulRegistration();
                    } else {
                        handleFailedRegistration("Registration failed. Please try again.");
                    }
                    loadingIndicator.setVisible(false);
                    if (registerButton.disableProperty().isBound()) {
                        registerButton.disableProperty().unbind();
                    }
                    registerButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    handleFailedRegistration("An error occurred. Please try again.");
                    loadingIndicator.setVisible(false);
                    if (registerButton.disableProperty().isBound()) {
                        registerButton.disableProperty().unbind();
                    }
                    registerButton.setDisable(false);
                });
            }
        }).start();
    }

    private void handleSuccessfulRegistration() {
        // Show success dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Registration successful! Please wait for admin approval.");
        // alert.getDialogPane().getStylesheets().add(
        //     getClass().getResource("/styles/dialog.css").toExternalForm()
        // );
        
        // After clicking OK, transition to login screen
        alert.showAndWait().ifPresent(response -> {
            FadeTransition fadeOut = new FadeTransition(
                Duration.millis(500), registrationContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> SceneManager.switchScene("Login.fxml"));
            fadeOut.play();
        });
    }

    private void handleFailedRegistration(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().add("error-label");
        shakeNode(registrationContainer);
    }

    private void validateField(TextField field, boolean condition, String errorMessage) {
        if (!condition && !field.getText().isEmpty()) {
            field.getStyleClass().add("error");
            statusLabel.setText(errorMessage);
        } else {
            field.getStyleClass().remove("error");
            if (statusLabel.getText().equals(errorMessage)) {
                statusLabel.setText("");
            }
        }
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    @FXML
    private void goToLogin() {
        FadeTransition fadeOut = new FadeTransition(
            Duration.millis(500), registrationContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> SceneManager.switchScene("Login.fxml"));
        fadeOut.play();
    }
}