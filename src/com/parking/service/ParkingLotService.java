package com.parking.service;

import com.parking.exception.DuplicateVehicleException;
import com.parking.exception.InvalidTicketException;
import com.parking.exception.ParkingException;
import com.parking.exception.ParkingFullException;
import com.parking.io.ParkingPersistenceManager;
import com.parking.io.ParkingPersistenceManager.ParkingLotState;
import com.parking.io.PipeAuditLogger;
import com.parking.model.ParkingFloor;
import com.parking.model.ParkingSpot;
import com.parking.model.ParkingTicket;
import com.parking.model.Vehicle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe central service managing floors, spots, ticketing, persistence, and audit logging.
 * Demonstrates Unit 2 (Static nested class, access control), Unit 3 (throws/throw),
 * Unit 4 (Serialization/Piped logging), and Unit 5 (Thread synchronization).
 */
public class ParkingLotService {
    public static final String DEFAULT_STATE_FILE = "data/parking_state.ser";
    public static final String DEFAULT_AUDIT_FILE = "data/audit_log.txt";

    private ParkingFloor[] floors;
    private Map<String, ParkingTicket> activeTickets;
    private List<ParkingTicket> completedTickets;
    private long ticketCounter;
    private double totalRevenue;
    private final PipeAuditLogger auditLogger;
    private final List<Runnable> changeListeners = new ArrayList<Runnable>();

    /**
     * Static nested class summarizing overall lot metrics (Unit 2: Static classes).
     */
    public static class LotStatistics {
        private final int totalSpots;
        private final int occupiedSpots;
        private final int availableSpots;
        private final int activeTicketCount;
        private final int completedTicketCount;
        private final double totalRevenue;

        public LotStatistics(int totalSpots, int occupiedSpots, int activeTicketCount,
                             int completedTicketCount, double totalRevenue) {
            this.totalSpots = totalSpots;
            this.occupiedSpots = occupiedSpots;
            this.availableSpots = totalSpots - occupiedSpots;
            this.activeTicketCount = activeTicketCount;
            this.completedTicketCount = completedTicketCount;
            this.totalRevenue = totalRevenue;
        }

        public int getTotalSpots() { return totalSpots; }
        public int getOccupiedSpots() { return occupiedSpots; }
        public int getAvailableSpots() { return availableSpots; }
        public int getActiveTicketCount() { return activeTicketCount; }
        public int getCompletedTicketCount() { return completedTicketCount; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public ParkingLotService(int numFloors, int spotsPerFloor) throws IOException {
        this.floors = new ParkingFloor[numFloors];
        for (int i = 0; i < numFloors; i++) {
            this.floors[i] = new ParkingFloor(i + 1, spotsPerFloor);
        }
        this.activeTickets = new LinkedHashMap<String, ParkingTicket>();
        this.completedTickets = new ArrayList<ParkingTicket>();
        this.ticketCounter = 1000L;
        this.totalRevenue = 0.0;
        this.auditLogger = new PipeAuditLogger(DEFAULT_AUDIT_FILE);
        this.auditLogger.start();
        this.auditLogger.log("SYSTEM INIT: " + numFloors + " floors x " + spotsPerFloor + " spots initialized.");
    }

    public synchronized void addChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    private void notifyListeners() {
        List<Runnable> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<Runnable>(changeListeners);
        }
        for (Runnable r : snapshot) {
            r.run();
        }
    }

    /**
     * Parks a vehicle in the best available spot in a thread-safe manner.
     */
    public synchronized ParkingTicket parkVehicle(Vehicle vehicle) throws ParkingException {
        if (vehicle == null) {
            throw new ParkingException("ERR_NULL_VEHICLE", "Vehicle cannot be null.");
        }
        // Check duplicate license plate among currently parked vehicles
        for (ParkingTicket active : activeTickets.values()) {
            if (active.getVehicle().getLicensePlate().equalsIgnoreCase(vehicle.getLicensePlate())) {
                throw new DuplicateVehicleException(vehicle.getLicensePlate());
            }
        }

        ParkingSpot chosenSpot = null;
        for (ParkingFloor floor : floors) {
            chosenSpot = floor.findAvailableSpot(vehicle);
            if (chosenSpot != null) {
                break;
            }
        }

        if (chosenSpot == null) {
            auditLogger.log("ENTRY DENIED (FULL): " + vehicle.toString());
            throw new ParkingFullException(vehicle.getVehicleCategory());
        }

        ticketCounter++;
        String ticketId = "TKT-" + ticketCounter;
        boolean assigned = chosenSpot.assignVehicle(vehicle, ticketId);
        if (!assigned) {
            throw new ParkingFullException(vehicle.getVehicleCategory());
        }

        ParkingTicket ticket = new ParkingTicket.TicketBuilder()
                .ticketId(ticketId)
                .vehicle(vehicle)
                .spot(chosenSpot)
                .entryTimeMillis(System.currentTimeMillis())
                .build();

        activeTickets.put(ticketId, ticket);
        auditLogger.log("PARKED [" + Thread.currentThread().getName() + "]: "
                + ticketId + " -> " + vehicle.getLicensePlate() + " at " + chosenSpot.getSpotId());
        notifyListeners();
        return ticket;
    }

