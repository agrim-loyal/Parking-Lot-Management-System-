package com.parking.model;

import java.io.Serializable;

/**
 * Represents a single parking floor containing a fixed array of ParkingSpots.
 * Demonstrates Unit 1 (Arrays) and Unit 2 (Non-static inner class: FloorSummary).
 */
public class ParkingFloor implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int floorNumber;
    private final ParkingSpot[] spots;

    public ParkingFloor(int floorNumber, int spotsCount) {
        this.floorNumber = floorNumber;
        this.spots = new ParkingSpot[spotsCount];
        initializeDefaultSpots();
    }

    private void initializeDefaultSpots() {
        for (int i = 0; i < spots.length; i++) {
            SpotType type;
            if (i < 2) {
                type = SpotType.MOTORCYCLE;
            } else if (i < 6) {
                type = SpotType.COMPACT;
            } else if (i < 9) {
                type = SpotType.REGULAR;
            } else if (i < 11) {
                type = SpotType.EV;
            } else {
                type = SpotType.LARGE;
            }
            String spotId = String.format("F%d-S%02d", floorNumber, i + 1);
            spots[i] = new ParkingSpot(spotId, floorNumber, type);
        }
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public ParkingSpot[] getSpots() {
        return spots;
    }

    public ParkingSpot findAvailableSpot(Vehicle vehicle) {
        // First pass: prefer exact required spot type
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied() && spot.getSpotType() == vehicle.getRequiredSpotType()) {
                return spot;
            }
        }
        // Second pass: any compatible larger/flexible spot
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied() && vehicle.canFitIn(spot.getSpotType())) {
                return spot;
            }
        }
        return null;
    }

    public ParkingSpot findSpotById(String spotId) {
        for (ParkingSpot spot : spots) {
            if (spot.getSpotId().equalsIgnoreCase(spotId)) {
                return spot;
            }
        }
        return null;
    }

    public FloorSummary getSummary() {
        return new FloorSummary();
    }

    /**
     * Non-static inner class (Unit 2: Inner classes) accessing enclosing floor's spots array.
     */
    public class FloorSummary {
        public int getTotalSpots() {
            return spots.length;
        }

        public int getOccupiedSpots() {
            int count = 0;
            for (ParkingSpot spot : spots) {
                if (spot.isOccupied()) {
                    count++;
                }
            }
            return count;
        }

        public int getAvailableSpots() {
            return getTotalSpots() - getOccupiedSpots();
        }

        @Override
        public String toString() {
            return String.format("Floor %d: %d/%d occupied (%d free)",
                    floorNumber, getOccupiedSpots(), getTotalSpots(), getAvailableSpots());
        }
    }
}
