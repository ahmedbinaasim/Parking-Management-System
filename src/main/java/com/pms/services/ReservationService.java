package com.pms.services;

import com.pms.dao.*;
import com.pms.models.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservationService {
    private final ReservationDAO reservationDAO;
    private final ParkingSpotDAO spotDAO;
    private final ParkingSessionDAO sessionDAO;
    
    private static final int MIN_RESERVATION_HOURS = 1;
    private static final int MAX_RESERVATION_HOURS = 24;
    private static final int MAX_ACTIVE_RESERVATIONS = 3;
    private static final int CANCELLATION_NOTICE_HOURS = 1;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.spotDAO = new ParkingSpotDAO();
        this.sessionDAO = new ParkingSessionDAO();
    }

    public ParkingResult createReservation(int userId, String vehicleNumber, 
                                         String spotNumber, LocalDateTime startTime, 
                                         LocalDateTime endTime) {
        // Validate time constraints
        if (!isValidReservationTime(startTime, endTime)) {
            return new ParkingResult(false, "Invalid reservation time period");
        }

        // Check maximum active reservations
        List<ParkingReservation> activeReservations = reservationDAO.getUserReservations(userId);
        if (activeReservations.size() >= MAX_ACTIVE_RESERVATIONS) {
            return new ParkingResult(false, "Maximum active reservations limit reached");
        }

        // Check spot availability
        if (!reservationDAO.isSpotAvailable(spotNumber, startTime, endTime)) {
            return new ParkingResult(false, "Spot not available for selected time period");
        }

        // Create reservation
        ParkingReservation reservation = new ParkingReservation(userId, spotNumber, 
                                                              vehicleNumber, startTime, endTime);
        reservation = reservationDAO.createReservation(reservation);
        
        if (reservation != null) {
            // Update spot status
            spotDAO.setSpotReservation(spotNumber, userId);
            return new ParkingResult(true, "Reservation created successfully", 
                                   reservation.getReservationId());
        }

        return new ParkingResult(false, "Failed to create reservation");
    }

    public ParkingResult cancelReservation(int reservationId, String reason) {
        ParkingReservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null) {
            return new ParkingResult(false, "Reservation not found");
        }

        // Check if cancellation is allowed
        if (!canCancelReservation(reservation)) {
            return new ParkingResult(false, 
                "Cancellation not allowed within " + CANCELLATION_NOTICE_HOURS + 
                " hours of start time");
        }

        if (reservationDAO.cancelReservation(reservationId, reason)) {
            // Free up the spot
            spotDAO.setSpotReservation(reservation.getSpotNumber(), null);
            return new ParkingResult(true, "Reservation cancelled successfully");
        }

        return new ParkingResult(false, "Failed to cancel reservation");
    }

    public ParkingResult extendReservation(int reservationId, LocalDateTime newEndTime) {
        ParkingReservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null) {
            return new ParkingResult(false, "Reservation not found");
        }

        // Validate new end time
        if (!isValidExtensionTime(reservation, newEndTime)) {
            return new ParkingResult(false, "Invalid extension time");
        }

        // Check spot availability for extended period
        if (!reservationDAO.isSpotAvailable(reservation.getSpotNumber(), 
                                          reservation.getEndTime(), newEndTime)) {
            return new ParkingResult(false, "Spot not available for extension period");
        }

        if (reservationDAO.extendReservation(reservationId, newEndTime)) {
            return new ParkingResult(true, "Reservation extended successfully");
        }

        return new ParkingResult(false, "Failed to extend reservation");
    }

    private boolean isValidReservationTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        long durationHours = ChronoUnit.HOURS.between(startTime, endTime);
        
        return startTime.isAfter(now) &&
               endTime.isAfter(startTime) &&
               durationHours >= MIN_RESERVATION_HOURS &&
               durationHours <= MAX_RESERVATION_HOURS;
    }

    private boolean canCancelReservation(ParkingReservation reservation) {
        return LocalDateTime.now().plusHours(CANCELLATION_NOTICE_HOURS)
                .isBefore(reservation.getStartTime());
    }

    private boolean isValidExtensionTime(ParkingReservation reservation, 
                                       LocalDateTime newEndTime) {
        return newEndTime.isAfter(reservation.getEndTime()) &&
               ChronoUnit.HOURS.between(reservation.getStartTime(), newEndTime) <= 
               MAX_RESERVATION_HOURS;
    }
}