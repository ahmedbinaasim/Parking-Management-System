package com.pms.models;

public class ParkingSpot {
    private int spotId;
    private String spotNumber;
    private SpotStatus status;
    private int floorNumber;      // Added field
    private String type;          // Added field
    private Integer reservedBy;

    public ParkingSpot(int spotId, String spotNumber, SpotStatus status, 
                      int floorNumber, String type, Integer reservedBy) {
        this.spotId = spotId;
        this.spotNumber = spotNumber;
        this.status = status;
        this.floorNumber = floorNumber;
        this.type = type;
        this.reservedBy = reservedBy;
    }

    // Getters and setters
    public int getSpotId() { return spotId; }
    public String getSpotNumber() { return spotNumber; }
    public SpotStatus getStatus() { return status; }
    public void setStatus(SpotStatus status) { this.status = status; }
    public void setSpotId(int id) { this.spotId = id; }
    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getReservedBy() { return reservedBy; }
    public void setReservedBy(Integer reservedBy) { this.reservedBy = reservedBy; }
}
