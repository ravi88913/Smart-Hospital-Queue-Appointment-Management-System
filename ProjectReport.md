# Project Report: Smart Hospital Queue and Appointment Management System

This document serves as the academic project report detailing the data structures, algorithms, complexity analysis, test configurations, expected console outputs, and viva questions.

---

## 1. Project Overview & Architecture

The **Smart Hospital Queue and Appointment Management System** is a standalone Java 17 console application designed for clinical queue coordination and scheduling. 

### Core Modules:
1. **Patient Registration**: Adds patients and indexes them by their ID.
2. **Prioritized Queueing**: Dequeues patients using a dual-priority approach (highest clinical severity first, tie-broken by FIFO arrival order).
3. **Appointment Engine**: Automates booking, prevents doctor double-booking, manages cancellations, and houses a stack-based undo mechanism.
4. **Diagnostic Logs**: Maintains a chronological linked-list history of diagnoses, treatments, and prescriptions for each patient.
5. **Data Persistence**: Uses object stream serialization to write and recover the hospital state.

---

## 2. DSA Complexity Table

This table details the time and space complexity of the primary operations performed across the system’s data structures:

| Data Structure | Operations | Time Complexity (Best/Avg) | Time Complexity (Worst) | Space Complexity | Rationale & Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Priority Queue** (`PriorityQueue<EmergencyPatient>`) | `add` (Enqueue)<br>`poll` (Dequeue)<br>`peek` (View Next) | $O(\log n)$<br>$O(\log n)$<br>$O(1)$ | $O(\log n)$<br>$O(\log n)$<br>$O(1)$ | $O(n)$ | Arranges emergency patients based on severity levels (1 to 4) and arrival order. |
| **FIFO Queue** (`Queue<Patient>` via `LinkedList`) | `add` (Enqueue)<br>`poll` (Dequeue)<br>`peek` (View Next) | $O(1)$<br>$O(1)$<br>$O(1)$ | $O(1)$<br>$O(1)$<br>$O(1)$ | $O(n)$ | Processes normal patients on a first-come, first-served (FIFO) basis. |
| **HashMap** (`HashMap<Integer, Patient>`) | `put` (Insert)<br>`get` (Search)<br>`remove` (Delete) | $O(1)$<br>$O(1)$<br>$O(1)$ | $O(n)$<br>$O(n)$<br>$O(n)$ | $O(n)$ | Provides near-instantaneous indexing and retrieval of patient files by unique ID. |
| **Linked List** (`LinkedList<VisitRecord>`) | `add` (Append)<br>`iterator` (Traversal) | $O(1)$<br>$O(n)$ | $O(1)$<br>$O(n)$ | $O(m)$ per patient | Tracks clinical visit records chronologically. |
| **Stack** (`Stack<Appointment>`) | `push` (Cancel)<br>`pop` (Undo)<br>`peek` (View Top) | $O(1)$<br>$O(1)$<br>$O(1)$ | $O(1)$<br>$O(1)$<br>$O(1)$ | $O(k)$ total | Implements the Last-In-First-Out undo transaction buffer for appointment cancellations. |
| **ArrayList** (`ArrayList<Doctor>`, `ArrayList<Appointment>`) | `add` (Append)<br>`get` (Index Lookup)<br>`iteration` (Filtering) | $O(1)$ amortized<br>$O(1)$<br>$O(n)$ | $O(n)$<br>$O(1)$<br>$O(n)$ | $O(n)$ | Collects lists of doctors and appointments for dynamic listing and query filters. |

*Note: $n$ represents the total number of items, $m$ represents the history entries per patient, and $k$ represents active cancelled appointments.*

---

## 3. Sample Test Data

To validate the systems, use the following sample inputs:

### Doctors:
1. Name: **Dr. Bob**, Specialization: **General Medicine** (ID: 1)
2. Name: **Dr. Sarah**, Specialization: **Cardiology** (ID: 2)

### Patients:
1. Name: **Alice**, Age: **30**, Gender: **Female**, Phone: **1234567890** (ID: 1)
2. Name: **Charlie**, Age: **25**, Gender: **Male**, Phone: **9876543210** (ID: 2)
3. Name: **Dave**, Age: **40**, Gender: **Male**, Phone: **1111111111** (ID: 3)

---

## 4. Expected Console Output for Major Operations

Below are the exact console transcript structures for the key operations of the application:

