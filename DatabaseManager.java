package smartparking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MODULE 1: Database Connectivity & CRUD Operations using PostgreSQL JDBC.
 * Implements: Singleton Design Pattern, PreparedStatement, Robust Exception Handling.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    // Database Configuration - Adjust credentials according to your local PostgreSQL setup
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/smart_parking_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "root"; // Change to your postgres password

    // Private constructor (Singleton Pattern)
    private DatabaseManager() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            initializeTables();
            System.out.println("[DatabaseManager] PostgreSQL connected successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] PostgreSQL JDBC Driver not found! Ensure postgresql jar is in classpath.");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] DB Connection failed: " + e.getMessage() + ". Running with in-memory sync.");
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Automatically creates tables if they do not exist.
     */
    private void initializeTables() {
        if (!isConnected()) return;

        String createSlotsTable = "CREATE TABLE IF NOT EXISTS parking_slots (" +
                "slot_code VARCHAR(10) PRIMARY KEY, " +
                "vehicle_type VARCHAR(20) NOT NULL, " +
                "is_occupied BOOLEAN DEFAULT FALSE, " +
                "is_reserved BOOLEAN DEFAULT FALSE);";

        String createParkingLogsTable = "CREATE TABLE IF NOT EXISTS parking_logs (" +
                "ticket_id VARCHAR(50) PRIMARY KEY, " +
                "plate_number VARCHAR(20) NOT NULL, " +
                "vehicle_type VARCHAR(20) NOT NULL, " +
                "slot_code VARCHAR(10) REFERENCES parking_slots(slot_code), " +
                "entry_time TIMESTAMP NOT NULL, " +
                "exit_time TIMESTAMP, " +
                "status VARCHAR(20) NOT NULL);";

        String createReservationsTable = "CREATE TABLE IF NOT EXISTS reservations (" +
                "reservation_id VARCHAR(50) PRIMARY KEY, " +
                "customer_name VARCHAR(50) NOT NULL, " +
                "contact_number VARCHAR(20) NOT NULL, " +
                "plate_number VARCHAR(20) NOT NULL, " +
                "slot_code VARCHAR(10), " +
                "booking_time TIMESTAMP NOT NULL, " +
                "status VARCHAR(20) NOT NULL);";

        String createBillingTable = "CREATE TABLE IF NOT EXISTS billing_invoices (" +
                "invoice_id VARCHAR(50) PRIMARY KEY, " +
                "plate_number VARCHAR(20) NOT NULL, " +
                "slot_code VARCHAR(10) NOT NULL, " +
                "hours_parked DOUBLE PRECISION NOT NULL, " +
                "rate_per_hour DOUBLE PRECISION NOT NULL, " +
                "total_paid DOUBLE PRECISION NOT NULL, " +
                "payment_mode VARCHAR(20) NOT NULL, " +
                "invoice_time TIMESTAMP NOT NULL);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSlotsTable);
            stmt.execute(createParkingLogsTable);
            stmt.execute(createReservationsTable);
            stmt.execute(createBillingTable);
            seedInitialSlots();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error initializing tables: " + e.getMessage());
        }
    }

    private void seedInitialSlots() {
        String countSql = "SELECT COUNT(*) FROM parking_slots";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSlot = "INSERT INTO parking_slots (slot_code, vehicle_type) VALUES (?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSlot)) {
                    // Zones: A (Bike), B (Car), C (EV), D (Heavy)
                    for (int i = 1; i <= 6; i++) { pstmt.setString(1, "A-" + String.format("%02d", i)); pstmt.setString(2, "BIKE"); pstmt.executeUpdate(); }
                    for (int i = 1; i <= 8; i++) { pstmt.setString(1, "B-" + String.format("%02d", i)); pstmt.setString(2, "CAR"); pstmt.executeUpdate(); }
                    for (int i = 1; i <= 6; i++) { pstmt.setString(1, "C-" + String.format("%02d", i)); pstmt.setString(2, "EV"); pstmt.executeUpdate(); }
                    for (int i = 1; i <= 4; i++) { pstmt.setString(1, "D-" + String.format("%02d", i)); pstmt.setString(2, "TRUCK"); pstmt.executeUpdate(); }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error seeding slots: " + e.getMessage());
        }
    }

    // CRUD: Save check-in
    public void recordCheckIn(String ticketId, String plate, String type, String slotCode, Timestamp entryTime) {
        if (!isConnected()) return;
        String logSql = "INSERT INTO parking_logs (ticket_id, plate_number, vehicle_type, slot_code, entry_time, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        String updateSlotSql = "UPDATE parking_slots SET is_occupied = TRUE WHERE slot_code = ?";

        try (PreparedStatement pstmtLog = connection.prepareStatement(logSql);
             PreparedStatement pstmtSlot = connection.prepareStatement(updateSlotSql)) {

            connection.setAutoCommit(false);

            pstmtLog.setString(1, ticketId);
            pstmtLog.setString(2, plate);
            pstmtLog.setString(3, type);
            pstmtLog.setString(4, slotCode);
            pstmtLog.setTimestamp(5, entryTime);
            pstmtLog.executeUpdate();

            pstmtSlot.setString(1, slotCode);
            pstmtSlot.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction();
            System.err.println("[DatabaseManager] Error recording check-in: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Error restoring autoCommit: " + ex.getMessage());
            }
        }
    }

    // CRUD: Save check-out
    public void recordCheckOut(String slotCode, Timestamp exitTime) {
        if (!isConnected()) return;
        String logSql = "UPDATE parking_logs SET exit_time = ?, status = 'COMPLETED' WHERE slot_code = ? AND status = 'ACTIVE'";
        String updateSlotSql = "UPDATE parking_slots SET is_occupied = FALSE WHERE slot_code = ?";

        try (PreparedStatement pstmtLog = connection.prepareStatement(logSql);
             PreparedStatement pstmtSlot = connection.prepareStatement(updateSlotSql)) {

            connection.setAutoCommit(false);

            pstmtLog.setTimestamp(1, exitTime);
            pstmtLog.setString(2, slotCode);
            pstmtLog.executeUpdate();

            pstmtSlot.setString(1, slotCode);
            pstmtSlot.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction();
            System.err.println("[DatabaseManager] Error recording check-out: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Error restoring autoCommit: " + ex.getMessage());
            }
        }
    }

    // CRUD: Save billing record
    public void saveInvoice(String invoiceId, String plate, String slotCode, double hours, double rate, double total, String mode) {
        if (!isConnected()) return;
        String sql = "INSERT INTO billing_invoices VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            pstmt.setString(2, plate);
            pstmt.setString(3, slotCode);
            pstmt.setDouble(4, hours);
            pstmt.setDouble(5, rate);
            pstmt.setDouble(6, total);
            pstmt.setString(7, mode);
            pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error saving invoice: " + e.getMessage());
        }
    }

    // CRUD: Save Reservation
    public void saveReservation(String resId, String name, String phone, String plate, String slotCode) {
        if (!isConnected()) return;
        String sql = "INSERT INTO reservations VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED')";
        String slotSql = "UPDATE parking_slots SET is_reserved = TRUE WHERE slot_code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             PreparedStatement pstmtSlot = connection.prepareStatement(slotSql)) {
            pstmt.setString(1, resId);
            pstmt.setString(2, name);
            pstmt.setString(3, phone);
            pstmt.setString(4, plate);
            pstmt.setString(5, slotCode);
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();

            if (slotCode != null) {
                pstmtSlot.setString(1, slotCode);
                pstmtSlot.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error saving reservation: " + e.getMessage());
        }
    }

    public void clearReservationStatus(String slotCode) {
        if (!isConnected()) return;
        String sql = "UPDATE parking_slots SET is_reserved = FALSE WHERE slot_code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, slotCode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error clearing reservation status: " + e.getMessage());
        }
    }

    private void rollbackTransaction() {
        try {
            if (connection != null) connection.rollback();
        } catch (SQLException ex) {
            System.err.println("[DatabaseManager] Rollback failed: " + ex.getMessage());
        }
    }
}