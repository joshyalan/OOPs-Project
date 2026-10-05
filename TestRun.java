package smartparking;

public class TestRun {
    public static void main(String[] args) {
        try {
            SmartParkingGUI gui = new SmartParkingGUI();
            System.out.println("GUI instantiated successfully.");
            
            // Try to access ParkingManager and do something to see if it throws error
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