### A. Register Patient
```text
--- Register New Patient ---
Enter Patient Name: Alice
Enter Patient Age (0-120): 30
Enter Patient Gender (e.g., Male/Female/Other): Female
Enter Phone Number: 1234567890

Patient registered successfully!
Patient ID: 1
Name: Alice
Age: 30
Gender: Female
Phone Number: 1234567890
Registration Date: 2026-07-09
Total Visits: 0
```

### B. View Emergency Queue showing Priorities
When Patient 1 (Alice, Severity 2), Patient 2 (Charlie, Severity 1), and Patient 3 (Dave, Severity 2) are enqueued in that sequence:
```text
--- Emergency Queue ---
--------------------------------
Patient ID: 2 | Name: Charlie | Severity: 1 | Arrival: 2
--------------------------------
Patient ID: 1 | Name: Alice | Severity: 2 | Arrival: 1
--------------------------------
Patient ID: 3 | Name: Dave | Severity: 2 | Arrival: 3
--------------------------------
```
*Notice that Charlie (Severity 1) comes first, and Alice is prioritized before Dave due to earlier arrival order (Arrival 1 vs 3).*

### C. Patient Treatment
```text
--- Treat Next Patient ---
Treating Patient:
Patient ID: 2
Name: Charlie
Age: 25
Gender: Male
Phone Number: 9876543210
Registration Date: 2026-07-09
Total Visits: 0
Patient Type: Emergency
Severity Level: 1 (Critical)
Arrival Order: 2

Select Doctor for Consultation:
ID: 1 | Dr. Bob | General Medicine
ID: 2 | Dr. Sarah | Cardiology
Enter Doctor ID: 1
Enter Diagnosis: Seasonal Flu
Enter Treatment: Adequate Rest and Hydration
Enter Prescription: Paracetamol 500mg daily for 3 days

Patient treated successfully! Visit record added.
```

### D. Appointment Conflict Prevention (Double Booking)
If Doctor 2 is booked for 2026-08-10 at 10:00:
```text
--- Schedule Appointment ---
Enter Patient ID: 2
Enter Doctor ID: 2
Enter Appointment Date (YYYY-MM-DD): 2026-08-10
Enter Appointment Time (HH:MM in 24hr format): 10:00
Error: Doctor is already booked for this slot.
```

### E. Stack Undo Failing Due to Slot Re-booking
When trying to undo a cancellation when the slot has since been filled by another appointment:
```text
Unable to restore appointment because the slot is no longer available.
```

---

## 5. Viva Questions & Answers

### Q1: What makes Java's `PriorityQueue` iteration behave differently than polling?
**Answer**: Java's `PriorityQueue` is backed by a binary heap array. When we iterate over it using an iterator or a foreach loop, it traverses the array sequentially, which does *not* represent sorted order. However, calling `poll()` repeatedly extracts the root of the heap (the minimum/maximum item) and re-heaps the structure in $O(\log n)$ time, ensuring items are retrieved in exact sorted order. In our system, to print the queue in sorted order without emptying it, we copy its contents to a temporary array and sort it explicitly.

### Q2: Why is the arrival counter field serializable and saved in `DataManager`?
**Answer**: If we reset the arrival counter to `1` when the application restarts, newly enqueued emergency patients might get arrival index values that clash with existing loaded patients in the queue. Serializing the counter ensures that the system picks up right where it left off, maintaining correct FIFO tie-breaking for equal severity.

### Q3: Why is standard FIFO queue implemented using a `LinkedList` rather than an `ArrayList`?
**Answer**: A queue requires elements to be added at the tail (enqueue) and removed from the head (dequeue). In an `ArrayList`, removing an element from index 0 requires shifting all subsequent elements down by one spot, making it an $O(n)$ operation. In a `LinkedList`, adding at the tail and removing from the head only involves pointer updates, which runs in $O(1)$ time complexity.

### Q4: How is a Hash Map query faster than a binary search or linear array scan?
**Answer**: A hash map uses a hashing function to map keys (Patient IDs) directly to array indices (buckets) in $O(1)$ average time. A linear scan takes $O(n)$ because it checks every element. A binary search takes $O(\log n)$ and requires the array to be kept sorted, which adds overhead during insertions. Thus, HashMap is the optimal structure for real-time lookups.

### Q5: What is serialization, and how does your system handle corrupted data loads?
**Answer**: Serialization is the process of converting an object's state into a byte stream to save it to disk. During startup, if the file `data/hospital_data.dat` is missing or contains invalid/corrupted data, the `DataManager` catches `IOException` or `ClassNotFoundException`, alerts the user, and initializes empty collections instead of crashing the program.
