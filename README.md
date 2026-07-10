# Route Finder Algorithm — Flight Booking System

**Students:** Idan Mashiah and Alon Iozef

A multi-module Java project implementing graph pathfinding algorithms (Dijkstra, A\*) and an N-Tier flight booking system using the **Strategy Pattern** and **SOLID principles**, communicating over **TCP sockets with JSON serialization**.

---

## Architecture

```
FlightBookingClient          CommunicationLayer (3 Servers)     FlightBookingProject
(JavaFX GUI)                 (ports 3001, 3002, 3003)           (Business Logic + DAO)
     |                              |                                  |
     |--- TCP/JSON ---->     FlightServer:3001  ------> FlightBookingService
     |                        BookingServer:3002  ------> FlightBookingService
     |                        UserServer:3003     ------> UserService
     |                                                              |
     |                                                   AlgorithmModule
     |                                                   (Dijkstra, A*)
```

### Server Ports

| Server        | Port | Actions Handled                                |
|---------------|------|-----------------------------------------------|
| FlightServer  | 3001 | `flight/get`, `flight/getall`, `flight/route` |
| BookingServer | 3002 | `booking/create`, `booking/cancel`            |
| UserServer    | 3003 | `user/register`, `user/login`                 |

### Protocol
- **Transport:** Raw TCP (one connection per request, then closed)
- **Format:** JSON over newline-delimited text
- **Request:** `{"headers":{"action":"flight/get"},"body":{"id":"FT001"}}` + blank line
- **Response:** `{"statusCode":200,"message":"Ticket found","data":{...}}`

---

## Project Structure

```
RouteFinderAlgorithm/
├── AlgorithmModule/                      # Algorithmic engine
│   ├── src/main/java/com/hit/algorithm/
│   │   ├── IAlgoShortestPath.java        # Strategy interface
│   │   ├── DijkstraAlgoImpl.java         # Dijkstra (PriorityQueue)
│   │   └── AStarAlgoImpl.java            # A* with heuristic map
│   └── src/test/java/.../IAlgoShortestPathTest.java
│
├── FlightBookingProject/                 # N-Tier application
│   ├── src/main/java/com/hit/
│   │   ├── dao/IDao.java                 # Generic DAO interface
│   │   ├── dao/DaoFileImpl.java          # File-backed DAO (ConcurrentHashMap + Serialization)
│   │   ├── dm/FlightTicket.java          # Flight ticket domain model
│   │   ├── dm/User.java                  # User domain model
│   │   ├── service/FlightBookingService.java  # Booking business logic
│   │   └── service/UserService.java      # User auth (in-memory)
│   └── src/test/java/.../
│       ├── dao/DaoFileImplTest.java
│       └── service/{FlightBookingServiceTest, FlightBookingServiceUnitTest, FlightBookingSystemE2ETest}.java
│
├── CommunicationLayer/                   # TCP servers + controllers
│   ├── src/main/java/com/hit/
│   │   ├── server/ServerDriver.java      # Entry point: starts 3 servers
│   │   ├── server/Server.java            # TCP ServerSocket listener
│   │   ├── server/HandleRequest.java     # Request dispatcher
│   │   ├── server/Request.java           # Request DTO
│   │   ├── server/Response.java          # Response DTO
│   │   ├── controller/FlightController.java
│   │   ├── controller/BookingController.java
│   │   ├── controller/UserController.java
│   │   ├── controller/Controller.java    # Controller interface
│   │   ├── client/InteractiveClient.java # Console client (menu-driven)
│   │   └── client/Client.java            # One-shot CLI client
│   │   └── util/ControllerFactory.java   # DI wiring
│
├── FlightBookingClient/                  # JavaFX GUI (separate module)
│   ├── src/main/java/com/hit/client/
│   │   ├── FlightBookingClientApp.java   # JavaFX entry point
│   │   ├── controller/MainController.java
│   │   └── model/{ServerClient.java, FlightData.java}
│   └── src/main/resources/
│       ├── fxml/main.fxml
│       └── css/style.css
│
└── src/com/hit/Main.java                 # Standalone demo (no server needed)
```

---

## Prerequisites

| Tool    | Version     | Notes                                      |
|---------|-------------|--------------------------------------------|
| JDK     | 8+          | JDK 11+ required for FlightBookingClient  |
| Maven   | 3.6+        |                                            |

---

## How to Build

```bash
# From the project root
mvn clean install
```

This builds and tests all modules: `AlgorithmModule`, `FlightBookingProject`, `CommunicationLayer` with **15 tests** (4 algorithm, 11 service/DAO).

