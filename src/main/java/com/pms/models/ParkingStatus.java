package com.pms.models;

public enum ParkingStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED,
    CANCELLED,
    VIOLATION,    // Added for violation cases
    LOST_TICKET;  // Added for lost ticket cases

    @Override
    public String toString() {
        return this.name();
    }
}


