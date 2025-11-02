package graph.scc;

import java.util.*;

public class SccUtils {
    public static int[] relabelByMinVertex(int[] compId, int compCount) {
        int n = compId.length;
        int[] minV = new int[compCount];
        Arrays.fill(minV, Integer.MAX_VALUE);
        for (int v = 0; v < n; v++) {
            int c = compId[v];
            if (v < minV[c]) minV[c] = v;
        }
        Integer[] comps = new Integer[compCount];
        for (int i = 0; i < compCount; i++) comps[i] = i;
        Arrays.sort(comps, java.util.Comparator.comparingInt(c -> minV[c]));

        int[] map = new int[compCount];
        for (int newId = 0; newId < compCount; newId++) map[comps[newId]] = newId;

        int[] relabeled = new int[n];
        for (int v = 0; v < n; v++) relabeled[v] = map[compId[v]];
        return relabeled;
    }
}
