// LostTicketController.java
package com.pms.controllers;

import com.pms.services.LostTicketService;
import com.pms.models.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.util.Duration;

public class LostTicketController {
    @FXML private VBox mainContainer;
    @FXML private VBox reportContainer;
    @FXML private VBox verificationContainer;
    @FXML private VBox paymentContainer;
    
    @FXML private TextField vehicleNumberField;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button reportButton;
    
    // Payment details
    @FXML private Label fineAmountLabel;
    @FXML private Button payFineButton;
    @FXML private VBox paymentStatusContainer;
    
    private final LostTicketService lostTicketService;
    private LostTicket currentLostTicket;

    public LostTicketController() {
        this.lostTicketService = new LostTicketService();
    }

    @FXML
    public void initialize() {
        setupUI();
        setupValidation();
    }

    private void setupUI() {
        reportContainer.setVisible(true);
        verificationContainer.setVisible(false);
        paymentContainer.setVisible(false);
        progressIndicator.setVisible(false);
    }

    private void setupValidation() {
        vehicleNumberField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("[A-Za-z0-9-]*")) {
                vehicleNumberField.setText(old);
            }
        });
    }

    @FXML
    private void handleLostTicketReport() {
        String vehicleNumber = vehicleNumberField.getText().trim();
        
        if (vehicleNumber.isEmpty()) {
            showError("Please enter vehicle number");
            return;
        }

        progressIndicator.setVisible(true);
        reportButton.setDisable(true);

        new Thread(() -> {
            ParkingResult result = lostTicketService.reportLostTicket(
                Session.getCurrentUserId(),
                vehicleNumber
            );

            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    currentLostTicket = (LostTicket) result.getData();
                    showPaymentDetails();
                } else {
                    showError(result.getMessage());
                    progressIndicator.setVisible(false);
                    reportButton.setDisable(false);
                }
            });
        }).start();
    }

    private void showPaymentDetails() {
        fineAmountLabel.setText(String.format("Fine Amount: $%.2f", 
                                            currentLostTicket.getFine()));
        
        fadeTransition(reportContainer, paymentContainer);
    }

    @FXML
    private void handleFinePayment() {
        progressIndicator.setVisible(true);
        payFineButton.setDisable(true);

        new Thread(() -> {
            ParkingResult result = lostTicketService.processLostTicketPayment(currentLostTicket);

            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    showSuccess("Fine paid successfully. Transaction ID: " + 
                              result.getData());
                    showPaymentStatus(true);
                } else {
                    showError("Payment failed: " + result.getMessage());
                    payFineButton.setDisable(false);
                }
                progressIndicator.setVisible(false);
            });
        }).start();
    }

    private void showPaymentStatus(boolean success) {
        paymentStatusContainer.setVisible(true);
        payFineButton.setVisible(false);
        
        // Auto-return to main menu after delay
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> goToMainMenu());
        pause.play();
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
    private void goToMainMenu() {
        SceneManager.switchScene("MainMenu.fxml");
    }
}