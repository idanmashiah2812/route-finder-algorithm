package com.hit.algorithm;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class IAlgoShortestPathTest {

    private Map<String, Map<String, Integer>> graph;
    private Map<String, Integer> heuristicMap;
    private IAlgoShortestPath dijkstra;
    private IAlgoShortestPath aStar;

    @Before
    public void setUp() {
        graph = new LinkedHashMap<>();

        Map<String, Integer> aEdges = new LinkedHashMap<>();
        aEdges.put("B", 4);
        aEdges.put("C", 2);
        graph.put("A", aEdges);

        Map<String, Integer> bEdges = new LinkedHashMap<>();
        bEdges.put("C", 1);
        bEdges.put("D", 5);
        graph.put("B", bEdges);

        Map<String, Integer> cEdges = new LinkedHashMap<>();
        cEdges.put("B", 1);
        cEdges.put("D", 8);
        cEdges.put("E", 10);
        graph.put("C", cEdges);

        Map<String, Integer> dEdges = new LinkedHashMap<>();
        dEdges.put("E", 2);
        dEdges.put("F", 6);
        graph.put("D", dEdges);

        Map<String, Integer> eEdges = new LinkedHashMap<>();
        eEdges.put("F", 3);
        graph.put("E", eEdges);

        graph.put("F", new LinkedHashMap<>());

        heuristicMap = new LinkedHashMap<>();
        heuristicMap.put("A", 7);
        heuristicMap.put("B", 6);
        heuristicMap.put("C", 5);
        heuristicMap.put("D", 3);
        heuristicMap.put("E", 2);
        heuristicMap.put("F", 0);

        dijkstra = new DijkstraAlgoImpl();
        aStar = new AStarAlgoImpl(heuristicMap);
    }

    @Test
    public void testHappyPath() {
        List<String> dijkstraPath = dijkstra.findShortestPath(graph, "A", "F");
        assertEquals(Arrays.asList("A", "C", "B", "D", "E", "F"), dijkstraPath);

        List<String> aStarPath = aStar.findShortestPath(graph, "A", "F");
        assertEquals(Arrays.asList("A", "C", "B", "D", "E", "F"), aStarPath);
    }

    @Test
    public void testNoPathAvailable() {
        List<String> dijkstraPath = dijkstra.findShortestPath(emptyGraph("X", "Y"), "X", "Y");
        assertTrue(dijkstraPath.isEmpty());

        List<String> aStarPath = aStar.findShortestPath(emptyGraph("X", "Y"), "X", "Y");
        assertTrue(aStarPath.isEmpty());
    }

    @Test
    public void testSameSourceAndDestination() {
        List<String> dijkstraPath = dijkstra.findShortestPath(graph, "A", "A");
        assertEquals(Collections.singletonList("A"), dijkstraPath);

        List<String> aStarPath = aStar.findShortestPath(graph, "A", "A");
        assertEquals(Collections.singletonList("A"), aStarPath);
    }

    @Test
    public void testHeuristicImpactOnAStar() {
        Map<String, Map<String, Integer>> branchingGraph = new LinkedHashMap<>();

        Map<String, Integer> sEdges = new LinkedHashMap<>();
        sEdges.put("A", 1);
        sEdges.put("B", 5);
        branchingGraph.put("S", sEdges);

        Map<String, Integer> aEdges = new LinkedHashMap<>();
        aEdges.put("T", 10);
        branchingGraph.put("A", aEdges);

        Map<String, Integer> bEdges = new LinkedHashMap<>();
        bEdges.put("T", 2);
        branchingGraph.put("B", bEdges);

        branchingGraph.put("T", new LinkedHashMap<>());

        Map<String, Integer> misleadingHeuristic = new LinkedHashMap<>();
        misleadingHeuristic.put("S", 10);
        misleadingHeuristic.put("A", 100);
        misleadingHeuristic.put("B", 1);
        misleadingHeuristic.put("T", 0);

        IAlgoShortestPath aStarMisleading = new AStarAlgoImpl(misleadingHeuristic);

        List<String> dijkstraPath = dijkstra.findShortestPath(branchingGraph, "S", "T");
        List<String> aStarPath = aStarMisleading.findShortestPath(branchingGraph, "S", "T");

        assertEquals(Arrays.asList("S", "B", "T"), dijkstraPath);
        assertEquals(Arrays.asList("S", "B", "T"), aStarPath);
    }

    private static Map<String, Map<String, Integer>> emptyGraph(String nodeA, String nodeB) {
        Map<String, Map<String, Integer>> g = new LinkedHashMap<>();
        g.put(nodeA, new LinkedHashMap<>());
        g.put(nodeB, new LinkedHashMap<>());
        return g;
    }
}
