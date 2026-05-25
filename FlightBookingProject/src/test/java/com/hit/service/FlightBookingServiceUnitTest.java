package com.hit.service;

import com.hit.algorithm.IAlgoShortestPath;
import com.hit.dao.IDao;
import com.hit.dm.FlightTicket;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FlightBookingServiceUnitTest {

    @Mock
    private IDao<FlightTicket> dao;

    @Mock
    private IAlgoShortestPath shortestPathAlgo;

    @InjectMocks
    private FlightBookingService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBookTicketDelegation() {
        FlightTicket ticket = new FlightTicket("FT999", "Mock Customer", "CityA", "CityB", 100);
        service.bookFlight(ticket);
        verify(dao, times(1)).save(ticket);
    }

    @Test
    public void testCalculateRouteDelegation() {
        Map<String, Map<String, Integer>> graph = new LinkedHashMap<>();
        List<String> expectedPath = Arrays.asList("A", "B");

        when(shortestPathAlgo.findShortestPath(graph, "A", "B")).thenReturn(expectedPath);

        List<String> result = service.getOptimalRoute(graph, "A", "B");

        assertEquals(expectedPath, result);
        verify(shortestPathAlgo, times(1)).findShortestPath(graph, "A", "B");
    }
}
