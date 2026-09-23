package hospital.ui;

import hospital.model.Doctor;
import hospital.model.MedicalRecord;
import hospital.model.Patient;
import hospital.service.HospitalService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * Medical Records Panel providing clinical entry for diagnoses,
 * prescriptions, lab recommendations, and consultation history using LinkedList.
 */
public class MedicalRecordPanel extends JPanel {
    private final HospitalService service = HospitalService.getInstance();
    private final MainFrame mainFrame;

    private DefaultTableModel tableModel;
    private JTable recordTable;

    private JComboBox<PatientItem> cmbPatient;
    private JComboBox<DoctorItem> cmbDoctor;
    private JTextField txtSymptoms;
    private JTextField txtDiagnosis;
    private JTextArea txtPrescription;
    private JTextField txtLabTests;
    private JTextArea txtNotes;

    private JComboBox<String> cmbPatientFilter;

    public MedicalRecordPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
        refreshDropdowns();
        loadRecords(null);
    }

    private void initComponents() {
        // Top Header
        add(UIConstants.createHeaderBanner(
            "Patient Medical Records & Clinical History (LinkedList)",
            "Record diagnoses, prescriptions, lab investigations, and maintain patient health files"
        ), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setOpaque(false);

        // --- LEFT FORM: ADD MEDICAL RECORD ---
        JPanel formCard = UIConstants.createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(400, 580));

        JLabel lblForm = new JLabel("New Clinical Consultation Entry");
        lblForm.setFont(UIConstants.FONT_HEADER);
        lblForm.setForeground(UIConstants.PRIMARY);
        formCard.add(lblForm, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        cmbPatient = new JComboBox<>();
        cmbDoctor = new JComboBox<>();
        txtSymptoms = new JTextField();
        txtDiagnosis = new JTextField();
        txtPrescription = new JTextArea(3, 20);
        txtPrescription.setLineWrap(true);
        txtPrescription.setWrapStyleWord(true);
        JScrollPane rxScroll = new JScrollPane(txtPrescription);
        rxScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));

        txtLabTests = new JTextField("None");
        txtNotes = new JTextArea(2, 20);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(txtNotes);
        notesScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));

        UIConstants.styleTextField(txtSymptoms);
        UIConstants.styleTextField(txtDiagnosis);
        UIConstants.styleTextField(txtLabTests);

        int row = 0;
        addFormRow(grid, gbc, row++, "Patient *:", cmbPatient);
        addFormRow(grid, gbc, row++, "Attending Doctor *:", cmbDoctor);
        addFormRow(grid, gbc, row++, "Chief Symptoms *:", txtSymptoms);
        addFormRow(grid, gbc, row++, "Clinical Diagnosis *:", txtDiagnosis);
        addFormRow(grid, gbc, row++, "Rx / Prescription:", rxScroll);
        addFormRow(grid, gbc, row++, "Lab Tests Advised:", txtLabTests);
        addFormRow(grid, gbc, row++, "Doctor's Notes:", notesScroll);

        formCard.add(grid, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.setOpaque(false);

        JButton btnSave = UIConstants.createPrimaryButton("Save Medical Record");
        btnSave.addActionListener(e -> handleSaveRecord());
        btnRow.add(btnSave);

        formCard.add(btnRow, BorderLayout.SOUTH);
        content.add(formCard, BorderLayout.WEST);

        // --- RIGHT: RECORDS TABLE ---
        JPanel rightCard = UIConstants.createCardPanel();
        rightCard.setLayout(new BorderLayout(12, 12));

        // Filter Bar
        JPanel toolBar = new JPanel(new BorderLayout(0, 10));
        toolBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter by Patient:");
        lblFilter.setFont(UIConstants.FONT_BODY_BOLD);

        cmbPatientFilter = new JComboBox<>();
        cmbPatientFilter.setFont(UIConstants.FONT_BODY);
        cmbPatientFilter.addActionListener(e -> {
            String selected = (String) cmbPatientFilter.getSelectedItem();
            if (selected != null && !selected.equals("All Patients")) {
                int start = selected.indexOf("(");
                int end = selected.indexOf(")");
                if (start != -1 && end != -1) {
                    String pid = selected.substring(start + 1, end);
                    loadRecords(pid);
                    return;
                }
            }
            loadRecords(null);
        });

        filterPanel.add(lblFilter);
        filterPanel.add(cmbPatientFilter);

        JButton btnViewRecord = UIConstants.createButton("View Full Prescription", UIConstants.PURPLE, Color.WHITE);
        btnViewRecord.addActionListener(e -> viewSelectedRecordDetails());

        toolBar.add(filterPanel, BorderLayout.NORTH);
        toolBar.add(btnViewRecord, BorderLayout.CENTER);
        rightCard.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Record ID", "Patient ID", "Attending Doctor", "Visit Date", "Diagnosis", "Prescription Summary"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        recordTable = new JTable(tableModel);
        UIConstants.styleTable(recordTable);
        recordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(recordTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        rightCard.add(scrollPane, BorderLayout.CENTER);

        content.add(rightCard, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    public void refreshDropdowns() {
        cmbPatient.removeAllItems();
        for (Patient p : service.getPatientsSortedByName()) {
            cmbPatient.addItem(new PatientItem(p.getId(), p.getName()));
        }

        cmbDoctor.removeAllItems();
        for (Doctor d : service.getAllDoctors()) {
            cmbDoctor.addItem(new DoctorItem(d.getId(), d.getName()));
        }

        cmbPatientFilter.removeAllItems();
        cmbPatientFilter.addItem("All Patients");
        for (Patient p : service.getPatientsSortedByName()) {
            cmbPatientFilter.addItem(p.getName() + " (" + p.getId() + ")");
        }
    }

    public void loadRecords(String patientIdFilter) {
        tableModel.setRowCount(0);
        LinkedList<MedicalRecord> list = service.getAllMedicalRecords();
        for (MedicalRecord r : list) {
            if (patientIdFilter == null || r.getPatientId().equalsIgnoreCase(patientIdFilter)) {
                tableModel.addRow(new Object[]{
                    r.getRecordId(),
                    r.getPatientId(),
                    r.getDoctorName(),
                    r.getVisitDate(),
                    r.getDiagnosis(),
                    r.getPrescription()
                });
            }
        }
    }

    private void handleSaveRecord() {
        PatientItem patient = (PatientItem) cmbPatient.getSelectedItem();
        DoctorItem doctor = (DoctorItem) cmbDoctor.getSelectedItem();
        String symptoms = txtSymptoms.getText().trim();
        String diagnosis = txtDiagnosis.getText().trim();
        String rx = txtPrescription.getText().trim();
        String lab = txtLabTests.getText().trim();
        String notes = txtNotes.getText().trim();

        if (patient == null || doctor == null || symptoms.isEmpty() || diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in patient, doctor, symptoms, and diagnosis.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String recordId = service.generateMedicalRecordId();
        MedicalRecord record = new MedicalRecord(
            recordId,
            patient.id,
            doctor.id,
            doctor.name,
            LocalDate.now(),
            symptoms,
            diagnosis,
            rx.isEmpty() ? "No prescription entered" : rx,
            lab.isEmpty() ? "None" : lab,
            notes
        );

        service.addMedicalRecord(record);
        loadRecords(null);
        txtSymptoms.setText("");
        txtDiagnosis.setText("");
        txtPrescription.setText("");
        txtLabTests.setText("None");
        txtNotes.setText("");

        JOptionPane.showMessageDialog(this, "Medical Record " + recordId + " recorded successfully for " + patient.name + ".", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewSelectedRecordDetails() {
        int row = recordTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String recId = (String) tableModel.getValueAt(row, 0);
        for (MedicalRecord r : service.getAllMedicalRecords()) {
            if (r.getRecordId().equals(recId)) {
                Patient p = service.getPatientById(r.getPatientId());
                StringBuilder sb = new StringBuilder();
                sb.append("═══════════════════════════════════════════════════\n");
                sb.append("             OFFICIAL CLINICAL RECORD              \n");
                sb.append("═══════════════════════════════════════════════════\n");
                sb.append("Record ID        : ").append(r.getRecordId()).append("\n");
                sb.append("Date of Visit    : ").append(r.getVisitDate()).append("\n");
                sb.append("Patient          : ").append(p != null ? p.getName() : "Unknown").append(" (ID: ").append(r.getPatientId()).append(")\n");
                sb.append("Attending Doctor : ").append(r.getDoctorName()).append("\n");
                sb.append("---------------------------------------------------\n");
                sb.append("Symptoms Reported:\n  ").append(r.getSymptoms()).append("\n\n");
                sb.append("Clinical Diagnosis:\n  ").append(r.getDiagnosis()).append("\n\n");
                sb.append("Prescription (Rx):\n  ").append(r.getPrescription()).append("\n\n");
                sb.append("Lab Tests Advised:\n  ").append(r.getLabTestsRecommended()).append("\n\n");
                sb.append("Clinical Notes:\n  ").append(r.getNotes()).append("\n");
                sb.append("═══════════════════════════════════════════════════\n");

                JTextArea area = new JTextArea(sb.toString(), 18, 45);
                area.setFont(new Font("Monospaced", Font.PLAIN, 12));
                area.setEditable(false);
                JScrollPane scroll = new JScrollPane(area);
                JOptionPane.showMessageDialog(this, scroll, "Clinical Record - " + r.getRecordId(), JOptionPane.PLAIN_MESSAGE);
                return;
            }
        }
    }

    private static class PatientItem {
        final String id;
        final String name;
        PatientItem(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name + " (" + id + ")"; }
    }

    private static class DoctorItem {
        final String id;
        final String name;
        DoctorItem(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
