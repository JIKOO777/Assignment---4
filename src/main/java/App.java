// src/main/java/App.java
import io.GraphLoader;
import io.GraphLoader.JsonGraph;
import model.*;
import graph.scc.*;
import graph.topo.*;
import graph.dagsp.*;
import util.CsvWriter;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class App {

    private static List<String[]> row(String... cols) {
        return Collections.singletonList(cols);
    }

    private static int edgeCount(Graph g) {
        int c = 0;
        for (var lst : g.adj) c += lst.size();
        return g.directed ? c : c / 2;
    }

    private static int sourceOfDAG(Graph dag) {
        int n = dag.n;
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (var e : dag.adj.get(u)) indeg[e.v]++;
        for (int i = 0; i < n; i++) if (indeg[i] == 0) return i;
        return 0;
    }

    static void ensureHeaders(Path outDir) throws Exception {
        CsvWriter.writeWithHeader(outDir.resolve("data_summary.csv"),
                new String[]{"dataset","index","n","m","directed","weight_model"}, Collections.emptyList());
        CsvWriter.writeWithHeader(outDir.resolve("scc.csv"),
                new String[]{"dataset","index","n","m","dfs_visits","dfs_edges","time_ms","components"}, Collections.emptyList());
        CsvWriter.writeWithHeader(outDir.resolve("topo.csv"),
                new String[]{"dataset","index","n","m","pushes","pops","time_ms","topo_len"}, Collections.emptyList());
        CsvWriter.writeWithHeader(outDir.resolve("dag_shortest.csv"),
                new String[]{"dataset","index","n","m","source","relaxations","time_ms","target","dist_target"}, Collections.emptyList());
        CsvWriter.writeWithHeader(outDir.resolve("dag_longest.csv"),
                new String[]{"dataset","index","n","m","time_ms","best_length","path_size"}, Collections.emptyList());
        CsvWriter.writeWithHeader(outDir.resolve("errors.csv"),
                new String[]{"dataset","index","stage","message"}, Collections.emptyList());
    }

    private static List<String> discoverJsonArgs(String[] args) throws IOException {
        if (args != null && args.length > 0) return Arrays.asList(args);
        // Автопоиск: ./data/*.json
        Path data = Paths.get("data");
        if (Files.isDirectory(data)) {
            try (var s = Files.list(data)) {
                List<String> found = s
                        .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                        .map(p -> p.toString())
                        .sorted()
                        .collect(Collectors.toList());
                if (!found.isEmpty()) {
                    System.out.println("No args passed. Auto-discovered JSON files: " + found);
                    return found;
                }
            }
        }
        return List.of();
    }

    public static void main(String[] args) throws Exception {
        Path outDir = Paths.get("out");
        ensureHeaders(outDir);

        List<String> datasets = discoverJsonArgs(args);
        if (datasets.isEmpty()) {
            System.out.println("""
                No JSON files provided/found.
                Usage:
                  java -cp target/graph-assignment4-1.0.0.jar App data/small.json [data/medium.json ...]
                """);
            CsvWriter.append(outDir.resolve("errors.csv"),
                    row("-", "-", "startup", "No JSON args and no ./data/*.json found"));
            return;
        }

        for (String datasetPath : datasets) {
            final String fileName = Paths.get(datasetPath).getFileName().toString();
            try {
                List<JsonGraph> J = GraphLoader.loadAny(datasetPath);
                System.out.println("Loaded " + J.size() + " graphs from " + datasetPath);

                if (J.isEmpty()) {
                    CsvWriter.append(outDir.resolve("errors.csv"),
                            row(fileName, "-", "load", "No graphs parsed from JSON (empty or unexpected format)"));
                    continue;
                }

                for (int idx = 0; idx < J.size(); idx++) {
                    try {
                        JsonGraph jg = J.get(idx);
                        Graph g = GraphLoader.toGraph(jg);
                        int n = g.n;
                        int m = edgeCount(g);
                        String weightModel = (jg.weight_model == null ? "edge" : jg.weight_model);

                        // Data summary
                        CsvWriter.append(outDir.resolve("data_summary.csv"),
                                row(fileName, String.valueOf(idx), String.valueOf(n), String.valueOf(m),
                                        String.valueOf(g.directed), weightModel));

                        // SCC
                        Metrics mScc = new Metrics();
                        TarjanSCC.Result sRes;
                        try (Stopwatch ignored = new Stopwatch(mScc)) {
                            sRes = new TarjanSCC(g, mScc).run();
                        }

                        int[] stableCompId = SccUtils.relabelByMinVertex(sRes.compId, sRes.components);
                        CsvWriter.append(outDir.resolve("scc.csv"),
                                row(fileName, String.valueOf(idx), String.valueOf(n), String.valueOf(m),
                                        String.valueOf(mScc.dfsVisits), String.valueOf(mScc.dfsEdges),
                                        String.format(Locale.US, "%.3f", mScc.nanos / 1e6),
                                        String.valueOf(sRes.components)));

                        // Condensed DAG & Topo
                        Graph dag = CondensationBuilder.build(g, sRes.components, stableCompId);
                        Metrics mTopo = new Metrics();
                        List<Integer> topo;
                        try (Stopwatch ignored = new Stopwatch(mTopo)) {
                            topo = TopoKahn.order(dag, mTopo);
                        }
                        CsvWriter.append(outDir.resolve("topo.csv"),
                                row(fileName, String.valueOf(idx), String.valueOf(dag.n),
                                        String.valueOf(edgeCount(dag)),
                                        String.valueOf(mTopo.queuePushes), String.valueOf(mTopo.queuePops),
                                        String.format(Locale.US, "%.3f", mTopo.nanos / 1e6),
                                        String.valueOf(topo.size())));

                        // DAG Shortest Path
                        int source = sourceOfDAG(dag);
                        Metrics mSp = new Metrics();
                        DagShortestPaths.Result spRes;
                        try (Stopwatch ignored = new Stopwatch(mSp)) {
                            spRes = DagShortestPaths.run(dag, topo, source, mSp);
                        }
                        int target = topo.get(topo.size() - 1);
                        long distTarget = spRes.dist[target];
                        CsvWriter.append(outDir.resolve("dag_shortest.csv"),
                                row(fileName, String.valueOf(idx), String.valueOf(dag.n),
                                        String.valueOf(edgeCount(dag)), String.valueOf(source),
                                        String.valueOf(mSp.relaxations),
                                        String.format(Locale.US, "%.3f", mSp.nanos / 1e6),
                                        String.valueOf(target), String.valueOf(distTarget)));

                        // DAG Longest Path
                        long start = System.nanoTime();
                        DagLongestPath.Result lp = DagLongestPath.run(dag, topo);
                        long end = System.nanoTime();
                        double lpMs = (end - start) / 1e6;
                        long bestLen = (lp.bestV < 0) ? Long.MIN_VALUE : lp.val[lp.bestV];
                        int pathSize = lp.reconstruct().size();
                        CsvWriter.append(outDir.resolve("dag_longest.csv"),
                                row(fileName, String.valueOf(idx), String.valueOf(dag.n),
                                        String.valueOf(edgeCount(dag)),
                                        String.format(Locale.US, "%.3f", lpMs),
                                        (lp.bestV < 0 ? "" : String.valueOf(bestLen)),
                                        String.valueOf(pathSize)));

                        System.out.printf(Locale.US,
                                "[%s#%d] n=%d m=%d | SCC=%d (%.3f ms) | Topo=%d (%.3f ms) | SP relax=%d (%.3f ms) | LP len=%s (%.3f ms)%n",
                                fileName, idx, n, m,
                                sRes.components, mScc.nanos / 1e6,
                                topo.size(), mTopo.nanos / 1e6,
                                mSp.relaxations, mSp.nanos / 1e6,
                                (lp.bestV < 0 ? "NA" : String.valueOf(bestLen)), lpMs
                        );

                    } catch (Exception perGraphEx) {
                        perGraphEx.printStackTrace();
                        CsvWriter.append(outDir.resolve("errors.csv"),
                                row(fileName, String.valueOf(idx), "graph", perGraphEx.toString()));
                    }
                }
            } catch (Exception datasetEx) {
                datasetEx.printStackTrace();
                CsvWriter.append(outDir.resolve("errors.csv"),
                        row(fileName, "-", "dataset", datasetEx.toString()));
            }
        }

        System.out.println("CSV готово: папка ./out");
    }
}
