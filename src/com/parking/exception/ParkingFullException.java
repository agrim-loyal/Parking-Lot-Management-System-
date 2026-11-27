package com.parking.exception;

/**
 * Thrown when no suitable parking spot is available for a vehicle.
 */
public class ParkingFullException extends ParkingException {
    private static final long serialVersionUID = 1L;

    public ParkingFullException(String vehicleType) {
        super("ERR_LOT_FULL", "No available parking spot for vehicle type: " + vehicleType);
    }
}
