package graph.dagsp;

import model.Edge;
import model.Graph;
import model.Metrics;

import java.util.*;

public class DagShortestPaths {
    public static class Result {
        public final long[] dist;
        public final int[] parent;
        public Result(long[] d, int[] p) { this.dist = d; this.parent = p; }
        public java.util.List<Integer> reconstruct(int t) {
            java.util.List<Integer> path = new ArrayList<>();
            if (dist[t] >= Long.MAX_VALUE/4) return path;
            for (int v = t; v != -1; v = parent[v]) path.add(v);
            java.util.Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph dag, java.util.List<Integer> topo, int s, Metrics m) {
        long INF = Long.MAX_VALUE/4;
        long[] dist = new long[dag.n];
        int[] parent = new int[dag.n];
        java.util.Arrays.fill(dist, INF);
        java.util.Arrays.fill(parent, -1);
        dist[s] = 0;

        for (int u : topo) {
            if (dist[u] == INF) continue;
            for (Edge e : dag.adj.get(u)) {
                long nd = dist[u] + e.w;
                if (nd < dist[e.v]) { dist[e.v] = nd; parent[e.v] = u; m.relaxations++; }
            }
        }
        return new Result(dist, parent);
    }
}
