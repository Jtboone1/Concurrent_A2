public class SimulationResults {
    public static int sim_number = -1;
    public int sim_id;
    public int total_served;
    public int total_turned_away;
    public int total_requests_created;
    public int elevator_capacity;
    public int number_of_elevators;
    public int time_of_simulation;

    public SimulationResults()
    {
        sim_number++;
        sim_id = sim_number;
    }

    public SimulationResults gather_results()
    {
        total_served = ElevatorSimulator.riders_served;
        total_turned_away = ElevatorSimulator.riders_turned_away;
        total_requests_created = ElevatorSimulator.total_requests_created;
        elevator_capacity = ElevatorSimulator.elevatorCapacity;
        time_of_simulation = ElevatorSimulator.simulationTime;
        number_of_elevators = ElevatorSimulator.numElevators;

        return this;
    }

    public void print_results()
    {
        System.out.println("\n\nSimulation #" + sim_id + " Complete:");

        System.out.println("Simulation Time:          " + time_of_simulation);
        System.out.println("Number of Elevators:      " + number_of_elevators);
        System.out.println("Capacity per Elevator:    " + elevator_capacity);
        System.out.println("Total Rider Requests:     " + total_requests_created);
        System.out.println("Total Riders Served:      " + total_served);
        System.out.println("Total Riders Turned Away: " + total_turned_away);
    }
}
