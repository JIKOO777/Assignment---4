package graph.scc;

import model.Graph;
import model.Metrics;

import java.util.*;

public class TarjanSCC {
    private final Graph g;
    private final Metrics m;
    private int time = 0, compCount = 0;
    private final int[] disc, low, compId;
    private final boolean[] inStack;
    private final Deque<Integer> st = new ArrayDeque<>();

    public TarjanSCC(Graph g, Metrics m) {
        this.g = g; this.m = m;
        int n = g.n;
        disc = new int[n]; Arrays.fill(disc, -1);
        low  = new int[n];
        compId = new int[n]; Arrays.fill(compId, -1);
        inStack = new boolean[n];
    }

    public static class Result {
        public final int components;
        public final java.util.List<java.util.List<Integer>> groups;
        public final int[] compId;
        public Result(int components, java.util.List<java.util.List<Integer>> groups, int[] compId) {
            this.components = components; this.groups = groups; this.compId = compId;
        }
    }

    public Result run() {
        java.util.List<java.util.List<Integer>> groups = new ArrayList<>();
        for (int v = 0; v < g.n; v++) if (disc[v] == -1) dfs(v, groups);
        return new Result(compCount, groups, compId);
    }

    private void dfs(int u, java.util.List<java.util.List<Integer>> groups) {
        m.dfsVisits++;
        disc[u] = low[u] = time++;
        st.push(u); inStack[u] = true;

        for (var e : g.adj.get(u)) {
            m.dfsEdges++;
            int v = e.v;
            if (disc[v] == -1) {
                dfs(v, groups);
                low[u] = Math.min(low[u], low[v]);
            } else if (inStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }
        if (low[u] == disc[u]) {
            java.util.List<Integer> comp = new ArrayList<>();
            while (true) {
                int v = st.pop(); inStack[v] = false;
                compId[v] = compCount;
                comp.add(v);
                if (v == u) break;
            }
            groups.add(comp);
            compCount++;
        }
    }
}
