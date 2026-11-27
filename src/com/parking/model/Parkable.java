package com.parking.model;

/**
 * Interface representing any entity that can be assigned to a parking spot.
 * Demonstrates Unit 2: Interfaces.
 */
public interface Parkable {
    String getLicensePlate();
    SpotType getRequiredSpotType();
    boolean canFitIn(SpotType spotType);
}
