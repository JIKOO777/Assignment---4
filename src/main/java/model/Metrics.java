 package model;

public class Metrics {
    // SCC
    public long dfsVisits = 0;
    public long dfsEdges  = 0;

    // Topo (Kahn)
    public long queuePushes = 0;
    public long queuePops   = 0;

    // DAG-SP
    public long relaxations = 0;

    public long nanos = 0;
}
