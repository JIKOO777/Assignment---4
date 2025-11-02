package graph.dagsp;

import model.Edge;
import model.Graph;

import java.util.*;

public class DagLongestPath {
    public static class Result {
        public final long[] val;
        public final int[] parent;
        public int bestV = -1;
        public Result(long[] v, int[] p) { this.val = v; this.parent = p; }
        public java.util.List<Integer> reconstruct() {
            if (bestV < 0) return java.util.List.of();
            java.util.List<Integer> path = new ArrayList<>();
            for (int v = bestV; v != -1; v = parent[v]) path.add(v);
            java.util.Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph dag, java.util.List<Integer> topo) {
        long NEG = Long.MIN_VALUE/4;
        long[] f = new long[dag.n];
        int[] parent = new int[dag.n];
        java.util.Arrays.fill(f, NEG);
        java.util.Arrays.fill(parent, -1);

        boolean[] hasPred = new boolean[dag.n];
        for (int u = 0; u < dag.n; u++) for (Edge e : dag.adj.get(u)) hasPred[e.v] = true;
        for (int v = 0; v < dag.n; v++) if (!hasPred[v]) f[v] = 0;

        for (int u : topo) {
            if (f[u] == NEG) continue;
            for (Edge e : dag.adj.get(u)) {
                long cand = f[u] + e.w;
                if (cand > f[e.v]) { f[e.v] = cand; parent[e.v] = u; }
            }
        }
        long best = NEG; int bestV = -1;
        for (int v = 0; v < dag.n; v++) if (f[v] > best) { best = f[v]; bestV = v; }
        Result r = new Result(f, parent);
        r.bestV = bestV;
        return r;
    }
}
