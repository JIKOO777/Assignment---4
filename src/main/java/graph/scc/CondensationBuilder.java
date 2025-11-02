package graph.scc;

import model.Edge;
import model.Graph;
import java.util.*;

public class CondensationBuilder {
    public static Graph build(Graph g, int comps, int[] compId) {
        Graph dag = new Graph(comps, true);
        Set<Long> seen = new HashSet<>();
        for (int u = 0; u < g.n; u++) {
            int cu = compId[u];
            for (Edge e : g.adj.get(u)) {
                int cv = compId[e.v];
                if (cu != cv) {
                    long key = ((long)cu << 32) | (cv & 0xffffffffL);
                    if (seen.add(key)) {
                        dag.addEdge(cu, cv, e.w); // cu -> cv (правильное направление)
                    }
                }
            }
        }
        return dag;
    }
}
