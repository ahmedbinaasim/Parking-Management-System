package com.pms.controllers;

import com.pms.services.ReservationService;
import com.pms.services.ParkingSpotService;
import com.pms.services.PaymentService;
import com.pms.models.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReservationController {
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> startTimeComboBox;
    @FXML private ComboBox<String> endTimeComboBox;
    @FXML private ComboBox<String> floorComboBox;
    @FXML private ComboBox<String> spotTypeComboBox;
    @FXML private TextField vehicleNumberField;
    @FXML private Label selectedSpotLabel;
    @FXML private Label durationLabel;
    @FXML private Label chargesLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Button reserveButton;
    @FXML private GridPane spotsGrid;
    @FXML private StackPane loadingOverlay;
    @FXML private Label loadingLabel;

    private final ReservationService reservationService;
    private final ParkingSpotService spotService;
    private final PaymentService paymentService;
    private ParkingSpot selectedSpot;
    private final Map<String, Button> spotButtons = new HashMap<>();
    
    private static final double HOURLY_RATE = 2.0;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public ReservationController() {
        this.reservationService = new ReservationService();
        this.spotService = new ParkingSpotService();
        this.paymentService = new PaymentService();
    }

    @FXML
    public void initialize() {
        setupDateTimePickers();
        setupComboBoxes();
        setupValidation();
        loadParkingSpots();
    }

    private void setupDateTimePickers() {
        // Set minimum date to today
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Populate time combo boxes
        List<String> timeSlots = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            String time = String.format("%02d:00", hour);
            timeSlots.add(time);
        }
        startTimeComboBox.setItems(FXCollections.observableArrayList(timeSlots));
        endTimeComboBox.setItems(FXCollections.observableArrayList(timeSlots));
        
        // Set default times
        startTimeComboBox.setValue("09:00");
        endTimeComboBox.setValue("10:00");

        // Add listeners for date/time changes
        startDatePicker.valueProperty().addListener((obs, old, newVal) -> {
            if (endDatePicker.getValue().isBefore(newVal)) {
                endDatePicker.setValue(newVal);
            }
            updateAvailableSpots();
        });

        endDatePicker.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal.isBefore(startDatePicker.getValue())) {
                endDatePicker.setValue(startDatePicker.getValue());
            }
            updateAvailableSpots();
        });

        startTimeComboBox.valueProperty().addListener((obs, old, newVal) -> updateAvailableSpots());
        endTimeComboBox.valueProperty().addListener((obs, old, newVal) -> updateAvailableSpots());
    }

    private void setupComboBoxes() {
        // Setup floor combo box
        floorComboBox.setItems(FXCollections.observableArrayList("Floor 1", "Floor 2", "Floor 3"));
        floorComboBox.setValue("Floor 1");
        floorComboBox.valueProperty().addListener((obs, old, newVal) -> updateAvailableSpots());

        // Setup spot type combo box
        spotTypeComboBox.setItems(FXCollections.observableArrayList("Standard", "Premium"));
        spotTypeComboBox.setValue("Standard");
        spotTypeComboBox.valueProperty().addListener((obs, old, newVal) -> updateAvailableSpots());

        // Setup payment method combo box
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
            "Credit Card", "Debit Card", "Cash"
        ));
        paymentMethodComboBox.setValue("Credit Card");
    }

    private void setupValidation() {
        // Vehicle number validation
        vehicleNumberField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("[A-Za-z0-9-]*")) {
                vehicleNumberField.setText(old);
            }
            updateReserveButton();
        });

        // Disable reserve button initially
        reserveButton.setDisable(true);
    }

    private void loadParkingSpots() {
        spotsGrid.getChildren().clear();
        spotButtons.clear();

        int floor = Integer.parseInt(floorComboBox.getValue().replace("Floor ", ""));
        List<ParkingSpot> spots = spotService.getSpotsByFloor(floor);

        int col = 0;
        int row = 0;
        int maxCols = 5;

        for (ParkingSpot spot : spots) {
            Button spotButton = createSpotButton(spot);
            spotsGrid.add(spotButton, col, row);
            spotButtons.put(spot.getSpotNumber(), spotButton);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }

    private Button createSpotButton(ParkingSpot spot) {
        Button button = new Button(spot.getSpotNumber());
        button.getStyleClass().addAll("parking-spot", spot.getStatus().toString());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        GridPane.setFillWidth(button, true);
        GridPane.setFillHeight(button, true);

        if (spot.getStatus().equals("AVAILABLE")) {
            button.setOnAction(e -> handleSpotSelection(spot, button));
        } else {
            button.setDisable(true);
        }

        return button;
    }

    private void handleSpotSelection(ParkingSpot spot, Button button) {
        // Clear previous selection
        if (selectedSpot != null) {
            Button prevButton = spotButtons.get(selectedSpot.getSpotNumber());
            prevButton.getStyleClass().remove("selected");
        }

        // Update selection
        selectedSpot = spot;
        button.getStyleClass().add("selected");
        selectedSpotLabel.setText(spot.getSpotNumber() + " (" + spot.getType() + ")");
        
        // Update duration and charges
        updateDurationAndCharges();
        updateReserveButton();
    }

    private void updateDurationAndCharges() {
        if (selectedSpot == null) return;

        LocalDateTime startDateTime = getStartDateTime();
        LocalDateTime endDateTime = getEndDateTime();
        Duration duration = Duration.between(startDateTime, endDateTime);
        long hours = duration.toHours();

        // Calculate base rate based on spot type
        double baseRate = selectedSpot.getType().equals("PREMIUM") ? HOURLY_RATE * 1.5 : HOURLY_RATE;
        double totalCharges = hours * baseRate;

        durationLabel.setText(String.format("%d hours", hours));
        chargesLabel.setText(String.format("$%.2f", totalCharges));
    }

    private void updateAvailableSpots() {
        LocalDateTime startDateTime = getStartDateTime();
        LocalDateTime endDateTime = getEndDateTime();
        int floor = Integer.parseInt(floorComboBox.getValue().replace("Floor ", ""));
        String type = spotTypeComboBox.getValue().toUpperCase();

        if (!isValidTimeRange(startDateTime, endDateTime)) {
            showError("Invalid time range selected");
            return;
        }

        loadingOverlay.setVisible(true);
        new Thread(() -> {
            List<ParkingSpot> availableSpots = spotService.getAvailableSpots(floor, type, startDateTime, endDateTime);
            
            Platform.runLater(() -> {
                updateSpotButtons(availableSpots);
                loadingOverlay.setVisible(false);
            });
        }).start();
    }

    private boolean isValidTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return false;
        
        // Minimum 1 hour, maximum 24 hours
        Duration duration = Duration.between(start, end);
        long hours = duration.toHours();
        
        return hours >= 1 && hours <= 24 && !start.isBefore(LocalDateTime.now());
    }

    private void updateSpotButtons(List<ParkingSpot> availableSpots) {
        Set<String> availableSpotNumbers = availableSpots.stream()
            .map(ParkingSpot::getSpotNumber)
            .collect(java.util.stream.Collectors.toSet());

        spotButtons.forEach((spotNumber, button) -> {
            button.getStyleClass().removeAll("available", "occupied");
            button.getStyleClass().add(availableSpotNumbers.contains(spotNumber) ? "available" : "occupied");
            button.setDisable(!availableSpotNumbers.contains(spotNumber));
        });

        // Clear selection if selected spot is no longer available
        if (selectedSpot != null && !availableSpotNumbers.contains(selectedSpot.getSpotNumber())) {
            selectedSpot = null;
            selectedSpotLabel.setText("None");
            durationLabel.setText("0 hours");
            chargesLabel.setText("$0.00");
        }

        updateReserveButton();
    }

    private void updateReserveButton() {
        boolean isValid = selectedSpot != null &&
                         !vehicleNumberField.getText().trim().isEmpty() &&
                         paymentMethodComboBox.getValue() != null &&
                         isValidTimeRange(getStartDateTime(), getEndDateTime());
        
        reserveButton.setDisable(!isValid);
    }

    @FXML
    private void handleReservation() {
        if (selectedSpot == null) return;

        LocalDateTime startDateTime = getStartDateTime();
        LocalDateTime endDateTime = getEndDateTime();
        String vehicleNumber = vehicleNumberField.getText().trim();
        
        loadingOverlay.setVisible(true);
        loadingLabel.setText("Processing reservation...");

        new Thread(() -> {
            try {
                // Create reservation
                ParkingResult result = reservationService.createReservation(
                    Session.getCurrentUserId(),
                    vehicleNumber,
                    selectedSpot.getSpotNumber(),
                    startDateTime,
                    endDateTime
                );

                if (result.isSuccess()) {
                    // Process payment
                    PaymentService.PaymentResult paymentResult = processPayment(result);
                    
                    Platform.runLater(() -> {
                        if (paymentResult.isSuccess()) {
                            showSuccess("Reservation completed successfully!");
                            goToMainMenu();
                        } else {
                            handlePaymentFailure(paymentResult.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() -> showError(result.getMessage()));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("An error occurred. Please try again."));
            } finally {
                Platform.runLater(() -> loadingOverlay.setVisible(false));
            }
        }).start();
    }

    private PaymentService.PaymentResult processPayment(ParkingResult reservationResult) {
        ParkingReservation reservation = (ParkingReservation) reservationResult.getData();
        return paymentService.processReservationPayment(reservation);
    }

    private void handlePaymentFailure(String message) {
        showError("Payment failed: " + message);
        // TODO: Cancel the reservation
    }

    private LocalDateTime getStartDateTime() {
        return LocalDateTime.of(
            startDatePicker.getValue(),
            LocalTime.parse(startTimeComboBox.getValue(), TIME_FORMATTER)
        );
    }

    private LocalDateTime getEndDateTime() {
        return LocalDateTime.of(
            endDatePicker.getValue(),
            LocalTime.parse(endTimeComboBox.getValue(), TIME_FORMATTER)
        );
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToMainMenu() {
        SceneManager.switchScene("MainMenu.fxml");
    }
}