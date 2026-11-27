package com.parking.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Demonstrates Unit 4 (Filter and Pipe streams) & Unit 5 (Multithreading).
 * Producer threads write log events into PipedOutputStream; a dedicated consumer
 * thread reads from PipedInputStream and writes to a persistent audit file.
 */
public class PipeAuditLogger implements Runnable {
    private final PipedOutputStream pipedOut;
    private final PipedInputStream pipedIn;
    private final File logFile;
    private final List<String> recentLogs = Collections.synchronizedList(new ArrayList<String>());
    private volatile boolean running = true;
    private Thread consumerThread;

    public PipeAuditLogger(String logFilePath) throws IOException {
        this.pipedOut = new PipedOutputStream();
        this.pipedIn = new PipedInputStream(pipedOut, 8192);
        this.logFile = new File(logFilePath);
        File parent = this.logFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    public void start() {
        if (consumerThread == null) {
            consumerThread = new Thread(this, "Pipe-Audit-Logger");
            consumerThread.setDaemon(true);
            consumerThread.start();
        }
    }

    public synchronized void log(String event) {
        if (!running) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = "[" + timestamp + "] " + event + System.lineSeparator();
        try {
            pipedOut.write(line.getBytes("UTF-8"));
            pipedOut.flush();
        } catch (IOException e) {
            // Pipe closed during shutdown
        }
    }

    public List<String> getRecentLogs() {
        synchronized (recentLogs) {
            return new ArrayList<String>(recentLogs);
        }
    }

    public void shutdown() {
        running = false;
        try {
            pipedOut.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        PrintWriter fileWriter = null;
        try {
            reader = new BufferedReader(new InputStreamReader(pipedIn, "UTF-8"));
            fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)), true);
            String line;
            while (running && (line = reader.readLine()) != null) {
                fileWriter.println(line);
                synchronized (recentLogs) {
                    recentLogs.add(line);
                    if (recentLogs.size() > 200) {
                        recentLogs.remove(0);
                    }
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
