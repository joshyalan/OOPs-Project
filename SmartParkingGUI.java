package smartparking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * MODULE 5: Java Swing Front-End & Orchestration Controller.
 * Demonstrates: MVC, Delegation Event Model, JTabbedPane, JTable, GridBagLayout.
 */
public class SmartParkingGUI extends JFrame {

    // Subsystems
    private final ParkingManager parkingManager;
    private final ReservationSystem reservationSystem;
    private final BillingAndReporting billingService;

    // UI Elements
    private JLabel lblTotalSlots, lblAvailableSlots, lblOccupiedSlots, lblWaitingCount, lblRevenue;
    private JProgressBar progressOccupancy;
    private JPanel gridPanel;
    private JTextArea logConsole;

    // Forms
    private JTextField txtPlateCheckIn, txtOwnerCheckIn;
    private JComboBox<VehicleType> cmbTypeCheckIn;
    private JCheckBox chkEvCharging;
    private JComboBox<String> cmbSpecificSlot;

    private JTextField txtCheckOutQuery;
    private JComboBox<String> cmbSimulatedTime;
    private JComboBox<String> cmbPaymentMethod;

    private JTable invoiceTable;
    private DefaultTableModel invoiceTableModel;

    public SmartParkingGUI() {
        super("Smart Parking Management System - S3 Java Mini Project");

        // Initialize Services
        this.parkingManager = new ParkingManager();
        this.reservationSystem = new ReservationSystem(parkingManager);
        this.billingService = new BillingAndReporting();

        initUI();
        refreshAllViews();
        logEvent("System initialized. PostgreSQL Status: " + (DatabaseManager.getInstance().isConnected() ? "Connected" : "Offline (Local mode)"));
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 820);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Top Dashboard Banner
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content: Left = Slot Grid, Right = Tabs
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // Left Grid
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.60; gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 8);
        centerPanel.add(createGridContainer(), gbc);

        // Right Operational Tabs
        gbc.gridx = 1; gbc.weightx = 0.40;
        gbc.insets = new Insets(0, 0, 0, 0);
        centerPanel.add(createTabbedControls(), gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Activity Bar & Console
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 44, 72));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("SMART PARKING AUTOMATION SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("PostgreSQL JDBC • Dynamic Pricing • Reservation & Waiting Queue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(189, 195, 199));

        titleBlock.add(title);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        // Stats Counters
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        stats.setOpaque(false);

        lblTotalSlots = makeStatBadge("TOTAL", "24", Color.LIGHT_GRAY);
        lblAvailableSlots = makeStatBadge("AVAILABLE", "0", new Color(46, 204, 113));
        lblOccupiedSlots = makeStatBadge("OCCUPIED", "0", new Color(231, 76, 60));
        lblWaitingCount = makeStatBadge("WAITING", "0", new Color(243, 156, 18));
        lblRevenue = makeStatBadge("REVENUE", "$0.00", new Color(241, 196, 15));

        stats.add(lblTotalSlots.getParent());
        stats.add(lblAvailableSlots.getParent());
        stats.add(lblOccupiedSlots.getParent());
        stats.add(lblWaitingCount.getParent());
        stats.add(lblRevenue.getParent());

        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JLabel makeStatBadge(String title, String val, Color c) {
        JPanel box = new JPanel(new GridLayout(2, 1));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 255, 255, 50), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 9));
        t.setForeground(new Color(200, 210, 220));

        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));
        v.setForeground(c);

        box.add(t);
        box.add(v);
        return v;
    }

    private JPanel createGridContainer() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Parking Slot Real-Time View (Click any slot)"));

        gridPanel = new JPanel(new GridLayout(6, 4, 6, 6));
        panel.add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        JLabel legend = new JLabel("Green: Available | Red: Occupied | Yellow: Reserved | Zone A: Bike, B: Car, C: EV, D: Truck", SwingConstants.CENTER);
        legend.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        panel.add(legend, BorderLayout.SOUTH);

        return panel;
    }

    private JTabbedPane createTabbedControls() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabs.addTab("Check-In", createCheckInPanel());
        tabs.addTab("Check-Out", createCheckOutPanel());
        tabs.addTab("Reservations", createReservationPanel());
        tabs.addTab("Billing & Reports", createBillingReportPanel());

        return tabs;
    }

    private JPanel createCheckInPanel() {
        JPanel p = new JPanel(new GridLayout(12, 1, 4, 4));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        p.add(new JLabel("License Plate Number:"));
        txtPlateCheckIn = new JTextField();
        p.add(txtPlateCheckIn);

        p.add(new JLabel("Driver / Owner Name:"));
        txtOwnerCheckIn = new JTextField();
        p.add(txtOwnerCheckIn);

        p.add(new JLabel("Vehicle Category:"));
        cmbTypeCheckIn = new JComboBox<>(VehicleType.values());
        p.add(cmbTypeCheckIn);

        chkEvCharging = new JCheckBox("Require EV Charging Station (+ surcharge)");
        p.add(chkEvCharging);

        p.add(new JLabel("Slot Preference:"));
        cmbSpecificSlot = new JComboBox<>();
        p.add(cmbSpecificSlot);

        JButton btnPark = new JButton("Confirm Park & Generate Ticket");
        btnPark.setBackground(new Color(41, 128, 185));
        btnPark.setForeground(Color.WHITE);
        btnPark.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPark.addActionListener(e -> handleCheckIn());
        p.add(btnPark);

        return p;
    }

    private JPanel createCheckOutPanel() {
        JPanel p = new JPanel(new GridLayout(10, 1, 6, 6));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        p.add(new JLabel("Enter Slot Code (e.g. B-01) or Plate:"));
        txtCheckOutQuery = new JTextField();
        p.add(txtCheckOutQuery);

        p.add(new JLabel("Duration Simulation (For instant testing):"));
        String[] options = {"Live Real Time", "Simulate: 30 Mins", "Simulate: 2 Hours", "Simulate: 5 Hours", "Simulate: 1 Day"};
        cmbSimulatedTime = new JComboBox<>(options);
        cmbSimulatedTime.setSelectedIndex(2);
        p.add(cmbSimulatedTime);

        p.add(new JLabel("Payment Method:"));
        cmbPaymentMethod = new JComboBox<>(new String[]{"Cash", "Credit Card", "UPI / QR"});
        p.add(cmbPaymentMethod);

        JButton btnExit = new JButton("Process Payment & Exit");
        btnExit.setBackground(new Color(192, 57, 43));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExit.addActionListener(e -> handleCheckOut());
        p.add(btnExit);

        return p;
    }

    private JPanel createReservationPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel form = new JPanel(new GridLayout(5, 2, 4, 4));
        JTextField txtResName = new JTextField();
        JTextField txtResPhone = new JTextField();
        JTextField txtResPlate = new JTextField();
        JComboBox<VehicleType> cmbResType = new JComboBox<>(VehicleType.values());

        form.add(new JLabel("Name:")); form.add(txtResName);
        form.add(new JLabel("Phone:")); form.add(txtResPhone);
        form.add(new JLabel("Plate:")); form.add(txtResPlate);
        form.add(new JLabel("Type:")); form.add(cmbResType);

        JButton btnReserve = new JButton("Book Advance Slot");
        btnReserve.setBackground(new Color(39, 174, 96));
        btnReserve.setForeground(Color.WHITE);
        btnReserve.addActionListener(e -> {
            boolean success = reservationSystem.reserveSlot(txtResName.getText(), txtResPhone.getText(), txtResPlate.getText(), (VehicleType) cmbResType.getSelectedItem());
            if (success) {
                JOptionPane.showMessageDialog(this, "Slot successfully reserved!");
                logEvent("RESERVATION: Confirmed for " + txtResPlate.getText());
            } else {
                JOptionPane.showMessageDialog(this, "No slots free! Added customer to Waiting List Queue.");
                logEvent("WAITING LIST: Added " + txtResPlate.getText() + " (Queue: " + reservationSystem.getWaitingCount() + ")");
            }
            refreshAllViews();
        });

        p.add(form, BorderLayout.NORTH);
        p.add(btnReserve, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createBillingReportPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(new EmptyBorder(6, 6, 6, 6));

        invoiceTableModel = new DefaultTableModel(new String[]{"Invoice", "Plate", "Slot", "Hours", "Total ($)", "Mode"}, 0);
        invoiceTable = new JTable(invoiceTableModel);
        p.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        JButton btnReport = new JButton("View Full Financial Report");
        btnReport.addActionListener(e -> {
            String report = billingService.generateSystemReport(24, parkingManager.getOccupiedCount(), parkingManager.getAvailableCount(), reservationSystem.getWaitingCount());
            JTextArea area = new JTextArea(report, 15, 45);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "System Analytics Report", JOptionPane.INFORMATION_MESSAGE);
        });
        p.add(btnReport, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createFooterPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(new EmptyBorder(0, 10, 8, 10));

        progressOccupancy = new JProgressBar(0, 24);
        progressOccupancy.setStringPainted(true);
        p.add(progressOccupancy, BorderLayout.NORTH);

        logConsole = new JTextArea(3, 50);
        logConsole.setEditable(false);
        logConsole.setFont(new Font("Consolas", Font.PLAIN, 11));
        p.add(new JScrollPane(logConsole), BorderLayout.CENTER);

        return p;
    }

    private void handleCheckIn() {
        String plate = txtPlateCheckIn.getText().trim();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a license plate number!");
            return;
        }

        VehicleType type = (VehicleType) cmbTypeCheckIn.getSelectedItem();
        Vehicle vehicle = VehicleFactory.createVehicle(type, plate, txtOwnerCheckIn.getText(), chkEvCharging.isSelected());

        String specific = (String) cmbSpecificSlot.getSelectedItem();
        if (specific != null && specific.startsWith("Auto")) specific = null;
        else if (specific != null) specific = specific.split(" ")[0];

        try {
            ParkingSlot allocated = parkingManager.parkVehicle(vehicle, specific);

            // Record to Database
            DatabaseManager.getInstance().recordCheckIn(
                    "TKT-" + (System.currentTimeMillis() % 100000),
                    vehicle.getLicensePlate(),
                    vehicle.getVehicleType().name(),
                    allocated.getSlotCode(),
                    new java.sql.Timestamp(vehicle.getEntryTime().getTime())
            );

            logEvent("CHECK-IN: [" + plate + "] parked at " + allocated.getSlotCode() + " (" + vehicle.getCategoryDescription() + ")");
            txtPlateCheckIn.setText("");
            txtOwnerCheckIn.setText("");
            refreshAllViews();
            JOptionPane.showMessageDialog(this, "Vehicle Parked at Slot: " + allocated.getSlotCode());
        } catch (SlotUnavailableException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckOut() {
        String query = txtCheckOutQuery.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Slot Code or License Plate!");
            return;
        }

        try {
            ParkingSlot slot = null;
            for (ParkingSlot s : parkingManager.getAllSlots()) {
                if (s.isOccupied() && (s.getSlotCode().equalsIgnoreCase(query) || s.getCurrentVehicle().getLicensePlate().equalsIgnoreCase(query))) {
                    slot = s;
                    break;
                }
            }

            if (slot == null) throw new VehicleNotFoundException("No vehicle found matching: " + query);

            long simMillis = 7200000L; // default 2 hours
            String choice = (String) cmbSimulatedTime.getSelectedItem();
            if (choice.contains("30 Mins")) simMillis = 1800000L;
            else if (choice.contains("5 Hours")) simMillis = 18000000L;
            else if (choice.contains("1 Day")) simMillis = 86400000L;

            PaymentStrategy paymentStrategy = new CashPayment();
            String mode = (String) cmbPaymentMethod.getSelectedItem();
            if ("Credit Card".equals(mode)) paymentStrategy = new CardPayment();
            else if ("UPI / QR".equals(mode)) paymentStrategy = new UPIPayment();

            Vehicle v = parkingManager.checkOutVehicle(slot.getSlotCode());
            DatabaseManager.getInstance().recordCheckOut(slot.getSlotCode(), new java.sql.Timestamp(System.currentTimeMillis()));

            Invoice invoice = billingService.generateAndProcessBill(v, slot.getSlotCode(), simMillis, paymentStrategy);

            invoiceTableModel.addRow(new Object[]{
                    invoice.getInvoiceId(), invoice.getLicensePlate(), invoice.getSlotCode(),
                    invoice.getHoursBilled(), invoice.getTotalAmount(), invoice.getPaymentMode()
            });

            logEvent("CHECK-OUT: [" + v.getLicensePlate() + "] departed. Bill Paid: $" + invoice.getTotalAmount());
            txtCheckOutQuery.setText("");

            // Auto-promote driver from waiting queue if any
            if (reservationSystem.getWaitingCount() > 0) {
                WaitingDriver next = reservationSystem.pollNextWaiting();
                logEvent("WAITING QUEUE: Slot freed! " + next.getPlate() + " can now check-in.");
            }

            refreshAllViews();

            JTextArea billText = new JTextArea(invoice.generateReceiptSlip(), 15, 35);
            billText.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(billText), "Checkout Completed", JOptionPane.INFORMATION_MESSAGE);

        } catch (VehicleNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Vehicle Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllViews() {
        gridPanel.removeAll();
        cmbSpecificSlot.removeAllItems();
        cmbSpecificSlot.addItem("Auto-Assign (Optimal)");

        for (ParkingSlot s : parkingManager.getAllSlots()) {
            JButton b = new JButton(s.getSlotCode());
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setFocusPainted(false);

            if (s.isOccupied()) {
                b.setBackground(new Color(255, 235, 238));
                b.setForeground(new Color(192, 57, 43));
                b.setBorder(new LineBorder(new Color(192, 57, 43), 2));
                b.setText("<html><center><b>" + s.getSlotCode() + "</b><br><small>" + s.getCurrentVehicle().getLicensePlate() + "</small></center></html>");
                b.addActionListener(e -> {
                    txtCheckOutQuery.setText(s.getSlotCode());
                    handleCheckOut();
                });
            } else if (s.isReserved()) {
                b.setBackground(new Color(254, 249, 231));
                b.setForeground(new Color(211, 84, 0));
                b.setBorder(new LineBorder(new Color(243, 156, 18), 2));
                b.setText(s.getSlotCode() + " (Res)");
            } else {
                b.setBackground(new Color(232, 245, 233));
                b.setForeground(new Color(39, 174, 96));
                b.setBorder(new LineBorder(new Color(39, 174, 96), 1));
                b.setText("<html><center><b>" + s.getSlotCode() + "</b><br><small>" + s.getDesignatedType() + "</small></center></html>");
                cmbSpecificSlot.addItem(s.getSlotCode() + " (" + s.getDesignatedType() + ")");
            }
            gridPanel.add(b);
        }

        int occ = parkingManager.getOccupiedCount();
        int avail = parkingManager.getAvailableCount();

        lblAvailableSlots.setText(String.valueOf(avail));
        lblOccupiedSlots.setText(String.valueOf(occ));
        lblWaitingCount.setText(String.valueOf(reservationSystem.getWaitingCount()));
        lblRevenue.setText("$" + new DecimalFormat("#,##0.00").format(billingService.getCumulativeRevenue()));

        progressOccupancy.setValue(occ);
        progressOccupancy.setString("Occupancy: " + occ + " / 24 Slots (" + (occ * 100 / 24) + "%)");

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void logEvent(String msg) {
        String t = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logConsole.append("[" + t + "] " + msg + "\n");
        logConsole.setCaretPosition(logConsole.getDocument().getLength());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new SmartParkingGUI().setVisible(true));
    }
}