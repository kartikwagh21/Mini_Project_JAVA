package hospital.service;

import hospital.model.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Central Service/Data Management class for the Hospital Patient Management System.
 * 
 * Demonstrates the use of:
 * 1. Arrays: Fixed metadata categories and a fixed-size buffer for recent patient registrations.
 * 2. LinkedList: Dynamic sequence of appointments and chronological medical history records.
 * 3. HashMap: Fast O(1) key-based lookup for patients, doctors, and billing records by ID.
 * 4. TreeMap: Automatically sorted patient records by ID, Name, and custom criteria.
 */
public class HospitalService {
    private static HospitalService instance;

    // -------------------------------------------------------------
    // 1. ARRAYS for fixed hospital configuration & patient tracking
    // -------------------------------------------------------------
    public static final String[] BLOOD_GROUPS = {
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    public static final String[] GENDERS = {
        "Male", "Female", "Other"
    };

    public static final String[] DEPARTMENTS = {
        "Cardiology", "Neurology", "Orthopedics", "Pediatrics",
        "General Medicine", "Dermatology", "Oncology", "ENT", "Emergency Care"
    };

    public static final String[] ROOM_TYPES = {
        "General Ward", "Semi-Private", "Private Room", "ICU", "CCU", "Day Care"
    };

    public static final String[] APPOINTMENT_TIME_SLOTS = {
        "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
        "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM"
    };

    public static final String[] PAYMENT_METHODS = {
        "Cash", "Credit Card", "Debit Card", "Health Insurance", "UPI / Net Banking"
    };

    // Fixed-size array for storing the last 5 registered patient IDs (Array Demonstration)
    private final String[] recentPatientIdsArray = new String[5];
    private int recentPatientCount = 0;

    // -------------------------------------------------------------
    // 2. LINKEDLIST for sequential records and queue-like workflows
    // -------------------------------------------------------------
    private final LinkedList<Appointment> appointmentList = new LinkedList<>();
    private final LinkedList<MedicalRecord> medicalRecordList = new LinkedList<>();

    // -------------------------------------------------------------
    // 3. HASHMAP for O(1) ID-based lookup and fast access
    // -------------------------------------------------------------
    private final HashMap<String, Patient> patientMap = new HashMap<>();
    private final HashMap<String, Doctor> doctorMap = new HashMap<>();
    private final HashMap<String, BillingRecord> billingMap = new HashMap<>();

    // -------------------------------------------------------------
    // 4. TREEMAP for sorted patient records
    // -------------------------------------------------------------
    // Sorted naturally by Patient Name (Case-Insensitive)
    private final TreeMap<String, Patient> patientsSortedByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    // Sorted naturally by Patient ID
    private final TreeMap<String, Patient> patientsSortedById = new TreeMap<>();

    // ID Sequence Counters
    private int nextPatientSeq = 1001;
    private int nextDoctorSeq = 201;
    private int nextAppointmentSeq = 5001;
    private int nextRecordSeq = 7001;
    private int nextBillSeq = 9001;

    private HospitalService() {
        seedInitialData();
    }

    public static synchronized HospitalService getInstance() {
        if (instance == null) {
            instance = new HospitalService();
        }
        return instance;
    }

    // =============================================================
    // PATIENT MANAGEMENT (HashMap, TreeMap, Arrays)
    // =============================================================

    /**
     * Generates a unique Patient ID (e.g. PAT-1001)
     */
    public synchronized String generatePatientId() {
        return "PAT-" + (nextPatientSeq++);
    }

    /**
     * Adds a new patient into HashMap, TreeMaps, and Recent Array.
     */
    public synchronized boolean addPatient(Patient patient) {
        if (patient == null || patient.getId() == null || patient.getId().trim().isEmpty()) {
            return false;
        }

        // Add to HashMap for O(1) lookup
        patientMap.put(patient.getId(), patient);

        // Add to TreeMaps for sorted retrieval
        patientsSortedById.put(patient.getId(), patient);
        // Key by Name + ID to avoid overwriting patients with identical names
        patientsSortedByName.put(patient.getName() + " (" + patient.getId() + ")", patient);

        // Add to fixed-capacity array (shifts existing elements)
        recordRecentPatientId(patient.getId());

        return true;
    }

    /**
     * Updates an existing patient's details across all data structures.
     */
    public synchronized boolean updatePatient(Patient updatedPatient) {
        if (updatedPatient == null || !patientMap.containsKey(updatedPatient.getId())) {
            return false;
        }

        Patient oldPatient = patientMap.get(updatedPatient.getId());

        // Remove old entry from Name TreeMap
        patientsSortedByName.remove(oldPatient.getName() + " (" + oldPatient.getId() + ")");

        // Update in HashMap and TreeMaps
        patientMap.put(updatedPatient.getId(), updatedPatient);
        patientsSortedById.put(updatedPatient.getId(), updatedPatient);
        patientsSortedByName.put(updatedPatient.getName() + " (" + updatedPatient.getId() + ")", updatedPatient);

        // Update patient name in existing appointments & bills for consistency
        for (Appointment apt : appointmentList) {
            if (apt.getPatientId().equals(updatedPatient.getId())) {
                apt.setPatientName(updatedPatient.getName());
            }
        }
        for (BillingRecord bill : billingMap.values()) {
            if (bill.getPatientId().equals(updatedPatient.getId())) {
                bill.setPatientName(updatedPatient.getName());
            }
        }

        return true;
    }

    /**
     * Deletes a patient from HashMap and TreeMaps.
     */
    public synchronized boolean deletePatient(String patientId) {
        if (!patientMap.containsKey(patientId)) {
            return false;
        }

        Patient patient = patientMap.remove(patientId);
        patientsSortedById.remove(patientId);
        patientsSortedByName.remove(patient.getName() + " (" + patientId + ")");

        return true;
    }

    /**
     * HashMap O(1) search by Patient ID.
     */
    public Patient getPatientById(String patientId) {
        if (patientId == null) return null;
        return patientMap.get(patientId.trim());
    }

    /**
     * Search patients by ID, Name, Blood Group, or Contact Number.
     */
    public List<Patient> searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(patientsSortedById.values());
        }
        String q = query.trim().toLowerCase();
        List<Patient> result = new ArrayList<>();
        for (Patient p : patientMap.values()) {
            if (p.getId().toLowerCase().contains(q) ||
                p.getName().toLowerCase().contains(q) ||
                p.getBloodGroup().toLowerCase().contains(q) ||
                p.getContactNumber().toLowerCase().contains(q) ||
                p.getAddress().toLowerCase().contains(q)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Returns all patients sorted by Patient ID using TreeMap.
     */
    public List<Patient> getPatientsSortedById() {
        return new ArrayList<>(patientsSortedById.values());
    }

    /**
     * Returns all patients sorted by Patient Name using TreeMap.
     */
    public List<Patient> getPatientsSortedByName() {
        return new ArrayList<>(patientsSortedByName.values());
    }

    /**
     * Returns all patients sorted by Age using TreeMap or List sorting.
     */
    public List<Patient> getPatientsSortedByAge(boolean ascending) {
        List<Patient> list = new ArrayList<>(patientMap.values());
        list.sort((p1, p2) -> ascending ? Integer.compare(p1.getAge(), p2.getAge()) : Integer.compare(p2.getAge(), p1.getAge()));
        return list;
    }

    /**
     * Returns all patients sorted by Registration Date.
     */
    public List<Patient> getPatientsSortedByRegistrationDate(boolean newestFirst) {
        List<Patient> list = new ArrayList<>(patientMap.values());
        list.sort((p1, p2) -> newestFirst ? p2.getRegistrationDate().compareTo(p1.getRegistrationDate()) : p1.getRegistrationDate().compareTo(p2.getRegistrationDate()));
        return list;
    }

    /**
     * Fixed-size Array tracking: Records recent patient IDs in a fixed 5-slot array.
     */
    private void recordRecentPatientId(String id) {
        // Shift elements to the right to keep most recent at index 0
        for (int i = recentPatientIdsArray.length - 1; i > 0; i--) {
            recentPatientIdsArray[i] = recentPatientIdsArray[i - 1];
        }
        recentPatientIdsArray[0] = id;
        if (recentPatientCount < recentPatientIdsArray.length) {
            recentPatientCount++;
        }
    }

    /**
     * Returns the raw fixed array of recent patient IDs.
     */
    public String[] getRecentPatientIdsArray() {
        return Arrays.copyOf(recentPatientIdsArray, recentPatientIdsArray.length);
    }

    public int getPatientCount() {
        return patientMap.size();
    }

    // =============================================================
    // DOCTOR MANAGEMENT (HashMap)
    // =============================================================

    public synchronized String generateDoctorId() {
        return "DOC-" + (nextDoctorSeq++);
    }

    public synchronized void addDoctor(Doctor doctor) {
        if (doctor != null && doctor.getId() != null) {
            doctorMap.put(doctor.getId(), doctor);
        }
    }

    public Doctor getDoctorById(String doctorId) {
        if (doctorId == null) return null;
        return doctorMap.get(doctorId.trim());
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>(doctorMap.values());
        list.sort(Comparator.comparing(Doctor::getName));
        return list;
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        if (specialization == null || specialization.equalsIgnoreCase("All")) {
            return getAllDoctors();
        }
        List<Doctor> filtered = new ArrayList<>();
        for (Doctor doc : doctorMap.values()) {
            if (doc.getSpecialization().equalsIgnoreCase(specialization)) {
                filtered.add(doc);
            }
        }
        return filtered;
    }

    public int getDoctorCount() {
        return doctorMap.size();
    }

    // =============================================================
    // APPOINTMENT MANAGEMENT (LinkedList)
    // =============================================================

    public synchronized String generateAppointmentId() {
        return "APT-" + (nextAppointmentSeq++);
    }

    /**
     * Adds an appointment to the LinkedList.
     */
    public synchronized boolean scheduleAppointment(Appointment appointment) {
        if (appointment == null) return false;
        // LinkedList insertion at the end
        appointmentList.addLast(appointment);
        return true;
    }

    /**
     * Updates appointment status (Scheduled, Completed, Cancelled).
     */
    public synchronized boolean updateAppointmentStatus(String appointmentId, String newStatus) {
        for (Appointment apt : appointmentList) {
            if (apt.getAppointmentId().equals(appointmentId)) {
                apt.setStatus(newStatus);
                return true;
            }
        }
        return false;
    }

    /**
     * Cancels an appointment.
     */
    public synchronized boolean cancelAppointment(String appointmentId) {
        return updateAppointmentStatus(appointmentId, "Cancelled");
    }

    /**
     * Returns a copy of the LinkedList of appointments.
     */
    public LinkedList<Appointment> getAllAppointments() {
        return new LinkedList<>(appointmentList);
    }

    /**
     * Returns appointments for a specific patient.
     */
    public List<Appointment> getAppointmentsByPatientId(String patientId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment apt : appointmentList) {
            if (apt.getPatientId().equalsIgnoreCase(patientId)) {
                list.add(apt);
            }
        }
        return list;
    }

    /**
     * Returns appointments for a specific doctor.
     */
    public List<Appointment> getAppointmentsByDoctorId(String doctorId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment apt : appointmentList) {
            if (apt.getDoctorId().equalsIgnoreCase(doctorId)) {
                list.add(apt);
            }
        }
        return list;
    }

