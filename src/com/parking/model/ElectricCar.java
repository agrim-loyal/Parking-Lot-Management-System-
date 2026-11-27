package com.parking.model;

import com.parking.exception.InvalidVehicleException;

/**
 * Concrete vehicle representing an electric vehicle requiring charging or EV spot.
 */
public class ElectricCar extends Vehicle {
    private static final long serialVersionUID = 1L;

    public ElectricCar(String licensePlate, String ownerName, String color) throws InvalidVehicleException {
        super(licensePlate, ownerName, color);
    }

    @Override
    public SpotType getRequiredSpotType() {
        return SpotType.EV;
    }

    @Override
    public boolean canFitIn(SpotType spotType) {
        return spotType == SpotType.EV || spotType == SpotType.REGULAR || spotType == SpotType.LARGE;
    }

    @Override
    public String getVehicleCategory() {
        return "Electric Car";
    }

    @Override
    public double getRateMultiplier() {
        return 1.15; // Includes charging infrastructure surcharge
    }
}
