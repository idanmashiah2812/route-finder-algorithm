package com.hit.algorithm;

import java.util.*;

public class DijkstraAlgoImpl implements IAlgoShortestPath {

    @Override
    public List<String> findShortestPath(Map<String, Map<String, Integer>> graph, String source, String destination) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String vertex : graph.keySet()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(source, 0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        pq.add(source);

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            if (current.equals(destination)) break;

            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors == null) continue;

            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String next = neighbor.getKey();
                int weight = neighbor.getValue();
                int newDist = distances.get(current) + weight;

                if (newDist < distances.get(next)) {
                    distances.put(next, newDist);
                    predecessors.put(next, current);
                    pq.add(next);
                }
            }
        }

        return buildPath(predecessors, destination, source);
    }

    private List<String> buildPath(Map<String, String> predecessors, String destination, String source) {
        List<String> path = new LinkedList<>();
        String current = destination;

        while (current != null) {
            path.add(0, current);
            current = predecessors.get(current);
        }

        if (path.size() == 1 && !path.get(0).equals(source)) {
            return Collections.emptyList();
        }

        return path;
    }
}
