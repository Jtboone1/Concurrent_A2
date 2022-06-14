import java.util.concurrent.locks.*;

public class ElevatorSimulator implements Runnable {

	public static Clock SimulationClock;
	// private static ElevatorArray elevators;	
	
	public static int numElevators = 0;
	public static int elevatorCapacity = 0;
	public static int simulationTime = 0;
	
	// private ElevatorStats elevatorStats;
	// private ElevatorRiderFactory elevatorRiderFactory;
	
	// Allocate synchronization variables
	ReentrantLock elevatorClockLock = new ReentrantLock();
	ReentrantLock elevatorLock = new ReentrantLock();

	Condition elevatorClockTicked = elevatorClockLock.newCondition();	

	//<MORE VARIABLES MAY BE NEEDED HERE>

	public static int get_sim_time()
	{
		return simulationTime;
	}
	
	// Constructor
	public ElevatorSimulator(int numElevators, int elevatorCapacity, int simulationTime)
	{
		this.numElevators = numElevators;
		this.elevatorCapacity = elevatorCapacity;
		this.simulationTime = simulationTime;
	}
			
	public void run() {		

		//<INITIALIZATION HERE>

		SimulationClock = new Clock();


		for (int i = 0; i < this.numElevators; i++)
		{
			new Elevator(i).run();
		}
		// Simulate Small Elevators		
		while (SimulationClock.getTick() < simulationTime)
		{
			try
			{
				Thread.sleep(50);
				elevatorClockLock.lockInterruptibly(); // Use lockInterruptibly so that thread doesn't get stuck waiting for lock
				SimulationClock.tick();		
				elevatorClockTicked.signalAll();										
			}	
			catch (InterruptedException e)
			{				
			}
			finally
			{	
				elevatorClockLock.unlock();			
			}	
		}		
		
		// Output elevator stats

		//<PRINT OUT STATS GATHERED DURING SIMULATION>

		SimulationClock.reset();			
	}	
}
