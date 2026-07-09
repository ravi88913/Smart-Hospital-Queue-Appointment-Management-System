# Smart Hospital Queue and Appointment Management System Using Data Structures and Algorithms

A complete standalone Java console-based application designed to demonstrate the implementation and practical usage of core Data Structures and Algorithms (DSA) in a real-world scenario.

---

## Project Overview

This system coordinates patient registration, queue management (with emergency prioritization and standard FIFO handling), doctor assignments, appointment scheduling, patient histories, and administrative reporting. It uses standard Java features (no external libraries or databases) to show direct implementation of core data structures.

---

## 🛠️ Technology Stack & Requirements

- **Language**: Java 17 or later
- **Architecture**: Object-Oriented Design (OOD)
- **Interface**: Interactive Console (CLI)
- **Data Persistence**: Java Binary Serialization (`ObjectOutputStream`/`ObjectInputStream`)
- **Libraries**: Pure Java Standard Library (no third-party dependencies)

---

## 📂 Project Structure

```text
SmartHospitalManagementSystem/
├── src/
│   ├── model/
│   │   ├── Patient.java             # Base Patient entity with Visit history
│   │   ├── EmergencyPatient.java    # Extended Patient with priority & arrival counters
│   │   ├── Doctor.java              # Doctor entity
│   │   ├── Appointment.java         # Appointment with status tracking
│   │   └── VisitRecord.java         # Diagnostic and visit log details
│   │
│   ├── service/
│   │   └── HospitalManagementSystem.java  # Core business logic and DSA coordinator
│   │
│   ├── util/
│   │   ├── DataManager.java         # Handles loading/saving state to disk
│   │   └── InputValidator.java      # Crash-proof CLI input scanner methods
│   │
│   └── Main.java                    # Entry point; CLI menu routing
│
├── data/
│   └── hospital_data.dat            # Persistent database file (binary serialization)
│
├── README.md                        # Compilation and usage guide (this file)
└── ProjectReport.md                 # Project Report (viva QA, DSA table, test data, expected outputs)
```

---

## 🚀 How to Compile and Run

Make sure you are in the project root directory (`SmartHospitalManagementSystem/`).

### 1. Compile the Source Code
Compile all package directories into the `bin` directory:
```bash
javac -d bin src/model/*.java src/service/*.java src/util/*.java src/Main.java
```

### 2. Run the Application
Start the interactive command-line interface:
```bash
java -cp bin Main
```

---

## 💻 Key Console Features

1. **Patient Management**: Register patients, search by ID in $O(1)$ time, update info, and delete records safely.
2. **Queue Management**: Add patients to standard FIFO or Priority Emergency Queue. Processes treatment using prioritized rules.
3. **Doctor Management**: Register doctors, view doctors, and search specialization details.
4. **Appointment Management**: Book, list, filter, cancel, and complete appointments. Includes a LIFO Undo Stack.
5. **Visit History**: Sequential log of diagnostics, treatments, and prescriptions per patient.
6. **Reports & Statistics**: Live dashboard tracking registration metrics, wait times, appointments, and doctor caseloads.
7. **Save Data**: Explicitly save live records to `data/hospital_data.dat` (also triggered automatically on exit).
