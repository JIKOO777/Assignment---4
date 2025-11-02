package graph.topo;

import model.Graph;
import model.Metrics;
import model.Edge;

import java.util.*;

public class TopoKahn {
    public static java.util.List<Integer> order(Graph dag, Metrics m) {
        int n = dag.n;
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (Edge e : dag.adj.get(u)) indeg[e.v]++;

        // детерминированно берём минимальный индекс
        PriorityQueue<Integer> q = new PriorityQueue<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) { q.offer(i); m.queuePushes++; }

        java.util.List<Integer> topo = new ArrayList<>(n);
        while (!q.isEmpty()) {
            int u = q.poll(); m.queuePops++;
            topo.add(u);
            for (Edge e : dag.adj.get(u)) {
                if (--indeg[e.v] == 0) { q.offer(e.v); m.queuePushes++; }
            }
        }
        if (topo.size() != n) throw new IllegalStateException("Graph is not a DAG");
        return topo;
    }
}
