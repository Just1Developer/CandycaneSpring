package net.justonedev.candycane.lobbysession.world.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tarjan's SCC algorithm is for detecting cycles in directed graphs in O(V + E) time with O(V) space.
 */
public class TarjansSCC<T> {
    private final Map<T, Set<T>> graph;
    private final Map<T, Integer> indices = new HashMap<>();
    private final Map<T, Integer> lowlink = new HashMap<>();
    private final Deque<T> stack = new ArrayDeque<>();
    private final Set<T> onStack = new HashSet<>();
    private final Set<T> nodesInCycles = new HashSet<>();
    private int index = 0;

    public TarjansSCC(Map<T, Set<T>> graph) {
        this.graph = graph;
    }

    public Set<T> getNodesInCycles() {
        for (T node : graph.keySet()) {
            if (!indices.containsKey(node)) {
                strongConnect(node);
            }
        }
        return nodesInCycles;
    }

    private void strongConnect(T node) {
        indices.put(node, index);
        lowlink.put(node, index);
        index++;
        stack.push(node);
        onStack.add(node);

        for (T neighbor : graph.getOrDefault(node, Set.of())) {
            if (!indices.containsKey(neighbor)) {
                strongConnect(neighbor);
                lowlink.put(node, Math.min(lowlink.get(node), lowlink.get(neighbor)));
            } else if (onStack.contains(neighbor)) {
                lowlink.put(node, Math.min(lowlink.get(node), indices.get(neighbor)));
            }
        }

        if (lowlink.get(node).equals(indices.get(node))) {
            List<T> scc = new ArrayList<>();
            T w;
            do {
                w = stack.pop();
                onStack.remove(w);
                scc.add(w);
            } while (!w.equals(node));

            // Nodes in SCC of size > 1 or self-loop are part of a cycle
            if (scc.size() > 1 || graph.getOrDefault(node, Set.of()).contains(node)) {
                nodesInCycles.addAll(scc);
            }
        }
    }
}