    /**
     * Exits a vehicle by ticket ID, computes fee, frees spot, and records payment.
     */
    public synchronized ParkingTicket exitVehicle(String ticketId) throws InvalidTicketException {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new InvalidTicketException("EMPTY");
        }
        String normalized = ticketId.trim().toUpperCase();
        ParkingTicket ticket = activeTickets.remove(normalized);
        if (ticket == null) {
            throw new InvalidTicketException(normalized);
        }

        ParkingSpot spot = findSpotById(ticket.getSpotId());
        if (spot != null) {
            spot.vacate();
        }

        double fee = ticket.settle(System.currentTimeMillis());
        totalRevenue += fee;
        completedTickets.add(ticket);

        auditLogger.log(String.format("EXIT [%s]: %s (%s) from %s | Fee: $%.2f",
                Thread.currentThread().getName(),
                ticket.getTicketId(),
                ticket.getVehicle().getLicensePlate(),
                ticket.getSpotId(),
                fee));
        notifyListeners();
        return ticket;
    }

    public synchronized ParkingTicket exitByLicensePlate(String licensePlate) throws InvalidTicketException {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new InvalidTicketException("PLATE_EMPTY");
        }
        String plate = licensePlate.trim().toUpperCase();
        for (ParkingTicket ticket : activeTickets.values()) {
            if (ticket.getVehicle().getLicensePlate().equalsIgnoreCase(plate)) {
                return exitVehicle(ticket.getTicketId());
            }
        }
        throw new InvalidTicketException("No active ticket for plate: " + plate);
    }

    public synchronized ParkingSpot findSpotById(String spotId) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findSpotById(spotId);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    public synchronized ParkingFloor[] getFloors() {
        return floors;
    }

    public synchronized List<ParkingTicket> getActiveTickets() {
        return new ArrayList<ParkingTicket>(activeTickets.values());
    }

    public synchronized List<ParkingTicket> getCompletedTickets() {
        return new ArrayList<ParkingTicket>(completedTickets);
    }

    public synchronized LotStatistics getStatistics() {
        int total = 0;
        int occupied = 0;
        for (ParkingFloor floor : floors) {
            ParkingFloor.FloorSummary summary = floor.getSummary();
            total += summary.getTotalSpots();
            occupied += summary.getOccupiedSpots();
        }
        return new LotStatistics(total, occupied, activeTickets.size(), completedTickets.size(), totalRevenue);
    }

    public synchronized void saveToDisk(String path) throws IOException {
        ParkingLotState state = new ParkingLotState(
                floors,
                new LinkedHashMap<String, ParkingTicket>(activeTickets),
                new ArrayList<ParkingTicket>(completedTickets),
                ticketCounter,
                totalRevenue);
        ParkingPersistenceManager.saveState(state, path);
        auditLogger.log("STATE SAVED: Serialized lot state to " + path);
    }

    public synchronized boolean loadFromDisk(String path) throws IOException, ClassNotFoundException {
        ParkingLotState state = ParkingPersistenceManager.loadState(path);
        if (state == null) {
            return false;
        }
        this.floors = state.getFloors();
        this.activeTickets = new LinkedHashMap<String, ParkingTicket>(state.getActiveTickets());
        this.completedTickets = new ArrayList<ParkingTicket>(state.getCompletedTickets());
        this.ticketCounter = state.getTicketCounter();
        this.totalRevenue = state.getTotalRevenue();
        auditLogger.log("STATE LOADED: Deserialized lot state from " + path);
        notifyListeners();
        return true;
    }

    public synchronized void exportReportCsv(String path) throws IOException {
        List<ParkingTicket> all = new ArrayList<ParkingTicket>(activeTickets.values());
        all.addAll(completedTickets);
        ParkingPersistenceManager.exportTicketsCsv(all, path);
        auditLogger.log("CSV EXPORTED: " + all.size() + " ticket records to " + path);
    }

    public PipeAuditLogger getAuditLogger() {
        return auditLogger;
    }

    public void shutdown() {
        auditLogger.shutdown();
    }
}