---

## How to Run

### 1. Start the Servers

**Using java directly** (project root directory):
```powershell
$env:JAVA_HOME="C:\path\to\jdk-8"
$env:Path="$env:JAVA_HOME\bin;C:\path\to\apache-maven-3.9.16\bin;$env:Path"
java -cp "CommunicationLayer\target\classes;FlightBookingProject\target\classes;AlgorithmModule\target\classes;$env:USERPROFILE\.m2\repository\com\google\code\gson\gson\2.8.9\gson-2.8.9.jar" com.hit.server.ServerDriver
```

**Or using Maven:**
```powershell
$env:JAVA_HOME="C:\path\to\jdk-8"
$env:Path="$env:JAVA_HOME\bin;C:\path\to\apache-maven-3.9.16\bin;$env:Path"
mvn exec:java -pl CommunicationLayer -Dexec.mainClass="com.hit.server.ServerDriver"
```

You should see:
```
All servers started:
  FlightServer   -> port 3001
  BookingServer  -> port 3002
  UserServer     -> port 3003
Press Ctrl+C to stop all servers.
```

**Note:** The data source path is configurable via the `datasource.path` system property:
```powershell
java -Ddatasource.path="E:\data\tickets.txt" -cp ...
```

### 2. Run the Console Client (Terminal 2)

```powershell
java -cp "CommunicationLayer\target\classes;FlightBookingProject\target\classes;AlgorithmModule\target\classes;$env:USERPROFILE\.m2\repository\com\google\code\gson\gson\2.8.9\gson-2.8.9.jar" `
com.hit.client.InteractiveClient
```

Menu options:
```
1. Register user
2. Login
3. Book a flight
4. Get ticket
5. Cancel flight
6. Calculate optimal route
7. Exit
```

### 3. Run the JavaFX GUI (requires JDK 11+)

```powershell
$env:JAVA_HOME="C:\path\to\jdk-11"; `
$env:Path="$env:JAVA_HOME\bin;$env:Path"; `
cd FlightBookingClient; `
mvn javafx:run
```

**GUI Walkthrough:**
1. **Route Planner** tab — Add edges (`Paris,Lyon,100` → Add, `Lyon,Geneva,80` → Add, etc.), set From/To cities, click **Find Shortest Route**
2. **My Bookings** tab — Book a flight (Ticket ID, Customer, Source, Destination, Price) → **Book Flight**
3. **Flight Search** tab — Click **Refresh All** to see all booked flights

### 4. Run the Standalone Demo (no servers needed)

```bash
java -cp "src;AlgorithmModule\target\classes;FlightBookingProject\target\classes" com.hit.Main
```

Runs algorithm demo, DAO persistence demo, and booking service demo.

### 5. Run Tests

```bash
mvn test
```

---

## One-Shot CLI Client

```bash
java com.hit.client.Client localhost 3001 "{\"headers\":{\"action\":\"flight/get\"},\"body\":{\"id\":\"FT001\"}}"
```

---

## Bug Fixes Applied

| # | Issue | File | Fix |
|---|-------|------|-----|
| 1 | Integer overflow when distance = MAX_VALUE | `DijkstraAlgoImpl.java`, `AStarAlgoImpl.java` | Skip nodes with `MAX_VALUE` distance before adding edge weight |
| 2 | Thread-unsafe DAO cache (`HashMap` + concurrent file writes) | `DaoFileImpl.java` | Replaced `HashMap` with `ConcurrentHashMap`; added `ReentrantReadWriteLock` for file I/O |
| 3 | Thread-unsafe user storage (`HashMap`) | `UserService.java` | Replaced with `ConcurrentHashMap` |
| 4 | Duplicate booking silently overwrites existing ticket | `BookingController.java` | Returns `409 Conflict` if ticket ID already exists |
| 5 | User password exposed in API responses | `UserController.java` | `sanitizeUser()` strips password before returning user data |
| 6 | Hardcoded data source path fails from different CWD | `ControllerFactory.java` | `resolveDataSourcePath()` tries multiple candidates; configurable via `-Ddatasource.path` |

---

## Built With

- **Java 1.8** (AlgorithmModule, FlightBookingProject, CommunicationLayer)
- **Java 11** (FlightBookingClient — JavaFX 17)
- **Maven** — multi-module build
- **JUnit 4.13.2** + **Mockito 3.12.4** — testing
- **Google Gson 2.8.9** — JSON serialization
- **JavaFX 17** — GUI client
