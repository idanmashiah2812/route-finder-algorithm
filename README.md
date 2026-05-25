# Route Finder Algorithm — Flight Booking System

**Students:** Alon Iozef and Idan Mashiah

A multi-module Java project implementing graph pathfinding algorithms and an N-Tier flight booking system using the Strategy Pattern and SOLID principles.

## Project Structure

```
RouteFinderAlgorithm/
├── AlgorithmModule/          # Part A — Algorithmic engine
│   ├── src/main/java/com/hit/algorithm/
│   │   ├── IAlgoShortestPath.java       # Strategy interface
│   │   ├── DijkstraAlgoImpl.java        # Dijkstra (PriorityQueue)
│   │   └── AStarAlgoImpl.java           # A* with heuristic map
│   └── src/test/java/.../IAlgoShortestPathTest.java
│
├── FlightBookingProject/     # Part B — N-Tier application
│   ├── src/main/java/com/hit/
│   │   ├── dao/IDao.java, DaoFileImpl.java    # Generic DAO + Serialization
│   │   ├── dm/FlightTicket.java               # Domain model
│   │   └── service/FlightBookingService.java  # Business logic
│   └── src/test/java/.../
│       ├── dao/DaoFileImplTest.java
│       └── service/{FlightBookingServiceTest, FlightBookingServiceUnitTest, FlightBookingSystemE2ETest}.java
│
└── src/com/hit/Main.java     # Demo runner
```

## How to Run

Open the root `pom.xml` in IntelliJ as a Maven project, then:

- **Demo:** Run `src/com/hit/Main.java`
- **Tests:** Run any test class or `mvn test`

Built with Java 1.8, Maven, JUnit 4, and Mockito.
