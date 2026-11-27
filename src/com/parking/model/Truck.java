package com.parking.model;

import com.parking.exception.InvalidVehicleException;

/**
 * Concrete vehicle representing a heavy truck/SUV/van.
 */
public class Truck extends Vehicle {
    private static final long serialVersionUID = 1L;

    public Truck(String licensePlate, String ownerName, String color) throws InvalidVehicleException {
        super(licensePlate, ownerName, color);
    }

    @Override
    public SpotType getRequiredSpotType() {
        return SpotType.LARGE;
    }

    @Override
    public boolean canFitIn(SpotType spotType) {
        return spotType == SpotType.LARGE;
    }

    @Override
    public String getVehicleCategory() {
        return "Truck";
    }

    @Override
    public double getRateMultiplier() {
        return 1.25;
    }
}
