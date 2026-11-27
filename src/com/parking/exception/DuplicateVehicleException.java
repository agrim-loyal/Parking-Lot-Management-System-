package com.parking.exception;

/**
 * Thrown when a vehicle with the same license plate is already parked.
 */
public class DuplicateVehicleException extends ParkingException {
    private static final long serialVersionUID = 1L;

    public DuplicateVehicleException(String licensePlate) {
        super("ERR_DUPLICATE_VEHICLE", "Vehicle already parked with plate: " + licensePlate);
    }
}
