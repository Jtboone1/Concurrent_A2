import java.util.concurrent.Semaphore;
import java.util.Collections;
import java.util.ArrayList;

// Each elevator works concurrently to handle requests.
// We use a find_rider semaphore to ensure each request is
// only handled by 1 elevator.
public class Elevator extends Thread {

    static Semaphore find_rider = new Semaphore(1);
    static int thread_id = -1;
    static int total_turned_away = 0;

    // Requests and requests per elevator
    static int total_served = 0;

    int elevator_number;
    int current_capacity;
    int current_floor = 0;
    int last_update_time = 0;
    int served = 0;
    int total_time_serving = 0;

    // The elevator operates on a state machine, where depending on its destination array and current state,
    // will determine the output of the next state.

    // When specific states are reached, we pass a specific amount of clock cycles before proceeding.
    String state = "Stationary Waiting For Requests";
    ArrayList<Integer> destination_floors = new ArrayList<Integer>(0);

    public Elevator(int capacity)
    {
        current_capacity = capacity;

        Elevator.thread_id++;
        this.elevator_number = Elevator.thread_id;
    }

    public static void reset()
    {
        find_rider = new Semaphore(1);
        total_turned_away = 0;
        total_served = 0;
        thread_id = -1;
    }

    // Causes thread to pause for a specified number of clock cycles.
    public void wait(int cycles)
    {
        // Wait however many clock cycles, or break if the main simulation is over!
        total_time_serving += cycles;
        while (ElevatorSimulator.SimulationClock.getTick() - last_update_time < cycles && ElevatorSimulator.SimulationClock.getTick() < ElevatorSimulator.simulationTime) 
        {
            try { Thread.sleep(1); }
            catch (InterruptedException e) {}
        }

        last_update_time = ElevatorSimulator.SimulationClock.getTick();
    }

    // Add new rider and start moving.
    public void pickup_new_rider(int pickup_floor)
    {
        // 5 seconds per floor we pass to get to our rider
        this.wait(5 * Math.abs(current_floor - pickup_floor));

        this.current_capacity--;
        Rider rider = RiderManager.floor_requests.get(pickup_floor).remove(0);

        // Add new rider destination, remove that request from the rider manager, and change our floor to the floor we've just moved to in order to pick up the rider.
        this.destination_floors.add(rider.destination_floor);
        current_floor = pickup_floor;
        ElevatorSimulator.print_info("Elevator " + this.elevator_number + " found rider on floor " + this.destination_floors.get(0) + " from floor " + current_floor + " and will now move to that floor!");

        // Once rider is on elevator (Indicated by destination_floor not being empty), start moving!
        if (current_floor < rider.destination_floor)
        {
            this.state = "Moving Upwards";
        }
        else
        {
            this.state = "Moving Downwards";
        }
    }

