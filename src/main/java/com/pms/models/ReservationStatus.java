package com.pms.models;

public enum ReservationStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    EXTENDED;

    public String toString() {
        return this.name();
    }
}