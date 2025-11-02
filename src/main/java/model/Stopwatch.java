 package model;

public class Stopwatch implements AutoCloseable {
    private final Metrics m;
    private final long start;
    public Stopwatch(Metrics m) {
        this.m = m; this.start = System.nanoTime();
    }
    @Override public void close() {
        m.nanos += (System.nanoTime() - start);
    }
}
