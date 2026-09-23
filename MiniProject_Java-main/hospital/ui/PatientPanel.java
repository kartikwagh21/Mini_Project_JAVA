package hospital.ui;

import hospital.model.Appointment;
import hospital.model.MedicalRecord;
import hospital.model.Patient;
import hospital.service.HospitalService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Patient Management Panel providing full CRUD operations,
 * instant HashMap O(1) search by ID, and patient record inspection.
 */
public class PatientPanel extends JPanel {
    private final HospitalService service = HospitalService.getInstance();
    private final MainFrame mainFrame;

    private DefaultTableModel tableModel;
    private JTable patientTable;
    private JTextField txtSearch;

    // Form fields
    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtAge;
    private JComboBox<String> cmbGender;
    private JComboBox<String> cmbBloodGroup;
    private JTextField txtContact;
    private JTextField txtEmergencyContact;
    private JTextField txtBloodPressure;
    private JTextField txtAllergies;
    private JTextArea txtAddress;

    private JButton btnSave;
    private JButton btnClear;
    private JButton btnDelete;
    private JButton btnViewDetails;

    private boolean isEditMode = false;

    public PatientPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
        loadPatients(null);
    }

    private void initComponents() {
        // Header Banner
        add(UIConstants.createHeaderBanner(
            "Patient Registration & Records Management",
            "Maintain patient demographics, medical identifiers, and fast HashMap search"
        ), BorderLayout.NORTH);

        // Main Split Pane: Left Form, Right Table & Search
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        // --- LEFT: REGISTRATION / EDIT FORM ---
        JPanel formCard = UIConstants.createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(380, 600));

        JLabel formTitle = new JLabel("Patient Details Form");
        formTitle.setFont(UIConstants.FONT_HEADER);
        formTitle.setForeground(UIConstants.PRIMARY);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));
        UIConstants.styleTextField(txtId);

        txtName = new JTextField();
        UIConstants.styleTextField(txtName);

        txtAge = new JTextField();
        UIConstants.styleTextField(txtAge);

        cmbGender = new JComboBox<>(HospitalService.GENDERS);
        cmbGender.setFont(UIConstants.FONT_BODY);

        // Using the String[] BLOOD_GROUPS array for dropdown
        cmbBloodGroup = new JComboBox<>(HospitalService.BLOOD_GROUPS);
        cmbBloodGroup.setFont(UIConstants.FONT_BODY);

        txtContact = new JTextField();
        UIConstants.styleTextField(txtContact);

        txtEmergencyContact = new JTextField();
        UIConstants.styleTextField(txtEmergencyContact);

        txtBloodPressure = new JTextField("120/80");
        UIConstants.styleTextField(txtBloodPressure);

        txtAllergies = new JTextField("None");
        UIConstants.styleTextField(txtAllergies);

        txtAddress = new JTextArea(3, 20);
        txtAddress.setFont(UIConstants.FONT_BODY);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(txtAddress);
        addressScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));

        int row = 0;
        addFormRow(formGrid, gbc, row++, "Patient ID:", txtId);
        addFormRow(formGrid, gbc, row++, "Full Name *:", txtName);
        addFormRow(formGrid, gbc, row++, "Age *:", txtAge);
        addFormRow(formGrid, gbc, row++, "Gender *:", cmbGender);
        addFormRow(formGrid, gbc, row++, "Blood Group:", cmbBloodGroup);
        addFormRow(formGrid, gbc, row++, "Contact Phone *:", txtContact);
        addFormRow(formGrid, gbc, row++, "Emergency Contact:", txtEmergencyContact);
        addFormRow(formGrid, gbc, row++, "Blood Pressure:", txtBloodPressure);
        addFormRow(formGrid, gbc, row++, "Known Allergies:", txtAllergies);
        addFormRow(formGrid, gbc, row++, "Address:", addressScroll);

        formCard.add(formGrid, BorderLayout.CENTER);

        // Form Buttons
        JPanel formBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        formBtnRow.setOpaque(false);

        btnClear = UIConstants.createSecondaryButton("Clear / New");
        btnSave = UIConstants.createPrimaryButton("Save Patient");

        btnClear.addActionListener(e -> resetForm());
        btnSave.addActionListener(e -> handleSavePatient());

        formBtnRow.add(btnClear);
        formBtnRow.add(btnSave);
        formCard.add(formBtnRow, BorderLayout.SOUTH);

        contentPanel.add(formCard, BorderLayout.WEST);

        // --- RIGHT: SEARCH TOOLBAR & PATIENTS TABLE ---
        JPanel rightCard = UIConstants.createCardPanel();
        rightCard.setLayout(new BorderLayout(12, 12));

        // Search and action header
        JPanel searchBar = new JPanel(new BorderLayout(10, 10));
        searchBar.setOpaque(false);

        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchInputPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Fast Search:");
        lblSearch.setFont(UIConstants.FONT_BODY_BOLD);

        txtSearch = new JTextField(18);
        UIConstants.styleTextField(txtSearch);
        txtSearch.setToolTipText("Enter Patient ID (e.g. PAT-1001) or Name");

        JButton btnSearch = UIConstants.createPrimaryButton("Search");
        JButton btnResetSearch = UIConstants.createSecondaryButton("Show All");

        btnSearch.addActionListener(e -> performSearch());
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadPatients(null);
        });
        txtSearch.addActionListener(e -> performSearch());

        searchInputPanel.add(lblSearch);
        searchInputPanel.add(txtSearch);
        searchInputPanel.add(btnSearch);
        searchInputPanel.add(btnResetSearch);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        btnViewDetails = UIConstants.createButton("View Full Profile", UIConstants.PURPLE, Color.WHITE);
        btnDelete = UIConstants.createDangerButton("Delete Selected");

        btnViewDetails.addActionListener(e -> viewSelectedPatientDetails());
        btnDelete.addActionListener(e -> handleDeletePatient());

        tableActions.add(btnViewDetails);
        tableActions.add(btnDelete);

        searchBar.add(searchInputPanel, BorderLayout.NORTH);
        searchBar.add(tableActions, BorderLayout.CENTER);
        rightCard.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Patient ID", "Name", "Age", "Gender", "Blood Group", "Contact", "Reg. Date", "BP"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        patientTable = new JTable(tableModel);
        UIConstants.styleTable(patientTable);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane tableScroll = new JScrollPane(patientTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        rightCard.add(tableScroll, BorderLayout.CENTER);

        contentPanel.add(rightCard, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        resetForm();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        lbl.setForeground(UIConstants.TEXT_MAIN);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void resetForm() {
        isEditMode = false;
        txtId.setText(service.generatePatientId());
        txtName.setText("");
        txtAge.setText("");
        cmbGender.setSelectedIndex(0);
        cmbBloodGroup.setSelectedIndex(0);
        txtContact.setText("");
        txtEmergencyContact.setText("");
        txtBloodPressure.setText("120/80");
        txtAllergies.setText("None");
        txtAddress.setText("");
        btnSave.setText("Register Patient");
        patientTable.clearSelection();
    }

    private void populateFormFromSelectedRow() {
        int row = patientTable.getSelectedRow();
        if (row >= 0) {
            String patientId = (String) tableModel.getValueAt(row, 0);
            // O(1) HashMap lookup
            Patient patient = service.getPatientById(patientId);
            if (patient != null) {
                isEditMode = true;
                txtId.setText(patient.getId());
                txtName.setText(patient.getName());
                txtAge.setText(String.valueOf(patient.getAge()));
                cmbGender.setSelectedItem(patient.getGender());
                cmbBloodGroup.setSelectedItem(patient.getBloodGroup());
                txtContact.setText(patient.getContactNumber());
                txtEmergencyContact.setText(patient.getEmergencyContact());
                txtBloodPressure.setText(patient.getBloodPressure());
                txtAllergies.setText(patient.getAllergies());
                txtAddress.setText(patient.getAddress());
                btnSave.setText("Update Patient");
            }
        }
    }

    private void handleSavePatient() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String ageStr = txtAge.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String blood = (String) cmbBloodGroup.getSelectedItem();
        String contact = txtContact.getText().trim();
        String emContact = txtEmergencyContact.getText().trim();
        String bp = txtBloodPressure.getText().trim();
        String allergies = txtAllergies.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all mandatory fields (Name, Age, Contact).",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 0 || age > 130) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid age between 0 and 130.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isEditMode) {
            Patient existing = service.getPatientById(id);
            if (existing != null) {
                existing.setName(name);
                existing.setAge(age);
                existing.setGender(gender);
                existing.setBloodGroup(blood);
                existing.setContactNumber(contact);
                existing.setEmergencyContact(emContact);
                existing.setBloodPressure(bp);
                existing.setAllergies(allergies);
                existing.setAddress(address);

                service.updatePatient(existing);
                JOptionPane.showMessageDialog(this, "Patient " + id + " successfully updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            Patient newPatient = new Patient(id, name, age, gender, blood, contact, address, LocalDate.now(), emContact, bp, allergies);
            service.addPatient(newPatient);
            JOptionPane.showMessageDialog(this, "Patient " + id + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }

        loadPatients(null);
        resetForm();
        mainFrame.refreshDashboard();
    }

    private void handleDeletePatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patientId = (String) tableModel.getValueAt(selectedRow, 0);
        String patientName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete patient: " + patientName + " (" + patientId + ")?\nThis will permanently remove their records.",
            "Confirm Patient Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            service.deletePatient(patientId);
            loadPatients(null);
            resetForm();
            mainFrame.refreshDashboard();
            JOptionPane.showMessageDialog(this, "Patient record removed.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        // If exact ID lookup, demonstrate HashMap O(1) direct retrieval
        if (query.toUpperCase().startsWith("PAT-")) {
            Patient p = service.getPatientById(query.toUpperCase());
            if (p != null) {
                loadPatients(List.of(p));
                return;
            }
        }
        List<Patient> results = service.searchPatients(query);
        loadPatients(results);
    }

    public void loadPatients(List<Patient> patientList) {
        tableModel.setRowCount(0);
        List<Patient> list = patientList != null ? patientList : service.getPatientsSortedById();
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getAge(),
                p.getGender(),
                p.getBloodGroup(),
                p.getContactNumber(),
                p.getRegistrationDate(),
                p.getBloodPressure()
            });
        }
    }

    private void viewSelectedPatientDetails() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient to view full history.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patientId = (String) tableModel.getValueAt(selectedRow, 0);
        Patient patient = service.getPatientById(patientId);
        if (patient == null) return;

        List<Appointment> apts = service.getAppointmentsByPatientId(patientId);
        List<MedicalRecord> recs = service.getMedicalRecordsByPatientId(patientId);

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("         PATIENT MEDICAL PROFILE & DOSSIER        \n");
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("Patient ID       : ").append(patient.getId()).append("\n");
        sb.append("Full Name        : ").append(patient.getName()).append("\n");
        sb.append("Age / Gender     : ").append(patient.getAge()).append(" years / ").append(patient.getGender()).append("\n");
        sb.append("Blood Group      : ").append(patient.getBloodGroup()).append("\n");
        sb.append("Contact Phone    : ").append(patient.getContactNumber()).append("\n");
        sb.append("Emergency Contact: ").append(patient.getEmergencyContact()).append("\n");
        sb.append("Registered Date  : ").append(patient.getRegistrationDate()).append("\n");
        sb.append("Vitals (BP)      : ").append(patient.getBloodPressure()).append("\n");
        sb.append("Known Allergies  : ").append(patient.getAllergies()).append("\n");
        sb.append("Address          : ").append(patient.getAddress()).append("\n\n");

        sb.append("─── APPOINTMENT HISTORY ───\n");
        if (apts.isEmpty()) {
            sb.append("  No appointment history recorded.\n");
        } else {
            for (Appointment a : apts) {
                sb.append("  • ").append(a.getAppointmentId()).append(" | ").append(a.getAppointmentDate())
                  .append(" (").append(a.getTimeSlot()).append(") | Doctor: ").append(a.getDoctorName())
                  .append(" | Status: ").append(a.getStatus()).append("\n    Reason: ").append(a.getReason()).append("\n");
            }
        }

        sb.append("\n─── CLINICAL MEDICAL RECORDS ───\n");
        if (recs.isEmpty()) {
            sb.append("  No clinical records recorded.\n");
        } else {
            for (MedicalRecord r : recs) {
                sb.append("  • Record ").append(r.getRecordId()).append(" [").append(r.getVisitDate()).append("] - ").append(r.getDoctorName()).append("\n")
                  .append("    Diagnosis: ").append(r.getDiagnosis()).append("\n")
                  .append("    Rx: ").append(r.getPrescription()).append("\n")
                  .append("    Notes: ").append(r.getNotes()).append("\n");
            }
        }
        sb.append("═══════════════════════════════════════════════════\n");

        JTextArea area = new JTextArea(sb.toString(), 22, 50);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);

        JOptionPane.showMessageDialog(this, scroll, "Patient Dossier - " + patient.getName(), JOptionPane.PLAIN_MESSAGE);
    }
}
