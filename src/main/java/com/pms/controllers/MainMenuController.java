package com.pms.controllers;

import com.pms.services.*;
import com.pms.models.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.application.Platform;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainMenuController {
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label availableSpotsLabel;
    @FXML private Label activeSessionsLabel;
    @FXML private Label todayRevenueLabel;
    
    @FXML private VBox adminSection;
    @FXML private VBox notificationsList;
    
    private final ParkingSessionService sessionService;
    private final ParkingSpotService spotService;
    private final NotificationService notificationService;
    private Timeline refreshTimeline;

    public MainMenuController() {
        this.sessionService = new ParkingSessionService();
        this.spotService = new ParkingSpotService();
        this.notificationService = new NotificationService();
    }

    @FXML
    public void initialize() {
        validateSession();
        setupUserInterface();
        startAutoRefresh();
    }

    private void validateSession() {
        if (!Session.isLoggedIn()) {
            showError("Session expired. Please login again.");
            SceneManager.switchScene("Login.fxml");
            return;
        }
    }

    private void setupUserInterface() {
        // Setup user info
        userNameLabel.setText("Welcome, " + Session.getUsername());
        userRoleLabel.setText(Session.getUserRole());
        
        // Configure admin section visibility
        boolean isAdmin = "ADMIN".equals(Session.getUserRole());
        adminSection.setVisible(isAdmin);
        adminSection.setManaged(isAdmin);
        
        // Setup date/time updater
        setupClock();
        
        // Load initial data
        updateDashboardStats();
        loadNotifications();
    }

    private void setupClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> 
            updateDateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        
        // Store for cleanup
        this.refreshTimeline = clock;
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy HH:mm:ss");
        dateTimeLabel.setText(now.format(formatter));
    }

    private void startAutoRefresh() {
        Timeline statsRefresh = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            updateDashboardStats();
            loadNotifications();
        }));
        statsRefresh.setCycleCount(Timeline.INDEFINITE);
        statsRefresh.play();
    }

    private void updateDashboardStats() {
        new Thread(() -> {
            try {
                int availableSpots = spotService.getAvailableSpotCount();
                int activeSessions = sessionService.getActiveSessionCount();
                double todayRevenue = sessionService.getTotalRevenue();

                Platform.runLater(() -> {
                    availableSpotsLabel.setText(String.valueOf(availableSpots));
                    activeSessionsLabel.setText(String.valueOf(activeSessions));
                    todayRevenueLabel.setText(String.format("$%.2f", todayRevenue));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    availableSpotsLabel.setText("N/A");
                    activeSessionsLabel.setText("N/A");
                    todayRevenueLabel.setText("N/A");
                });
            }
        }).start();
    }

    private void loadNotifications() {
        new Thread(() -> {
            List<String> notifications = notificationService.getRecentNotifications(
                Session.getCurrentUserId());
            
            Platform.runLater(() -> {
                notificationsList.getChildren().clear();
                notifications.forEach(notification -> {
                    Label notifLabel = new Label(notification);
                    notifLabel.getStyleClass().add("notification-item");
                    notificationsList.getChildren().add(notifLabel);
                });
            });
        }).start();
    }

    // Navigation Methods
    @FXML private void goToParkingEntry() {
        SceneManager.switchScene("EntryGate.fxml");
    }

    @FXML private void goToParkingExit() {
        SceneManager.switchScene("ExitGate.fxml");
    }

    @FXML private void goToParkingHistory() {
        SceneManager.switchScene("ParkingHistory.fxml");
    }

    @FXML private void goToReservations() {
        SceneManager.switchScene("ReservationView.fxml");
    }

    @FXML private void goToFeedback() {
        SceneManager.switchScene("FeedbackView.fxml");
    }

    @FXML private void goToManageUsers() {
        //SceneManager.switchScene("ManageUsers.fxml");
    }

    @FXML private void goToManageParking() {
        //SceneManager.switchScene("ManageParking.fxml");
    }

    @FXML
    private void handleLogout() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        Session.clearSession();
        SceneManager.switchScene("Login.fxml");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}