    public void run()
    {
        // While the Elevator Simulator is running we want this thread to keep running!
        while (ElevatorSimulator.SimulationClock.getTick() < ElevatorSimulator.simulationTime)
        {
            try { Thread.sleep(ElevatorSimulator.milli_per_cycle); }
            catch (InterruptedException e) {}

            if (this.state == "Stationary Waiting For Requests")
            {
                try 
                {
                    // We want to make sure that our request handling isn't being performed at the same time,
                    // as we don't want two elevators handling the same request!
                    ElevatorSimulator.print_info("Elevator " + this.elevator_number + " on floor " + current_floor + " is trying to request a rider");
                    find_rider.acquire();

                    for (int i = 0; i < 4; i++)
                    {
                        // Find nearest floor
                        int destination_floor_up = current_floor + i;
                        int destination_floor_down = current_floor - i;
                        if (destination_floor_up < 5 && !RiderManager.floor_requests.get(destination_floor_up).isEmpty())
                        {
                            this.pickup_new_rider(destination_floor_up);
                            break;
                        }
                        else if (destination_floor_down >= 0 && !RiderManager.floor_requests.get(destination_floor_down).isEmpty())
                        {
                            this.pickup_new_rider(destination_floor_down);
                            break;
                        }
                    }
                }
                catch (InterruptedException e) {}
                finally { find_rider.release(); }
            }
            // When we have someone loaded into the elevator, move up or down.
            else if (this.state == "Moving Upwards" || this.state == "Moving Downwards")
            {
                this.wait(5);

                if (this.state == "Moving Upwards")
                {
                    current_floor++;
                    ElevatorSimulator.print_info("Elevator " + this.elevator_number + " is moving upwards towards " + this.destination_floors.get(0));
                }
                else
                {
                    current_floor--;
                    ElevatorSimulator.print_info("Elevator " + this.elevator_number + " is moving downwards towards " + this.destination_floors.get(0));
                }

                if (current_floor == this.destination_floors.get(0))
                {
                    ElevatorSimulator.print_info("Elevator " + this.elevator_number + " is now unloading at destination");
                    this.state = "Unloading At Destination";
                }
                else
                {
                    ElevatorSimulator.print_info("Elevator " + this.elevator_number + " is doing intermediate check");
                    this.state = "Intermediate Floor Check";
                }
            }
            // See if anyone on an intermediate floor is moving in same direction.
            else if (this.state == "Intermediate Floor Check")
            {
                boolean found_new_rider = false;

                try
                {
                    find_rider.acquire();
                    while (!RiderManager.floor_requests.get(current_floor).isEmpty())
                    {
                        Rider rider = RiderManager.floor_requests.get(current_floor).get(0);

                        // If the intermediate rider wants to go in the same direction that we're already going
                        if (  
                            ((rider.destination_floor > current_floor && destination_floors.get(0) > current_floor)
                            || (rider.destination_floor < current_floor && destination_floors.get(0) < current_floor))
                            && this.current_capacity > 0
                        )
                        {
                            destination_floors.add(rider.destination_floor);
                            Collections.sort(destination_floors);

                            // If were moving downwards, we reverse the priority of destination floors
                            if (rider.destination_floor < current_floor) 
                            { 
                                Collections.reverse(destination_floors); 
                            }

                            this.current_capacity--;
                            RiderManager.floor_requests.get(current_floor).remove(0);
                            found_new_rider = true;
                            break;
                        }
                        // If the person on the floor wants to go in the opposite direction, turn them away.
                        else
                        {
                            ElevatorSimulator.print_info("Elevator " + this.elevator_number + " is turning someone away on floor + " + current_floor);
                            RiderManager.floor_requests.get(current_floor).remove(0);
                            Elevator.total_turned_away++;
                            break;
                        }
                    }
                }
                catch (InterruptedException e) {}
                finally { find_rider.release(); }

                // If we found a new rider along our path, we wait 15 clock cycles as we stop to pick them up.
                if (found_new_rider)
                {
                    this.wait(15);
                }

                if (current_floor < this.destination_floors.get(0))
                {
                    this.state = "Moving Upwards";
                }
                else
                {
                    this.state = "Moving Downwards";
                }
            }
            else if (this.state == "Unloading At Destination")
            {

                this.wait(15);

                while (!destination_floors.isEmpty() && destination_floors.get(0) == current_floor)
                {
                    destination_floors.remove(0);
                    total_served++;
                    this.current_capacity++;

                    int elevator_total_requests = ElevatorSimulator.requests_per_elevator.get(elevator_number) + 1;
                    ElevatorSimulator.requests_per_elevator.set(elevator_number, elevator_total_requests);
                }

                if (destination_floors.isEmpty())
                {
                    ElevatorSimulator.print_info("Elevator " + this.elevator_number + " has stopped moving on floor " + current_floor + " and has no requests");
                    this.state = "Stationary Waiting For Requests";
                }
                else if (current_floor < this.destination_floors.get(0))
                {
                    this.state = "Moving Upwards";
                }
                else
                {
                    this.state = "Moving Downwards";
                }
            }
        }
        ElevatorSimulator.print_info("Elevator " + this.elevator_number + " thread has finished!");
        ElevatorSimulator.non_idle_time_per_elevator.set(elevator_number, total_time_serving);
    }
}
