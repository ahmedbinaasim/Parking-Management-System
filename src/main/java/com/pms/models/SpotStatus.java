// Update SpotStatus.java
package com.pms.models;

public enum SpotStatus {
    AVAILABLE,
    RESERVED,
    OCCUPIED,
    MAINTENANCE,  // Added for spots under maintenance
    OUT_OF_SERVICE;  // Added for temporarily unusable spots

    @Override
    public String toString() {
        return this.name();
    }
}