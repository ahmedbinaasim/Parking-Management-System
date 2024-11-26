// ExitGateController.java
package com.pms.controllers;

import com.pms.services.*;
import com.pms.models.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.util.Duration;
import java.time.format.DateTimeFormatter;

public class ExitGateController {
    @FXML private VBox mainContainer;
    @FXML private VBox receiptInputContainer;
    @FXML private VBox sessionDetailsContainer;
    @FXML private VBox paymentContainer;
    
    @FXML private TextField receiptNumberField;
    @FXML private ProgressIndicator progressIndicator;
    
    // Session details
    @FXML private Label vehicleNumberLabel;
    @FXML private Label spotNumberLabel;
    @FXML private Label entryTimeLabel;
    @FXML private Label durationLabel;
    @FXML private Label chargesLabel;
    
    // Payment details
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Label totalAmountLabel;
    @FXML private Button payButton;
    @FXML private Button printReceiptButton;

    private final ParkingSessionService sessionService;
    private final PaymentService paymentService;
    private ParkingSession currentSession;

    public ExitGateController() {
        this.sessionService = new ParkingSessionService();
        this.paymentService = new PaymentService();
    }

    @FXML
    public void initialize() {
        setupUI();
        setupValidation();
        setupPaymentMethods();
    }

    private void setupUI() {
        receiptInputContainer.setVisible(true);
        sessionDetailsContainer.setVisible(false);
        paymentContainer.setVisible(false);
        progressIndicator.setVisible(false);
        printReceiptButton.setVisible(false);
    }

    private void setupValidation() {
        receiptNumberField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("[A-Za-z0-9-]*")) {
                receiptNumberField.setText(old);
            }
        });
    }

    private void setupPaymentMethods() {
        paymentMethodComboBox.getItems().addAll(
            "Credit Card",
            "Debit Card",
            "Cash"
        );
        paymentMethodComboBox.setValue("Credit Card");
    }

    // @FXML
    // private void handleReceiptValidation() {
    //     String receiptNumber = receiptNumberField.getText().trim();
        
    //     if (receiptNumber.isEmpty()) {
    //         showError("Please enter receipt number");
    //         return;
    //     }
    
    //     progressIndicator.setVisible(true);
        
    //     new Thread(() -> {
    //         // Use getSessionByReceipt instead of getSessionByReceipt
    //         ParkingSession session = sessionService.getSessionByReceipt(receiptNumber);
            
    //         Platform.runLater(() -> {
    //             if (session != null && session.isActive()) {
    //                 currentSession = session;
    //                 showSessionDetails();
    //             } else {
    //                 showError("Invalid or inactive receipt number");
    //                 progressIndicator.setVisible(false);
    //             }
    //         });
    //     }).start();
    // }

    private void showSessionDetails() {
        // Update session details
        vehicleNumberLabel.setText("Vehicle: " + currentSession.getVehicleNumber());
        spotNumberLabel.setText("Spot: " + currentSession.getSpotNumber());
        entryTimeLabel.setText("Entry Time: " + 
            currentSession.getCheckIn().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        
        // Calculate duration and charges
        currentSession.calculateCharges();
        durationLabel.setText("Duration: " + formatDuration(currentSession.getDuration()));
        chargesLabel.setText(String.format("Charges: $%.2f", currentSession.getCharges()));
        totalAmountLabel.setText(String.format("Total Amount: $%.2f", 
                                             currentSession.getCharges()));

        // Show session details container
        fadeTransition(receiptInputContainer, sessionDetailsContainer);
    }

    // @FXML
    // private void handlePayment() {
    //     String paymentMethod = paymentMethodComboBox.getValue();
    //     payButton.setDisable(true);
    //     progressIndicator.setVisible(true);

    //     new Thread(() -> {
    //         PaymentService.PaymentResult paymentResult = 
    //             paymentService.processParkingPayment(currentSession);
            
    //         Platform.runLater(() -> {
    //             if (paymentResult.isSuccess()) {
    //                 handleSuccessfulPayment();
    //             } else {
    //                 showError("Payment failed: " + paymentResult.getMessage());
    //                 payButton.setDisable(false);
    //                 progressIndicator.setVisible(false);
    //             }
    //         });
    //     }).start();
    // }

    // private void handleSuccessfulPayment() {
    //     // End parking session
    //     ParkingResult result = sessionService.endSession(currentSession.getReceiptNumber());
        
    //     if (result.isSuccess()) {
    //         showSuccess("Payment successful! Gate opening...");
    //         printReceiptButton.setVisible(true);
    //         payButton.setVisible(false);
            
    //         // Simulate gate opening
    //         PauseTransition pause = new PauseTransition(Duration.seconds(2));
    //         pause.setOnFinished(e -> {
    //             showSuccess("Gate opened. Have a nice day!");
    //         });
    //         pause.play();
    //     } else {
    //         showError("Error ending session: " + result.getMessage());
    //         payButton.setDisable(false);
    //     }
        
    //     progressIndicator.setVisible(false);
    // }

    @FXML
    private void handlePrintReceipt() {
        printReceiptButton.setDisable(true);
        printReceiptButton.setText("Printing...");
        
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            showSuccess("Receipt printed successfully!");
            goToMainMenu();
        });
        pause.play();
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%d hours, %d minutes", hours, mins);
    }

    private void fadeTransition(Node fromNode, Node toNode) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), fromNode);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            fromNode.setVisible(false);
            toNode.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toNode);
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
private void handleReceiptValidation() {
    String receiptNumber = receiptNumberField.getText().trim();
    
    if (receiptNumber.isEmpty()) {
        showError("Please enter receipt number");
        return;
    }

    progressIndicator.setVisible(true);
    
    new Thread(() -> {
        ParkingSession session = sessionService.getSessionByReceipt(receiptNumber);
        
        Platform.runLater(() -> {
            if (session != null && session.isActive()) {
                currentSession = session;
                showSessionDetails();
            } else {
                showError("Invalid or inactive receipt number");
            }
            progressIndicator.setVisible(false);
        });
    }).start();
}

@FXML
private void handlePayment() {
    payButton.setDisable(true);
    progressIndicator.setVisible(true);

    new Thread(() -> {
        PaymentService.PaymentResult paymentResult = 
            paymentService.processParkingPayment(currentSession);
        
        Platform.runLater(() -> {
            if (paymentResult.isSuccess()) {
                handleSuccessfulPayment();
            } else {
                showError("Payment failed: " + paymentResult.getMessage());
                payButton.setDisable(false);
            }
            progressIndicator.setVisible(false);
        });
    }).start();
}

private void handleSuccessfulPayment() {
    ParkingResult result = sessionService.endSession(currentSession.getReceiptNumber());
    
    if (result.isSuccess()) {
        showSuccess("Payment successful! Gate opening...");
        printReceiptButton.setVisible(true);
        payButton.setVisible(false);
        
        // Simulate gate opening
        simulateGateOperation();
    } else {
        showError("Error ending session: " + result.getMessage());
        payButton.setDisable(false);
    }
}

private void simulateGateOperation() {
    PauseTransition pause = new PauseTransition(Duration.seconds(2));
    pause.setOnFinished(e -> {
        showSuccess("Gate opened. Have a nice day!");
    });
    pause.play();
}
}