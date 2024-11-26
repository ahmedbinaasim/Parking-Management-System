package com.pms.models;

import java.time.LocalDateTime;

public class Feedback {
    private int feedbackId;
    private int userId;
    private String feedbackType;
    private String content;
    private Integer rating;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private Integer acknowledgedBy;

    public Feedback(int feedbackId, int userId, String feedbackType, String content,
                   Integer rating, String status, LocalDateTime createdAt,
                   LocalDateTime acknowledgedAt, Integer acknowledgedBy) {
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.feedbackType = feedbackType;
        this.content = content;
        this.rating = rating;
        this.status = status;
        this.createdAt = createdAt;
        this.acknowledgedAt = acknowledgedAt;
        this.acknowledgedBy = acknowledgedBy;
    }

    // Getters and setters
    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public Integer getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(Integer acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
}