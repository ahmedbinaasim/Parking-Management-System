// LostTicketService.java
package com.pms.services;

import com.pms.dao.LostTicketDAO;
import com.pms.dao.ParkingSessionDAO;
import com.pms.dao.UserDAO;
import com.pms.models.*;
import java.time.LocalDateTime;

public class LostTicketService {
    private final LostTicketDAO lostTicketDAO;
    private final ParkingSessionDAO sessionDAO;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final UserDAO userDAO;
    
    private static final double BASE_FINE = 50.0;

    public LostTicketService() {
        this.lostTicketDAO = new LostTicketDAO();
        this.sessionDAO = new ParkingSessionDAO();
        this.notificationService = new NotificationService();
        this.paymentService = new PaymentService();
        this.userDAO = new UserDAO();
    }

    public ParkingResult reportLostTicket(int userId, String vehicleNumber) {
        // Find active session
        ParkingSession session = sessionDAO.getActiveSessionByVehicle(userId, vehicleNumber);
        if (session == null) {
            return new ParkingResult(false, "No active parking session found for this vehicle");
        }

        // Create lost ticket with fine
        LostTicket lostTicket = new LostTicket(session.getHistoryId(), userId);
        lostTicket.setFine(BASE_FINE);
        
        lostTicket = lostTicketDAO.reportLostTicket(lostTicket);
        if (lostTicket != null) {
            User user = userDAO.getUserById(userId);
            if (user != null) {
                notificationService.sendEmail(
                    user.getEmail(),
                    "Lost Ticket Report",
                    String.format("Lost ticket reported. Fine amount: $%.2f", BASE_FINE)
                );
            }
            return new ParkingResult(true, "Lost ticket reported successfully", lostTicket);
        }

        return new ParkingResult(false, "Failed to report lost ticket");
    }

    public ParkingResult processLostTicketPayment(LostTicket lostTicket) {
        // Create violation for payment processing
        ParkingViolation violation = new ParkingViolation(
            0,                              // violation ID will be assigned by database
            lostTicket.getSessionId(),      // session ID
            lostTicket.getUserId(),         // user ID
            "LOST_TICKET",                  // violation type
            "Lost parking ticket",          // description
            lostTicket.getFine(),           // fine amount
            "PENDING",                      // status
            LocalDateTime.now(),            // reported at
            null,                           // resolved at (null until resolved)
            false,                          // disputed
            null                            // dispute reason
        );

        PaymentService.PaymentResult result = paymentService.processViolationPayment(violation);

        if (result.isSuccess()) {
            lostTicketDAO.resolveLostTicket(lostTicket.getLostTicketId());
            return new ParkingResult(true, 
                                   "Lost ticket fine paid successfully",
                                   result.getTransactionId());
        }

        return new ParkingResult(false, "Failed to process lost ticket payment");
    }
}