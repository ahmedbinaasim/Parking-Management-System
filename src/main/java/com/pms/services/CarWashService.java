package com.pms.services;

import com.pms.dao.*;
import com.pms.models.*;
import java.time.LocalDateTime;
import java.util.List;

public class CarWashService {
    private final CarWashRequestDAO carWashDAO;
    private final ParkingSessionDAO sessionDAO;
    private final UserDAO userDAO;
    private final NotificationService notificationService;

    
    private static final double CAR_WASH_CHARGE = 25.0;
    private static final int MAX_CONCURRENT_REQUESTS = 5;

    public CarWashService() {
        this.carWashDAO = new CarWashRequestDAO();
        this.notificationService = new NotificationService();
        this.userDAO = new UserDAO();
        this.sessionDAO = new ParkingSessionDAO();
    }

    public ParkingResult requestCarWash(int userId) {
        // Check for active parking session
        ParkingSession activeSession = sessionDAO.getActiveSession(userId);
        if (activeSession == null) {
            return new ParkingResult(false, "No active parking session found");
        }

        // Check service availability
        if (!isServiceAvailable()) {
            return new ParkingResult(false, "Car wash service currently unavailable");
        }

        // Create car wash request using the correct constructor
        CarWashRequest request = new CarWashRequest(
            userId,                        // userId
            activeSession.getHistoryId(),  // historyId
            activeSession.getVehicleNumber(), // vehicle number
            LocalDateTime.now()            // scheduled time (now)
        );

        request = carWashDAO.createRequest(request);
        if (request != null) {
            return new ParkingResult(true, 
                String.format("Car wash request scheduled. Charges: $%.2f", CAR_WASH_CHARGE),
                request.getRequestId());
        }

        return new ParkingResult(false, "Failed to schedule car wash");
    }

    public ParkingResult updateRequestStatus(int requestId, String newStatus) {
        CarWashRequest request = carWashDAO.getRequestById(requestId);
        if (request == null) {
            return new ParkingResult(false, "Car wash request not found");
        }
    
        if (carWashDAO.updateRequestStatus(requestId, newStatus)) {
            // If completed, notify user
            if (newStatus.equals("COMPLETED")) {
                User user = userDAO.getUserById(request.getUserId());
                if (user != null) {
                    notifyUserOfCompletion(user.getEmail(), requestId);
                }
            }
            return new ParkingResult(true, "Request status updated successfully");
        }
    
        return new ParkingResult(false, "Failed to update request status");
    }

    private boolean isServiceAvailable() {
        List<CarWashRequest> pendingRequests = carWashDAO.getPendingRequests();
        return pendingRequests.size() < MAX_CONCURRENT_REQUESTS;
    }

    private void notifyUserOfCompletion(String email, int requestId) {
        notificationService.sendEmail(
            email,
            "Car Wash Complete",
            "Your car wash service (Request ID: " + requestId + ") has been completed."
        );
    }

    public ParkingResult requestService(int userId, int historyId, String vehicleNumber, 
                                      LocalDateTime scheduledTime) {
        // Validate scheduled time
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            return new ParkingResult(false, "Cannot schedule for past time");
        }

        // Create new request
        CarWashRequest request = new CarWashRequest(
            userId,
            historyId,
            vehicleNumber,
            scheduledTime
        );

        // Save request
        request = carWashDAO.createRequest(request);
        if (request != null) {
            // Send notification
            notificationService.sendCarWashConfirmation(userId, request);
            return new ParkingResult(true, "Car wash service scheduled successfully", request);
        }

        return new ParkingResult(false, "Failed to schedule car wash service");
    }
}