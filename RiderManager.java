import java.util.*;
import java.util.ArrayList;
import java.util.List;

// Rider Manager creates Riders for each floor depending on
// if enough time has past.

// This class, besides the rider request matrix, will be completely independant
// from the elevator threads
public class RiderManager extends Thread {

    // Bounding time for each new rider request
    private final int request_time_upper = 121;
    private final int request_time_lower = 20;

    private final int max_floor;
    private final int min_floor = 0;

    // Keeps track of each elevators time since it got a new request
    public ArrayList<Integer> floor_last_request_time;

    // If the difference between the clock's tick and the last request time is greater than
    // the offset, that's how we'll know to generate a new request
    public ArrayList<Integer> floor_time_offset;

    // List of riders that are made available for elevators to pickup
    public static ArrayList<List<Rider>> floor_requests;
    public static int total_riders = 0;

    public RiderManager(int floors)
    {
        floor_last_request_time = new ArrayList<Integer>(floors);
        floor_time_offset = new ArrayList<Integer>(floors);
        max_floor = floors;
        total_riders = 0;

        // This matrix will have a list of request for each floor. Each index,
        // in the matrix will represent a floor. Our elevators will access this to take on
        // new requests
        floor_requests = new ArrayList<List<Rider>>(floors);

        // Initialize each floors last request time as 0 and each floor has
        // 0 request pending on initialization
        for (int i = 0; i < floors; i++)
        {
            // Considering both the elevatuors and the requests will be removing / adding the available riders concurrently,
            // it would be smart to use a synchronized list, as accessing a list whilst riders are being added / removed
            // could lead to some issues.
            List<Rider> new_floor = new ArrayList<Rider>(0);
            List<Rider> new_sync_floor = Collections.synchronizedList(new_floor);
            floor_requests.add(new_sync_floor);

            int random_offset = (int) (Math.random() * (this.request_time_upper - this.request_time_lower)) + this.request_time_lower;
            floor_last_request_time.add(0);
            floor_time_offset.add(random_offset);
        }
    }

    public static void reset()
    {
        floor_requests = null;
        total_riders = 0;
    }

    public static void print_info(String msg)
    {
        if (ElevatorSimulator.debug_on)
        {
            System.out.println(msg);
        }
    }

    public void run()
    {
        // While the Elevator Simulator is running we want this thread to keep running!
        while (ElevatorSimulator.SimulationClock.getTick() < ElevatorSimulator.simulationTime)
        {
            try { Thread.sleep(ElevatorSimulator.milli_per_cycle); }
            catch (InterruptedException e) {}

            for (int floor = 0; floor < floor_last_request_time.size(); floor++)
            {
                int currentClockCycle = ElevatorSimulator.SimulationClock.getTick();

                // If sufficient clock ticks have passed since last request
                if (currentClockCycle - floor_last_request_time.get(floor) > floor_time_offset.get(floor))
                {
                    // Generate new rider request on random floor
                    int random_destination = floor;

                    // Makes sure the destination is not the same as the floor!
                    while (random_destination == floor)
                    {
                        random_destination = (int) (Math.random() * (this.max_floor - this.min_floor)) + this.min_floor;
                    }

                    // Create new request
                    Rider new_rider = new Rider(random_destination, total_riders);
                    floor_requests.get(floor).add(new_rider);
                    total_riders++;

                    int new_offset = (int) (Math.random() * (this.request_time_upper - this.request_time_lower)) + this.request_time_lower;

                    // Reset Ticks
                    floor_time_offset.set(floor, new_offset);
                    floor_last_request_time.set(floor, currentClockCycle);
                }
            }
            ElevatorSimulator.print_info("Total Requests Created: " + total_riders + " after " + ElevatorSimulator.SimulationClock.getTick() + " cycles");
        }
    }
}
