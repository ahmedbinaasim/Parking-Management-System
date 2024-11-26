// CarWashController.java
package com.pms.controllers;

import com.pms.services.*;
import com.pms.models.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.application.Platform;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CarWashController {
    @FXML private VBox mainContainer;
    @FXML private VBox requestContainer;
    @FXML private VBox paymentContainer;
    @FXML private VBox confirmationContainer;
    
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private TextField vehicleNumberField;
    @FXML private Label activeSessionLabel;
    @FXML private Label priceLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button requestButton;
    
    @FXML private Label paymentAmountLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Button payButton;
    @FXML private ProgressIndicator paymentProgressIndicator;
    
    @FXML private Label confirmationNumberLabel;
    @FXML private Label scheduledTimeLabel;
    @FXML private Label vehicleLabel;
    @FXML private Button printButton;

    private final CarWashService carWashService;
    private final PaymentService paymentService;
    private final ParkingSessionService sessionService;
    private CarWashRequest currentRequest;
    private ParkingSession activeSession;

    private static final double STANDARD_PRICE = 25.0;

    public CarWashController() {
        this.carWashService = new CarWashService();
        this.paymentService = new PaymentService();
        this.sessionService = new ParkingSessionService();
    }

    @FXML
    public void initialize() {
        setupUI();
        checkActiveSession();
        setupValidation();
    }

    private void setupUI() {
        // Initialize containers
        requestContainer.setVisible(true);
        paymentContainer.setVisible(false);
        confirmationContainer.setVisible(false);
        progressIndicator.setVisible(false);
        paymentProgressIndicator.setVisible(false);

        // Setup date picker
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Setup time slots
        setupTimeSlots();

        // Setup payment methods
        setupPaymentMethods();

        // Set price
        priceLabel.setText(String.format("Service Price: $%.2f", STANDARD_PRICE));
    }

    private void setupTimeSlots() {
        timeSlotComboBox.setItems(FXCollections.observableArrayList(
            "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
            "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"
        ));
    }

    private void setupPaymentMethods() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
            "Credit Card",
            "Debit Card",
            "Cash"
        ));
        paymentMethodComboBox.setValue("Credit Card");
    }

    private void setupValidation() {
        // Vehicle number validation
        vehicleNumberField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("[A-Za-z0-9-]*")) {
                vehicleNumberField.setText(old);
            }
        });

        // Enable/disable request button based on input
        requestButton.disableProperty().bind(
            vehicleNumberField.textProperty().isEmpty()
            .or(timeSlotComboBox.valueProperty().isNull())
            .or(datePicker.valueProperty().isNull())
        );
    }

    private void checkActiveSession() {
        new Thread(() -> {
            activeSession = sessionService.getActiveSession(Session.getCurrentUserId());
            Platform.runLater(() -> {
                if (activeSession == null) {
                    activeSessionLabel.setText("No active parking session found.");
                    activeSessionLabel.setStyle("-fx-text-fill: #e74c3c;");
                    requestButton.setDisable(true);
                } else {
                    activeSessionLabel.setText("Active session found: " + 
                                            activeSession.getSpotNumber());
                    activeSessionLabel.setStyle("-fx-text-fill: #27ae60;");
                    vehicleNumberField.setText(activeSession.getVehicleNumber());
                    vehicleNumberField.setEditable(false);
                }
            });
        }).start();
    }

    @FXML
    private void handleRequest() {
        if (!validateRequest()) {
            return;
        }

        LocalDateTime scheduledTime = LocalDateTime.of(
            datePicker.getValue(),
            LocalTime.parse(timeSlotComboBox.getValue(), 
                          DateTimeFormatter.ofPattern("hh:mm a"))
        );

        progressIndicator.setVisible(true);
        requestButton.setDisable(true);

        new Thread(() -> {
            ParkingResult result = carWashService.requestService(
                Session.getCurrentUserId(),
                activeSession.getHistoryId(),
                vehicleNumberField.getText(),
                scheduledTime
            );

            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                if (result.isSuccess()) {
                    currentRequest = (CarWashRequest) result.getData();
                    showPaymentDetails();
                } else {
                    showError(result.getMessage());
                    requestButton.setDisable(false);
                }
            });
        }).start();
    }

    private boolean validateRequest() {
        if (activeSession == null) {
            showError("Active parking session required");
            return false;
        }

        if (datePicker.getValue() == null) {
            showError("Please select a date");
            return false;
        }

        if (timeSlotComboBox.getValue() == null) {
            showError("Please select a time slot");
            return false;
        }

        return true;
    }

    private void showPaymentDetails() {
        paymentAmountLabel.setText(String.format("Amount to Pay: $%.2f", 
                                               currentRequest.getCharges()));
        fadeTransition(requestContainer, paymentContainer);
    }

    @FXML
    private void handlePayment() {
        paymentProgressIndicator.setVisible(true);
        payButton.setDisable(true);

        new Thread(() -> {
            PaymentService.PaymentResult result = 
                paymentService.processCarWashPayment(currentRequest);

            Platform.runLater(() -> {
                paymentProgressIndicator.setVisible(false);
                if (result.isSuccess()) {
                    showConfirmation(result.getTransactionId());
                } else {
                    showError("Payment failed: " + result.getMessage());
                    payButton.setDisable(false);
                }
            });
        }).start();
    }

    private void showConfirmation(String transactionId) {
        confirmationNumberLabel.setText("Confirmation #: " + currentRequest.getRequestId());
        scheduledTimeLabel.setText("Scheduled Time: " + 
            currentRequest.getScheduledTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
            ));
        vehicleLabel.setText("Vehicle: " + currentRequest.getVehicleNumber());
        
        fadeTransition(paymentContainer, confirmationContainer);
    }

    @FXML
    private void handlePrintConfirmation() {
        printButton.setDisable(true);
        printButton.setText("Printing...");

        // Simulate printing
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            showSuccess("Confirmation printed successfully!");
            goToMainMenu();
        });
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