package com.parking.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents an issued parking ticket.
 * Demonstrates Unit 2: Static nested class (TicketBuilder), Wrapper classes, Interfaces (Billable).
 */
public class ParkingTicket implements Billable, Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        ACTIVE, PAID
    }

    private final String ticketId;
    private final Vehicle vehicle;
    private final String spotId;
    private final int floorNumber;
    private final SpotType spotType;
    private final long entryTimeMillis;
    private Long exitTimeMillis; // Wrapper class (nullable until exit)
    private Double amountPaid;   // Wrapper class (nullable until exit)
    private Status status;

    private ParkingTicket(TicketBuilder builder) {
        this.ticketId = builder.ticketId;
        this.vehicle = builder.vehicle;
        this.spotId = builder.spotId;
        this.floorNumber = builder.floorNumber;
        this.spotType = builder.spotType;
        this.entryTimeMillis = builder.entryTimeMillis;
        this.status = Status.ACTIVE;
    }

    /**
     * Static nested Builder class (Unit 2: Static classes).
     */
    public static class TicketBuilder {
        private String ticketId;
        private Vehicle vehicle;
        private String spotId;
        private int floorNumber;
        private SpotType spotType;
        private long entryTimeMillis = System.currentTimeMillis();

        public TicketBuilder ticketId(String ticketId) {
            this.ticketId = ticketId;
            return this;
        }

        public TicketBuilder vehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public TicketBuilder spot(ParkingSpot spot) {
            this.spotId = spot.getSpotId();
            this.floorNumber = spot.getFloorNumber();
            this.spotType = spot.getSpotType();
            return this;
        }

        public TicketBuilder entryTimeMillis(long entryTimeMillis) {
            this.entryTimeMillis = entryTimeMillis;
            return this;
        }

        public ParkingTicket build() {
            return new ParkingTicket(this);
        }
    }

    @Override
    public double calculateFee(long exitTime) {
        long durationMillis = Math.max(1L, exitTime - entryTimeMillis);
        // Minimum 1 hour billing; each started hour counts as a full hour
        long hours = (long) Math.ceil(durationMillis / 3600000.0);
        if (hours < 1L) {
            hours = 1L;
        }
        return Math.round(hours * spotType.getHourlyRate() * vehicle.getRateMultiplier() * 100.0) / 100.0;
    }

    public double settle(long exitTime) {
        double fee = calculateFee(exitTime);
        this.exitTimeMillis = Long.valueOf(exitTime);
        this.amountPaid = Double.valueOf(fee);
        this.status = Status.PAID;
        return fee;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getSpotId() {
        return spotId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public long getEntryTimeMillis() {
        return entryTimeMillis;
    }

    public Long getExitTimeMillis() {
        return exitTimeMillis;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public Status getStatus() {
        return status;
    }

    public String getFormattedEntryTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(entryTimeMillis));
    }

    public String getFormattedExitTime() {
        if (exitTimeMillis == null) {
            return "-";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(exitTimeMillis));
    }
}
