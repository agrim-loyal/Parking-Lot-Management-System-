package com.parking.exception;

/**
 * Thrown when a ticket ID is not found or has already been settled.
 */
public class InvalidTicketException extends ParkingException {
    private static final long serialVersionUID = 1L;

    public InvalidTicketException(String ticketId) {
        super("ERR_INVALID_TICKET", "Invalid or already closed ticket: " + ticketId);
    }
}
