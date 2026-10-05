package smartparking;

import java.util.*;

/**
 * MODULE 2: Core Parking Management Business Logic & OOP Architecture.
 * Demonstrates: Inheritance, Polymorphism, Abstraction, Encapsulation, Factory Pattern.
 */

// --- CUSTOM EXCEPTIONS ---
class SlotUnavailableException extends Exception {
    public SlotUnavailableException(String message) { super(message); }
}

class VehicleNotFoundException extends Exception {
    public VehicleNotFoundException(String message) { super(message); }
}

// --- VEHICLE TYPE ENUM ---
enum VehicleType {
    BIKE, CAR, EV, TRUCK
}

// --- ABSTRACT BASE CLASS (Abstraction & Encapsulation) ---
abstract class Vehicle {
    private final String licensePlate;
    private final String ownerName;
    private final Date entryTime;

    public Vehicle(String licensePlate, String ownerName) {
        this.licensePlate = licensePlate.toUpperCase().trim();
        this.ownerName = (ownerName == null || ownerName.trim().isEmpty()) ? "Guest Driver" : ownerName.trim();
        this.entryTime = new Date();
    }

    public String getLicensePlate() { return licensePlate; }
    public String getOwnerName() { return ownerName; }
    public Date getEntryTime() { return entryTime; }

    // Dynamic Polymorphism: Abstract methods overridden by subclasses
    public abstract VehicleType getVehicleType();
    public abstract double getBaseHourlyRate();
    public abstract String getCategoryDescription();
}

// --- INHERITANCE & METHOD OVERRIDING ---
class Bike extends Vehicle {
    public Bike(String licensePlate, String ownerName) { super(licensePlate, ownerName); }
    @Override public VehicleType getVehicleType() { return VehicleType.BIKE; }
    @Override public double getBaseHourlyRate() { return 10.0; }
    @Override public String getCategoryDescription() { return "Two-Wheeler / Bike"; }
}

class Car extends Vehicle {
    public Car(String licensePlate, String ownerName) { super(licensePlate, ownerName); }
    @Override public VehicleType getVehicleType() { return VehicleType.CAR; }
    @Override public double getBaseHourlyRate() { return 20.0; }
    @Override public String getCategoryDescription() { return "Standard Sedan / Hatchback"; }
}

class ElectricVehicle extends Vehicle {
    private final boolean chargingRequested;

    public ElectricVehicle(String licensePlate, String ownerName, boolean chargingRequested) {
        super(licensePlate, ownerName);
        this.chargingRequested = chargingRequested;
    }

    public boolean isChargingRequested() { return chargingRequested; }
    @Override public VehicleType getVehicleType() { return VehicleType.EV; }
    @Override public double getBaseHourlyRate() { return chargingRequested ? 28.0 : 22.0; }
    @Override public String getCategoryDescription() { return "EV (" + (chargingRequested ? "With Fast Charging" : "Standard") + ")"; }
}

class HeavyVehicle extends Vehicle {
    public HeavyVehicle(String licensePlate, String ownerName) { super(licensePlate, ownerName); }
    @Override public VehicleType getVehicleType() { return VehicleType.TRUCK; }
    @Override public double getBaseHourlyRate() { return 35.0; }
    @Override public String getCategoryDescription() { return "Heavy Vehicle / SUV / Bus"; }
}

// --- FACTORY PATTERN ---
class VehicleFactory {
    public static Vehicle createVehicle(VehicleType type, String plate, String owner, boolean evCharging) {
        switch (type) {
            case BIKE: return new Bike(plate, owner);
            case CAR: return new Car(plate, owner);
            case EV: return new ElectricVehicle(plate, owner, evCharging);
            case TRUCK: return new HeavyVehicle(plate, owner);
            default: throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
    }
}

// --- PARKING SLOT CLASS ---
class ParkingSlot {
    private final String slotCode;
    private final VehicleType designatedType;
    private boolean occupied;
    private boolean reserved;
    private Vehicle currentVehicle;

    public ParkingSlot(String slotCode, VehicleType designatedType) {
        this.slotCode = slotCode;
        this.designatedType = designatedType;
        this.occupied = false;
        this.reserved = false;
    }

