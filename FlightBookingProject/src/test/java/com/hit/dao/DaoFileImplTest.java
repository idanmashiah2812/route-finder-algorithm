package com.hit.dao;

import com.hit.dm.FlightTicket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DaoFileImplTest {

    private static final String TEST_FILE = "src/test/resources/test_datasource.txt";
    private DaoFileImpl<FlightTicket> dao;

    @Before
    public void setUp() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
        dao = new DaoFileImpl<>(TEST_FILE);
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    @Test
    public void testSaveAndFind() {
        FlightTicket ticket = new FlightTicket("FT100", "Alice Smith", "London", "Rome", 320);
        dao.save(ticket);

        FlightTicket retrieved = dao.find("FT100");
        assertNotNull(retrieved);
        assertEquals("FT100", retrieved.getId());
        assertEquals("Alice Smith", retrieved.getCustomerName());
        assertEquals("London", retrieved.getSource());
        assertEquals("Rome", retrieved.getDestination());
        assertEquals(320, retrieved.getPrice());
    }

    @Test
    public void testDelete() {
        FlightTicket ticket = new FlightTicket("FT200", "Bob Jones", "Berlin", "Madrid", 210);
        dao.save(ticket);
        assertNotNull(dao.find("FT200"));

        dao.delete("FT200");
        assertNull(dao.find("FT200"));
    }

    @Test
    public void testPersistenceAcrossInstances() {
        FlightTicket ticket = new FlightTicket("FT300", "Carol White", "Dublin", "Vienna", 180);
        dao.save(ticket);

        DaoFileImpl<FlightTicket> secondDao = new DaoFileImpl<>(TEST_FILE);
        FlightTicket retrieved = secondDao.find("FT300");
        assertNotNull(retrieved);
        assertEquals("FT300", retrieved.getId());
        assertEquals("Carol White", retrieved.getCustomerName());
        assertEquals("Dublin", retrieved.getSource());
        assertEquals("Vienna", retrieved.getDestination());
        assertEquals(180, retrieved.getPrice());
    }
}
