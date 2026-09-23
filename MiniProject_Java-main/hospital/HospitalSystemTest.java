package hospital;

import hospital.model.*;
import hospital.service.HospitalService;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * Automated Verification & Unit Test Suite for Hospital Patient Management System.
 */
public class HospitalSystemTest {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" HOSPITAL PATIENT MANAGEMENT SYSTEM - TEST SUITE ");
        System.out.println("=================================================");

        HospitalService service = HospitalService.getInstance();
        int passed = 0;
        int failed = 0;

        // Test 1: Seed Data Verification
        try {
            assert service.getPatientCount() >= 7 : "Expected at least 7 initial patients";
            assert service.getDoctorCount() >= 6 : "Expected at least 6 initial doctors";
            assert service.getAppointmentCount() >= 5 : "Expected at least 5 appointments";
            assert service.getMedicalRecordCount() >= 3 : "Expected at least 3 medical records";
            System.out.println("Test 1 Passed: Initial seed data loaded correctly.");
            passed++;
        } catch (AssertionError e) {
            System.err.println("✗ Test 1 Failed: " + e.getMessage());
            failed++;
        }

        // Test 2: HashMap O(1) Search by Patient ID
        try {
            Patient p = service.getPatientById("PAT-1001");
            assert p != null : "PAT-1001 should exist in HashMap";
            assert "Aarav Gupta".equals(p.getName()) : "Patient name mismatch";
            assert "O+".equals(p.getBloodGroup()) : "Blood group mismatch";

            Patient nonExistent = service.getPatientById("PAT-9999");
            assert nonExistent == null : "Non-existent patient should return null";
            System.out.println("Test 2 Passed: HashMap O(1) ID search functioning as expected.");
            passed++;
        } catch (AssertionError e) {
            System.err.println("✗ Test 2 Failed: " + e.getMessage());
            failed++;
        }

        // Test 3: Arrays Verification (Fixed configs & Recent buffer)
        try {
            assert HospitalService.BLOOD_GROUPS.length == 8 : "Blood groups array must have 8 standard types";
            assert HospitalService.DEPARTMENTS.length >= 8 : "Departments array must have at least 8 specialties";
            
            String[] recentIds = service.getRecentPatientIdsArray();
            assert recentIds.length == 5 : "Recent IDs array buffer size must be 5";
            assert recentIds[0] != null : "Most recent patient slot 0 must not be null";
            System.out.println("Test 3 Passed: Arrays for metadata & fixed circular buffer working.");
            passed++;
        } catch (AssertionError e) {
            System.err.println("✗ Test 3 Failed: " + e.getMessage());
            failed++;
        }

        // Test 4: LinkedList Appointment Management
        try {
            String aptId = service.generateAppointmentId();
            Appointment newApt = new Appointment(aptId, "PAT-1002", "Meera Kulkarni", "DOC-201", "Dr. Rajesh Sharma", LocalDate.now().plusDays(2), "11:00 AM", "Cardio follow-up", "Scheduled");
            service.scheduleAppointment(newApt);

            LinkedList<Appointment> aptList = service.getAllAppointments();
            assert aptList.getLast().getAppointmentId().equals(aptId) : "New appointment must be enqueued at end of LinkedList";

            service.updateAppointmentStatus(aptId, "Completed");
            Appointment updated = null;
            for (Appointment a : service.getAllAppointments()) {
                if (a.getAppointmentId().equals(aptId)) {
                    updated = a;
                    break;
                }
            }
            assert updated != null && "Completed".equals(updated.getStatus()) : "Appointment status update failed";
            System.out.println("Test 4 Passed: LinkedList FIFO queue and status update working.");
            passed++;
        } catch (AssertionError e) {
            System.err.println("✗ Test 4 Failed: " + e.getMessage());
            failed++;
        }

        // Test 5: TreeMap Sorted Retrieval
        try {
            List<Patient> sortedByName = service.getPatientsSortedByName();
            assert !sortedByName.isEmpty() : "Sorted list should not be empty";
            for (int i = 0; i < sortedByName.size() - 1; i++) {
                String name1 = sortedByName.get(i).getName();
                String name2 = sortedByName.get(i + 1).getName();
                assert name1.compareToIgnoreCase(name2) <= 0 : "Patients must be sorted alphabetically by TreeMap";
            }

            List<Patient> sortedById = service.getPatientsSortedById();
            for (int i = 0; i < sortedById.size() - 1; i++) {
                String id1 = sortedById.get(i).getId();
                String id2 = sortedById.get(i + 1).getId();
                assert id1.compareTo(id2) <= 0 : "Patients must be sorted by ID in TreeMap";
            }
            System.out.println("Test 5 Passed: TreeMap sorted natural order and name order verified.");
            passed++;
        } catch (AssertionError e) {
            System.err.println("✗ Test 5 Failed: " + e.getMessage());
            failed++;
        }

        // Test 6: Billing Calculation & HashMap Retrieval
        try {
            BillingRecord bill = new BillingRecord(
                    "INV-TEST", "PAT-TEST", "Test Patient", "APT-TEST",
                    1000.0, 500.0, 500.0, 0.0, 5.0, 100.0,
                    "Pending", "None", LocalDate.now()
            );
            assert Math.abs(bill.getTotalAmount() - 2000.0) < 0.001 : "Billing calculation mismatch: expected 2000.0, got " + bill.getTotalAmount();

            service.addBillingRecord(bill);
            BillingRecord retrieved = service.getBillById("INV-TEST");
            assert retrieved != null : "Bill must be retrievable from HashMap by ID";

            service.updateBillPaymentStatus("INV-TEST", "Paid", "Credit Card");
            assert "Paid".equals(retrieved.getPaymentStatus()) : "Payment status should be Paid";
            assert "Credit Card".equals(retrieved.getPaymentMethod()) : "Payment method should be Credit Card";
            System.out.println("Test 6 Passed: Billing calculations, tax, discount & HashMap lookup verified.");
            passed++;
        } catch (AssertionError e) {
            System.err.println("✗ Test 6 Failed: " + e.getMessage());
            failed++;
        }

        // Test 7: Patient Update & Deletion CRUD
        try {
            String tempId = service.generatePatientId();
            Patient temp = new Patient(tempId, "Test Temp", 25, "Other", "+91-00000-00000", "AB-", "Temp Addr", LocalDate.now(), "+91-00000-00001", "120/80", "None");
            service.addPatient(temp);
            assert service.getPatientById(tempId) != null : "Temp patient should exist";

            temp.setName("Test Temp Updated");
            service.updatePatient(temp);
            assert "Test Temp Updated".equals(service.getPatientById(tempId).getName()) : "Patient name update failed";

            service.deletePatient(tempId);
            assert service.getPatientById(tempId) == null : "Patient should be deleted from HashMap";
            System.out.println("Test 7 Passed: Patient CRUD (Add, Update, Delete) across collections verified.");
            passed++;
        } catch (AssertionError e) {
            System.err.println("✗ Test 7 Failed: " + e.getMessage());
            failed++;
        }

        System.out.println("=================================================");
        System.out.println("TEST RESULTS: " + passed + " Passed, " + failed + " Failed.");
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