    public int getAppointmentCount() {
        return appointmentList.size();
    }

    public long getTodayAppointmentCount() {
        LocalDate today = LocalDate.now();
        return appointmentList.stream()
            .filter(a -> a.getAppointmentDate().equals(today) && !"Cancelled".equalsIgnoreCase(a.getStatus()))
            .count();
    }

    // =============================================================
    // MEDICAL RECORDS (LinkedList)
    // =============================================================

    public synchronized String generateMedicalRecordId() {
        return "MED-" + (nextRecordSeq++);
    }

    public synchronized boolean addMedicalRecord(MedicalRecord record) {
        if (record == null) return false;
        medicalRecordList.addLast(record);
        return true;
    }

    public LinkedList<MedicalRecord> getAllMedicalRecords() {
        return new LinkedList<>(medicalRecordList);
    }

    public List<MedicalRecord> getMedicalRecordsByPatientId(String patientId) {
        List<MedicalRecord> list = new ArrayList<>();
        for (MedicalRecord rec : medicalRecordList) {
            if (rec.getPatientId().equalsIgnoreCase(patientId)) {
                list.add(rec);
            }
        }
        return list;
    }

    public int getMedicalRecordCount() {
        return medicalRecordList.size();
    }

    // =============================================================
    // BILLING & INVOICING (HashMap)
    // =============================================================

