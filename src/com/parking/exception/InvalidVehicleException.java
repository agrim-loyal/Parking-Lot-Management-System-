package com.parking.exception;

/**
 * Thrown when vehicle input data (e.g., blank license plate) fails validation.
 */
public class InvalidVehicleException extends ParkingException {
    private static final long serialVersionUID = 1L;

    public InvalidVehicleException(String message) {
        super("ERR_INVALID_VEHICLE", message);
    }
}
