package com.parking.model;

/**
 * Interface for entities capable of computing parking fees.
 * Demonstrates Unit 2: Interfaces.
 */
public interface Billable {
    double calculateFee(long exitTimeMillis);
}
