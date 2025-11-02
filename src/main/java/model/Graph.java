package model;

import java.util.*;

public class Graph {
    public final int n;
    public final boolean directed;
    public final java.util.List<java.util.List<Edge>> adj;

    public Graph(int n, boolean directed) {
        this.n = n; this.directed = directed;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }
    public void addEdge(int u, int v, int w) {
        adj.get(u).add(new Edge(u, v, w));
        if (!directed) adj.get(v).add(new Edge(v, u, w));
    }
}
