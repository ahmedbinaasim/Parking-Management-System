// AdminDashboardController.java
package com.pms.controllers;

import com.pms.services.*;
import com.pms.dao.ParkingSpotDAO;
import com.pms.models.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.chart.*;

public class AdminDashboardController {
    @FXML private TabPane mainTabPane;
    @FXML private VBox dashboardContent;
    
    // Tables
    @FXML private TableView<User> pendingRegistrationsTable;
    @FXML private TableView<ParkingViolation> violationsTable;
    @FXML private TableView<Feedback> feedbackTable;
    
    // Charts
    @FXML private LineChart<String, Number> occupancyChart;
    @FXML private PieChart revenueChart;
    
    // Statistics Labels
    @FXML private Label totalSpotsLabel;
    @FXML private Label availableSpotsLabel;
    @FXML private Label activeSessionsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label pendingApprovalsLabel;
    @FXML private Label activeViolationsLabel;

    // Services
    private final UserService userService;
    private final ViolationService violationService;
    private final FeedbackService feedbackService;
    private final ParkingSessionService sessionService;
    private final ParkingSpotDAO spotDAO;
    
    private Timeline refreshTimeline;

    public AdminDashboardController() {
        this.userService = new UserService();
        this.violationService = new ViolationService();
        this.feedbackService = new FeedbackService();
        this.sessionService = new ParkingSessionService();
        this.spotDAO = new ParkingSpotDAO();
    }

    @FXML
    public void initialize() {
        setupTables();
        setupCharts();
        loadData();
        setupAutoRefresh();
    }

    private void setupTables() {
        setupPendingRegistrationsTable();
        setupViolationsTable();
        setupFeedbackTable();
    }

