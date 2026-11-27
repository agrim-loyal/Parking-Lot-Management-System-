package com.parking.model;

/**
 * Defines supported parking spot categories and their hourly base rates.
 */
public enum SpotType {
    MOTORCYCLE("Motorcycle", 10.0),
    COMPACT("Compact", 20.0),
    REGULAR("Regular", 30.0),
    LARGE("Large", 50.0),
    EV("EV Charging", 40.0);

    private final String displayName;
    private final double hourlyRate;

    SpotType(String displayName, double hourlyRate) {
        this.displayName = displayName;
        this.hourlyRate = hourlyRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }
}
