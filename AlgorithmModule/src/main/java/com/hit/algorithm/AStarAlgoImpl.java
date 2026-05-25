package com.hit.algorithm;

import java.util.*;

public class AStarAlgoImpl implements IAlgoShortestPath {

    private final Map<String, Integer> heuristicMap;

    public AStarAlgoImpl(Map<String, Integer> heuristicMap) {
        this.heuristicMap = heuristicMap;
    }

    @Override
    public List<String> findShortestPath(Map<String, Map<String, Integer>> graph, String source, String destination) {
        Map<String, Integer> gScore = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String vertex : graph.keySet()) {
            gScore.put(vertex, Integer.MAX_VALUE);
        }
        gScore.put(source, 0);

        PriorityQueue<String> openSet = new PriorityQueue<>(
                Comparator.comparingInt(v -> gScore.get(v) + heuristicMap.getOrDefault(v, 0))
        );
        openSet.add(source);

        while (!openSet.isEmpty()) {
            String current = openSet.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            if (current.equals(destination)) break;

            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors == null) continue;

            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String next = neighbor.getKey();
                int weight = neighbor.getValue();
                int tentativeG = gScore.get(current) + weight;

                if (tentativeG < gScore.get(next)) {
                    gScore.put(next, tentativeG);
                    predecessors.put(next, current);
                    openSet.add(next);
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
