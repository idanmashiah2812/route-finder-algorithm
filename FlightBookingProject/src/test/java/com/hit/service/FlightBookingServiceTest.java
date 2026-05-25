package com.hit.service;

import com.hit.algorithm.DijkstraAlgoImpl;
import com.hit.algorithm.IAlgoShortestPath;
import com.hit.dao.DaoFileImpl;
import com.hit.dm.FlightTicket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class FlightBookingServiceTest {

    private FlightBookingService service;
    private DaoFileImpl<FlightTicket> dao;
    private IAlgoShortestPath shortestPathAlgo;
    private static final String TEST_DATA_FILE = "src/main/resources/datasource.txt";

    @Before
    public void setUp() throws Exception {
        // Clean up test file before each test
        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(TEST_DATA_FILE));
        
        // Initialize DAO with test file
        dao = new DaoFileImpl<>(TEST_DATA_FILE);
        
        // Initialize algorithm (would be imported from AlgorithmModule.jar in real scenario)
        shortestPathAlgo = new DijkstraAlgoImpl();
        
        // Wire dependencies into service
        service = new FlightBookingService(dao, shortestPathAlgo);
    }

    @After
    public void tearDown() throws Exception {
        // Clean up test file after each test
        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(TEST_DATA_FILE));
    }

    @Test
    public void testBookAndRetrieveFlight() {
        // Create a flight ticket
        FlightTicket ticket = new FlightTicket("FT001", "John Doe", "Paris", "Geneva", 150);
        
        // Book the flight
        service.bookFlight(ticket);
        
        // Retrieve the flight
        FlightTicket retrieved = service.getTicket("FT001");
        
        // Verify
        assertNotNull(retrieved);
        assertEquals("FT001", retrieved.getId());
        assertEquals("John Doe", retrieved.getCustomerName());
        assertEquals("Paris", retrieved.getSource());
        assertEquals("Geneva", retrieved.getDestination());
        assertEquals(150, retrieved.getPrice());
    }

    @Test
    public void testCancelFlight() {
        // Create and book a flight
        FlightTicket ticket = new FlightTicket("FT002", "Jane Smith", "London", "Berlin", 200);
        service.bookFlight(ticket);
        
        // Verify it exists
        assertNotNull(service.getTicket("FT002"));
        
        // Cancel the flight
        service.cancelFlight("FT002");
        
        // Verify it's gone
        assertNull(service.getTicket("FT002"));
    }

    @Test
    public void testOptimalRouteCalculation() {
        // Create a mock graph of flight routes
        Map<String, Map<String, Integer>> graph = new LinkedHashMap<>();
        
        // Paris -> Lyon (70), Lyon -> Geneva (50), Paris -> Geneva (150)
        Map<String, Integer> parisEdges = new LinkedHashMap<>();
        parisEdges.put("Lyon", 70);
        parisEdges.put("Geneva", 150);
        graph.put("Paris", parisEdges);
        
        Map<String, Integer> lyonEdges = new LinkedHashMap<>();
        lyonEdges.put("Paris", 70);
        lyonEdges.put("Geneva", 50);
        graph.put("Lyon", lyonEdges);
        
        Map<String, Integer> genevaEdges = new LinkedHashMap<>();
        genevaEdges.put("Paris", 150);
        genevaEdges.put("Lyon", 50);
        graph.put("Geneva", genevaEdges);
        
        // Test optimal route from Paris to Geneva
        // Direct: 150
        // Via Lyon: 70 + 50 = 120 (should be chosen)
        List<String> optimalRoute = service.getOptimalRoute(graph, "Paris", "Geneva");
        
        assertNotNull(optimalRoute);
        assertFalse(optimalRoute.isEmpty());
        assertEquals(Arrays.asList("Paris", "Lyon", "Geneva"), optimalRoute);
    }

    @Test
    public void testOptimalRouteSameSourceDestination() {
        Map<String, Map<String, Integer>> graph = new LinkedHashMap<>();
        Map<String, Integer> parisEdges = new LinkedHashMap<>();
        parisEdges.put("Lyon", 70);
        graph.put("Paris", parisEdges);
        
        Map<String, Integer> lyonEdges = new LinkedHashMap<>();
        lyonEdges.put("Paris", 70);
        graph.put("Lyon", lyonEdges);
        
        // Test route from Paris to Paris
        List<String> optimalRoute = service.getOptimalRoute(graph, "Paris", "Paris");
        
        assertNotNull(optimalRoute);
        assertEquals(Collections.singletonList("Paris"), optimalRoute);
    }

    @Test
    public void testOptimalRouteNoPath() {
        Map<String, Map<String, Integer>> graph = new LinkedHashMap<>();
        Map<String, Integer> parisEdges = new LinkedHashMap<>();
        parisEdges.put("Lyon", 70);
        graph.put("Paris", parisEdges);
        
        Map<String, Integer> lyonEdges = new LinkedHashMap<>();
        lyonEdges.put("Paris", 70);
        graph.put("Lyon", lyonEdges);
        
        // Geneva is not connected to Paris/Lyon
        Map<String, Integer> genevaEdges = new LinkedHashMap<>();
        genevaEdges.put("Bern", 30);
        graph.put("Geneva", genevaEdges);
        
        // Test route from Paris to Geneva (no path)
        List<String> optimalRoute = service.getOptimalRoute(graph, "Paris", "Geneva");
        
        assertNotNull(optimalRoute);
        assertTrue(optimalRoute.isEmpty()); // Empty list indicates no path
    }
}
