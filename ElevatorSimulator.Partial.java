package mun.concurrent.assignment.two;

import java.util.concurrent.locks.*;

public class ElevatorSimulator implements Runnable {

	public static Clock SimulationClock;
	private static ElevatorArray elevators;	
	
	public final int numElevators;
	public final int elevatorCapacity;
	public final int simulationTime;
	
	private ElevatorStats elevatorStats;
	private ElevatorRiderFactory elevatorRiderFactory;
	
	// Allocate synchronization variables
	ReentrantLock elevatorClockLock = new ReentrantLock();
	ReentrantLock elevatorLock = new ReentrantLock();

	Condition elevatorClockTicked = elevatorClockLock.newCondition();	

	//<MORE VARIABLES MAY BE NEEDED HERE>
	
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
			Elevator new_elevator = new Elevator(i).run();
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
