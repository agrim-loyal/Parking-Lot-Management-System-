package com.parking.model;

import java.io.Serializable;

/**
 * Represents a single physical parking spot on a floor.
 * Demonstrates synchronization at spot level and Serializable support.
 */
public class ParkingSpot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String spotId;
    private final int floorNumber;
    private final SpotType spotType;
    private Vehicle parkedVehicle;
    private String activeTicketId;

    public ParkingSpot(String spotId, int floorNumber, SpotType spotType) {
        this.spotId = spotId;
        this.floorNumber = floorNumber;
        this.spotType = spotType;
    }

    public String getSpotId() {
        return spotId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public synchronized boolean isOccupied() {
        return parkedVehicle != null;
    }

    public synchronized Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public synchronized String getActiveTicketId() {
        return activeTicketId;
    }

    public synchronized boolean assignVehicle(Vehicle vehicle, String ticketId) {
        if (parkedVehicle != null || !vehicle.canFitIn(spotType)) {
            return false;
        }
        this.parkedVehicle = vehicle;
        this.activeTicketId = ticketId;
        return true;
    }

    public synchronized Vehicle vacate() {
        Vehicle removed = this.parkedVehicle;
        this.parkedVehicle = null;
        this.activeTicketId = null;
        return removed;
    }

    @Override
    public String toString() {
        return spotId + " (" + spotType.getDisplayName() + ") - "
                + (isOccupied() ? "Occupied by " + parkedVehicle.getLicensePlate() : "Available");
    }
}
