package com.parking.model;

import com.parking.exception.InvalidVehicleException;

/**
 * Concrete vehicle representing a motorcycle/two-wheeler.
 * Demonstrates Unit 2: Inheritance and super keyword.
 */
public class Motorcycle extends Vehicle {
    private static final long serialVersionUID = 1L;

    public Motorcycle(String licensePlate, String ownerName, String color) throws InvalidVehicleException {
        super(licensePlate, ownerName, color);
    }

    @Override
    public SpotType getRequiredSpotType() {
        return SpotType.MOTORCYCLE;
    }

    @Override
    public boolean canFitIn(SpotType spotType) {
        return spotType == SpotType.MOTORCYCLE || spotType == SpotType.COMPACT || spotType == SpotType.REGULAR;
    }

    @Override
    public String getVehicleCategory() {
        return "Motorcycle";
    }

    @Override
    public double getRateMultiplier() {
        return 1.0;
    }
}
