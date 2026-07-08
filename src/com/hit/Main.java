package com.hit;

import com.hit.algorithm.*;
import com.hit.dao.*;
import com.hit.dm.*;
import com.hit.service.*;

import java.util.*;



public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("  Flight Booking System - Demo");
        System.out.println("========================================\n");

        demoAlgorithms();
        demoDaoLayer();
        demoFlightBookingService();

        System.out.println("\n========================================");
        System.out.println("  ALL DEMOS PASSED SUCCESSFULLY");
        System.out.println("========================================");
    }

    // ==================== PART A: Algorithm Module ====================

    private static void demoAlgorithms() {
        System.out.println("--- Algorithm Module Demo ---\n");

        Map<String, Map<String, Integer>> graph = buildSampleGraph();

        IAlgoShortestPath dijkstra = new DijkstraAlgoImpl();

        Map<String, Integer> heuristic = new LinkedHashMap<>();
        heuristic.put("A", 7);
        heuristic.put("B", 6);
        heuristic.put("C", 5);
        heuristic.put("D", 3);
        heuristic.put("E", 2);
        heuristic.put("F", 0);
        IAlgoShortestPath aStar = new AStarAlgoImpl(heuristic);

        System.out.println("Graph: A->B(4) A->C(2), B->C(1) B->D(5), C->B(1) C->D(8) C->E(10)");
        System.out.println("       D->E(2) D->F(6), E->F(3)\n");

        System.out.println("1. Dijkstra: A -> F");
        List<String> path = dijkstra.findShortestPath(graph, "A", "F");
        System.out.println("   Path: " + path + " (expected: [A, C, B, D, E, F])\n");

        System.out.println("2. A*: A -> F");
        path = aStar.findShortestPath(graph, "A", "F");
        System.out.println("   Path: " + path + " (expected: [A, C, B, D, E, F])\n");

        System.out.println("3. Dijkstra: A -> A (same node)");
        path = dijkstra.findShortestPath(graph, "A", "A");
        System.out.println("   Path: " + path + " (expected: [A])\n");

        System.out.println("4. Dijkstra: X -> Y (no path)");
        Map<String, Map<String, Integer>> empty = new LinkedHashMap<>();
        empty.put("X", new LinkedHashMap<>());
        empty.put("Y", new LinkedHashMap<>());
        path = dijkstra.findShortestPath(empty, "X", "Y");
        System.out.println("   Path: " + path + " (expected: [])\n");
    }

    // ==================== PART B: DAO Layer ====================

    private static void demoDaoLayer() throws Exception {
        System.out.println("--- DAO Layer Demo ---\n");

        String testFile = "demo_datasource.txt";

        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(testFile));

        IDao<FlightTicket> dao = new DaoFileImpl<>(testFile);

        FlightTicket ticket = new FlightTicket("DEMO001", "John Doe",
                "Paris", "Rome", 350);

        System.out.println("1. Saving ticket: " + ticket);
        dao.save(ticket);

        FlightTicket found = dao.find("DEMO001");
        System.out.println("2. Found ticket:   " + found);
        assert found != null : "Ticket should not be null";

        System.out.println("3. Deleting ticket...");
        dao.delete("DEMO001");
        FlightTicket afterDelete = dao.find("DEMO001");
        System.out.println("   After delete: " + afterDelete);
        assert afterDelete == null : "Ticket should be null after delete";

        System.out.println("4. Persistence across instances...");
        dao.save(ticket);
        IDao<FlightTicket> secondDao = new DaoFileImpl<>(testFile);
        FlightTicket reloaded = secondDao.find("DEMO001");
        System.out.println("   Reloaded: " + reloaded);
        assert reloaded != null : "Reloaded ticket should not be null";
        assert ticket.equals(reloaded) : "Tickets should match";

        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(testFile));
        System.out.println();
    }

    // ==================== PART B: Service Layer (Full E2E) ====================

    private static void demoFlightBookingService() throws Exception {
        System.out.println("--- Flight Booking Service Demo ---\n");

        String testFile = "demo_service.txt";
        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(testFile));

        IDao<FlightTicket> dao = new DaoFileImpl<>(testFile);
        IAlgoShortestPath algo = new DijkstraAlgoImpl();
        FlightBookingService service = new FlightBookingService(dao, algo);

        Map<String, Map<String, Integer>> graph = new LinkedHashMap<>();
        graph.put("Paris", singleEntry("Lyon", 100));
        graph.put("Lyon", singleEntry("Geneva", 80));
        graph.put("Geneva", singleEntry("Milan", 120));
        graph.put("Milan", singleEntry("Rome", 200));
        graph.put("Rome", new LinkedHashMap<>());

        System.out.println("1. Calculating optimal route: Paris -> Milan");
        List<String> route = service.getOptimalRoute(graph, "Paris", "Milan");
        System.out.println("   Optimal route: " + route);
        assert route.equals(Arrays.asList("Paris", "Lyon", "Geneva", "Milan")) : "Wrong route";

        FlightTicket booking = new FlightTicket("DEMO002", "Jane Smith",
                "Paris", "Milan", 920);

        System.out.println("\n2. Booking ticket: " + booking);
        service.bookFlight(booking);

        FlightTicket retrieved = service.getTicket("DEMO002");
        System.out.println("   Retrieved:      " + retrieved);
        assert retrieved != null : "Retrieved ticket should not be null";
        assert booking.equals(retrieved) : "Tickets should match";

        System.out.println("\n3. Cancelling ticket...");
        service.cancelFlight("DEMO002");
        FlightTicket afterCancel = service.getTicket("DEMO002");
        System.out.println("   After cancel: " + afterCancel);
        assert afterCancel == null : "Ticket should be null after cancel";

        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(testFile));
        System.out.println("\n   Service demo completed successfully!\n");
    }

    // ==================== Helpers ====================

    private static Map<String, Map<String, Integer>> buildSampleGraph() {
        Map<String, Map<String, Integer>> graph = new LinkedHashMap<>();

        graph.put("A", edges(entry("B", 4), entry("C", 2)));
        graph.put("B", edges(entry("C", 1), entry("D", 5)));
        graph.put("C", edges(entry("B", 1), entry("D", 8), entry("E", 10)));
        graph.put("D", edges(entry("E", 2), entry("F", 6)));
        graph.put("E", edges(entry("F", 3)));
        graph.put("F", new LinkedHashMap<>());

        return graph;
    }

    @SafeVarargs
    private static Map<String, Integer> edges(Map.Entry<String, Integer>... entries) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entries) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }

    private static Map.Entry<String, Integer> entry(String key, int value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private static Map<String, Integer> singleEntry(String key, int value) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }
}
