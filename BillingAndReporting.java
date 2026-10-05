package smartparking;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * MODULE 4: Billing Strategy, Payment Polymorphism & Operational Reporting.
 * Demonstrates: Interfaces, Dynamic Method Dispatch, Financial Math, Text Reports.
 */

// --- INTERFACE (Abstraction) ---
interface Billable {
    double calculateFee(long durationMillis, double hourlyRate);
}

// --- PAYMENT POLYMORPHISM ---
abstract class PaymentStrategy {
    public abstract boolean processPayment(double amount, String reference);
    public abstract String getMethodName();
}

class CashPayment extends PaymentStrategy {
    @Override public boolean processPayment(double amount, String reference) { return true; }
    @Override public String getMethodName() { return "Cash Counter"; }
}

class CardPayment extends PaymentStrategy {
    @Override public boolean processPayment(double amount, String cardLast4) { return true; }
    @Override public String getMethodName() { return "Credit / Debit Card"; }
}

class UPIPayment extends PaymentStrategy {
    @Override public boolean processPayment(double amount, String upiId) { return true; }
    @Override public String getMethodName() { return "UPI / QR Code Scan"; }
}

// --- INVOICE RECORD MODEL ---
class Invoice {
    private final String invoiceId;
    private final String licensePlate;
    private final String slotCode;
    private final Date entryTime;
    private final Date exitTime;
    private final double hoursBilled;
    private final double ratePerHour;
    private final double totalAmount;
    private final String paymentMode;

    public Invoice(String licensePlate, String slotCode, Date entryTime, Date exitTime,
                   double hoursBilled, double ratePerHour, double totalAmount, String paymentMode) {
        this.invoiceId = "INV-" + (System.currentTimeMillis() % 1000000);
        this.licensePlate = licensePlate;
        this.slotCode = slotCode;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.hoursBilled = hoursBilled;
        this.ratePerHour = ratePerHour;
        this.totalAmount = totalAmount;
        this.paymentMode = paymentMode;
    }

    public String getInvoiceId() { return invoiceId; }
    public String getLicensePlate() { return licensePlate; }
    public String getSlotCode() { return slotCode; }
    public Date getEntryTime() { return entryTime; }
    public Date getExitTime() { return exitTime; }
    public double getHoursBilled() { return hoursBilled; }
    public double getRatePerHour() { return ratePerHour; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentMode() { return paymentMode; }

    public String generateReceiptSlip() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "=================================================\n" +
               "           SMART PARKING RECEIPT & INVOICE       \n" +
               "=================================================\n" +
               " Invoice No    : " + invoiceId + "\n" +
               " License Plate : " + licensePlate + "\n" +
               " Assigned Slot : " + slotCode + "\n" +
               "-------------------------------------------------\n" +
               " Entry Time    : " + sdf.format(entryTime) + "\n" +
               " Exit Time     : " + sdf.format(exitTime) + "\n" +
               " Billed Hours  : " + hoursBilled + " hr(s)\n" +
               " Hourly Rate   : $" + df.format(ratePerHour) + "\n" +
               " Payment Mode  : " + paymentMode + "\n" +
               "-------------------------------------------------\n" +
               " TOTAL CHARGE  : $" + df.format(totalAmount) + "\n" +
               "=================================================\n" +
               "   Thank you for parking safely with us!         \n" +
               "=================================================";
    }
}

// --- BILLING AND REPORTING CONTROLLER ---
public class BillingAndReporting implements Billable {

    private final List<Invoice> invoiceHistory = new ArrayList<>();
    private double cumulativeRevenue = 0.0;

    @Override
    public double calculateFee(long durationMillis, double hourlyRate) {
        double hours = durationMillis / (1000.0 * 60 * 60);
        // Minimum charge: 1 full hour. Round up to nearest half hour.
        double billedHours = Math.max(1.0, Math.ceil(hours * 2.0) / 2.0);
        return billedHours * hourlyRate;
    }

    public Invoice generateAndProcessBill(Vehicle vehicle, String slotCode, long simulatedDurationMillis, PaymentStrategy payment) {
        Date entry = vehicle.getEntryTime();
        Date exit = new Date(entry.getTime() + simulatedDurationMillis);

        double rate = vehicle.getBaseHourlyRate();
        double billedHours = Math.max(1.0, Math.ceil((simulatedDurationMillis / (1000.0 * 60 * 60)) * 2.0) / 2.0);
        double total = calculateFee(simulatedDurationMillis, rate);

        payment.processPayment(total, "TXN-" + System.currentTimeMillis() % 10000);

        Invoice inv = new Invoice(vehicle.getLicensePlate(), slotCode, entry, exit, billedHours, rate, total, payment.getMethodName());
        invoiceHistory.add(inv);
        cumulativeRevenue += total;

        // Persist to PostgreSQL
        DatabaseManager.getInstance().saveInvoice(inv.getInvoiceId(), inv.getLicensePlate(), slotCode, billedHours, rate, total, payment.getMethodName());

        return inv;
    }

    public double getCumulativeRevenue() { return cumulativeRevenue; }
    public List<Invoice> getInvoiceHistory() { return Collections.unmodifiableList(invoiceHistory); }

    public String generateSystemReport(int totalSlots, int occupiedSlots, int availableSlots, int waitingCount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("         EXECUTIVE PARKING & REVENUE REPORT         \n");
        sb.append("====================================================\n");
        sb.append(" Total Parking Slots      : ").append(totalSlots).append("\n");
        sb.append(" Currently Occupied       : ").append(occupiedSlots).append("\n");
        sb.append(" Currently Available      : ").append(availableSlots).append("\n");
        sb.append(" Drivers in Waiting List  : ").append(waitingCount).append("\n");
        sb.append(" Total Invoices Settled   : ").append(invoiceHistory.size()).append("\n");
        sb.append(" Total Revenue Collected  : $").append(df.format(cumulativeRevenue)).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append(" RECENT TRANSACTIONS:\n");
        for (int i = Math.max(0, invoiceHistory.size() - 5); i < invoiceHistory.size(); i++) {
            Invoice inv = invoiceHistory.get(i);
            sb.append("  • ").append(inv.getInvoiceId()).append(" | ")
              .append(inv.getLicensePlate()).append(" | ")
              .append(inv.getSlotCode()).append(" | $")
              .append(df.format(inv.getTotalAmount())).append(" (")
              .append(inv.getPaymentMode()).append(")\n");
        }
        sb.append("====================================================\n");
        return sb.toString();
    }
}