import java.util.concurrent.locks.*;
import java.util.ArrayList;

public class ElevatorSimulator implements Runnable {

	public static Clock SimulationClock;
	public static int simulationTime;
    public static int riders_served;
    public static int riders_turned_away;
    public static int total_requests_created;
	public static int numElevators = 0;
	public static int elevatorCapacity = 0;
	public static int milli_per_cycle;
	public static boolean debug_on;

	public ArrayList<Elevator> elevator_arr;
	
	// Allocate synchronization variables
	ReentrantLock elevatorClockLock = new ReentrantLock();
	ReentrantLock elevatorLock = new ReentrantLock();

	Condition elevatorClockTicked = elevatorClockLock.newCondition();	
	
	// Run the thread once we create it
	public ElevatorSimulator(int numElevators, int elevatorCapacity, int simulationTime, boolean debug, int milli)
	{
		ElevatorSimulator.numElevators = numElevators;
		ElevatorSimulator.elevatorCapacity = elevatorCapacity;
		ElevatorSimulator.debug_on = debug;
		ElevatorSimulator.simulationTime = simulationTime;
		ElevatorSimulator.milli_per_cycle = milli;

		this.elevator_arr = new ArrayList<Elevator>(numElevators);
		this.run();
	}

    public static void print_info(String msg)
    {
        if (ElevatorSimulator.debug_on)
        {
            System.out.println(msg);
        }
    }
			
	public void run() {		

		SimulationClock = new Clock();

		// Create Rider Manager
		RiderManager rider_manager = new RiderManager(5);
		rider_manager.start();

		for (int i = 0; i < ElevatorSimulator.numElevators; i++)
		{
			elevator_arr.add(new Elevator(ElevatorSimulator.elevatorCapacity));
			elevator_arr.get(i).start();
		}

		// Simulate Small Elevators		
		while (SimulationClock.getTick() < ElevatorSimulator.simulationTime)
		{
			try
			{
				Thread.sleep(ElevatorSimulator.milli_per_cycle);
				elevatorClockLock.lockInterruptibly(); // Use lockInterruptibly so that thread doesn't get stuck waiting for lock
				SimulationClock.tick();		
				elevatorClockTicked.signalAll();										
			}	
			catch (InterruptedException e) {}
			finally { elevatorClockLock.unlock(); }	
		}		

		for (var elevator : this.elevator_arr)
		{
			try { elevator.join(); }
			catch (InterruptedException e) {}
		}

		try { rider_manager.join(); }
		catch (InterruptedException e) {}

		// We keep the results in static variables.
		// These will get overwritten when we start another simulation
		ElevatorSimulator.riders_served = Elevator.served;
		ElevatorSimulator.riders_turned_away = Elevator.turned_away;
		ElevatorSimulator.total_requests_created = RiderManager.total_riders;

		Elevator.reset();
		RiderManager.reset();
		SimulationClock.reset();			
	}	
}
