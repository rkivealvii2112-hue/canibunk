# 🎓 CanIBunk.com - Phase 1: Core Logic & Academic Data Structures

> **Combined Academic Project for Java Programming & Data Structures Course**  
> An attendance monitor and bunkability predictor enforcing a strict **75% college attendance threshold** (e.g., for portals like *aesajce.in* / KTU / engineering colleges).

---

## 📌 Phase 1 Objectives Accomplished

- [x] **Zero External Libraries for Core DS**: Custom data structures written in pure Java to earn maximum marks in university DS evaluations.
- [x] **Mathematical Calculation Engine**: Exact formulas for Current %, Safe Bunks, Classes Needed, and Next Hour Bunkability.
- [x] **Custom Data Structures Implemented From Scratch**:
  1. `CustomHashMap<K, V>`: Separate chaining hash table for $O(1)$ subject lookup.
  2. `CustomStack<T>`: Dynamic array LIFO stack for the "What-If" simulator with **multi-step Undo & Redo**.
  3. `CustomQueue<T>`: Circular buffer FIFO queue for today's lecture schedule.
  4. `DoublyLinkedList<T>`: Bidirectional linked nodes for the semester academic calendar.
  5. `AttendanceBST`: Binary Search Tree for **Danger Sorting**; in-order traversal yields subjects ordered from lowest percentage (Danger Zone) to highest (Safe Zone).
- [x] **Phase 2: Selenium Web Scraper & HTML Table Parser**:
  1. `AesPortalScraper`: Headless browser automation logging into `https://login.aesajce.in/`, extracting the attendance table, and cleanly exiting.
  2. `AttendanceTableParser`: Robust HTML table parser capable of identifying codes, titles, fractions (`24/26`), and integers while ignoring serial numbers.
  3. Zero Password Storage: Credentials are read with masking, used exclusively for the session, wiped from memory, and never persisted.
- [x] **Automated Test Suite**: 64 unit tests validating math, edge cases, custom data structures, and HTML scraper parsing.
- [x] **Interactive CLI Dashboard**: Full terminal interface to inspect attendance, simulate choices, step through today's classes, and test live or offline scraping.

---

## 📐 The Bunking Math (Strict 75% Rule)

### 1. Current Percentage
$$\text{Current \%} = \left(\frac{\text{Classes Attended}}{\text{Total Classes Conducted}}\right) \times 100$$

### 2. Safe to Bunk Formula (If Current % $\ge 75\%$)
Determines how many consecutive future classes can be skipped before dropping below 75%:
$$\frac{\text{Attended}}{\text{Conducted} + B} \ge 0.75 \implies B \le \frac{\text{Attended}}{0.75} - \text{Conducted}$$
$$\mathbf{\text{Safe Bunks}} = \left\lfloor \frac{\text{Attended}}{0.75} - \text{Conducted} \right\rfloor$$
*(Note: If the result is 2.8, you can safely bunk 2 classes).*

> **Edge Case Handled**: If attendance is 80% (8/10), $\lfloor(8/0.75) - 10\rfloor = \lfloor 0.666 \rfloor = 0$. Even though 80% is above 75%, bunking 1 class drops attendance to $8/11 = 72.7\%$. The formula accurately returns **0 safe bunks**!

### 3. Danger Zone Formula (If Current % $< 75\%$)
Determines how many consecutive future classes must be attended to climb back to 75%:
$$\frac{\text{Attended} + M}{\text{Conducted} + M} \ge 0.75 \implies M \ge \frac{0.75 \times \text{Conducted} - \text{Attended}}{0.25}$$
$$\mathbf{\text{Classes Needed}} = \left\lceil \frac{0.75 \times \text{Conducted} - \text{Attended}}{0.25} \right\rceil$$
*(Note: If the result is 4.1, you must attend 5 classes straight).*

### 4. Next Hour Bunkability Verdict
$$\text{Projected \% If Bunked} = \left(\frac{\text{Attended}}{\text{Conducted} + 1}\right) \times 100 \ge 75.0\%$$

---

## 🏗️ Data Structures Mapping (Academic Core)

