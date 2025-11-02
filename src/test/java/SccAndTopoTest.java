import model.*;
import graph.scc.*;
import graph.topo.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class SccAndTopoTest {
    @Test void simpleDag() {
        Graph g = new Graph(4, true);
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(2,3,1);

        Metrics m = new Metrics();
        TarjanSCC.Result r = new TarjanSCC(g, m).run();
        assertEquals(4, r.components);

         int[] stable = SccUtils.relabelByMinVertex(r.compId, r.components);

        Graph dag = CondensationBuilder.build(g, r.components, stable);
        var topo = TopoKahn.order(dag, new Metrics());

        assertEquals(List.of(0,1,2,3), topo); // теперь пройдёт
    }

    @Test void oneCycle() {
        Graph g = new Graph(3, true);
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(2,0,1);
        TarjanSCC.Result r = new TarjanSCC(g, new Metrics()).run();
        assertEquals(1, r.components);
    }
}
