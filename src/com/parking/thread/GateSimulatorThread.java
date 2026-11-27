package com.parking.thread;

import com.parking.exception.ParkingException;
import com.parking.model.Car;
import com.parking.model.ElectricCar;
import com.parking.model.Motorcycle;
import com.parking.model.ParkingTicket;
import com.parking.model.Truck;
import com.parking.model.Vehicle;
import com.parking.service.ParkingLotService;

import java.util.List;
import java.util.Random;

/**
 * Simulates concurrent entry/exit gate operations on a dedicated Thread.
 * Demonstrates Unit 5: Multithreading, Thread life cycle, and synchronization.
 */
public class GateSimulatorThread extends Thread {
    private final ParkingLotService service;
    private final Random random = new Random();
    private volatile boolean running = true;
    private int sequence = 1;

    public GateSimulatorThread(String gateName, ParkingLotService service) {
        super(gateName);
        this.service = service;
        setDaemon(true);
    }

    public void stopGate() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                boolean shouldPark = random.nextInt(10) < 6; // 60% entry, 40% exit
                if (shouldPark) {
                    Vehicle v = createRandomVehicle();
                    service.parkVehicle(v);
                } else {
                    List<ParkingTicket> active = service.getActiveTickets();
                    if (!active.isEmpty()) {
                        ParkingTicket t = active.get(random.nextInt(active.size()));
                        service.exitVehicle(t.getTicketId());
                    }
                }
                Thread.sleep(1500L + random.nextInt(1500));
            } catch (InterruptedException e) {
                if (!running) {
                    break;
                }
            } catch (ParkingException e) {
                // Expected when lot is full or ticket was just exited by another gate thread
            }
        }
    }

    private Vehicle createRandomVehicle() throws ParkingException {
        String prefix = getName().replaceAll("[^A-Z0-9]", "");
        if (prefix.length() > 3) {
            prefix = prefix.substring(0, 3);
        }
        String plate = String.format("%s-%03d", prefix.isEmpty() ? "SIM" : prefix, sequence++);
        int kind = random.nextInt(4);
        String[] colors = {"Silver", "Black", "White", "Blue", "Red"};
        String color = colors[random.nextInt(colors.length)];
        switch (kind) {
            case 0:
                return new Motorcycle(plate, "AutoRider", color);
            case 1:
                return new ElectricCar(plate, "EcoDriver", color);
            case 2:
                return new Truck(plate, "CargoLogistics", color);
            default:
                return new Car(plate, "Commuter", color);
        }
    }
}
