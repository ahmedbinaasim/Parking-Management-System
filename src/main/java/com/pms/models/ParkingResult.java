package com.pms.models;

public class ParkingResult {
    private final boolean success;
    private final String message;
    private final Object data;

    public ParkingResult(boolean success, String message) {
        this(success, message, null);
    }

    public ParkingResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}