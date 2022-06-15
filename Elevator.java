import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class Elevator extends Thread {
    static Semaphore find_rider = new Semaphore(1);
    static int thread_id = -1;
    int elevator_number;
    int current_capacity;

    static int turned_away = 0;
    static int served = 0;

    String state = "Stationary Waiting For Requests";
    int current_floor = 0;
    ArrayList<Integer> destination_floors = new ArrayList<Integer>(0);
    int last_update_time = 0;

    public Elevator(int capacity)
    {
        current_capacity = capacity;
        Elevator.thread_id++;
        this.elevator_number = Elevator.thread_id;
    }

    // Causes thread to pause for a specified number of clock cycles
    public void wait(int cycles)
    {
        // Wait 5 clock cycles
        while (ElevatorSimulator.SimulationClock.getTick() - last_update_time < cycles && ElevatorSimulator.SimulationClock.getTick() != 0) 
        {
            System.out.println("Elevator " + this.elevator_number + " is waiting at tick: " + ElevatorSimulator.SimulationClock.getTick());
        }

        last_update_time = ElevatorSimulator.SimulationClock.getTick();
    }

    public void run()
    {
        // While the Elevator Simulator is running we want this thread to keep running!
        while (ElevatorSimulator.SimulationClock.getTick() != 0)
        {
            try { Thread.sleep(5); }
            catch (InterruptedException e) {}

            if (this.state == "Stationary Waiting For Requests")
            {
                boolean found_rider = false;
                try 
                {
                    // We want to make sure that our request handling isn't being performed at the same time,
                    // as we don't want two elevators handling the same request!
                    System.out.println("Elevator: " + this.elevator_number + " is requesting a rider at stationary floor on floor " + this.current_floor);
                    find_rider.acquire();
                    System.out.println("Elevator: " + this.elevator_number + " has lock");

                    for (int i = 1; i < 5; i++)
                    {
                        // Find nearest floor
                        int destination_floor_up = this.current_floor + i;
                        int destination_floor_down = this.current_floor - i;
                        if (destination_floor_up < 5 && !RiderManager.floor_requests.get(destination_floor_up).isEmpty())
                        {
                            this.current_capacity--;
                            Rider rider = RiderManager.floor_requests.get(destination_floor_up).remove(0);
                            this.destination_floors.add(rider.destination_floor);
                            this.state = "Moving Upwards";
                            found_rider = true;
                            System.out.println("Elevator: " + this.elevator_number + " found rider on floor " + this.destination_floors.get(0) + " and will now move to that floor upwards!");

                            break;
                        }
                        else if (destination_floor_down >= 0 && !RiderManager.floor_requests.get(destination_floor_down).isEmpty())
                        {
                            this.current_capacity--;
                            Rider rider = RiderManager.floor_requests.get(destination_floor_down).remove(0);
                            this.destination_floors.add(rider.destination_floor);
                            this.state = "Moving Downwards";
                            found_rider = true;
                            System.out.println("Elevator: " + this.elevator_number + " found rider on floor " + this.destination_floors.get(0) + " and will now move to that floor downwards!");

                            break;
                        }
                    }
                }
                catch (InterruptedException e) {}
                finally { 
                    System.out.println("Elevator: " + this.elevator_number + " is releasing lock on floor " + this.current_floor + " whilst stationary");
                    if (!found_rider)
                    {
                        System.out.println("Found a rider for Elevator " + this.elevator_number);
                    }
                    else
                    {
                        System.out.println("No rider found for Elevator " + this.elevator_number);
                    }
                    find_rider.release(); 
                }
            }
            else if (this.state == "Moving Upwards" || this.state == "Moving Downwards")
            {
                this.wait(5);

                if (this.state == "Moving Upwards")
                {
                    this.current_floor++;
                    System.out.println("Elevator: " + this.elevator_number + " is moving upwards towards " + this.destination_floors.get(0));
                }
                else
                {
                    this.current_floor--;
                    System.out.println("Elevator: " + this.elevator_number + " is moving downwards towards " + this.destination_floors.get(0));
                }

                if (this.current_floor == this.destination_floors.get(0))
                {
                    System.out.println("Elevator: " + this.elevator_number + " is now unloading at destination!");
                    this.state = "Unloading At Destination";
                }
                else
                {
                    System.out.println("Elevator: " + this.elevator_number + " will now do intermediate check!");
                    this.state = "Intermediate Floor Check";
                }
            }
            else if (this.state == "Intermediate Floor Check")
            {
                boolean found_new_rider = false;

                try
                {
                    System.out.println("Elevator: " + this.elevator_number + " is requesting a rider at intermediate at floor " + this.current_floor);
                    find_rider.acquire();
                    System.out.println("Elevator: " + this.elevator_number + " has lock");
                    while (this.current_capacity != 0 && !RiderManager.floor_requests.get(current_floor).isEmpty())
                    {
                        for (int i = 0; i < RiderManager.floor_requests.get(current_floor).size(); i++)
                        {
                            Rider rider = RiderManager.floor_requests.get(current_floor).get(i);

                            // If the intermediate rider wants to go in the same direction that we're already going
                            if (  
                                ((rider.destination_floor > current_floor && destination_floors.get(0) > current_floor)
                                || (rider.destination_floor < current_floor && destination_floors.get(0) < current_floor))
                                && this.current_capacity > 0
                            )
                            {
                                destination_floors.add(rider.destination_floor);
                                this.current_capacity--;
                                RiderManager.floor_requests.get(current_floor).remove(i);
                                found_new_rider = true;
                                break;
                            }
                            else
                            {
                                RiderManager.floor_requests.get(current_floor).remove(i);
                                Elevator.turned_away++;
                                break;
                            }
                        }
                    }
                }
                catch (InterruptedException e) {}
                finally { 
                    System.out.println("Elevator: " + this.elevator_number + " is releasing lock on floor " + this.current_floor + " whilst stopping intermediately");
                    if (found_new_rider)
                    {
                        System.out.println("Rider found for Elevator " + this.elevator_number + " on floor " + this.current_floor + " during intermediate stop!");
                    }
                    else
                    {
                        System.out.println("No rider found for Elevator " + this.elevator_number + " on floor " + this.current_floor + " during intermediate stop!");
                    }
                    find_rider.release(); 
                }

                // If we found a new rider along our path, we wait 15 clock cycles as we stop to pick them up.
                if (found_new_rider)
                {
                    this.wait(15);
                }

                if (this.current_floor < this.destination_floors.get(0))
                {
                    System.out.println("Elevator: " + this.elevator_number + " is moving upwards after intermediate stop!");
                    this.state = "Moving Upwards";
                }
                else
                {
                    System.out.println("Elevator: " + this.elevator_number + " is moving downwards after intermediate stop!");
                    this.state = "Moving Downwards";
                }
            }
            else if (this.state == "Unloading At Destination")
            {

                this.wait(15);

                System.out.println("Unloading at floor: " + current_floor);
                while (!destination_floors.isEmpty() && destination_floors.get(0) == current_floor)
                {
                    destination_floors.remove(0);
                    served++;
                    this.current_capacity++;
                }

                if (destination_floors.isEmpty())
                {
                    System.out.println("Floor " + current_floor + " is back to waiting for requests!");
                    this.state = "Stationary Waiting For Requests";
                }
                else if (this.current_floor < this.destination_floors.get(0))
                {
                    System.out.println("Floor " + current_floor + " is moving upwards after unloading!");
                    this.state = "Moving Upwards";
                }
                else
                {
                    System.out.println("Floor " + current_floor + " is moving downwards after unloading!");
                    this.state = "Moving Downwards";
                }
            }
        }
    }
}
