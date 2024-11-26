// ParkingHistoryController.java
package com.pms.controllers;

import com.pms.services.ParkingSessionService;
import com.pms.models.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParkingHistoryController {
    @FXML private TableView<ParkingSession> historyTable;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Label totalChargesLabel;
    @FXML private Label totalSessionsLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button exportButton;
    @FXML private VBox dateFilterContainer;

    private final ParkingSessionService sessionService;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ParkingHistoryController() {
        this.sessionService = new ParkingSessionService();
    }

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadHistory();
    }

    private void setupTable() {
        // Date/Time Column
        TableColumn<ParkingSession, String> dateCol = new TableColumn<>("Date/Time");
        dateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCheckIn().format(DATE_FORMATTER)
            )
        );
        dateCol.setPrefWidth(150);

        // Spot Column
        TableColumn<ParkingSession, String> spotCol = new TableColumn<>("Spot");
        spotCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getSpotNumber()
            )
        );
        spotCol.setPrefWidth(100);

        // Vehicle Column
        TableColumn<ParkingSession, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getVehicleNumber()
            )
        );
        vehicleCol.setPrefWidth(120);

        // Duration Column
        TableColumn<ParkingSession, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                formatDuration(data.getValue().getDuration())
            )
        );
        durationCol.setPrefWidth(120);

        // Status Column
        TableColumn<ParkingSession, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus().toString()
            )
        );
        statusCol.setPrefWidth(100);

        // Charges Column
        TableColumn<ParkingSession, String> chargesCol = new TableColumn<>("Charges");
        chargesCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", data.getValue().getCharges())
            )
        );
        chargesCol.setPrefWidth(100);

        historyTable.getColumns().addAll(
            dateCol, spotCol, vehicleCol, durationCol, statusCol, chargesCol
        );

        // Add row double-click handler for details
        historyTable.setRowFactory(tv -> {
            TableRow<ParkingSession> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showSessionDetails(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Time",
            "Today",
            "Last 7 Days",
            "Last 30 Days",
            "This Month",
            "Custom Range"
        ));

        filterComboBox.setValue("All Time");
        filterComboBox.setOnAction(e -> handleFilterChange());

        // Setup date pickers
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());

        fromDatePicker.setOnAction(e -> {
            if (toDatePicker.getValue() != null && 
                fromDatePicker.getValue().isAfter(toDatePicker.getValue())) {
                toDatePicker.setValue(fromDatePicker.getValue());
            }
            if (filterComboBox.getValue().equals("Custom Range")) {
                loadHistory();
            }
        });

        toDatePicker.setOnAction(e -> {
            if (fromDatePicker.getValue() != null && 
                toDatePicker.getValue().isBefore(fromDatePicker.getValue())) {
                fromDatePicker.setValue(toDatePicker.getValue());
            }
            if (filterComboBox.getValue().equals("Custom Range")) {
                loadHistory();
            }
        });

        dateFilterContainer.setVisible(false);
    }

    private void handleFilterChange() {
        String selectedFilter = filterComboBox.getValue();
        dateFilterContainer.setVisible("Custom Range".equals(selectedFilter));
        loadHistory();
    }

    @FXML
    private void loadHistory() {
        progressIndicator.setVisible(true);
        historyTable.setItems(null);

        new Thread(() -> {
            LocalDateTime fromDate = null;
            LocalDateTime toDate = null;

            switch (filterComboBox.getValue()) {
                case "Today":
                    fromDate = LocalDate.now().atStartOfDay();
                    toDate = LocalDateTime.now();
                    break;
                case "Last 7 Days":
                    fromDate = LocalDateTime.now().minusDays(7);
                    toDate = LocalDateTime.now();
                    break;
                case "Last 30 Days":
                    fromDate = LocalDateTime.now().minusDays(30);
                    toDate = LocalDateTime.now();
                    break;
                case "This Month":
                    fromDate = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                    toDate = LocalDateTime.now();
                    break;
                case "Custom Range":
                    if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                        fromDate = fromDatePicker.getValue().atStartOfDay();
                        toDate = toDatePicker.getValue().atTime(LocalTime.MAX);
                    }
                    break;
            }

            List<ParkingSession> sessions = sessionService.getUserParkingHistory(
                Session.getCurrentUserId(),
                fromDate,
                toDate
            );

            Platform.runLater(() -> {
                historyTable.setItems(FXCollections.observableArrayList(sessions));
                updateStatistics(sessions);
                progressIndicator.setVisible(false);
            });
        }).start();
    }

    private void updateStatistics(List<ParkingSession> sessions) {
        double totalCharges = sessions.stream()
            .mapToDouble(ParkingSession::getCharges)
            .sum();
        
        totalChargesLabel.setText(String.format("Total Charges: $%.2f", totalCharges));
        totalSessionsLabel.setText(String.format("Total Sessions: %d", sessions.size()));
    }

    private void showSessionDetails(ParkingSession session) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Session Details");
        dialog.setHeaderText(null);

        VBox content = new VBox(10);
        content.getStyleClass().add("session-details");
        content.getChildren().addAll(
            createDetailLabel("Vehicle:", session.getVehicleNumber()),
            createDetailLabel("Spot:", session.getSpotNumber()),
            createDetailLabel("Check-in:", formatDateTime(session.getCheckIn())),
            createDetailLabel("Check-out:", session.getCheckOut() != null ? 
                formatDateTime(session.getCheckOut()) : "Active"),
            createDetailLabel("Duration:", formatDuration(session.getDuration())),
            createDetailLabel("Charges:", String.format("$%.2f", session.getCharges())),
            createDetailLabel("Status:", session.getStatus().toString()),
            createDetailLabel("Receipt:", session.getReceiptNumber())
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/parking-history.css").toExternalForm()
        );

        dialog.show();
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Parking History");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("parking-history.csv");

        File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());
        if (file != null) {
            exportButton.setDisable(true);
            progressIndicator.setVisible(true);

            new Thread(() -> {
                boolean success = sessionService.exportParkingHistory(
                    Session.getCurrentUserId(),
                    file.getAbsolutePath()
                );

                Platform.runLater(() -> {
                    if (success) {
                        showSuccess("History exported successfully");
                    } else {
                        showError("Failed to export history");
                    }
                    exportButton.setDisable(false);
                    progressIndicator.setVisible(false);
                });
            }).start();
        }
    }

    private Label createDetailLabel(String label, String value) {
        return new Label(label + " " + value);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%d hours, %d minutes", hours, mins);
    }

    @FXML
    private void refreshHistory() {
        loadHistory();
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
}