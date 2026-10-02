package smartparking;

import java.util.*;

/**
 * MODULE 3: Reservation System & Queue-Based Waiting List Management.
 * Demonstrates: Interfaces, Collections (Queue / LinkedList), Event Synchronization.
 */

// --- INTERFACE (Abstraction) ---
interface Reservable {
    boolean reserveSlot(String customerName, String phone, String plate, VehicleType type);
    boolean cancelReservation(String reservationId);
}

// --- RESERVATION DATA MODEL ---
class Reservation {
    private final String reservationId;
    private final String customerName;
    private final String contactPhone;
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final String allocatedSlotCode;
    private final Date bookingTime;

    public Reservation(String customerName, String contactPhone, String licensePlate, VehicleType vehicleType, String allocatedSlotCode) {
        this.reservationId = "RES-" + (System.currentTimeMillis() % 100000);
        this.customerName = customerName;
        this.contactPhone = contactPhone;
        this.licensePlate = licensePlate.toUpperCase().trim();
        this.vehicleType = vehicleType;
        this.allocatedSlotCode = allocatedSlotCode;
        this.bookingTime = new Date();
    }

    public String getReservationId() { return reservationId; }
    public String getCustomerName() { return customerName; }
    public String getContactPhone() { return contactPhone; }
    public String getLicensePlate() { return licensePlate; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getAllocatedSlotCode() { return allocatedSlotCode; }
    public Date getBookingTime() { return bookingTime; }
}

// --- WAITING DRIVER RECORD ---
class WaitingDriver {
    private final String name;
    private final String plate;
    private final VehicleType type;
    private final Date joinedTime;

    public WaitingDriver(String name, String plate, VehicleType type) {
        this.name = name;
        this.plate = plate.toUpperCase().trim();
        this.type = type;
        this.joinedTime = new Date();
    }

    public String getName() { return name; }
    public String getPlate() { return plate; }
    public VehicleType getType() { return type; }
    public Date getJoinedTime() { return joinedTime; }
}

// --- RESERVATION & WAITING LIST CONTROLLER ---
public class ReservationSystem implements Reservable {

    private final ParkingManager parkingManager;
    private final Map<String, Reservation> activeReservations = new LinkedHashMap<>();
    private final Queue<WaitingDriver> waitingQueue = new LinkedList<>();

    public ReservationSystem(ParkingManager parkingManager) {
        this.parkingManager = parkingManager;
    }

    @Override
    public synchronized boolean reserveSlot(String customerName, String phone, String plate, VehicleType type) {
        try {
            // Find a slot to reserve
            ParkingSlot availableSlot = parkingManager.findOptimalSlot(type);
            availableSlot.setReserved(true);

            Reservation res = new Reservation(customerName, phone, plate, type, availableSlot.getSlotCode());
            activeReservations.put(res.getReservationId(), res);

            // Persist to DB
            DatabaseManager.getInstance().saveReservation(res.getReservationId(), customerName, phone, plate, availableSlot.getSlotCode());
            return true;
        } catch (SlotUnavailableException e) {
            // Add to waiting list queue
            addToWaitingList(customerName, plate, type);
            return false;
        }
    }

    public synchronized void addToWaitingList(String name, String plate, VehicleType type) {
        waitingQueue.offer(new WaitingDriver(name, plate, type));
    }

    public synchronized WaitingDriver peekNextWaiting() {
        return waitingQueue.peek();
    }

    public synchronized WaitingDriver pollNextWaiting() {
        return waitingQueue.poll();
    }

    public synchronized int getWaitingCount() {
        return waitingQueue.size();
    }

    public synchronized List<WaitingDriver> getWaitingListSnapshot() {
        return new ArrayList<>(waitingQueue);
    }

    public synchronized List<Reservation> getActiveReservations() {
        return new ArrayList<>(activeReservations.values());
    }

    @Override
    public synchronized boolean cancelReservation(String reservationId) {
        Reservation res = activeReservations.remove(reservationId);
        if (res != null) {
            for (ParkingSlot s : parkingManager.getAllSlots()) {
                if (s.getSlotCode().equals(res.getAllocatedSlotCode())) {
                    s.setReserved(false);
                    break;
                }
            }
            return true;
        }
        return false;
    }
}