    public String getSlotCode() { return slotCode; }
    public VehicleType getDesignatedType() { return designatedType; }
    public boolean isOccupied() { return occupied; }
    public boolean isReserved() { return reserved; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }

    public void assignVehicle(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.occupied = true;
        this.reserved = false;
    }

    public void vacate() {
        this.currentVehicle = null;
        this.occupied = false;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
}

// --- CORE PARKING SERVICE ---
public class ParkingManager {
    private final List<ParkingSlot> slots = new ArrayList<>();

    public ParkingManager() {
        initDefaultSlots();
    }

    private void initDefaultSlots() {
        // Zone A: 6 Bikes, Zone B: 8 Cars, Zone C: 6 EVs, Zone D: 4 Trucks (Total = 24)
        for (int i = 1; i <= 6; i++) slots.add(new ParkingSlot("A-" + String.format("%02d", i), VehicleType.BIKE));
        for (int i = 1; i <= 8; i++) slots.add(new ParkingSlot("B-" + String.format("%02d", i), VehicleType.CAR));
        for (int i = 1; i <= 6; i++) slots.add(new ParkingSlot("C-" + String.format("%02d", i), VehicleType.EV));
        for (int i = 1; i <= 4; i++) slots.add(new ParkingSlot("D-" + String.format("%02d", i), VehicleType.TRUCK));
    }

    public List<ParkingSlot> getAllSlots() { return Collections.unmodifiableList(slots); }

    public ParkingSlot findOptimalSlot(VehicleType type) throws SlotUnavailableException {
        // First look for designated unoccupied and unreserved slot
        for (ParkingSlot slot : slots) {
            if (!slot.isOccupied() && !slot.isReserved() && slot.getDesignatedType() == type) {
                return slot;
            }
        }
        // Fallback for cars in unoccupied EV or truck slots if allowed
        for (ParkingSlot slot : slots) {
            if (!slot.isOccupied() && !slot.isReserved()) {
                return slot;
            }
        }
        throw new SlotUnavailableException("No parking slot available for " + type);
    }

    public ParkingSlot parkVehicle(Vehicle vehicle, String specificSlotCode) throws SlotUnavailableException {
        // Check if already parked
        for (ParkingSlot s : slots) {
            if (s.isOccupied() && s.getCurrentVehicle().getLicensePlate().equalsIgnoreCase(vehicle.getLicensePlate())) {
                throw new SlotUnavailableException("Vehicle " + vehicle.getLicensePlate() + " is already parked at " + s.getSlotCode());
            }
        }

        ParkingSlot targetSlot = null;
        if (specificSlotCode != null && !specificSlotCode.trim().isEmpty()) {
            for (ParkingSlot s : slots) {
                if (s.getSlotCode().equalsIgnoreCase(specificSlotCode)) {
                    if (s.isOccupied()) throw new SlotUnavailableException("Slot " + specificSlotCode + " is already occupied!");
                    targetSlot = s;
                    break;
                }
            }
        }

        if (targetSlot == null) {
            targetSlot = findOptimalSlot(vehicle.getVehicleType());
        }

        targetSlot.assignVehicle(vehicle);
        return targetSlot;
    }

    public Vehicle checkOutVehicle(String identifier) throws VehicleNotFoundException {
        for (ParkingSlot s : slots) {
            if (s.isOccupied() && s.getCurrentVehicle() != null) {
                if (s.getSlotCode().equalsIgnoreCase(identifier) ||
                    s.getCurrentVehicle().getLicensePlate().equalsIgnoreCase(identifier)) {
                    Vehicle v = s.getCurrentVehicle();
                    s.vacate();
                    return v;
                }
            }
        }
        throw new VehicleNotFoundException("No vehicle found matching '" + identifier + "'");
    }

    public ParkingSlot searchVehicle(String plate) {
        for (ParkingSlot s : slots) {
            if (s.isOccupied() && s.getCurrentVehicle().getLicensePlate().equalsIgnoreCase(plate)) {
                return s;
            }
        }
        return null;
    }

    public int getAvailableCount() {
        int count = 0;
        for (ParkingSlot s : slots) if (!s.isOccupied() && !s.isReserved()) count++;
        return count;
    }

    public int getOccupiedCount() {
        int count = 0;
        for (ParkingSlot s : slots) if (s.isOccupied()) count++;
        return count;
    }
}