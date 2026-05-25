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
    public void testDijkstraShortestPathAF() {
        List<String> path = dijkstra.findShortestPath(graph, "A", "F");
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(Arrays.asList("A", "B", "D", "E", "F"), path);
    }

    @Test
    public void testDijkstraShortestPathAD() {
        List<String> path = dijkstra.findShortestPath(graph, "A", "D");
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(Arrays.asList("A", "B", "D"), path);
    }

    @Test
    public void testDijkstraSourceEqualsDestination() {
        List<String> path = dijkstra.findShortestPath(graph, "A", "A");
        assertNotNull(path);
        assertEquals(Collections.singletonList("A"), path);
    }

    @Test
    public void testDijkstraDisconnectedNode() {
        Map<String, Map<String, Integer>> disconnectedGraph = new LinkedHashMap<>();
        disconnectedGraph.put("A", new LinkedHashMap<>());
        disconnectedGraph.put("B", new LinkedHashMap<>());

        List<String> path = dijkstra.findShortestPath(disconnectedGraph, "A", "B");
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test
    public void testAStarShortestPathAF() {
        List<String> path = aStar.findShortestPath(graph, "A", "F");
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(Arrays.asList("A", "B", "D", "E", "F"), path);
    }

    @Test
    public void testAStarShortestPathAD() {
        List<String> path = aStar.findShortestPath(graph, "A", "D");
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(Arrays.asList("A", "B", "D"), path);
    }

    @Test
    public void testAStarSourceEqualsDestination() {
        List<String> path = aStar.findShortestPath(graph, "A", "A");
        assertNotNull(path);
        assertEquals(Collections.singletonList("A"), path);
    }

    @Test
    public void testBothAlgorithmsProduceSameResult() {
        List<String> dijkstraPath = dijkstra.findShortestPath(graph, "A", "F");
        List<String> aStarPath = aStar.findShortestPath(graph, "A", "F");
        assertEquals(dijkstraPath, aStarPath);
    }
}
