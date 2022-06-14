import java.util.concurrent.*;

public class Elevator extends Thread {
    public static Semaphore currentCapacity;
    public int id;
    public string state = "Stationary";
    public int start_time = 0;

    public Elevator(int thread_id)
    {
        id = thread_id;
    }

    public void run()
    {

    }
    
}
