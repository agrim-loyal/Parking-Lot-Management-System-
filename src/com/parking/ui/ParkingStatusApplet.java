package com.parking.ui;

import com.parking.exception.ParkingException;
import com.parking.model.Car;
import com.parking.model.ParkingTicket;
import com.parking.service.ParkingLotService;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Demonstrates Unit 5: JApplet, Applet life-cycle (init, start, stop, destroy),
 * and reading HTML <param> tags via getParameter().
 */
public class ParkingStatusApplet extends JApplet {
    private static final long serialVersionUID = 1L;

    private ParkingLotService service;
    private JLabel statusLabel;
    private JLabel lifecycleLabel;
    private JTextField plateField;

    @Override
    public void init() {
        String lotName = getParameter("lotName");
        if (lotName == null || lotName.trim().isEmpty()) {
            lotName = "Campus Parking Lot";
        }
        int floors = parseIntParam("floors", 2);
        int spotsPerFloor = parseIntParam("spotsPerFloor", 10);

        try {
            service = new ParkingLotService(floors, spotsPerFloor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel(lotName + " - Applet Monitor", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout());
        plateField = new JTextField(10);
        JButton quickParkBtn = new JButton("Quick Park Car");
        quickParkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ParkingTicket t = service.parkVehicle(new Car(plateField.getText(), "AppletUser", "Blue"));
                    plateField.setText("");
                    updateStatus("Parked: " + t.getTicketId() + " at " + t.getSpotId());
                } catch (ParkingException ex) {
                    updateStatus("Error: " + ex.getMessage());
                }
            }
        });

        lifecycleLabel = new JLabel("Lifecycle: init()");
        controls.add(new JLabel("Plate:"));
        controls.add(plateField);
        controls.add(quickParkBtn);
        controls.add(lifecycleLabel);
        add(controls, BorderLayout.SOUTH);

        updateStatus("Ready");
    }

    @Override
    public void start() {
        if (lifecycleLabel != null) {
            lifecycleLabel.setText("Lifecycle: start()");
        }
        updateStatus("Applet active");
    }

    @Override
    public void stop() {
        if (lifecycleLabel != null) {
            lifecycleLabel.setText("Lifecycle: stop()");
        }
    }

    @Override
    public void destroy() {
        if (service != null) {
            service.shutdown();
        }
    }

    private int parseIntParam(String name, int fallback) {
        try {
            String raw = getParameter(name);
            return raw != null ? Integer.parseInt(raw.trim()) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void updateStatus(String note) {
        if (service == null || statusLabel == null) {
            return;
        }
        ParkingLotService.LotStatistics stats = service.getStatistics();
        statusLabel.setText(String.format("Occupied: %d / %d | Available: %d | %s",
                stats.getOccupiedSpots(), stats.getTotalSpots(), stats.getAvailableSpots(), note));
    }
}
