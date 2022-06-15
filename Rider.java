public class Rider implements Comparable<Rider> {
    public int destination_floor;
    public int rider_id;

    // When an elevator picks up an intermediate rider whilst going towards a destination, we want to make sure that
    // we don't skip over any floors

    // This prevents schenarios where say the elevator starts on floor 0 going to 4, then finds a person on 2 going
    // to floor 3. Without this we would go 0 -> 2 -> 4 -> 3 instead of  0 -> 2 -> 3 -> 4

    // This will get used when our elevators pick up intermediate riders
    @Override public int compareTo(Rider other_rider) {
        return this.destination_floor - other_rider.destination_floor;
    }

    public Rider(int destination, int id)
    {
        destination_floor = destination;
        rider_id = id;
    }
}