    @SuppressWarnings("unchecked")
    private void setupPendingRegistrationsTable() {
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getPhoneNumber()));

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        TableColumn<User, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            {
                approveBtn.getStyleClass().add("approve-button");
                rejectBtn.getStyleClass().add("reject-button");
                
                approveBtn.setOnAction(e -> {
                    User user = getTableRow() != null ? getTableRow().getItem() : null;
                    if (user != null) handleApproval(user);
                });
                rejectBtn.setOnAction(e -> {
                    User user = getTableRow() != null ? getTableRow().getItem() : null;
                    if (user != null) handleRejection(user);
                });
            }
        
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                
                User user = getTableRow().getItem();
                if (user.getStatus().equals("PENDING")) {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(approveBtn, rejectBtn);
                    setGraphic(buttons);
                } else {
                    setGraphic(null);
                }
            }
        });

        pendingRegistrationsTable.getColumns().addAll(
            usernameCol, emailCol, phoneCol, statusCol, actionCol
        );
        
        // Make table fill available space
        pendingRegistrationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupViolationsTable() {
        TableColumn<ParkingViolation, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getReportedAt().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                )
            ));

        TableColumn<ParkingViolation, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getViolationType()
            ));

        TableColumn<ParkingViolation, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDescription()
            ));

        TableColumn<ParkingViolation, Double> fineCol = new TableColumn<>("Fine");
        fineCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleDoubleProperty(
                data.getValue().getFineAmount()
            ).asObject());

        TableColumn<ParkingViolation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus()
            ));

        TableColumn<ParkingViolation, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button resolveBtn = new Button("Resolve");
            {
                resolveBtn.getStyleClass().add("resolve-button");
                resolveBtn.setOnAction(e -> {
                    ParkingViolation violation = getTableRow() != null ? getTableRow().getItem() : null;
                    if (violation != null) handleViolationResolution(violation);
                });
            }
        
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                
                ParkingViolation violation = getTableRow().getItem();
                if (violation.getStatus().equals("PENDING")) {
                    setGraphic(resolveBtn);
                } else {
                    setGraphic(null);
                }
            }
        });

        violationsTable.getColumns().addAll(
            timeCol, typeCol, descCol, fineCol, statusCol, actionCol
        );
        
        violationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupFeedbackTable() {
        TableColumn<Feedback, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreatedAt().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
            ));

        TableColumn<Feedback, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFeedbackType()
            ));

        TableColumn<Feedback, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getContent()
            ));
        contentCol.setPrefWidth(200);

        TableColumn<Feedback, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(
                data.getValue().getRating() != null ? 
                data.getValue().getRating() : 0
            ).asObject());

        TableColumn<Feedback, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus()
            ));

        TableColumn<Feedback, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button acknowledgeBtn = new Button("Acknowledge");
            {
                acknowledgeBtn.getStyleClass().add("acknowledge-button");
                acknowledgeBtn.setOnAction(e -> {
                    Feedback feedback = getTableRow() != null ? getTableRow().getItem() : null;
                    if (feedback != null) handleFeedbackAcknowledgment(feedback);
                });
            }
        
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                
                Feedback feedback = getTableRow().getItem();
                if (!feedback.getStatus().equals("ACKNOWLEDGED")) {
                    setGraphic(acknowledgeBtn);
                } else {
                    setGraphic(null);
                }
            }
        });

        feedbackTable.getColumns().addAll(
            dateCol, typeCol, contentCol, ratingCol, statusCol, actionCol
        );
        
        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupCharts() {
        // Setup Occupancy Chart
        occupancyChart.setTitle("Parking Occupancy Over Time");
        occupancyChart.getXAxis().setLabel("Time");
        occupancyChart.getYAxis().setLabel("Occupied Spots");
        
        // Setup Revenue Chart
        revenueChart.setTitle("Revenue Distribution");
    }

    private void loadData() {
        // Load table data
        loadPendingRegistrations();
        loadViolations();
        loadFeedback();
        
        // Update statistics
        updateStatistics();
        updateCharts();
    }

    private void loadPendingRegistrations() {
        List<User> pendingUsers = userService.getPendingRegistrations();
        pendingRegistrationsTable.setItems(FXCollections.observableArrayList(pendingUsers));
        pendingApprovalsLabel.setText(String.valueOf(pendingUsers.size()));
    }

    private void loadViolations() {
        List<ParkingViolation> violations = violationService.getAllViolations();
        violationsTable.setItems(FXCollections.observableArrayList(violations));
        
        long activeViolations = violations.stream()
            .filter(v -> v.getStatus().equals("PENDING"))
            .count();
        activeViolationsLabel.setText(String.valueOf(activeViolations));
    }

    private void loadFeedback() {
        List<Feedback> feedback = feedbackService.getPendingFeedback();
        feedbackTable.setItems(FXCollections.observableArrayList(feedback));
    }

    private void updateStatistics() {
        int availableSpots = spotDAO.getAvailableSpotCount();
        int totalSpots = spotDAO.getTotalSpotCount();
        int activeSessions = sessionService.getActiveSessionCount();
        double totalRevenue = sessionService.getTotalRevenue();

        Platform.runLater(() -> {
            totalSpotsLabel.setText(String.valueOf(totalSpots));
            availableSpotsLabel.setText(String.valueOf(availableSpots));
            activeSessionsLabel.setText(String.valueOf(activeSessions));
            totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
        });
    }

    private void updateCharts() {
    updateOccupancyChart();
    updateRevenueChart();
}

    private void updateOccupancyChart() {
    // Clear existing data
    occupancyChart.getData().clear();

    // Create series for occupancy data
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Occupied Spots");

    // Get last 24 hours of occupancy data
    try {
        // Assuming hourly data points for the last 24 hours
        LocalDateTime now = LocalDateTime.now();
        XYChart.Data<String, Number>[] dataPoints = new XYChart.Data[24];
        
        for (int i = 23; i >= 0; i--) {
            LocalDateTime time = now.minusHours(i);
            String hourLabel = time.format(DateTimeFormatter.ofPattern("HH:00"));
            
            // Calculate occupied spots (total - available)
            int totalSpots = spotDAO.getTotalSpotCount();
            int availableSpots = spotDAO.getAvailableSpotCount();
            int occupiedSpots = totalSpots - availableSpots;
            
            dataPoints[23-i] = new XYChart.Data<>(hourLabel, occupiedSpots);
        }
        
        // Add all data points to series
        series.getData().addAll(dataPoints);
        
        // Add series to chart
        occupancyChart.getData().add(series);
        
        // Style the chart
        series.getNode().setStyle("-fx-stroke: #3498db;");
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void updateRevenueChart() {
    // Clear existing data
    revenueChart.getData().clear();

    try {
        // Get revenue data by category
        double parkingRevenue = sessionService.getTotalRevenue();
        double violationRevenue = violationService.getTotalFinesCollected();
        double reservationRevenue = sessionService.getTotalReservationRevenue();

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Parking Fees", parkingRevenue),
            new PieChart.Data("Violation Fines", violationRevenue),
            new PieChart.Data("Reservations", reservationRevenue)
        );

        // Add data to chart
        revenueChart.setData(pieChartData);

        // Add value labels
        pieChartData.forEach(data ->
            data.setName(data.getName() + String.format(" ($%.2f)", data.getPieValue()))
        );

        // Style the chart
        revenueChart.setLegendVisible(true);
        revenueChart.setLabelsVisible(true);
        revenueChart.setStartAngle(90);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void setupAutoRefresh() {
        refreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(30), event -> loadData())
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    // Event Handlers
    private void handleApproval(User user) {
        if (user == null) return;

        new Thread(() -> {
            boolean success = userService.approveRegistration(user.getUserId());
            Platform.runLater(() -> {
                if (success) {
                    showSuccess("Registration approved successfully");
                    loadData();
                } else {
                    showError("Failed to approve registration");
                }
            });
        }).start();
    }

    private void handleRejection(User user) {
        if (user == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Registration");
        dialog.setHeaderText("Please provide a reason for rejection:");
        dialog.setContentText("Reason:");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dialog.css").toExternalForm()
        );

        dialog.showAndWait().ifPresent(reason -> {
            new Thread(() -> {
                boolean success = userService.rejectRegistration(user.getUserId(), reason);
                Platform.runLater(() -> {
                    if (success) {
                        showSuccess("Registration rejected");
                        loadData();
                    } else {
                        showError("Failed to reject registration");
                    }
                });
            }).start();
        });
    }

    private void handleViolationResolution(ParkingViolation violation) {
        if (violation == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resolve Violation");
        alert.setHeaderText("Are you sure you want to resolve this violation?");
        alert.setContentText("This action cannot be undone.");
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/pms/css/dialog.css").toExternalForm()
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    boolean success = violationService.resolveViolation(
                        violation.getViolationId()
                    );
                    Platform.runLater(() -> {
                        if (success) {
                            showSuccess("Violation resolved successfully");
                            loadData();
                        } else {
                            showError("Failed to resolve violation");
                        }
                    });
                }).start();
            }
        });
    }

    private void handleFeedbackAcknowledgment(Feedback feedback) {
        if (feedback == null) return;

        new Thread(() -> {
            boolean success = feedbackService.acknowledgeFeedback(
                feedback.getFeedbackId(),
                Session.getCurrentUserId()
            );
            Platform.runLater(() -> {
                if (success) {
                    showSuccess("Feedback acknowledged");
                    loadData();
                } else {
                    showError("Failed to acknowledge feedback");
                }
            });
        }).start();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dialog.css").toExternalForm()
        );
        alert.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        // Continuing AdminDashboardController.java
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dialog.css").toExternalForm()
        );
        alert.show();
    }

    @FXML
    private void handleLogout() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        Session.clearSession();
        SceneManager.switchScene("Login.fxml");
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    // Clean up resources
    public void cleanUp() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }
}