    public synchronized String generateBillId() {
        return "INV-" + (nextBillSeq++);
    }

    public synchronized boolean addBillingRecord(BillingRecord bill) {
        if (bill == null || bill.getBillId() == null) return false;
        billingMap.put(bill.getBillId(), bill);
        return true;
    }

    public BillingRecord getBillById(String billId) {
        if (billId == null) return null;
        return billingMap.get(billId.trim());
    }

    public List<BillingRecord> getAllBills() {
        List<BillingRecord> list = new ArrayList<>(billingMap.values());
        list.sort((b1, b2) -> b2.getBillingDate().compareTo(b1.getBillingDate()));
        return list;
    }

    public List<BillingRecord> getBillsByPatientId(String patientId) {
        List<BillingRecord> list = new ArrayList<>();
        for (BillingRecord bill : billingMap.values()) {
            if (bill.getPatientId().equalsIgnoreCase(patientId)) {
                list.add(bill);
            }
        }
        return list;
    }

    public synchronized boolean updateBillPaymentStatus(String billId, String status, String paymentMethod) {
        BillingRecord bill = billingMap.get(billId);
        if (bill != null) {
            bill.setPaymentStatus(status);
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                bill.setPaymentMethod(paymentMethod);
            }
            return true;
        }
        return false;
    }

    public double getTotalRevenue() {
        double total = 0.0;
        for (BillingRecord b : billingMap.values()) {
            if ("Paid".equalsIgnoreCase(b.getPaymentStatus())) {
                total += b.getTotalAmount();
            }
        }
        return total;
    }

    public double getPendingRevenue() {
        double pending = 0.0;
        for (BillingRecord b : billingMap.values()) {
            if ("Pending".equalsIgnoreCase(b.getPaymentStatus())) {
                pending += b.getTotalAmount();
            }
        }
        return pending;
    }

    // =============================================================
    // INITIAL SAMPLE DATA SEEDING
    // =============================================================

    private void seedInitialData() {
        // 1. Doctors
        Doctor d1 = new Doctor("DOC-201", "Dr. Rajesh Sharma", "Cardiology", "MD, DM (Cardiology)", "+91-98765-43210", "dr.rajesh@hospital.com", 1200.0, "Room 101", "Mon, Wed, Fri");
        Doctor d2 = new Doctor("DOC-202", "Dr. Ananya Iyer", "Neurology", "MBBS, M.Ch (Neuro)", "+91-98765-43211", "dr.ananya@hospital.com", 1500.0, "Room 102", "Tue, Thu, Sat");
        Doctor d3 = new Doctor("DOC-203", "Dr. Vikram Malhotra", "Orthopedics", "MS (Orthopedics)", "+91-98765-43212", "dr.vikram@hospital.com", 1000.0, "Room 201", "Mon, Tue, Thu");
        Doctor d4 = new Doctor("DOC-204", "Dr. Priya Deshmukh", "Pediatrics", "MD (Pediatrics)", "+91-98765-43213", "dr.priya@hospital.com", 900.0, "Room 205", "Daily (Mon-Sat)");
        Doctor d5 = new Doctor("DOC-205", "Dr. Amit Patel", "General Medicine", "MBBS, MD (Medicine)", "+91-98765-43214", "dr.amit@hospital.com", 800.0, "Room 104", "Daily (Mon-Sun)");
        Doctor d6 = new Doctor("DOC-206", "Dr. Sunita Sen", "Dermatology", "MD (Dermatology)", "+91-98765-43215", "dr.sunita@hospital.com", 1100.0, "Room 302", "Wed, Fri, Sat");

        addDoctor(d1);
        addDoctor(d2);
        addDoctor(d3);
        addDoctor(d4);
        addDoctor(d5);
        addDoctor(d6);

        // 2. Patients
        Patient p1 = new Patient("PAT-1001", "Aarav Gupta", 29, "Male", "+91-91234-56780", "O+", "45 Park Avenue, Mumbai", LocalDate.now().minusDays(15), "+91-91234-99990", "120/80", "Penicillin");
        Patient p2 = new Patient("PAT-1002", "Meera Kulkarni", 42, "Female", "+91-91234-56781", "B+", "12 Hill Road, Bandra, Mumbai", LocalDate.now().minusDays(12), "+91-91234-99991", "130/85", "Sulfa drugs");
        Patient p3 = new Patient("PAT-1003", "Rohan Mehta", 56, "Male", "+91-91234-56782", "A+", "78 Linking Road, Mumbai", LocalDate.now().minusDays(10), "+91-91234-99992", "140/90", "None");
        Patient p4 = new Patient("PAT-1004", "Zara Khan", 8, "Female", "+91-91234-56783", "AB+", "88 Sea View, Worli, Mumbai", LocalDate.now().minusDays(8), "+91-91234-99993", "110/70", "Peanuts");
        Patient p5 = new Patient("PAT-1005", "Devendra Joshi", 68, "Male", "+91-91234-56784", "O-", "23 MG Road, Pune", LocalDate.now().minusDays(5), "+91-91234-99994", "135/88", "Aspirin");
        Patient p6 = new Patient("PAT-1006", "Pooja Verma", 34, "Female", "+91-91234-56785", "A-", "102 Lake View, Thane", LocalDate.now().minusDays(2), "+91-91234-99995", "118/78", "None");
        Patient p7 = new Patient("PAT-1007", "Karan Singhania", 24, "Male", "+91-91234-56786", "B-", "15 Palm Beach, Navi Mumbai", LocalDate.now().minusDays(1), "+91-91234-99996", "122/80", "Pollen");

        addPatient(p1);
        addPatient(p2);
        addPatient(p3);
        addPatient(p4);
        addPatient(p5);
        addPatient(p6);
        addPatient(p7);

        // 3. Appointments
        Appointment a1 = new Appointment("APT-5001", "PAT-1001", "Aarav Gupta", "DOC-205", "Dr. Amit Patel", LocalDate.now(), "10:00 AM", "Routine annual health checkup", "Completed");
        Appointment a2 = new Appointment("APT-5002", "PAT-1003", "Rohan Mehta", "DOC-201", "Dr. Rajesh Sharma", LocalDate.now(), "11:30 AM", "Chest tightness and palpitations", "Scheduled");
        Appointment a3 = new Appointment("APT-5003", "PAT-1004", "Zara Khan", "DOC-204", "Dr. Priya Deshmukh", LocalDate.now(), "02:30 PM", "Seasonal fever and persistent cough", "Scheduled");
        Appointment a4 = new Appointment("APT-5004", "PAT-1002", "Meera Kulkarni", "DOC-202", "Dr. Ananya Iyer", LocalDate.now().plusDays(1), "09:30 AM", "Chronic migraine consultations", "Scheduled");
        Appointment a5 = new Appointment("APT-5005", "PAT-1005", "Devendra Joshi", "DOC-203", "Dr. Vikram Malhotra", LocalDate.now().minusDays(3), "03:00 PM", "Severe knee joint pain", "Completed");

        scheduleAppointment(a1);
        scheduleAppointment(a2);
        scheduleAppointment(a3);
        scheduleAppointment(a4);
        scheduleAppointment(a5);

        // 4. Medical Records
        MedicalRecord mr1 = new MedicalRecord("MED-7001", "PAT-1001", "DOC-205", "Dr. Amit Patel", LocalDate.now().minusDays(15), "Mild fatigue, body ache", "Viral pharyngitis", "Paracetamol 650mg, Vitamin C, Hydration", "Complete Blood Count (CBC)", "Advised 3 days rest.");
        MedicalRecord mr2 = new MedicalRecord("MED-7002", "PAT-1003", "DOC-201", "Dr. Rajesh Sharma", LocalDate.now().minusDays(10), "Mild angina on exertion", "Stage 1 Hypertension & Ischemia eval", "Amlodipine 5mg OD, Atorvastatin 10mg", "ECG, Lipid Profile, 2D Echo", "Follow up in 2 weeks.");
        MedicalRecord mr3 = new MedicalRecord("MED-7003", "PAT-1005", "DOC-203", "Dr. Vikram Malhotra", LocalDate.now().minusDays(3), "Bilateral knee swelling", "Osteoarthritis Grade 2", "Glucosamine tab, topical pain relief gel", "X-Ray Both Knees AP/Lat", "Physiotherapy recommended.");

        addMedicalRecord(mr1);
        addMedicalRecord(mr2);
        addMedicalRecord(mr3);

        // 5. Billing Records
        BillingRecord b1 = new BillingRecord("INV-9001", "PAT-1001", "Aarav Gupta", "APT-5001", 800.0, 350.0, 250.0, 0.0, 5.0, 100.0, "Paid", "UPI / Net Banking", LocalDate.now().minusDays(15));
        BillingRecord b2 = new BillingRecord("INV-9002", "PAT-1003", "Rohan Mehta", "APT-5002", 1200.0, 950.0, 450.0, 0.0, 5.0, 0.0, "Pending", "None", LocalDate.now());
        BillingRecord b3 = new BillingRecord("INV-9003", "PAT-1005", "Devendra Joshi", "APT-5005", 1000.0, 600.0, 400.0, 0.0, 5.0, 150.0, "Paid", "Credit Card", LocalDate.now().minusDays(3));

        addBillingRecord(b1);
        addBillingRecord(b2);
        addBillingRecord(b3);
    }
}
