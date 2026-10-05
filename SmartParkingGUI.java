package smartparking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SmartParkingGUI extends JFrame {

    // --- Subsystems ---
    private final ParkingManager parkingManager;
    private final ReservationSystem reservationSystem;
    private final BillingAndReporting billingService;

    // --- Layout & Colors ---
    private CardLayout cardLayout;
    private JPanel mainContent;
    private JPanel sidebarPanel;

    private final Color SIDEBAR_BG = new Color(32, 33, 36);
    private final Color SIDEBAR_HOVER = new Color(43, 45, 48);
    private final Color MAIN_BG = new Color(245, 243, 238);
    private final Color ACCENT = new Color(22, 128, 92);
    private final Color ACCENT_HOVER = new Color(33, 163, 102);
    private final Color ACCENT_PRESSED = new Color(15, 92, 67);
    private final Color SECONDARY_ACCENT = new Color(200, 155, 60);
    private final Color TEXT_DARK = new Color(32, 33, 36);
    private final Color TEXT_SECONDARY = new Color(107, 109, 112);
    private final Color TEXT_MUTED = new Color(150, 152, 155);
    private final Color CARD_BG = new Color(255, 253, 248);
    private final Color BORDER_COLOR = new Color(221, 217, 208);

    private final Color C_AVAILABLE = new Color(22, 128, 92);
    private final Color C_OCCUPIED = new Color(211, 47, 47);
    private final Color C_RESERVED = new Color(200, 155, 60);
    private final Color C_EV = new Color(22, 128, 92);

    // Typography System
    private final Font FONT_APP_TITLE = new Font("Inter", Font.BOLD, 20);
    private final Font FONT_PAGE_TITLE = new Font("Inter", Font.BOLD, 26);
    private final Font FONT_PAGE_SUBTITLE = new Font("Inter", Font.PLAIN, 14);
    private final Font FONT_SECTION = new Font("Inter", Font.BOLD, 16);
    private final Font FONT_LABEL = new Font("Inter", Font.BOLD, 13);
    private final Font FONT_INPUT = new Font("Inter", Font.PLAIN, 14);
    private final Font FONT_BUTTON = new Font("Inter", Font.BOLD, 14);
    private final Font FONT_SLOT_TITLE = new Font("Inter", Font.BOLD, 26);
    private final Font FONT_SLOT_INFO = new Font("Inter", Font.PLAIN, 13);
    private final Font FONT_SLOT_STATUS = new Font("Inter", Font.BOLD, 12);
    private final Font FONT_TABLE = new Font("Inter", Font.PLAIN, 14);
    private final Font FONT_TABLE_HEADER = new Font("Inter", Font.BOLD, 12);
    private final Font FONT_LOG = new Font("Inter", Font.PLAIN, 14);
    private final Font FONT_SIDEBAR = new Font("Inter", Font.BOLD, 14);
    private final Font FONT_STAT_VALUE = new Font("Inter", Font.BOLD, 28);

    // --- UI Elements ---
    private JLabel lblAvailableSlots, lblOccupiedSlots, lblWaitingCount, lblRevenue;
    private JPanel parkingGridContainer;

    // Check-In
    private JTextField txtPlateCheckIn, txtOwnerCheckIn;
    private JComboBox<VehicleType> cmbTypeCheckIn;
    private JCheckBox chkEvCharging;
    private JComboBox<String> cmbSpecificSlot;

    // Check-Out
    private JTextField txtCheckOutQuery;
    private JComboBox<String> cmbSimulatedTime;
    private JComboBox<String> cmbPaymentMethod;

    // Reservations
    private JTextField txtResName, txtResPhone, txtResPlate;
    private JComboBox<VehicleType> cmbResType;

    // Billing
    private DefaultTableModel invoiceTableModel;
    private JTable invoiceTable;
    private JLabel lblBillRevenue, lblBillCheckouts, lblBillParked, lblBillTransactions;

    // Activity Log
    private DefaultTableModel activityTableModel;
    private JTable activityTable;
    private JLabel lblTotalActivities;
    private JLabel lblTodayActivities;
    private int totalActivities = 0;

    public SmartParkingGUI() {
        super("Smart Parking Management System");
        this.parkingManager = new ParkingManager();
        this.reservationSystem = new ReservationSystem(parkingManager);
        this.billingService = new BillingAndReporting();

        initUI();
        refreshAllViews();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(MAIN_BG);

        // Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Main Center Panel
        JPanel centerPanel = new ThemeBackgroundPanel(new BorderLayout());
        
        centerPanel.add(createTopHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(15, 20, 15, 20));

        mainContent.add(createParkingPanel(), "Parking");
        mainContent.add(createCheckInPanel(), "Check-In");
        mainContent.add(createCheckOutPanel(), "Check-Out");
        mainContent.add(createReservationPanel(), "Reservations");
        mainContent.add(createBillingPanel(), "Billing & Reports");
        mainContent.add(createActivityLogPanel(), "Activity Log");

        centerPanel.add(mainContent, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    // ==========================================
    // UI BUILDER METHODS
    // ==========================================

    private JPanel createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(SIDEBAR_BG);
        brandPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brand = new JLabel("SMART PARKING");
        brand.setFont(FONT_APP_TITLE);
        brand.setForeground(new Color(255, 253, 248));
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitle = new JLabel("Intelligent Parking Management");
        subtitle.setFont(new Font("Inter", Font.PLAIN, 13));
        subtitle.setForeground(ACCENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandPanel.add(brand);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        brandPanel.add(subtitle);
        
        sidebarPanel.add(brandPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        addNavButton("Parking");
        addNavButton("Check-In");
        addNavButton("Check-Out");
        addNavButton("Reservations");
        addNavButton("Billing & Reports");
        addNavButton("Activity Log");

        sidebarPanel.add(Box.createVerticalGlue());

        JLabel statusLbl = new JLabel("● System Online");
        statusLbl.setFont(new Font("Inter", Font.BOLD, 12));
        statusLbl.setForeground(C_AVAILABLE);
        statusLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(statusLbl);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        return sidebarPanel;
    }

    private void addNavButton(String title) {
        JButton btn = new JButton(title);
        btn.setFont(FONT_SIDEBAR);
        btn.setForeground(new Color(245, 243, 238));
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(230, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setBackground(SIDEBAR_BG);
        wrapper.setMaximumSize(new Dimension(260, 50));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(btn.getBackground().equals(SIDEBAR_BG)) btn.setBackground(SIDEBAR_HOVER); }
            public void mouseExited(MouseEvent e) { if(btn.getBackground().equals(SIDEBAR_HOVER)) btn.setBackground(SIDEBAR_BG); }
        });

        btn.setForeground(new Color(201, 199, 193));
        btn.addActionListener(e -> {
            cardLayout.show(mainContent, title);
            for (Component c : sidebarPanel.getComponents()) {
                if (c instanceof JPanel && ((JPanel)c).getComponentCount() > 0 && ((JPanel)c).getComponent(0) instanceof JButton) {
                    JButton b = (JButton) ((JPanel)c).getComponent(0);
                    b.setBackground(SIDEBAR_BG);
                    b.setForeground(new Color(201, 199, 193));
                    b.setBorder(new EmptyBorder(10, 20, 10, 20));
                    b.setForeground(new Color(201, 199, 193));
                    b.setBorder(new EmptyBorder(10, 20, 10, 20));
                }
            }
            btn.setBackground(ACCENT);
            btn.setForeground(CARD_BG);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, SECONDARY_ACCENT),
                new EmptyBorder(10, 16, 10, 20)
            ));
        });

        if (title.equals("Parking")) {
            btn.setBackground(ACCENT);
            btn.setForeground(CARD_BG);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, SECONDARY_ACCENT),
                new EmptyBorder(10, 16, 10, 20)
            ));
        }
        
        wrapper.add(btn);
        sidebarPanel.add(wrapper);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(20, 30, 20, 30)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(CARD_BG);
        JLabel lblTitle = new JLabel("Parking Overview");
        lblTitle.setFont(FONT_PAGE_TITLE);
        lblTitle.setForeground(TEXT_DARK);
        titlePanel.add(lblTitle);
        
        JLabel lblSub = new JLabel("Real-time parking availability and system status");
        lblSub.setFont(FONT_PAGE_SUBTITLE);
        lblSub.setForeground(TEXT_SECONDARY);
        titlePanel.add(lblSub);
        
        header.add(titlePanel, BorderLayout.WEST);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        stats.setBackground(CARD_BG);

        lblAvailableSlots = new JLabel("0");
        lblOccupiedSlots = new JLabel("0");
        lblWaitingCount = new JLabel("0");
        lblRevenue = new JLabel("₹0");

        stats.add(createStatCard("AVAILABLE", lblAvailableSlots, C_AVAILABLE));
        stats.add(createStatCard("OCCUPIED", lblOccupiedSlots, C_OCCUPIED));
        stats.add(createStatCard("WAITING", lblWaitingCount, C_RESERVED));
        stats.add(createStatCard("REVENUE", lblRevenue, ACCENT));

        header.add(stats, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setBackground(CARD_BG);
        
        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("Inter", Font.BOLD, 10));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(dot);
        titlePanel.add(lblTitle);
        
        valueLabel.setFont(FONT_STAT_VALUE);
        valueLabel.setForeground(TEXT_DARK);
        
        card.add(titlePanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createLegendItem(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SLOT_STATUS);
        l.setForeground(color);
        return l;
    }

    private JPanel createActivityLogPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Top Summary Info
        JPanel summaryGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        summaryGrid.setOpaque(false);
        
        lblTotalActivities = new JLabel("0"); lblTotalActivities.setFont(FONT_STAT_VALUE);
        lblTodayActivities = new JLabel("0"); lblTodayActivities.setFont(FONT_STAT_VALUE);
        
        summaryGrid.add(createStatCard("TOTAL ACTIVITIES", lblTotalActivities, ACCENT));
        summaryGrid.add(createStatCard("TODAY'S ACTIVITIES", lblTodayActivities, C_AVAILABLE));

        // Title Area
        JPanel tableSection = new JPanel(new BorderLayout(0, 15));
        tableSection.setOpaque(false);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel lblTableTitle = new JLabel("Activity Log");
        lblTableTitle.setFont(FONT_PAGE_TITLE);
        lblTableTitle.setForeground(TEXT_DARK);
        
        JLabel lblSub = new JLabel("Recent parking system activities");
        lblSub.setFont(FONT_PAGE_SUBTITLE);
        lblSub.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTableTitle);
        titlePanel.add(lblSub);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Table
        activityTableModel = new DefaultTableModel(new String[]{"Time", "Activity", "Vehicle / Plate", "Slot", "Details", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        activityTable = new JTable(activityTableModel);
        styleTable(activityTable);
        
        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BG);
        
        tableSection.add(headerPanel, BorderLayout.NORTH);
        tableSection.add(scrollPane, BorderLayout.CENTER);
        
        p.add(summaryGrid, BorderLayout.NORTH);
        p.add(tableSection, BorderLayout.CENTER);
        
        return p;
    }

    private JPanel createParkingPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        parkingGridContainer = new JPanel();
        parkingGridContainer.setLayout(new BoxLayout(parkingGridContainer, BoxLayout.Y_AXIS));
        parkingGridContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(parkingGridContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private JPanel createCheckInPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            new EmptyBorder(40, 50, 40, 50)
        ));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(CARD_BG);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblTitle = new JLabel("CHECK-IN VEHICLE");
        lblTitle.setFont(FONT_PAGE_TITLE);
        
        JLabel lblSub = new JLabel("Register and allocate a parking slot");
        lblSub.setFont(FONT_PAGE_SUBTITLE);
        lblSub.setForeground(TEXT_SECONDARY);
        
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(lblSub);
        
        card.add(headerPanel);
        card.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel lblSec1 = new JLabel("VEHICLE INFORMATION");
        lblSec1.setFont(FONT_SECTION);
        lblSec1.setForeground(SIDEBAR_BG);
        lblSec1.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSec1);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        JComponent row1 = createFormRow("License Plate Number", txtPlateCheckIn = new JTextField());
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(row1);
        
        JComponent row2 = createFormRow("Driver / Owner Name", txtOwnerCheckIn = new JTextField());
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(row2);
        
        JComponent row3 = createFormRow("Vehicle Category", cmbTypeCheckIn = new JComboBox<>(VehicleType.values()));
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(row3);
        
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel lblSec2 = new JLabel("PARKING OPTIONS");
        lblSec2.setFont(FONT_SECTION);
        lblSec2.setForeground(SIDEBAR_BG);
        lblSec2.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSec2);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        chkEvCharging = new JCheckBox("Require EV Charging Station (+ surcharge)");
        chkEvCharging.setBackground(CARD_BG);
        chkEvCharging.setFont(FONT_INPUT);
        chkEvCharging.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkEvCharging.setFocusPainted(false);
        JPanel chkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chkPanel.setBackground(CARD_BG);
        chkPanel.add(chkEvCharging);
        chkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(chkPanel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        JComponent row4 = createFormRow("Slot Preference", cmbSpecificSlot = new JComboBox<>());
        row4.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(row4);

        card.add(Box.createRigidArea(new Dimension(0, 25)));

        ModernButton btnPark = new ModernButton("CHECK IN VEHICLE", ACCENT, ButtonStyle.PRIMARY);
        btnPark.setPreferredSize(new Dimension(400, 48));
        btnPark.setMaximumSize(new Dimension(400, 48));
        btnPark.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPark.addActionListener(e -> handleCheckIn());
        card.add(btnPark);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(40, 0, 40, 0);
        
        wrapper.add(card, gbc);
        
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel outer = new JPanel(new BorderLayout());
        outer.add(scrollPane, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createCheckOutPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel lblTitle = new JLabel("CHECK-OUT VEHICLE");
        lblTitle.setFont(FONT_PAGE_TITLE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        
        JLabel lblSub = new JLabel("Process vehicle exit and calculate parking charges");
        lblSub.setFont(FONT_PAGE_SUBTITLE);
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSub);
        card.add(Box.createRigidArea(new Dimension(0, 25)));

        card.add(createFormRow("Enter Slot Code (e.g. B-01) or Plate", txtCheckOutQuery = new JTextField()));
        
        String[] options = {"Live Real Time", "Simulate: 30 Mins", "Simulate: 2 Hours", "Simulate: 5 Hours", "Simulate: 1 Day"};
        cmbSimulatedTime = new JComboBox<>(options);
        cmbSimulatedTime.setSelectedIndex(2);
        card.add(createFormRow("Duration Simulation", cmbSimulatedTime));

        cmbPaymentMethod = new JComboBox<>(new String[]{"Cash", "Credit Card", "UPI / QR"});
        card.add(createFormRow("Payment Method", cmbPaymentMethod));

        // Parking Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        summaryPanel.setBackground(MAIN_BG);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));
        summaryPanel.setVisible(false);
        JLabel sumSlot = new JLabel();
        JLabel sumVehicle = new JLabel();
        JLabel sumType = new JLabel();
        JLabel sumRate = new JLabel();
        sumSlot.setFont(FONT_SLOT_INFO); sumVehicle.setFont(FONT_SLOT_INFO); 
        sumType.setFont(FONT_SLOT_INFO); sumRate.setFont(FONT_SLOT_INFO);
        summaryPanel.add(sumSlot); summaryPanel.add(sumVehicle);
        summaryPanel.add(sumType); summaryPanel.add(sumRate);
        
        JPanel sumWrapper = new JPanel(new BorderLayout());
        sumWrapper.setBackground(CARD_BG);
        sumWrapper.add(summaryPanel, BorderLayout.CENTER);
        sumWrapper.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(sumWrapper);

        txtCheckOutQuery.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSummary(); }
            private void updateSummary() {
                String query = txtCheckOutQuery.getText().trim();
                ParkingSlot found = null;
                for (ParkingSlot s : parkingManager.getAllSlots()) {
                    if (s.isOccupied() && (s.getSlotCode().equalsIgnoreCase(query) || s.getCurrentVehicle().getLicensePlate().equalsIgnoreCase(query))) {
                        found = s; break;
                    }
                }
                if (found != null) {
                    Vehicle v = found.getCurrentVehicle();
                    sumSlot.setText("Slot: " + found.getSlotCode());
                    sumVehicle.setText("Vehicle: " + v.getLicensePlate() + " (" + v.getOwnerName() + ")");
                    sumType.setText("Type: " + v.getCategoryDescription());
                    
                    long simMillis = 7200000L; 
                    String choice = (String) cmbSimulatedTime.getSelectedItem();
                    if (choice != null) {
                        if (choice.contains("30 Mins")) simMillis = 1800000L;
                        else if (choice.contains("5 Hours")) simMillis = 18000000L;
                        else if (choice.contains("1 Day")) simMillis = 86400000L;
                        else if (choice.contains("Live")) simMillis = System.currentTimeMillis() - v.getEntryTime().getTime();
                    }
                    double hrs = Math.max(1.0, Math.ceil(simMillis / 3600000.0));
                    sumRate.setText("Estimated Fee: ₹" + String.format("%.2f", hrs * v.getBaseHourlyRate()));
                    
                    summaryPanel.setVisible(true);
                } else {
                    summaryPanel.setVisible(false);
                }
                card.revalidate();
                card.repaint();
            }
        });
        
        cmbSimulatedTime.addActionListener(e -> {
            try { txtCheckOutQuery.getDocument().insertString(0, "", null); } catch(Exception ex) {}
        });

        ModernButton btnExit = new ModernButton("PROCESS PAYMENT & EXIT", ACCENT);
        btnExit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExit.setMaximumSize(new Dimension(300, 48));
        btnExit.setPreferredSize(new Dimension(300, 48));
        btnExit.addActionListener(e -> handleCheckOut());
        card.add(btnExit);

        p.add(card);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(p);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createReservationPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false);

        JPanel topCard = new JPanel(new BorderLayout());
        topCard.setBackground(CARD_BG);
        topCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR),
            new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblTitle = new JLabel("Book Advance Slot");
        lblTitle.setFont(FONT_PAGE_TITLE);
        topCard.add(lblTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 10));
        grid.setBackground(CARD_BG);
        grid.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        txtResName = new JTextField();
        txtResPhone = new JTextField();
        txtResPlate = new JTextField();
        cmbResType = new JComboBox<>(VehicleType.values());

        grid.add(createFormRow("Name", txtResName));
        grid.add(createFormRow("Phone", txtResPhone));
        grid.add(createFormRow("License Plate", txtResPlate));
        grid.add(createFormRow("Vehicle Type", cmbResType));
        
        topCard.add(grid, BorderLayout.CENTER);

        ModernButton btnReserve = new ModernButton("CONFIRM RESERVATION", ACCENT);
        btnReserve.setPreferredSize(new Dimension(250, 45));
        btnReserve.addActionListener(e -> {
            boolean success = reservationSystem.reserveSlot(txtResName.getText(), txtResPhone.getText(), txtResPlate.getText(), (VehicleType) cmbResType.getSelectedItem());
            if (success) {
                JOptionPane.showMessageDialog(this, "✓ Slot successfully reserved for " + txtResPlate.getText(), "Success", JOptionPane.INFORMATION_MESSAGE);
                logEvent("Reservation Created", txtResPlate.getText(), "-", "Advance reservation", "CONFIRMED");
            } else {
                JOptionPane.showMessageDialog(this, "⚠ No slots free! Added customer to Waiting List Queue.", "Waitlist", JOptionPane.WARNING_MESSAGE);
                logEvent("Added to Waitlist", txtResPlate.getText(), "-", "Queue: " + reservationSystem.getWaitingCount(), "WAITING");
            }
            refreshAllViews();
        });
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(CARD_BG);
        btnPanel.add(btnReserve);
        topCard.add(btnPanel, BorderLayout.SOUTH);

        p.add(topCard, BorderLayout.NORTH);
        return p;
    }

    private JPanel createBillingPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel summaryGrid = new JPanel(new GridLayout(1, 4, 20, 0));
        summaryGrid.setOpaque(false);
        
        lblBillRevenue = new JLabel("₹0.00"); lblBillRevenue.setFont(FONT_STAT_VALUE);
        lblBillCheckouts = new JLabel("0"); lblBillCheckouts.setFont(FONT_STAT_VALUE);
        lblBillParked = new JLabel("0"); lblBillParked.setFont(FONT_STAT_VALUE);
        lblBillTransactions = new JLabel("0"); lblBillTransactions.setFont(FONT_STAT_VALUE);
        
        summaryGrid.add(createStatCard("TODAY'S REVENUE", lblBillRevenue, ACCENT));
        summaryGrid.add(createStatCard("COMPLETED CHECK-OUTS", lblBillCheckouts, C_AVAILABLE));
        summaryGrid.add(createStatCard("CURRENTLY PARKED", lblBillParked, C_RESERVED));
        summaryGrid.add(createStatCard("TOTAL TRANSACTIONS", lblBillTransactions, SIDEBAR_BG));
        
        JPanel tableSection = new JPanel(new BorderLayout(0, 15));
        tableSection.setOpaque(false);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel lblTableTitle = new JLabel("Parking Transactions");
        lblTableTitle.setFont(FONT_SECTION);
        lblTableTitle.setForeground(TEXT_DARK);
        
        ModernButton btnRefresh = new ModernButton("Refresh", SIDEBAR_HOVER, ButtonStyle.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(100, 35));
        btnRefresh.addActionListener(e -> refreshBillingView());
        
        headerPanel.add(lblTableTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        
        invoiceTableModel = new DefaultTableModel(new String[]{"Vehicle / Plate", "Parking Slot", "Entry Time", "Exit Time", "Duration (hrs)", "Amount (₹)", "Payment Method", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        invoiceTable = new JTable(invoiceTableModel);
        styleTable(invoiceTable);
        
        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BG);
        
        tableSection.add(headerPanel, BorderLayout.NORTH);
        tableSection.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(15, 0, 0, 0));
        ModernButton btnReport = new ModernButton("VIEW FINANCIAL REPORT", SIDEBAR_HOVER, ButtonStyle.SECONDARY);
        btnReport.setPreferredSize(new Dimension(250, 40));
        btnReport.addActionListener(e -> {
            String report = billingService.generateSystemReport(24, parkingManager.getOccupiedCount(), parkingManager.getAvailableCount(), reservationSystem.getWaitingCount());
            JTextArea area = new JTextArea(report, 15, 45);
            area.setFont(new Font("Inter", Font.PLAIN, 13));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "System Analytics Report", JOptionPane.INFORMATION_MESSAGE);
        });
        bot.add(btnReport);

        tableSection.add(bot, BorderLayout.SOUTH);

        p.add(summaryGrid, BorderLayout.NORTH);
        p.add(tableSection, BorderLayout.CENTER);
        
        return p;
    }

    private void refreshBillingView() {
        if (lblBillRevenue == null) return;
        lblBillRevenue.setText("₹" + new DecimalFormat("#,##0.00").format(billingService.getCumulativeRevenue()));
        lblBillCheckouts.setText(String.valueOf(billingService.getInvoiceHistory().size()));
        lblBillParked.setText(String.valueOf(parkingManager.getOccupiedCount()));
        lblBillTransactions.setText(String.valueOf(billingService.getInvoiceHistory().size()));
        
        invoiceTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        if (billingService.getInvoiceHistory().isEmpty()) {
            invoiceTableModel.addRow(new Object[]{"No parking transactions recorded yet.", "", "", "", "", "", "", ""});
        } else {
            for (smartparking.Invoice inv : billingService.getInvoiceHistory()) {
                invoiceTableModel.addRow(new Object[]{
                    inv.getLicensePlate(),
                    inv.getSlotCode(),
                    sdf.format(inv.getEntryTime()),
                    sdf.format(inv.getExitTime()),
                    String.format("%.1f", inv.getHoursBilled()),
                    "₹" + df.format(inv.getTotalAmount()),
                    inv.getPaymentMode(),
                    "● PAID"
                });
            }
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(FONT_TABLE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(233, 245, 239));
        table.setSelectionForeground(TEXT_DARK);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(SIDEBAR_BG);
        header.setForeground(CARD_BG);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 15, 0, 15));
                if (!isSelected) c.setBackground(row % 2 == 0 ? CARD_BG : MAIN_BG);
                
                String colName = t.getColumnName(col);
                if (colName.equals("Time") || colName.contains("Time")) {
                    setFont(new Font("Inter", Font.PLAIN, 12));
                    if (!isSelected) setForeground(TEXT_SECONDARY);
                } else if (colName.equals("Status")) {
                    setFont(new Font("Inter", Font.BOLD, 12));
                } else {
                    setFont(FONT_TABLE);
                    if (!isSelected) setForeground(TEXT_DARK);
                }
                
                return c;
            }
        });
    }

    private void styleFormInput(JComponent input) {
        input.setFont(FONT_INPUT);
        input.setForeground(TEXT_DARK);
        if (input instanceof JTextField) {
            JTextField txt = (JTextField) input;
            txt.setPreferredSize(new Dimension(400, 42));
            txt.setBackground(CARD_BG);
            txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
            ));
            txt.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    txt.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT, 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                    ));
                }
                public void focusLost(java.awt.event.FocusEvent e) {
                    txt.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                    ));
                }
            });
        } else if (input instanceof JComboBox) {
            JComboBox<?> cmb = (JComboBox<?>) input;
            cmb.setPreferredSize(new Dimension(400, 42));
            cmb.setBackground(CARD_BG);
            cmb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 8, 4, 8)
            ));
        }
    }

    private JPanel createFormRow(String labelText, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(CARD_BG);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        
        styleFormInput(input);
        
        p.add(input, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(0, 0, 15, 0));
        p.setMaximumSize(new Dimension(400, 85));
        return p;
    }

    // ==========================================
    // LOGIC & REFRESH METHODS
    // ==========================================

    private void handleCheckIn() {
        String plate = txtPlateCheckIn.getText().trim();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠ Please enter a valid license plate number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        for (ParkingSlot s : parkingManager.getAllSlots()) {
            if (s.isOccupied() && s.getCurrentVehicle().getLicensePlate().equalsIgnoreCase(plate)) {
                JOptionPane.showMessageDialog(this, "Vehicle is already checked in.", "Already Parked", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        VehicleType type = (VehicleType) cmbTypeCheckIn.getSelectedItem();
        Vehicle vehicle = VehicleFactory.createVehicle(type, plate, txtOwnerCheckIn.getText(), chkEvCharging.isSelected());

        String specific = (String) cmbSpecificSlot.getSelectedItem();
        if (specific != null && specific.startsWith("Auto")) specific = null;
        else if (specific != null) specific = specific.split(" ")[0];

        // Check if user has a reservation
        for (smartparking.Reservation r : reservationSystem.getActiveReservations()) {
            if (r.getLicensePlate().equalsIgnoreCase(plate)) {
                specific = r.getAllocatedSlotCode();
                reservationSystem.cancelReservation(r.getReservationId());
                logEvent("Reservation Claimed", plate, specific, "Claiming reserved slot", "SUCCESS");
                break;
            }
        }

        try {
            ParkingSlot allocated = parkingManager.parkVehicle(vehicle, specific);

            DatabaseManager.getInstance().recordCheckIn(
                    "TKT-" + (System.currentTimeMillis() % 100000),
                    vehicle.getLicensePlate(),
                    vehicle.getVehicleType().name(),
                    allocated.getSlotCode(),
                    new java.sql.Timestamp(vehicle.getEntryTime().getTime())
            );

            logEvent("Vehicle Checked In", plate, allocated.getSlotCode(), vehicle.getCategoryDescription(), "SUCCESS");
            txtPlateCheckIn.setText("");
            txtOwnerCheckIn.setText("");
            chkEvCharging.setSelected(false);
            
            refreshAllViews();
            
            showCheckInConfirmation(vehicle, allocated);
            
            // Navigate back to parking screen
            cardLayout.show(mainContent, "Parking");
        } catch (SlotUnavailableException ex) {
            if (ex.getMessage().contains("No parking slot available")) {
                JOptionPane.showMessageDialog(this, "No suitable parking slot is currently available.", "Capacity Full", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "✕ Check-In Failed\n\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCheckInConfirmation(Vehicle vehicle, ParkingSlot allocated) {
        JDialog dialog = new JDialog(this, "Vehicle Checked In Successfully", true);
        dialog.setLayout(new BorderLayout());
        dialog.setUndecorated(true);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, C_AVAILABLE),
                new EmptyBorder(30, 40, 30, 40)
            )
        ));

        JLabel lblTitle = new JLabel("VEHICLE CHECKED IN SUCCESSFULLY");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitle.setForeground(C_AVAILABLE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(lblTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        int row = 0;
        addDetailRow(grid, gbc, row++, "License Plate", vehicle.getLicensePlate(), false);
        addDetailRow(grid, gbc, row++, "Driver", vehicle.getOwnerName() == null || vehicle.getOwnerName().isEmpty() ? "N/A" : vehicle.getOwnerName(), false);
        addDetailRow(grid, gbc, row++, "Vehicle", vehicle.getCategoryDescription(), false);
        
        addDetailRow(grid, gbc, row++, " ", " ", false); // spacer
        
        addDetailRow(grid, gbc, row++, "ASSIGNED SLOT", allocated.getSlotCode(), true);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        addDetailRow(grid, gbc, row++, "ENTRY TIME", sdf.format(vehicle.getEntryTime()), false);
        
        addDetailRow(grid, gbc, row++, " ", " ", false); // spacer
        
        addDetailRow(grid, gbc, row++, "PARKING RATE", "₹" + vehicle.getBaseHourlyRate() + "/hour", false);

        panel.add(grid);
        panel.add(Box.createRigidArea(new Dimension(0, 35)));
        
        ModernButton btnDone = new ModernButton("DONE", ACCENT);
        btnDone.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDone.setPreferredSize(new Dimension(250, 45));
        btnDone.setMaximumSize(new Dimension(250, 45));
        btnDone.addActionListener(e -> dialog.dispose());
        panel.add(btnDone);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel grid, GridBagConstraints gbc, int row, String label, String value, boolean highlight) {
        gbc.gridy = row;
        
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        JLabel lblLeft = new JLabel(label);
        lblLeft.setFont(new Font("Inter", Font.BOLD, 13));
        lblLeft.setForeground(TEXT_SECONDARY);
        grid.add(lblLeft, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        JLabel lblRight = new JLabel(value);
        lblRight.setFont(new Font("Inter", highlight ? Font.BOLD : Font.PLAIN, highlight ? 18 : 14));
        lblRight.setForeground(highlight ? ACCENT : TEXT_DARK);
        grid.add(lblRight, gbc);
    }

    private void handleCheckOut() {
        String query = txtCheckOutQuery.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠ Please enter a valid Slot Code or License Plate.", "Validation Error", JOptionPane.WARNING_MESSAGE);
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

            long simMillis = 7200000L; 
            String choice = (String) cmbSimulatedTime.getSelectedItem();
            if (choice.contains("30 Mins")) simMillis = 1800000L;
            else if (choice.contains("5 Hours")) simMillis = 18000000L;
            else if (choice.contains("1 Day")) simMillis = 86400000L;
            else if (choice.contains("Live")) simMillis = System.currentTimeMillis() - slot.getCurrentVehicle().getEntryTime().getTime();

            PaymentStrategy paymentStrategy = new CashPayment();
            String mode = (String) cmbPaymentMethod.getSelectedItem();
            if ("Credit Card".equals(mode)) paymentStrategy = new CardPayment();
            else if ("UPI / QR".equals(mode)) paymentStrategy = new UPIPayment();

            Vehicle v = parkingManager.checkOutVehicle(slot.getSlotCode());
            DatabaseManager.getInstance().recordCheckOut(slot.getSlotCode(), new java.sql.Timestamp(System.currentTimeMillis()));

            Invoice invoice = billingService.generateAndProcessBill(v, slot.getSlotCode(), simMillis, paymentStrategy);

            logEvent("Vehicle Checked Out", v.getLicensePlate(), slot.getSlotCode(), "Amount: ₹" + String.format("%.2f", invoice.getTotalAmount()), "COMPLETED");
            txtCheckOutQuery.setText("");

            if (reservationSystem.getWaitingCount() > 0) {
                WaitingDriver next = reservationSystem.pollNextWaiting();
                logEvent("Slot Freed (Queue)", next.getPlate(), "-", "Driver can check-in", "NOTIFIED");
            }

            refreshAllViews();

            JTextArea billText = new JTextArea(invoice.generateReceiptSlip(), 15, 35);
            billText.setFont(new Font("Inter", Font.PLAIN, 13));
            JOptionPane.showMessageDialog(this, new JScrollPane(billText), "Checkout Completed", JOptionPane.INFORMATION_MESSAGE);
            
            cardLayout.show(mainContent, "Parking");

        } catch (VehicleNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "✕ Check-Out Failed\n\n" + ex.getMessage(), "Vehicle Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllViews() {
        int occ = parkingManager.getOccupiedCount();
        int avail = parkingManager.getAvailableCount();

        lblAvailableSlots.setText(String.valueOf(avail));
        lblOccupiedSlots.setText(String.valueOf(occ));
        lblWaitingCount.setText(String.valueOf(reservationSystem.getWaitingCount()));
        lblRevenue.setText("₹" + new DecimalFormat("#,##0.00").format(billingService.getCumulativeRevenue()));
        
        refreshBillingView();

        // Update Specific slot combo
        if (cmbSpecificSlot != null) {
            cmbSpecificSlot.removeAllItems();
            cmbSpecificSlot.addItem("Auto-Assign (Optimal)");
            for (ParkingSlot s : parkingManager.getAllSlots()) {
                if (!s.isOccupied() && !s.isReserved()) {
                    cmbSpecificSlot.addItem(s.getSlotCode() + " (" + s.getDesignatedType() + ")");
                }
            }
        }

        // Render Parking Zones
        if (parkingGridContainer != null) {
            parkingGridContainer.removeAll();
            
            String[] zonePrefixes = {"A", "B", "C", "D"};
            String[] zoneNames = {"ZONE A · BIKE", "ZONE B · CAR", "ZONE C · EV", "ZONE D · TRUCK"};

            for (int i = 0; i < zonePrefixes.length; i++) {
                JPanel zonePanel = new JPanel(new BorderLayout(10, 10));
                zonePanel.setOpaque(false);
                zonePanel.setBorder(new EmptyBorder(10, 20, 20, 20));

                JLabel lblZone = new JLabel(zoneNames[i]);
                lblZone.setFont(FONT_SECTION);
                lblZone.setForeground(TEXT_DARK);
                lblZone.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
                zonePanel.add(lblZone, BorderLayout.NORTH);

                JPanel grid = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
                grid.setOpaque(false);

                for (ParkingSlot s : parkingManager.getAllSlots()) {
                    if (s.getSlotCode().startsWith(zonePrefixes[i])) {
                        grid.add(createSlotCard(s));
                    }
                }
                zonePanel.add(grid, BorderLayout.CENTER);
                parkingGridContainer.add(zonePanel);
            }
            parkingGridContainer.revalidate();
            parkingGridContainer.repaint();
        }
    }

    private JPanel createSlotCard(ParkingSlot s) {
        Color statusColor = C_AVAILABLE;
        String statusStr = "AVAILABLE";
        String infoStr = "";
        
        if (s.isOccupied()) {
            statusColor = C_OCCUPIED;
            statusStr = "OCCUPIED";
            infoStr = s.getCurrentVehicle().getLicensePlate();
        } else if (s.isReserved()) {
            statusColor = C_RESERVED;
            statusStr = "RESERVED";
        }

        final Color finalStatusColor = statusColor;
        final String finalStatusStr = statusStr;

        JPanel card = new JPanel() {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    public void mouseClicked(MouseEvent e) {
                        if (s.isOccupied()) {
                            int choice = JOptionPane.showConfirmDialog(SmartParkingGUI.this, "Checkout vehicle " + s.getCurrentVehicle().getLicensePlate() + "?", "Slot " + s.getSlotCode(), JOptionPane.YES_NO_OPTION);
                            if (choice == JOptionPane.YES_OPTION) {
                                txtCheckOutQuery.setText(s.getSlotCode());
                                cardLayout.show(mainContent, "Check-Out");
                                handleCheckOut();
                            }
                        } else {
                            JOptionPane.showMessageDialog(SmartParkingGUI.this, "Slot " + s.getSlotCode() + " is " + finalStatusStr.toLowerCase() + ".", "Slot Information", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (hovered) {
                    g2.setColor(new Color(32, 33, 36, 15));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 2, 12, 12);
                }
                
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                g2.setColor(hovered ? ACCENT : BORDER_COLOR);
                g2.setStroke(new BasicStroke(hovered ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(160, 110));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(12, 15, 5, 15));
        
        JLabel lblCode = new JLabel(s.getSlotCode());
        lblCode.setFont(FONT_SLOT_TITLE);
        lblCode.setForeground(TEXT_DARK);
        
        topPanel.add(lblCode, BorderLayout.WEST);
        
        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
        midPanel.setOpaque(false);
        midPanel.setBorder(new EmptyBorder(0, 15, 10, 15));
        
        JLabel lblType = new JLabel(s.getDesignatedType().name() + (infoStr.isEmpty() ? "" : " · " + infoStr));
        lblType.setFont(FONT_SLOT_INFO);
        lblType.setForeground(TEXT_SECONDARY);
        midPanel.add(lblType);
        
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botPanel.setOpaque(false);
        botPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(8, 15, 8, 15)
        ));
        
        JLabel lblStatus = new JLabel("● " + statusStr);
        lblStatus.setFont(FONT_SLOT_STATUS);
        lblStatus.setForeground(statusColor);
        botPanel.add(lblStatus);
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(midPanel, BorderLayout.CENTER);
        card.add(botPanel, BorderLayout.SOUTH);

        return card;
    }

    private void logEvent(String activity, String plate, String slot, String details, String status) {
        String t = new SimpleDateFormat("hh:mm a").format(new Date());
        
        if (activityTableModel != null) {
            activityTableModel.insertRow(0, new Object[]{t, activity, plate, slot, details, status});
        }
        
        totalActivities++;
        if (lblTotalActivities != null) {
            lblTotalActivities.setText(String.valueOf(totalActivities));
            lblTodayActivities.setText(String.valueOf(totalActivities));
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new SmartParkingGUI().setVisible(true));
    }
    // ==========================================
    // MODERN BUTTON COMPONENT
    // ==========================================
    enum ButtonStyle { PRIMARY, SECONDARY, DANGER }
    
    class ModernButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private Color pressedBg;
        private Color textColor;
        private ButtonStyle style;

        public ModernButton(String text, Color baseColor, ButtonStyle style) {
            super(text);
            this.style = style;
            
            if (style == ButtonStyle.SECONDARY) {
                this.normalBg = CARD_BG;
                this.hoverBg = MAIN_BG;
                this.pressedBg = BORDER_COLOR;
                this.textColor = SIDEBAR_BG;
            } else if (style == ButtonStyle.DANGER) {
                this.normalBg = C_OCCUPIED;
                this.hoverBg = new Color(229, 115, 115);
                this.pressedBg = new Color(198, 40, 40);
                this.textColor = new Color(255, 253, 248);
            } else {
                this.normalBg = baseColor;
                if (baseColor.equals(ACCENT)) {
                    this.hoverBg = ACCENT_HOVER;
                    this.pressedBg = ACCENT_PRESSED;
                } else {
                    this.hoverBg = new Color(Math.min(baseColor.getRed() + 20, 255), 
                                             Math.min(baseColor.getGreen() + 20, 255), 
                                             Math.min(baseColor.getBlue() + 20, 255));
                    this.pressedBg = new Color(Math.max(baseColor.getRed() - 20, 0), 
                                               Math.max(baseColor.getGreen() - 20, 0), 
                                               Math.max(baseColor.getBlue() - 20, 0));
                }
                this.textColor = new Color(255, 253, 248);
            }

            setForeground(textColor);
            setFont(FONT_BUTTON);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { if(isEnabled()) setBackground(hoverBg); }
                public void mouseExited(java.awt.event.MouseEvent e) { if(isEnabled()) setBackground(normalBg); }
                public void mousePressed(java.awt.event.MouseEvent e) { if(isEnabled()) setBackground(pressedBg); }
                public void mouseReleased(java.awt.event.MouseEvent e) { if(isEnabled()) setBackground(hoverBg); }
            });
            setBackground(normalBg);
        }

        public ModernButton(String text, Color baseColor) {
            this(text, baseColor, ButtonStyle.PRIMARY);
        }

        @Override
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            setBackground(b ? normalBg : BORDER_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            
            if (style == ButtonStyle.SECONDARY) {
                g2.setColor(BORDER_COLOR); // Subtle neutral border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
            
            if (!isEnabled()) {
                setForeground(TEXT_SECONDARY);
            } else {
                setForeground(textColor);
            }
            
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) { return layoutSize(target, false); }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + hgap * 2;
                int maxWidth = targetWidth - horizontalInsetsAndGap;
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.height += rowHeight + vgap;
                            dim.width = Math.max(dim.width, rowWidth);
                            rowWidth = d.width; rowHeight = d.height;
                        } else {
                            if (rowWidth != 0) rowWidth += hgap;
                            rowWidth += d.width;
                            rowHeight = Math.max(rowHeight, d.height);
                        }
                    }
                }
                dim.height += rowHeight + vgap + insets.top + insets.bottom;
                dim.width = Math.max(dim.width, rowWidth) + insets.left + insets.right;
                return dim;
            }
        }
    }

    class ThemeBackgroundPanel extends JPanel {
        public ThemeBackgroundPanel(LayoutManager layout) { super(layout); }
        public ThemeBackgroundPanel() { super(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, MAIN_BG, 0, getHeight(), BORDER_COLOR));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(32, 33, 36, 6)); // Extremely subtle dark decorative shapes
            g2.fillOval(-150, -150, 500, 500);
            g2.fillOval(getWidth() - 300, getHeight() - 200, 600, 600);
            g2.dispose();
        }
    }
}