# Parking Lot Management System (BCO 035B: Programming in Java)

A complete Core Java SE 8 desktop & applet application built strictly in accordance with the **BCO 035B: Programming in Java** syllabus.

## Syllabus Alignment

| Unit | Topics Covered | Implementation in Project |
| :--- | :--- | :--- |
| **Unit 1** | OOPs, JVM, Reflection, Data types, Arrays, Control structures, Objects & Classes | `Vehicle`, `ParkingFloor` (`ParkingSpot[]` arrays), `ReflectionInspector` (`java.lang.reflect`) |
| **Unit 2** | Abstract classes, Static classes, Inner classes, Packages, Wrapper classes, Interfaces, `this`, `super`, Access control | `Vehicle` (abstract), `Parkable` & `Billable` (interfaces), `ParkingTicket.TicketBuilder` & `ParkingLotService.LotStatistics` (static nested classes), `ParkingFloor.FloorSummary` (inner class), `Long`/`Double` wrappers, `com.parking.*` packages |
| **Unit 3** | Exception hierarchy, `try-catch-finally`, `throw`, `throws` | `ParkingException` -> `ParkingFullException`, `InvalidTicketException`, `DuplicateVehicleException`, `InvalidVehicleException` |
| **Unit 4** | Input/Output streams, Filter streams, Serialization/Deserialization, Pipe streams, File I/O | `ParkingPersistenceManager` (`ObjectOutputStream`/`ObjectInputStream` + `Buffered*` + CSV `PrintWriter`), `PipeAuditLogger` (`PipedOutputStream`/`PipedInputStream`) |
| **Unit 5** | Multithreading, Thread life cycle, Synchronization, AWT & Swing GUI, `JApplet` + HTML `<param>` | `ParkingLotService` (`synchronized`), `GateSimulatorThread`, `AutoSaveThread`, `ParkingLotApp` (`JFrame`, `JTable`, `JTabbedPane`, AWT layouts & listeners), `ParkingStatusApplet` + `applet/parking_applet.html` |

## Build & Run (JDK 8)

### 1. Compile all sources
```powershell
mkdir bin -Force
Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName } | javac -d bin -sourcepath src
```
*(Or pass all `.java` paths to `javac -d bin`.)*

### 2. Run Automated Test Suite
```powershell
java -cp bin com.parking.test.ParkingLotTest
```

### 3. Run the Swing Desktop Application
```powershell
java -cp bin com.parking.ui.ParkingLotApp
```

### 4. Run the JApplet (JDK 8 `appletviewer`)
```powershell
appletviewer applet/parking_applet.html
```
