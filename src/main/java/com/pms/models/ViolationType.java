package com.pms.models;

public enum ViolationType {
    OVERTIME_PARKING("Overtime Parking"),
    WRONG_SPOT("Wrong Parking Spot"),
    NO_TICKET("No Ticket Displayed"),
    IMPROPER_PARKING("Improper Parking"),
    BLOCKING_EXIT("Blocking Exit"),
    OTHER("Other Violation");

    private final String description;

    ViolationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}