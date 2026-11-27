package com.parking.ui;

import com.parking.exception.ParkingException;
import com.parking.model.Car;
import com.parking.model.ElectricCar;
import com.parking.model.Motorcycle;
import com.parking.model.ParkingFloor;
import com.parking.model.ParkingSpot;
import com.parking.model.ParkingTicket;
import com.parking.model.Truck;
import com.parking.model.Vehicle;
import com.parking.service.ParkingLotService;
import com.parking.service.ParkingLotService.LotStatistics;
import com.parking.thread.AutoSaveThread;
import com.parking.thread.GateSimulatorThread;
import com.parking.util.ReflectionInspector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

/**
 * Main Swing Desktop Application for the Parking Lot Management System.
 * Demonstrates Unit 5: Swing (JFrame, JPanel, JButton, JTable, JTabbedPane, etc.),
 * AWT Layout Managers, Event Handling, and Multithreading integration.
 */
public class ParkingLotApp extends JFrame {
    private static final long serialVersionUID = 1L;

    private final ParkingLotService service;
    private final AutoSaveThread autoSaveThread;
    private GateSimulatorThread gate1;
    private GateSimulatorThread gate2;
    private boolean simulationRunning = false;

    private JLabel statsLabel;
    private JPanel floorsContainer;
    private DefaultTableModel activeTableModel;
    private DefaultTableModel historyTableModel;
    private JTextArea auditLogArea;
    private JTextArea reflectionArea;

    private JComboBox<String> vehicleTypeBox;
    private JTextField plateField;
    private JTextField ownerField;
    private JTextField colorField;
    private JTextField exitKeyField;
    private JButton simToggleBtn;

