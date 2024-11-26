// EntryGateController.java
package com.pms.controllers;

import com.pms.services.*;
import com.pms.dao.ParkingSpotDAO;
import com.pms.models.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EntryGateController {
    @FXML private VBox mainContainer;
    @FXML private VBox cardScanContainer;
    @FXML private VBox spotSelectionContainer;
    @FXML private VBox receiptContainer;
    
    @FXML private TextField cardNumberField;
    @FXML private TextField vehicleNumberField;
    @FXML private TextField plateNumberField;
    @FXML private ComboBox<String> spotNumberComboBox;
    
    @FXML private Text statusText;
    @FXML private Button confirmButton;
    @FXML private ProgressIndicator progressIndicator;
    
    // Labels for displaying reservation details if exists
    @FXML private VBox reservationDetailsContainer;
    @FXML private Label reservedSpotLabel;
    @FXML private Label reservationTimeLabel;
    
    // Receipt details
    @FXML private Label receiptNumberLabel;
    @FXML private Label entryTimeLabel;
    @FXML private Label spotNumberLabel;
    @FXML private Button printReceiptButton;

    private final ParkingSessionService sessionService;
    private final ParkingSpotDAO spotDAO;
    private ParkingCard currentCard;
    private ParkingReservation activeReservation;

    public EntryGateController() {
        this.sessionService = new ParkingSessionService();
        this.spotDAO = new ParkingSpotDAO();
    }

    @FXML
    public void initialize() {
        setupUI();
        setupValidation();
    }

    private void setupUI() {
        // Initially show only card scan container
        cardScanContainer.setVisible(true);
        spotSelectionContainer.setVisible(false);
        receiptContainer.setVisible(false);
        reservationDetailsContainer.setVisible(false);
        progressIndicator.setVisible(false);
        
        // Setup spot selection dropdown
        updateAvailableSpots();
    }

    private void setupValidation() {
        // Card number validation
        cardNumberField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = newVal.matches("PMS-\\d{13}-\\d{4}");
            cardNumberField.getStyleClass().removeAll("valid-input", "invalid-input");
            cardNumberField.getStyleClass().add(isValid ? "valid-input" : "invalid-input");
            confirmButton.setDisable(!isValid);
        });

        // Vehicle number validation
        vehicleNumberField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("[A-Za-z0-9-]*")) {
                vehicleNumberField.setText(old);
            }
        });
    }

    private void updateAvailableSpots() {
        List<ParkingSpot> availableSpots = spotDAO.getAvailableSpots();
        spotNumberComboBox.getItems().clear();
        availableSpots.forEach(spot -> 
            spotNumberComboBox.getItems().add(spot.getSpotNumber())
        );
    }

    // @FXML
    // private void handleCardScan() {
    //     String cardNumber = cardNumberField.getText().trim();
    //     progressIndicator.setVisible(true);
    //     confirmButton.setDisable(true);

    //     new Thread(() -> {
    //         ParkingResult result = sessionService.validateParkingCard(cardNumber);
            
    //         Platform.runLater(() -> {
    //             if (result.isSuccess()) {
    //                 currentCard = (ParkingCard) result.getData();
    //                 checkForReservation();
    //                 transitionToSpotSelection();
    //             } else {
    //                 showError(result.getMessage());
    //                 progressIndicator.setVisible(false);
    //                 confirmButton.setDisable(false);
    //             }
    //         });
    //     }).start();
    // }

    private void checkForReservation() {
        activeReservation = sessionService.getActiveReservation(currentCard.getUserId());
        if (activeReservation != null) {
            reservationDetailsContainer.setVisible(true);
            reservedSpotLabel.setText("Reserved Spot: " + activeReservation.getSpotNumber());
            reservationTimeLabel.setText(String.format("Valid until: %s",
                activeReservation.getEndTime().format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )));
            // Pre-select the reserved spot
            spotNumberComboBox.setValue(activeReservation.getSpotNumber());
            spotNumberComboBox.setDisable(true);
        }
    }

//     @FXML
// private void handleEntryConfirmation() {
//     String vehicleNumber = vehicleNumberField.getText().trim();
//     String plateNumber = plateNumberField.getText().trim();
//     String spotNumber = spotNumberComboBox.getValue();

