import java.util.Map;
import java.util.ArrayList;

import mun.concurrent.assignment.two.ElevatorSimulator;

public class RiderManager extends Thread {

    private final int request_time_upper = 120;
    private final int request_time_lower = 20;

    // Floors will contain the time from last request
    public static Map<Integer, Integer> floors_and_riders;
    public static ArrayList<Integer> time_since_last_request;

    public RiderManager(int size)
    {
        floors_and_riders = new Map<Integer, Integer>(size);
        time_since_last_request = new ArrayList<Integer>(size);

        for (int i = 0; i < size; i++)
        {
            floors_and_riders.put(i, 0);
            time_since_last_request.add(0);
        }
    }

    public void run()
    {
        while (ElevatorSimulator.SimulationClock.getTick() < ElevatorSimulator.simulatorTime)
        {
            for(var floor : floors_and_riders.entrySet())
            {
                int time_for_request = (int) (Math.random() * (this.request_time_upper - this.request_time_lower)) + this.request_time_lower;

            }

        }
    }

    
}
