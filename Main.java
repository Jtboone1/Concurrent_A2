public class Main {

	// Simulator takes in 5 arguments:
	// 1. Number of elevators
	// 2. Number of people allowed in each elevator
	// 3. Time of the simulation
	// 4. Debug mode will print statements during the simulation
	// 5. The number of milliseconds per clock cycle

	public static void main(String[] args)
	{
		new ElevatorSimulator(2, 2, 7200, true, 3);
		SimulationResults results1 = new SimulationResults();
		results1.gather_results();

		new ElevatorSimulator(4, 1, 7200, true, 3);
		SimulationResults results2 = new SimulationResults();
		results2.gather_results();

		results1.print_results();
		results2.print_results();
	}
}