    public ParkingLotApp() throws IOException {
        super("Parking Lot Management System - BCO 035B");
        this.service = new ParkingLotService(3, 12);

        // Try restoring previous serialized state if present
        try {
            this.service.loadFromDisk(ParkingLotService.DEFAULT_STATE_FILE);
        } catch (Exception ignored) {
        }

        this.autoSaveThread = new AutoSaveThread(service, 30000L);
        this.autoSaveThread.start();

        initUI();

        this.service.addChangeListener(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshAllViews();
                    }
                });
            }
        });

        // Periodic timer to refresh piped audit logs smoothly
        new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAuditLog();
            }
        }).start();

        refreshAllViews();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownApp();
            }
        });

        // Top stats banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 47, 61));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("Smart Parking Lot Management System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        statsLabel = new JLabel();
        statsLabel.setForeground(new Color(213, 245, 227));
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(statsLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Parking Grid", createGridTab());
        tabs.addTab("Tickets & Billing", createTicketsTab());
        tabs.addTab("Piped Audit Log", createAuditTab());
        tabs.addTab("Reflection Inspector", createReflectionTab());
        add(tabs, BorderLayout.CENTER);

        // Right control sidebar
        add(createControlPanel(), BorderLayout.EAST);
    }

    private JScrollPane createGridTab() {
        floorsContainer = new JPanel();
        floorsContainer.setLayout(new GridLayout(0, 1, 10, 10));
        floorsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return new JScrollPane(floorsContainer);
    }

    private JPanel createTicketsTab() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] activeCols = {"Ticket ID", "Plate", "Vehicle", "Owner", "Spot", "Floor", "Entry Time"};
        activeTableModel = new DefaultTableModel(activeCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable activeTable = new JTable(activeTableModel);
        JScrollPane activeScroll = new JScrollPane(activeTable);
        activeScroll.setBorder(BorderFactory.createTitledBorder("Currently Parked Vehicles"));

        String[] histCols = {"Ticket ID", "Plate", "Vehicle", "Spot", "Entry", "Exit", "Fee ($)"};
        historyTableModel = new DefaultTableModel(histCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable histTable = new JTable(historyTableModel);
        JScrollPane histScroll = new JScrollPane(histTable);
        histScroll.setBorder(BorderFactory.createTitledBorder("Completed / Paid Tickets"));

        panel.add(activeScroll);
        panel.add(histScroll);
        return panel;
    }

    private JPanel createAuditTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        auditLogArea = new JTextArea();
        auditLogArea.setEditable(false);
        auditLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(auditLogArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReflectionTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JComboBox<String> classSelector = new JComboBox<String>(new String[]{
                "com.parking.model.Vehicle",
                "com.parking.model.Car",
                "com.parking.model.ElectricCar",
                "com.parking.model.ParkingTicket",
                "com.parking.model.ParkingFloor",
                "com.parking.service.ParkingLotService",
                "com.parking.io.PipeAuditLogger",
                "com.parking.thread.GateSimulatorThread"
        });
        JButton inspectBtn = new JButton("Inspect Class via Reflection");
        top.add(new JLabel("Target Class:"));
        top.add(classSelector);
        top.add(inspectBtn);

        reflectionArea = new JTextArea();
        reflectionArea.setEditable(false);
        reflectionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        inspectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cls = (String) classSelector.getSelectedItem();
                reflectionArea.setText(ReflectionInspector.inspectClass(cls));
                reflectionArea.setCaretPosition(0);
            }
        });

        reflectionArea.setText(ReflectionInspector.inspectClass("com.parking.model.Vehicle"));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(reflectionArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setPreferredSize(new Dimension(310, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Park Vehicle Section
        JPanel parkPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        parkPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Entry"));

        vehicleTypeBox = new JComboBox<String>(new String[]{"Car", "Motorcycle", "Electric Car", "Truck"});
        plateField = new JTextField();
        ownerField = new JTextField();
        colorField = new JTextField();
        JButton parkBtn = new JButton("Park Vehicle");
        parkBtn.setBackground(new Color(39, 174, 96));
        parkBtn.setForeground(Color.WHITE);

        parkPanel.add(new JLabel("Type:"));
        parkPanel.add(vehicleTypeBox);
        parkPanel.add(new JLabel("License Plate:"));
        parkPanel.add(plateField);
        parkPanel.add(new JLabel("Owner:"));
        parkPanel.add(ownerField);
        parkPanel.add(new JLabel("Color:"));
        parkPanel.add(colorField);
        parkPanel.add(new JLabel(""));
        parkPanel.add(parkBtn);

        parkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePark();
            }
        });

        gbc.gridy = 0;
        sidebar.add(parkPanel, gbc);

        // Exit Vehicle Section
        JPanel exitPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        exitPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Exit & Billing"));
        exitKeyField = new JTextField();
        JButton exitBtn = new JButton("Exit & Calculate Fee");
        exitBtn.setBackground(new Color(192, 57, 43));
        exitBtn.setForeground(Color.WHITE);

        exitPanel.add(new JLabel("Ticket ID (e.g. TKT-1001) or Plate:"));
        exitPanel.add(exitKeyField);
        exitPanel.add(exitBtn);

        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExit();
            }
        });

        gbc.gridy = 1;
        sidebar.add(exitPanel, gbc);

        // Multithreading & I/O Section
        JPanel opsPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        opsPanel.setBorder(BorderFactory.createTitledBorder("Concurrency & Persistence"));

        simToggleBtn = new JButton("Start Gate Simulation (2 Threads)");
        JButton saveBtn = new JButton("Save State (Serialize)");
        JButton loadBtn = new JButton("Load State (Deserialize)");
        JButton exportBtn = new JButton("Export CSV Report");

        simToggleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSimulation();
            }
        });
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    service.saveToDisk(ParkingLotService.DEFAULT_STATE_FILE);
                    JOptionPane.showMessageDialog(ParkingLotApp.this,
                            "State serialized to " + ParkingLotService.DEFAULT_STATE_FILE);
                } catch (IOException ex) {
                    showError(ex.getMessage());
                }
            }
        });
        loadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean loaded = service.loadFromDisk(ParkingLotService.DEFAULT_STATE_FILE);
                    JOptionPane.showMessageDialog(ParkingLotApp.this,
                            loaded ? "State deserialized successfully." : "No saved state file found.");
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        });
        exportBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String path = "data/tickets_report.csv";
                    service.exportReportCsv(path);
                    JOptionPane.showMessageDialog(ParkingLotApp.this, "Exported CSV to " + path);
                } catch (IOException ex) {
                    showError(ex.getMessage());
                }
            }
        });

        opsPanel.add(simToggleBtn);
        opsPanel.add(saveBtn);
        opsPanel.add(loadBtn);
        opsPanel.add(exportBtn);

        gbc.gridy = 2;
        sidebar.add(opsPanel, gbc);

        // Spacer
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        sidebar.add(new JPanel(), gbc);

        return sidebar;
    }

    private void handlePark() {
        try {
            String type = (String) vehicleTypeBox.getSelectedItem();
            String plate = plateField.getText();
            String owner = ownerField.getText();
            String color = colorField.getText();

            Vehicle vehicle;
            if ("Motorcycle".equals(type)) {
                vehicle = new Motorcycle(plate, owner, color);
            } else if ("Electric Car".equals(type)) {
                vehicle = new ElectricCar(plate, owner, color);
            } else if ("Truck".equals(type)) {
                vehicle = new Truck(plate, owner, color);
            } else {
                vehicle = new Car(plate, owner, color);
            }

            ParkingTicket ticket = service.parkVehicle(vehicle);
            plateField.setText("");
            ownerField.setText("");
            colorField.setText("");
            JOptionPane.showMessageDialog(this,
                    "Parked successfully!\nTicket: " + ticket.getTicketId()
                            + "\nSpot: " + ticket.getSpotId());
        } catch (ParkingException ex) {
            showError("[" + ex.getErrorCode() + "] " + ex.getMessage());
        }
    }

    private void handleExit() {
        String key = exitKeyField.getText().trim();
        if (key.isEmpty()) {
            showError("Please enter a Ticket ID or License Plate.");
            return;
        }
        try {
            ParkingTicket settled;
            if (key.toUpperCase().startsWith("TKT-")) {
                settled = service.exitVehicle(key);
            } else {
                settled = service.exitByLicensePlate(key);
            }
            exitKeyField.setText("");
            JOptionPane.showMessageDialog(this,
                    String.format("Exit Complete!\nTicket: %s\nPlate: %s\nSpot: %s\nAmount Paid: $%.2f",
                            settled.getTicketId(),
                            settled.getVehicle().getLicensePlate(),
                            settled.getSpotId(),
                            settled.getAmountPaid()));
        } catch (ParkingException ex) {
            showError("[" + ex.getErrorCode() + "] " + ex.getMessage());
        }
    }

    private void toggleSimulation() {
        if (!simulationRunning) {
            gate1 = new GateSimulatorThread("GATE-1", service);
            gate2 = new GateSimulatorThread("GATE-2", service);
            gate1.start();
            gate2.start();
            simulationRunning = true;
            simToggleBtn.setText("Stop Gate Simulation");
        } else {
            if (gate1 != null) gate1.stopGate();
            if (gate2 != null) gate2.stopGate();
            simulationRunning = false;
            simToggleBtn.setText("Start Gate Simulation (2 Threads)");
        }
    }

    private void refreshAllViews() {
        LotStatistics stats = service.getStatistics();
        statsLabel.setText(String.format("Total: %d  |  Occupied: %d  |  Free: %d  |  Revenue: $%.2f",
                stats.getTotalSpots(), stats.getOccupiedSpots(),
                stats.getAvailableSpots(), stats.getTotalRevenue()));

        // Rebuild floor grid
        floorsContainer.removeAll();
        for (ParkingFloor floor : service.getFloors()) {
            JPanel floorPanel = new JPanel(new GridLayout(2, 6, 8, 8));
            floorPanel.setBorder(BorderFactory.createTitledBorder(floor.getSummary().toString()));
            for (final ParkingSpot spot : floor.getSpots()) {
                JButton btn = new JButton();
                btn.setFont(new Font("SansSerif", Font.BOLD, 11));
                if (spot.isOccupied()) {
                    btn.setBackground(new Color(231, 76, 60));
                    btn.setForeground(Color.WHITE);
                    btn.setText("<html><center>" + spot.getSpotId() + "<br>"
                            + spot.getParkedVehicle().getLicensePlate() + "</center></html>");
                } else {
                    btn.setBackground(new Color(46, 204, 113));
                    btn.setForeground(Color.BLACK);
                    btn.setText("<html><center>" + spot.getSpotId() + "<br>"
                            + spot.getSpotType().getDisplayName() + "</center></html>");
                }
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onSpotClicked(spot);
                    }
                });
                floorPanel.add(btn);
            }
            floorsContainer.add(floorPanel);
        }
        floorsContainer.revalidate();
        floorsContainer.repaint();

        // Update ticket tables
        activeTableModel.setRowCount(0);
        for (ParkingTicket t : service.getActiveTickets()) {
            activeTableModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getVehicle().getLicensePlate(),
                    t.getVehicle().getVehicleCategory(),
                    t.getVehicle().getOwnerName(),
                    t.getSpotId(),
                    t.getFloorNumber(),
                    t.getFormattedEntryTime()
            });
        }

        historyTableModel.setRowCount(0);
        for (ParkingTicket t : service.getCompletedTickets()) {
            historyTableModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getVehicle().getLicensePlate(),
                    t.getVehicle().getVehicleCategory(),
                    t.getSpotId(),
                    t.getFormattedEntryTime(),
                    t.getFormattedExitTime(),
                    String.format("%.2f", t.getAmountPaid() != null ? t.getAmountPaid() : 0.0)
            });
        }

        refreshAuditLog();
    }

    private void refreshAuditLog() {
        List<String> logs = service.getAuditLogger().getRecentLogs();
        StringBuilder sb = new StringBuilder();
        for (String line : logs) {
            sb.append(line).append("\n");
        }
        auditLogArea.setText(sb.toString());
    }

    private void onSpotClicked(ParkingSpot spot) {
        if (!spot.isOccupied()) {
            JOptionPane.showMessageDialog(this,
                    spot.getSpotId() + " (" + spot.getSpotType().getDisplayName()
                            + ", $" + spot.getSpotType().getHourlyRate() + "/hr) is available.");
            return;
        }
        String ticketId = spot.getActiveTicketId();
        int choice = JOptionPane.showConfirmDialog(this,
                "Spot: " + spot.getSpotId() + "\nVehicle: " + spot.getParkedVehicle()
                        + "\nTicket: " + ticketId + "\n\nExit and settle this vehicle now?",
                "Spot Details", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                ParkingTicket settled = service.exitVehicle(ticketId);
                JOptionPane.showMessageDialog(this,
                        String.format("Settled %s — Amount Paid: $%.2f",
                                settled.getTicketId(), settled.getAmountPaid()));
            } catch (ParkingException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void shutdownApp() {
        if (gate1 != null) gate1.stopGate();
        if (gate2 != null) gate2.stopGate();
        autoSaveThread.stop();
        try {
            service.saveToDisk(ParkingLotService.DEFAULT_STATE_FILE);
        } catch (IOException ignored) {
        }
        service.shutdown();
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new ParkingLotApp().setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
