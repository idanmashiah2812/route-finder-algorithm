package com.hit.service;

import com.hit.algorithm.DijkstraAlgoImpl;
import com.hit.algorithm.IAlgoShortestPath;
import com.hit.dao.DaoFileImpl;
import com.hit.dao.IDao;
import com.hit.dm.FlightTicket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class FlightBookingSystemE2ETest {

    private static final String TEST_FILE = "src/test/resources/e2e_datasource.txt";
    private FlightBookingService service;

    @Before
    public void setUp() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));

        IDao<FlightTicket> dao = new DaoFileImpl<>(TEST_FILE);
        IAlgoShortestPath algo = new DijkstraAlgoImpl();
        service = new FlightBookingService(dao, algo);
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    @Test
    public void testCompleteCustomerJourney() {
        Map<String, Map<String, Integer>> graph = buildFlightRouteGraph();

        List<String> optimalRoute = service.getOptimalRoute(graph, "Paris", "Milan");
        assertNotNull(optimalRoute);
        assertFalse(optimalRoute.isEmpty());
        assertEquals(Arrays.asList("Paris", "Lyon", "Geneva", "Milan"), optimalRoute);

        FlightTicket ticket = new FlightTicket("E2E001", "E2E Customer",
                "Paris", "Milan", 920);
        service.bookFlight(ticket);

        FlightTicket retrieved = service.getTicket("E2E001");
        assertNotNull(retrieved);
        assertEquals("E2E001", retrieved.getId());
        assertEquals("E2E Customer", retrieved.getCustomerName());
        assertEquals("Paris", retrieved.getSource());
        assertEquals("Milan", retrieved.getDestination());
        assertEquals(920, retrieved.getPrice());

        service.cancelFlight("E2E001");
        assertNull(service.getTicket("E2E001"));
    }

    private static Map<String, Map<String, Integer>> buildFlightRouteGraph() {
        Map<String, Map<String, Integer>> graph = new LinkedHashMap<>();

        graph.put("Paris", singleEntry("Lyon", 100));
        graph.put("Lyon", singleEntry("Geneva", 80));
        graph.put("Geneva", singleEntry("Milan", 120));
        Map<String, Integer> milanEdges = new LinkedHashMap<>();
        milanEdges.put("Rome", 200);
        milanEdges.put("Naples", 250);
        graph.put("Milan", milanEdges);
        graph.put("Rome", new LinkedHashMap<>());
        graph.put("Naples", new LinkedHashMap<>());

        return graph;
    }

    private static Map<String, Integer> singleEntry(String key, int value) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }
}
