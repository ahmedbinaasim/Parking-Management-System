package com.pms.controllers;

import com.pms.services.FeedbackService;
import com.pms.models.Feedback;
import com.pms.models.ParkingResult;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.application.Platform;
import java.time.format.DateTimeFormatter;
import javafx.scene.Node;
import javafx.scene.text.Text;
import java.util.List;

public class FeedbackController {
    @FXML private ToggleGroup feedbackType;
    @FXML private RadioButton generalRadio;
    @FXML private RadioButton serviceRadio;
    @FXML private RadioButton facilityRadio;
    @FXML private RadioButton suggestionRadio;
    @FXML private RadioButton complaintRadio;
    @FXML private TextArea feedbackContent;
    @FXML private Label characterCount;
    @FXML private Label ratingLabel;
    @FXML private Button submitButton;
    @FXML private VBox previousFeedbackContainer;
    @FXML private StackPane loadingOverlay;
    @FXML private Label loadingLabel;
    @FXML private Button star1;
    @FXML private Button star2;
    @FXML private Button star3;
    @FXML private Button star4;
    @FXML private Button star5;

    private final FeedbackService feedbackService;
    private int currentRating = 0;
    private static final int MAX_CHARS = 500;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public FeedbackController() {
        this.feedbackService = new FeedbackService();
    }

    @FXML
    public void initialize() {
        setupCharacterCount();
        setupStarRating();
        setupValidation();
        loadPreviousFeedback();
    }

    private void setupCharacterCount() {
        feedbackContent.textProperty().addListener((obs, old, newVal) -> {
            int remaining = MAX_CHARS - newVal.length();
            characterCount.setText(remaining + " characters remaining");
            
            if (remaining < 0) {
                characterCount.setStyle("-fx-text-fill: #e74c3c;");
                feedbackContent.setText(old);
            } else {
                characterCount.setStyle("-fx-text-fill: #7f8c8d;");
            }
            
            updateSubmitButton();
        });
    }

    private void setupStarRating() {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnAction(e -> updateRating(rating));
            
            // Set star icons
            Text starIcon = new Text("★");
            starIcon.getStyleClass().add("star-icon");
            stars[i].setGraphic(starIcon);
        }
    }

    private void updateRating(int rating) {
        currentRating = rating;
        Button[] stars = {star1, star2, star3, star4, star5};
        
        for (int i = 0; i < stars.length; i++) {
            stars[i].getStyleClass().remove("selected");
            if (i < rating) {
                stars[i].getStyleClass().add("selected");
            }
        }

        String[] ratingTexts = {
            "Poor", "Fair", "Good", "Very Good", "Excellent"
        };
        ratingLabel.setText(ratingTexts[rating - 1]);
        
        updateSubmitButton();
    }

    private void setupValidation() {
        submitButton.setDisable(true);
        feedbackType.selectedToggleProperty().addListener((obs, old, newVal) -> 
            updateSubmitButton());
    }

    private void updateSubmitButton() {
        boolean isValid = feedbackContent.getText().trim().length() > 0 &&
                         feedbackContent.getText().length() <= MAX_CHARS &&
                         feedbackType.getSelectedToggle() != null &&
                         currentRating > 0;
        
        submitButton.setDisable(!isValid);
    }

    @FXML
    private void handleSubmit() {
        String type = ((RadioButton) feedbackType.getSelectedToggle()).getText().toUpperCase();
        String content = feedbackContent.getText().trim();
        
        loadingOverlay.setVisible(true);
        submitButton.setDisable(true);

        new Thread(() -> {
            try {
                ParkingResult result = feedbackService.submitFeedback(
                    Session.getCurrentUserId(),
                    type,
                    content,
                    currentRating
                );

                Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        showSuccess("Thank you for your feedback!");
                        clearForm();
                        loadPreviousFeedback();
                    } else {
                        showError(result.getMessage());
                    }
                    loadingOverlay.setVisible(false);
                    submitButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("An error occurred. Please try again.");
                    loadingOverlay.setVisible(false);
                    submitButton.setDisable(false);
                });
            }
        }).start();
    }

    private void loadPreviousFeedback() {
        previousFeedbackContainer.getChildren().clear();
        
        new Thread(() -> {
            List<Feedback> feedbackList = feedbackService.getUserFeedback(
                Session.getCurrentUserId()
            );
            
            Platform.runLater(() -> {
                if (feedbackList.isEmpty()) {
                    Label emptyLabel = new Label("No previous feedback");
                    emptyLabel.getStyleClass().add("empty-label");
                    previousFeedbackContainer.getChildren().add(emptyLabel);
                } else {
                    feedbackList.forEach(this::addFeedbackCard);
                }
            });
        }).start();
    }

    private void addFeedbackCard(Feedback feedback) {
        VBox card = new VBox(5);
        card.getStyleClass().add("feedback-card");
        
        // Feedback date
        Label dateLabel = new Label(feedback.getCreatedAt().format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("feedback-date");
        
        // Feedback type
        Label typeLabel = new Label(feedback.getFeedbackType());
        typeLabel.getStyleClass().add("feedback-type");
        
        // Rating
        // Rating display
        HBox ratingBox = new HBox(2);
        ratingBox.getStyleClass().add("feedback-rating");
        for (int i = 0; i < feedback.getRating(); i++) {
            Text star = new Text("★");
            star.getStyleClass().add("star-icon");
            ratingBox.getChildren().add(star);
        }

        // Feedback content
        Label contentLabel = new Label(feedback.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("feedback-content");

        // Status label
        Label statusLabel = new Label(feedback.getStatus());
        statusLabel.getStyleClass().addAll("feedback-status", feedback.getStatus().toLowerCase());

        // Acknowledgment info if available
        if (feedback.getAcknowledgedAt() != null) {
            Label acknowledgedLabel = new Label("Acknowledged on: " + 
                feedback.getAcknowledgedAt().format(DATE_FORMATTER));
            acknowledgedLabel.getStyleClass().add("acknowledgment-info");
            card.getChildren().addAll(dateLabel, typeLabel, ratingBox, 
                                    contentLabel, statusLabel, acknowledgedLabel);
        } else {
            card.getChildren().addAll(dateLabel, typeLabel, ratingBox, 
                                    contentLabel, statusLabel);
        }

        previousFeedbackContainer.getChildren().add(0, card);
    }

    private void clearForm() {
        feedbackType.selectToggle(generalRadio);
        feedbackContent.clear();
        currentRating = 0;
        updateRating(0);
        
        Button[] stars = {star1, star2, star3, star4, star5};
        for (Button star : stars) {
            star.getStyleClass().remove("selected");
        }
        ratingLabel.setText("");
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