//     if (validateEntryInputs(vehicleNumber, plateNumber, spotNumber)) {
//         progressIndicator.setVisible(true);
//         confirmButton.setDisable(true);

//         new Thread(() -> {
//             ParkingResult result = sessionService.startSession(
//                 currentCard.getCardNumber(),  // card number
//                 vehicleNumber,                // vehicle number
//                 plateNumber,                  // plate number
//                 activeReservation != null ? activeReservation.getReservationId() : null  // reservation ID
//             );

//             Platform.runLater(() -> {
//                 if (result.isSuccess()) {
//                     showReceipt((String) result.getData());
//                 } else {
//                     showError(result.getMessage());
//                     progressIndicator.setVisible(false);
//                     confirmButton.setDisable(false);
//                 }
//             });
//         }).start();
//     }
// }

    private boolean validateEntryInputs(String vehicleNumber, String plateNumber, String spotNumber) {
        if (vehicleNumber.isEmpty()) {
            showError("Please enter vehicle number");
            return false;
        }
        if (plateNumber.isEmpty()) {
            showError("Please enter plate number");
            return false;
        }
        if (spotNumber == null || spotNumber.isEmpty()) {
            showError("Please select a parking spot");
            return false;
        }
        return true;
    }

    private void showReceipt(String receiptNumber) {
        // Update receipt details
        receiptNumberLabel.setText("Receipt #: " + receiptNumber);
        entryTimeLabel.setText("Entry Time: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        spotNumberLabel.setText("Spot #: " + spotNumberComboBox.getValue());

        // Transition to receipt view
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), spotSelectionContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            spotSelectionContainer.setVisible(false);
            receiptContainer.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), receiptContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void handlePrintReceipt() {
        // Implement receipt printing logic
        // For now, just simulate printing
        printReceiptButton.setDisable(true);
        printReceiptButton.setText("Printing...");
        
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            showSuccess("Receipt printed successfully!");
            goToMainMenu();
        });
        pause.play();
    }

    private void transitionToSpotSelection() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), cardScanContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            cardScanContainer.setVisible(false);
            spotSelectionContainer.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), spotSelectionContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void goToMainMenu() {
        SceneManager.switchScene("MainMenu.fxml");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dialog.css").toExternalForm()
        );
        alert.show();
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

    @FXML
private void handleCardScan() {
    String cardNumber = cardNumberField.getText().trim();
    if (!validateCardNumber(cardNumber)) {
        showError("Invalid card number format");
        return;
    }

    progressIndicator.setVisible(true);
    confirmButton.setDisable(true);

    new Thread(() -> {
        ParkingResult result = sessionService.validateParkingCard(cardNumber);
        
        Platform.runLater(() -> {
            if (result.isSuccess()) {
                currentCard = (ParkingCard) result.getData();
                transitionToSpotSelection();
            } else {
                showError(result.getMessage());
            }
            progressIndicator.setVisible(false);
            confirmButton.setDisable(false);
        });
    }).start();
}

@FXML
private void handleEntryConfirmation() {
    if (!validateEntryInputs()) return;

    String vehicleNumber = vehicleNumberField.getText().trim();
    String plateNumber = plateNumberField.getText().trim();
    String spotNumber = spotNumberComboBox.getValue();

    progressIndicator.setVisible(true);
    confirmButton.setDisable(true);

    new Thread(() -> {
        ParkingResult result = sessionService.startSession(
            currentCard.getCardNumber(),
            vehicleNumber,
            plateNumber,
            null  // No reservation
        );

        Platform.runLater(() -> {
            if (result.isSuccess()) {
                showReceipt((String) result.getData());
            } else {
                showError(result.getMessage());
                progressIndicator.setVisible(false);
                confirmButton.setDisable(false);
            }
        });
    }).start();
}

private boolean validateCardNumber(String cardNumber) {
    return cardNumber.matches("PMS-\\d{13}-\\d{4}");
}

private boolean validateEntryInputs() {
    if (vehicleNumberField.getText().trim().isEmpty()) {
        showError("Please enter vehicle number");
        return false;
    }
    if (plateNumberField.getText().trim().isEmpty()) {
        showError("Please enter plate number");
        return false;
    }
    if (spotNumberComboBox.getValue() == null) {
        showError("Please select a parking spot");
        return false;
    }
    return true;
}
}