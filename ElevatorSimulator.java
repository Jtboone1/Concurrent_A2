import java.util.concurrent.locks.*;

public class ElevatorSimulator implements Runnable {

	public static Clock SimulationClock;
	
	public int numElevators = 0;
	public static int elevatorCapacity = 0;
	public static int simulationTime;
	
	// Allocate synchronization variables
	ReentrantLock elevatorClockLock = new ReentrantLock();
	ReentrantLock elevatorLock = new ReentrantLock();

	Condition elevatorClockTicked = elevatorClockLock.newCondition();	

	public int get_sim_time()
	{
		return simulationTime;
	}
	
	public ElevatorSimulator(int numElevators, int elevatorCapacity, int simulationTime)
	{
		this.numElevators = numElevators;
		ElevatorSimulator.elevatorCapacity = elevatorCapacity;
		ElevatorSimulator.simulationTime = simulationTime;
	}
			
	public void run() {		

		SimulationClock = new Clock();

		// Create Rider Manager
		RiderManager rider_manager = new RiderManager(5);
		rider_manager.start();

		for (int i = 0; i < this.numElevators; i++)
		{
			new Elevator(i).start();
		}

		// Simulate Small Elevators		
		while (SimulationClock.getTick() < simulationTime)
		{
			try
			{
				Thread.sleep(5);
				elevatorClockLock.lockInterruptibly(); // Use lockInterruptibly so that thread doesn't get stuck waiting for lock
				SimulationClock.tick();		
				elevatorClockTicked.signalAll();										
			}	
			catch (InterruptedException e) {}
			finally { elevatorClockLock.unlock(); }	
		}		

		System.out.println("FINISHED!");
		System.out.println("Riders served: " + Elevator.served);
		System.out.println("Riders turned away: " + Elevator.turned_away);

		SimulationClock.reset();			
	}	
}
