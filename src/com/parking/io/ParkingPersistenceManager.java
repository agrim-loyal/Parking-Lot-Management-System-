package com.parking.io;

import com.parking.model.ParkingFloor;
import com.parking.model.ParkingTicket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Handles Object Serialization / Deserialization and File I/O exports.
 * Demonstrates Unit 4: Input/Output streams, Filter streams (Buffered*),
 * ObjectInputStream / ObjectOutputStream.
 */
public class ParkingPersistenceManager {

    /**
     * Serializable snapshot container for the entire parking lot state.
     */
    public static class ParkingLotState implements Serializable {
        private static final long serialVersionUID = 1L;
        private final ParkingFloor[] floors;
        private final Map<String, ParkingTicket> activeTickets;
        private final List<ParkingTicket> completedTickets;
        private final long ticketCounter;
        private final double totalRevenue;

        public ParkingLotState(ParkingFloor[] floors,
                               Map<String, ParkingTicket> activeTickets,
                               List<ParkingTicket> completedTickets,
                               long ticketCounter,
                               double totalRevenue) {
            this.floors = floors;
            this.activeTickets = activeTickets;
            this.completedTickets = completedTickets;
            this.ticketCounter = ticketCounter;
            this.totalRevenue = totalRevenue;
        }

        public ParkingFloor[] getFloors() {
            return floors;
        }

        public Map<String, ParkingTicket> getActiveTickets() {
            return activeTickets;
        }

        public List<ParkingTicket> getCompletedTickets() {
            return completedTickets;
        }

        public long getTicketCounter() {
            return ticketCounter;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }
    }

    public static void saveState(ParkingLotState state, String filePath) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(state);
            oos.flush();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static ParkingLotState loadState(String filePath) throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
            return (ParkingLotState) ois.readObject();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static void exportTicketsCsv(List<ParkingTicket> tickets, String filePath) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            writer.println("TicketId,LicensePlate,VehicleType,Owner,SpotId,Floor,EntryTime,ExitTime,AmountPaid,Status");
            for (ParkingTicket t : tickets) {
                writer.printf("%s,%s,%s,%s,%s,%d,%s,%s,%.2f,%s%n",
                        t.getTicketId(),
                        t.getVehicle().getLicensePlate(),
                        t.getVehicle().getVehicleCategory(),
                        t.getVehicle().getOwnerName(),
                        t.getSpotId(),
                        t.getFloorNumber(),
                        t.getFormattedEntryTime(),
                        t.getFormattedExitTime(),
                        t.getAmountPaid() != null ? t.getAmountPaid().doubleValue() : 0.0,
                        t.getStatus().name());
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
