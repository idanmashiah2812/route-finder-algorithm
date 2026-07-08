package com.hit.service;

import com.hit.algorithm.IAlgoShortestPath;
import com.hit.dao.IDao;
import com.hit.dm.FlightTicket;

import java.util.List;
import java.util.Map;

public class FlightBookingService {

    private final IDao<FlightTicket> dao;
    private final IAlgoShortestPath shortestPathAlgo;

    public FlightBookingService(IDao<FlightTicket> dao, IAlgoShortestPath shortestPathAlgo) {
        this.dao = dao;
        this.shortestPathAlgo = shortestPathAlgo;
    }

    public void bookFlight(FlightTicket ticket) {
        dao.save(ticket);
    }

    public FlightTicket getTicket(String id) {
        return dao.find(id);
    }

    public void cancelFlight(String id) {
        dao.delete(id);
    }

    public List<String> getOptimalRoute(Map<String, Map<String, Integer>> graph, String from, String to) {
        return shortestPathAlgo.findShortestPath(graph, from, to);
    }

    public List<FlightTicket> getAllTickets() {
        return dao.findAll();
    }
}
