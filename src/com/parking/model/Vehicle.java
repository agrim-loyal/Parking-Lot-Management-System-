package com.parking.model;

import com.parking.exception.InvalidVehicleException;
import java.io.Serializable;

/**
 * Abstract base class for all vehicles entering the parking lot.
 * Demonstrates Unit 1 (OOP), Unit 2 (Abstract class, access control, this),
 * and Unit 4 (Serializable).
 */
public abstract class Vehicle implements Parkable, Serializable {
    private static final long serialVersionUID = 1L;

    protected final String licensePlate;
    protected final String ownerName;
    protected final String color;

    protected Vehicle(String licensePlate, String ownerName, String color) throws InvalidVehicleException {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new InvalidVehicleException("License plate cannot be empty.");
        }
        String normalized = licensePlate.trim().toUpperCase();
        if (!normalized.matches("[A-Z0-9-]{3,15}")) {
            throw new InvalidVehicleException("License plate must be 3-15 alphanumeric/hyphen characters.");
        }
        this.licensePlate = normalized;
        this.ownerName = (ownerName == null || ownerName.trim().isEmpty()) ? "Walk-in" : ownerName.trim();
        this.color = (color == null || color.trim().isEmpty()) ? "Unspecified" : color.trim();
    }

    @Override
    public String getLicensePlate() {
        return licensePlate;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getColor() {
        return color;
    }

    public abstract String getVehicleCategory();

    /**
     * Multiplier applied on top of spot hourly rate (polymorphic per vehicle type).
     */
    public abstract double getRateMultiplier();

    @Override
    public String toString() {
        return String.format("%s [%s] (%s, Owner: %s)",
                getVehicleCategory(), licensePlate, color, ownerName);
    }
}
