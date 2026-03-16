import java.time.*;
import java.time.temporal.ChronoUnit;

class ParkingSpot {
    String licensePlate;
    LocalDateTime entryTime;
    Status status;

    enum Status { EMPTY, OCCUPIED, DELETED }

    ParkingSpot() {
        status = Status.EMPTY;
    }
}

class ParkingSystem {

    private static final int TOTAL_SPOTS = 500;
    private final ParkingSpot[] table = new ParkingSpot[TOTAL_SPOTS];

    // Stats
    private int occupiedCount = 0;
    private int totalProbes = 0;
    private int totalParks = 0;

    public ParkingSystem() {
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Custom hash function
    private int hash(String plate) {
        int hash = 0;
        for (char c : plate.toCharArray()) {
            hash = (hash * 31 + c) % TOTAL_SPOTS;
        }
        return hash;
    }

    // Park vehicle
    public void parkVehicle(String plate) {
        int index = hash(plate);
        int probes = 0;

        while (table[index].status == ParkingSpot.Status.OCCUPIED) {
            index = (index + 1) % TOTAL_SPOTS;
            probes++;
        }

        table[index].licensePlate = plate;
        table[index].entryTime = LocalDateTime.now();
        table[index].status = ParkingSpot.Status.OCCUPIED;

        occupiedCount++;
        totalProbes += probes;
        totalParks++;

        System.out.println("Assigned spot #" + index + " (" + probes + " probes)");
    }

    // Exit vehicle
    public void exitVehicle(String plate) {
        int index = hash(plate);

        while (table[index].status != ParkingSpot.Status.EMPTY) {
            if (table[index].status == ParkingSpot.Status.OCCUPIED &&
                table[index].licensePlate.equals(plate)) {

                LocalDateTime exitTime = LocalDateTime.now();
                long minutes = ChronoUnit.MINUTES.between(
                        table[index].entryTime, exitTime);

                double fee = calculateFee(minutes);

                table[index].status = ParkingSpot.Status.DELETED;
                occupiedCount--;

                System.out.println("Spot #" + index + " freed");
                System.out.println("Duration: " + (minutes/60) + "h " + (minutes%60) + "m");
                System.out.printf("Fee: $%.2f\n", fee);
                return;
            }
            index = (index + 1) % TOTAL_SPOTS;
        }

        System.out.println("Vehicle not found.");
    }

    private double calculateFee(long minutes) {
        double hours = Math.ceil(minutes / 60.0);
        return hours * 5.0; // $5 per hour
    }

    // Nearest spot to entrance (index 0)
    public int findNearestAvailable() {
        for (int i = 0; i < TOTAL_SPOTS; i++) {
            if (table[i].status != ParkingSpot.Status.OCCUPIED)
                return i;
        }
        return -1;
    }

    // Statistics
    public void getStatistics() {
        double occupancy = (occupiedCount * 100.0) / TOTAL_SPOTS;
        double avgProbes = totalParks == 0 ? 0 : (double) totalProbes / totalParks;

        System.out.printf("Occupancy: %.1f%%\n", occupancy);
        System.out.printf("Avg Probes: %.2f\n", avgProbes);
        System.out.println("Peak Hour: 2–3 PM (sample)");
    }
}

public class ParkingApp {
    public static void main(String[] args) {
        ParkingSystem ps = new ParkingSystem();

        ps.parkVehicle("ABC-1234");
        ps.parkVehicle("ABC-1235");
        ps.parkVehicle("XYZ-9999");

        ps.exitVehicle("ABC-1234");

        ps.getStatistics();
    }
}
