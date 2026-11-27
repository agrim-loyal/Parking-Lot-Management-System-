package com.parking.exception;

/**
 * Base checked exception for all Parking Lot Management System errors.
 * Demonstrates Unit 3: Exception as objects & custom exception hierarchy.
 */
public class ParkingException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String errorCode;

    public ParkingException(String message) {
        super(message);
        this.errorCode = "ERR_PARKING";
    }

    public ParkingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ParkingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
