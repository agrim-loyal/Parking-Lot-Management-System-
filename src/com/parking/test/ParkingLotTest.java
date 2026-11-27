package com.parking.test;

import com.parking.exception.DuplicateVehicleException;
import com.parking.exception.InvalidTicketException;
import com.parking.exception.InvalidVehicleException;
import com.parking.exception.ParkingFullException;
import com.parking.model.Car;
import com.parking.model.ElectricCar;
import com.parking.model.Motorcycle;
import com.parking.model.ParkingTicket;
import com.parking.model.Truck;
import com.parking.service.ParkingLotService;
import com.parking.util.ReflectionInspector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Self-contained verification suite testing all 5 units of the BCO 035B syllabus:
 * OOP & Reflection, Classes/Interfaces/Nested/Inner, Exceptions, IO/Serialization/Pipes,
 * and Multithreading synchronization.
 */
public class ParkingLotTest {

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("ASSERTION FAILED: " + message);
        }
    }

    public static void main(String[] args) throws Exception {
        String testStatePath = "data/test_parking_state.ser";
        String testCsvPath = "data/test_report.csv";
        new File(testStatePath).delete();
        new File(testCsvPath).delete();

        // 1. Initialize 1 floor with 12 spots (2 MC, 4 Compact, 3 Regular, 2 EV, 1 Large)
        final ParkingLotService service = new ParkingLotService(1, 12);

        // 2. Test OOP Polymorphism & Parking
        ParkingTicket t1 = service.parkVehicle(new Motorcycle("MC-101", "Alice", "Black"));
        ParkingTicket t2 = service.parkVehicle(new Car("CAR-202", "Bob", "Silver"));
        ParkingTicket t3 = service.parkVehicle(new ElectricCar("EV-303", "Carol", "White"));
        ParkingTicket t4 = service.parkVehicle(new Truck("TRK-404", "Dave", "Red"));

        assertTrue(t1 != null && t2 != null && t3 != null && t4 != null, "All 4 vehicle types should park.");
        assertTrue(service.getStatistics().getOccupiedSpots() == 4, "4 spots should be occupied.");

        // 3. Test Custom Exception Hierarchy
        boolean caughtDuplicate = false;
        try {
            service.parkVehicle(new Car("CAR-202", "Bob", "Silver"));
        } catch (DuplicateVehicleException e) {
            caughtDuplicate = true;
        }
        assertTrue(caughtDuplicate, "DuplicateVehicleException should be thrown.");

        boolean caughtInvalidVehicle = false;
        try {
            new Car("   ", "Nobody", "Blue");
        } catch (InvalidVehicleException e) {
            caughtInvalidVehicle = true;
        }
        assertTrue(caughtInvalidVehicle, "InvalidVehicleException should be thrown for blank plate.");

        boolean caughtFull = false;
        try {
            // Only 1 LARGE spot exists on 1 floor of 12 spots, already taken by TRK-404
            service.parkVehicle(new Truck("TRK-999", "Eve", "Gray"));
        } catch (ParkingFullException e) {
            caughtFull = true;
        }
        assertTrue(caughtFull, "ParkingFullException should be thrown when no LARGE spot is left.");

        boolean caughtInvalidTicket = false;
        try {
            service.exitVehicle("TKT-UNKNOWN");
        } catch (InvalidTicketException e) {
            caughtInvalidTicket = true;
        }
        assertTrue(caughtInvalidTicket, "InvalidTicketException should be thrown for unknown ticket.");

        // 4. Test Serialization & Deserialization (Unit 4)
        service.saveToDisk(testStatePath);
        ParkingLotService restored = new ParkingLotService(1, 12);
        boolean loaded = restored.loadFromDisk(testStatePath);
        assertTrue(loaded, "State should load from serialized file.");
        assertTrue(restored.getStatistics().getOccupiedSpots() == 4, "Restored lot should have 4 occupied spots.");

        // 5. Test Exit & Billing
        ParkingTicket settled = restored.exitVehicle(t2.getTicketId());
        assertTrue(settled.getStatus() == ParkingTicket.Status.PAID, "Ticket status must be PAID.");
        assertTrue(settled.getAmountPaid() != null && settled.getAmountPaid() > 0.0, "Fee must be positive.");

        // 6. Test CSV Export (Unit 4)
        restored.exportReportCsv(testCsvPath);
        assertTrue(new File(testCsvPath).exists(), "CSV report file should exist.");

        // 7. Test Reflection Inspector (Unit 1)
        String report = ReflectionInspector.inspectClass("com.parking.model.ParkingTicket");
        assertTrue(report.contains("calculateFee"), "Reflection report should list calculateFee method.");

        // 8. Test Multithreaded Concurrent Parking (Unit 5)
        final ParkingLotService concurrentLot = new ParkingLotService(3, 12);
        List<Thread> workers = new ArrayList<Thread>();
        for (int i = 0; i < 4; i++) {
            final int workerId = i;
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 5; j++) {
                        try {
                            concurrentLot.parkVehicle(new Car("TH" + workerId + "-" + j, "User", "White"));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }, "Worker-" + i);
            workers.add(th);
            th.start();
        }
        for (Thread th : workers) {
            th.join();
        }
        assertTrue(concurrentLot.getStatistics().getOccupiedSpots() == 20,
                "All 20 concurrent cars should park safely without race conditions.");

        // Give piped audit logger a brief moment to flush
        Thread.sleep(200);
        assertTrue(!concurrentLot.getAuditLogger().getRecentLogs().isEmpty(),
                "Piped audit logger should have recorded events.");

        service.shutdown();
        restored.shutdown();
        concurrentLot.shutdown();

        // Clean up test artifacts
        new File(testStatePath).delete();
        new File(testCsvPath).delete();

        System.out.println("ALL BCO 035B PARKING LOT TESTS PASSED!");
    }
}
