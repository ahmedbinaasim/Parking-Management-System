package com.pms.services;

import com.pms.dao.FeedbackDAO;
import com.pms.models.Feedback;
import com.pms.models.ParkingResult;
import java.time.LocalDateTime;
import java.util.List;

public class FeedbackService {
    private final FeedbackDAO feedbackDAO;
    private final NotificationService notificationService;
    
    private static final int MIN_CONTENT_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 500;

    public FeedbackService() {
        this.feedbackDAO = new FeedbackDAO();
        this.notificationService = new NotificationService();
    }

    public ParkingResult submitFeedback(int userId, String feedbackType, 
                                      String content, Integer rating) {
        // Validate input
        if (!isValidContent(content)) {
            return new ParkingResult(false, 
                "Feedback must be between 10 and 500 characters");
        }

        if (!isValidRating(rating)) {
            return new ParkingResult(false, "Rating must be between 1 and 5");
        }

        try {
            // Create feedback object
            Feedback feedback = new Feedback(
                0,  // ID will be assigned by database
                userId,
                feedbackType,
                content,
                rating,
                "PENDING",
                LocalDateTime.now(),
                null,
                null
            );

            // Save feedback
            feedback = feedbackDAO.submitFeedback(feedback);
            if (feedback != null) {
                // Notify admin of new feedback
                notifyAdminOfNewFeedback(feedback);
                return new ParkingResult(true, "Feedback submitted successfully", 
                                       feedback.getFeedbackId());
            }

            return new ParkingResult(false, "Failed to submit feedback");
        } catch (Exception e) {
            return new ParkingResult(false, "An error occurred while submitting feedback");
        }
    }

    public List<Feedback> getUserFeedback(int userId) {
        return feedbackDAO.getUserFeedback(userId);
    }

    public List<Feedback> getPendingFeedback() {
        return feedbackDAO.getPendingFeedback();
    }

    public boolean acknowledgeFeedback(int feedbackId, int adminId) {
        Feedback feedback = feedbackDAO.getFeedbackById(feedbackId);
        if (feedback == null) {
            return false;
        }

        boolean success = feedbackDAO.acknowledgeFeedback(feedbackId, adminId);
        if (success) {
            notifyUserOfAcknowledgment(feedback);
        }
        return success;
    }

    private boolean isValidContent(String content) {
        if (content == null) return false;
        content = content.trim();
        return content.length() >= MIN_CONTENT_LENGTH && 
               content.length() <= MAX_CONTENT_LENGTH;
    }

    private boolean isValidRating(Integer rating) {
        return rating != null && rating >= 1 && rating <= 5;
    }

    private void notifyAdminOfNewFeedback(Feedback feedback) {
        String message = String.format(
            "New feedback received:\nType: %s\nRating: %d/5\nContent: %s",
            feedback.getFeedbackType(),
            feedback.getRating(),
            feedback.getContent()
        );
        notificationService.sendAdminNotification("New Feedback", message);
    }

    private void notifyUserOfAcknowledgment(Feedback feedback) {
        String message = String.format(
            "Your feedback from %s has been reviewed by our team.\n" +
            "Thank you for helping us improve our service!",
            feedback.getCreatedAt().toLocalDate()
        );
        notificationService.sendEmail(feedback.getUserId(), 
            "Feedback Acknowledged", message);
    }
}