| Data Structure | Class Name | Academic Role in CanIBunk | Time Complexity |
| :--- | :--- | :--- | :--- |
| **Hash Table** | [`CustomHashMap<K, V>`](src/com/canibunk/ds/CustomHashMap.java) | Fast $O(1)$ lookup of subjects by course code (e.g., `"CS203"` $\to$ `Subject`). Separate chaining collision resolution. | $O(1)$ avg lookup & insert |
| **Stack (LIFO)** | [`CustomStack<T>`](src/com/canibunk/ds/CustomStack.java) | **"What-If" Simulator**: Pushes `SimulationAction` whenever user simulates attending/bunking. `undo()` pops the last action and restores previous state. | $O(1)$ push / pop |
| **Queue (FIFO)** | [`CustomQueue<T>`](src/com/canibunk/ds/CustomQueue.java) | **Daily Schedule**: Loads today's periods in order. `dequeue()` advances hour-by-hour and evaluates next hour bunkability. | $O(1)$ enqueue / dequeue |
| **Doubly Linked List** | [`DoublyLinkedList<T>`](src/com/canibunk/ds/DoublyLinkedList.java) | **Academic Calendar**: Node-based calendar. Allows bidirectional traversal (forward to count remaining working days, backward from exam dates). | $O(1)$ insert, $O(n)$ search |
| **Binary Search Tree** | [`AttendanceBST`](src/com/canibunk/ds/AttendanceBST.java) | **Danger Sorting**: Nodes ordered by attendance %. In-order traversal naturally outputs subjects from lowest attendance (Danger Zone) to highest (Safe Zone). | $O(\log n)$ avg insert |

---

## 📁 Project Directory Structure

```text
canibunk/
├── src/
│   └── com/
│       └── canibunk/
│           ├── model/
│           │   ├── Subject.java             # Core subject model & statistics
│           │   ├── ClassSession.java        # Daily schedule period model
│           │   ├── CalendarDay.java         # Academic calendar node payload
│           │   ├── SimulationAction.java    # Action encapsulation for Stack undo/redo
│           │   └── AttendanceStatus.java    # Enum (CRITICAL, ON_THE_EDGE, SAFE)
│           ├── ds/
│           │   ├── CustomStack.java         # Generic LIFO Dynamic Array
│           │   ├── CustomQueue.java         # Generic FIFO Circular Buffer
│           │   ├── DoublyLinkedList.java    # Generic Bidirectional Linked List
│           │   ├── AttendanceBST.java       # Binary Search Tree for Danger Sorting
│           │   └── CustomHashMap.java       # Separate Chaining Hash Map
│           ├── service/
│           │   ├── AttendanceCalculator.java # Pure math calculation formulas
│           │   ├── WhatIfSimulator.java      # Simulator engine (Stack undo/redo)
│           │   ├── ScheduleManager.java      # Daily schedule engine (Queue FIFO)
│           │   ├── CalendarManager.java      # Calendar engine (DLL traversal)
│           │   └── DangerZoneService.java    # Danger ranking engine (BST)
│           ├── Main.java                    # Interactive CLI Dashboard & Demo
│           └── TestRunner.java              # Automated test suite (52 tests)
├── build_and_run.bat                        # Windows CMD build & run launcher
├── build_and_run.ps1                        # PowerShell build & run launcher
└── README.md                                # Project documentation
```

---

## 🚀 How to Run

### Option 1: Run the Automated Test Suite
```powershell
# Compile all source files
javac -d bin (Get-ChildItem -Recurse src\*.java).FullName

# Run tests
java -cp bin com.canibunk.TestRunner
```

### Option 2: Run the Automated Feature Demonstration
```powershell
java -cp bin com.canibunk.Main --demo
```

### Option 3: Launch the Interactive CLI Dashboard
```powershell
java -cp bin com.canibunk.Main
```
Or simply double-click **`build_and_run.bat`** in Windows Explorer.

---

## 🔮 Roadmap: Next Steps

- **Phase 2 (Scraping)**: Implement headless browser scraping (Selenium / Playwright for Java) to authenticate against `aesajce.in`, extract live tables, and hydrate `CustomHashMap`.
- **Phase 3 (Web UI)**: Connect a lightweight web backend (Spring Boot or Spark Java) with a modern frontend interface for canibunk.com.
