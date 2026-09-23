# Hospital Patient Management System (CarePulse)

A robust, full-featured desktop application for managing patient records, doctor allocations, appointments, clinical medical records, and billing workflows in a hospital. Built in **Java** with a modern **Swing GUI**, applying fundamental Object-Oriented Design principles and core Java Data Structures (**Arrays**, **LinkedList**, **HashMap**, and **TreeMap**).

---

## 🎯 Objectives & Data Structures Mapping

| Requirement / Objective | Implementation in System | Time Complexity | Justification & Use-Case |
| :--- | :--- | :--- | :--- |
| **Classes & Objects (OOP)** | `Patient`, `Doctor`, `Appointment`, `MedicalRecord`, `BillingRecord` | N/A | Encapsulates all domain entities with attributes, getters/setters, validation, and helper methods. |
| **Arrays (`T[]`)** | `BLOOD_GROUPS`, `DEPARTMENTS`, `ROOM_TYPES`, `APPOINTMENT_TIME_SLOTS`, `PAYMENT_METHODS`, and `recentPatientIdsArray[5]` | $O(1)$ indexing, $O(N)$ search | Fixed medical metadata categories, dropdown menus, and a 5-slot circular buffer for recent patient admissions. |
| **`LinkedList<T>`** | `LinkedList<Appointment>` & `LinkedList<MedicalRecord>` | $O(1)$ insertion / deletion, $O(N)$ traversal | Manages consultation queues in chronological order (FIFO scheduling) and patient clinical history logs. |
| **`HashMap<K, V>`** | `patientMap` (`PAT-xxxx`), `doctorMap` (`DOC-xxx`), `billingMap` (`INV-xxxx`) | $O(1)$ average lookup / insertion | Instantaneous key-based patient searching by Patient ID, physician retrieval, and invoice queries. |
| **`TreeMap<K, V>`** | `patientsSortedByName` & `patientsSortedById` | $O(\log N)$ sorted operations | Red-Black Tree maintaining balanced alphabetical and natural ID-sorted patient directories with $O(\log N)$ search/insertion. |
| **Swing GUI** | `MainFrame`, `DashboardPanel`, `PatientPanel`, `DoctorPanel`, `AppointmentPanel`, `MedicalRecordPanel`, `BillingPanel`, `SortedRecordsPanel` | N/A | Professional multi-view dashboard with sidebar navigation, metric cards, styled data tables, and modal dialogs. |

---

## 📂 Project Architecture & Package Structure

```
MiniProject_Java/
├── src/
│   └── com/hospital/
│       ├── Main.java                     # Application GUI Entry Point (Swing EDT)
│       ├── HospitalSystemTest.java       # Automated unit and DSA test suite
│       ├── model/                        # Domain Models (OOP)
│       │   ├── Patient.java              # Patient entity (demographics, blood group, vitals, allergies)
│       │   ├── Doctor.java               # Doctor entity (specialization, fee, schedule, contact)
│       │   ├── Appointment.java          # Appointment model (status, date, queue order)
│       │   ├── MedicalRecord.java        # Clinical diagnoses, prescriptions & lab tests
│       │   └── BillingRecord.java        # Financial invoices, tax calculations, discounts
│       ├── service/
│       │   └── HospitalService.java      # Central Data Store (Arrays, LinkedList, HashMap, TreeMap)
│       └── ui/                           # Modern Java Swing User Interface
│           ├── UIConstants.java          # Custom color palette, fonts, buttons, table stylers
│           ├── MainFrame.java            # Main window with sidebar navigation & card layout
│           ├── DashboardPanel.java       # KPI cards, upcoming queue, recent patients
│           ├── PatientPanel.java         # Patient CRUD + O(1) HashMap Search + Profile Dossier
│           ├── DoctorPanel.java          # Specialist directory & Doctor registration
│           ├── AppointmentPanel.java     # LinkedList queue scheduling & status updates
│           ├── MedicalRecordPanel.java   # Clinical history timeline & prescription viewer
│           ├── BillingPanel.java         # Invoice generator, tax calculator & payment processor
│           └── SortedRecordsPanel.java   # TreeMap sorting visualizer & DSA summary
├── compile.sh                            # Script to compile all .java files into bin/
├── run.sh                                # Script to compile and launch the Swing GUI
├── test.sh                               # Script to run the automated unit test suite
└── README.md                             # Documentation & Setup Guide
```

---

## 🚀 How to Run the Application

### Prerequisites
- **Java Development Kit (JDK 8 or higher / JDK 17 / JDK 21 / JDK 26+)** installed.

### 1. Launch the Swing GUI
Run the launch script from the project root:
```bash
./run.sh
```
*Or manually:*
```bash
mkdir -p bin
javac -d bin $(find src -name "*.java")
java -cp bin hospital.Main
```

### 2. Run Automated Verification Tests
Execute the automated test suite covering all data structure operations and validation:
```bash
./test.sh
```

---

## 🖥️ Feature Walkthrough

1. **Overview Dashboard**:
   - Live KPI metric cards for Total Registered Patients, Active Doctors, Today's Consultations, and Pending Receivables.
   - Quick administrative actions to jump to any module.
   - Live **LinkedList** upcoming appointments queue table.
   - Live **Fixed Array Buffer** displaying the 5 most recently registered patient slots.

2. **Patient Management**:
   - Register new patients with auto-generated unique IDs (`PAT-100x`).
   - Edit, update, and delete patient records across both HashMap and TreeMaps.
   - Fast $O(1)$ search by Patient ID or substring search by Name, Phone, and Blood Group.
   - Detailed patient dossier popup summarizing demographic data, appointment history, and medical history.

3. **Doctor Directory**:
   - View physicians and filter by specialization using the `DEPARTMENTS` array.
   - Register new specialist doctors with consultation fees and schedule details.

4. **Appointment Management (LinkedList)**:
   - Schedule consultations for patients with available doctors and time slots.
   - Enqueue appointments in FIFO order within the `LinkedList<Appointment>`.
   - Update appointment status (`In Progress`, `Completed`, `Cancelled`).

5. **Medical Records (LinkedList)**:
   - Log clinical visits, patient symptoms, diagnoses, prescriptions (Rx), and lab test recommendations.
   - Inspect individual medical records in a formatted clinical report viewer.

6. **Billing & Financial Invoices (HashMap)**:
   - Generate itemized bills with automated subtotal, configurable tax %, and discount deductions.
   - Process payments via Cash, Credit Card, Health Insurance, or UPI (`PAYMENT_METHODS` array).
   - View and print formatted hospital invoices with itemized cost breakdown.

7. **Sorted Records & DSA Visualizer (TreeMap)**:
   - Interactive sorting powered by `TreeMap<String, Patient>` and custom comparators:
     - Alphabetical by Patient Name (Case-Insensitive Red-Black Tree)
     - Natural Order by Patient ID
     - Age (Youngest to Oldest & Oldest to Youngest)
     - Most Recent Registrations First
   - Educational breakdown detailing time complexities and practical hospital software justification.
