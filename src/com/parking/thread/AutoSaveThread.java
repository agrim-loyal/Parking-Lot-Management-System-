package com.parking.thread;

import com.parking.service.ParkingLotService;
import java.io.IOException;

/**
 * Daemon thread that periodically serializes the parking lot state to disk.
 * Demonstrates Unit 5: Multithreading & Daemon threads + Unit 4: Serialization.
 */
public class AutoSaveThread implements Runnable {
    private final ParkingLotService service;
    private final long intervalMillis;
    private volatile boolean running = true;
    private Thread worker;

    public AutoSaveThread(ParkingLotService service, long intervalMillis) {
        this.service = service;
        this.intervalMillis = intervalMillis;
    }

    public void start() {
        if (worker == null) {
            worker = new Thread(this, "AutoSave-Daemon");
            worker.setDaemon(true);
            worker.start();
        }
    }

    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(intervalMillis);
                service.saveToDisk(ParkingLotService.DEFAULT_STATE_FILE);
            } catch (InterruptedException e) {
                if (!running) {
                    break;
                }
            } catch (IOException ignored) {
            }
        }
    }
}
