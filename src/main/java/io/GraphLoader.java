package io;

import model.Edge;
import model.Graph;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;

public class GraphLoader {
    public static class JsonEdge { public int u, v, w; }
    public static class JsonGraph {
        public boolean directed;
        public int n;
        public List<JsonEdge> edges;
        public Integer source;
        public String weight_model;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<JsonGraph> loadAny(String path) throws IOException {
        String raw = Files.readString(Path.of(path));
        raw = raw.trim();
        if (raw.startsWith("[")) {
            return MAPPER.readValue(raw, new TypeReference<List<JsonGraph>>() {});
        } else {
            JsonGraph g = MAPPER.readValue(raw, JsonGraph.class);
            return List.of(g);
        }
    }

    public static Graph toGraph(JsonGraph jg) {
        Graph g = new Graph(jg.n, jg.directed);
        for (JsonEdge e : jg.edges) g.addEdge(e.u, e.v, e.w);
        return g;
    }
}
