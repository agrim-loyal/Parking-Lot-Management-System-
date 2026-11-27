package com.parking.model;

import com.parking.exception.InvalidVehicleException;

/**
 * Concrete vehicle representing a standard passenger car.
 */
public class Car extends Vehicle {
    private static final long serialVersionUID = 1L;

    public Car(String licensePlate, String ownerName, String color) throws InvalidVehicleException {
        super(licensePlate, ownerName, color);
    }

    @Override
    public SpotType getRequiredSpotType() {
        return SpotType.COMPACT;
    }

    @Override
    public boolean canFitIn(SpotType spotType) {
        return spotType == SpotType.COMPACT || spotType == SpotType.REGULAR || spotType == SpotType.LARGE;
    }

    @Override
    public String getVehicleCategory() {
        return "Car";
    }

    @Override
    public double getRateMultiplier() {
        return 1.0;
    }
}
