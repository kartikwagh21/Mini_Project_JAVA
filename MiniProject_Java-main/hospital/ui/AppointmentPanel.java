package hospital.ui;

import hospital.model.Appointment;
import hospital.model.Doctor;
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
 * Appointment Management Panel utilizing LinkedList for maintaining the
 * sequential queue and history of consultations.
 */
public class AppointmentPanel extends JPanel {
    private final HospitalService service = HospitalService.getInstance();
    private final MainFrame mainFrame;

    private DefaultTableModel tableModel;
    private JTable appointmentTable;

    // Booking Form Components
    private JComboBox<PatientItem> cmbPatients;
    private JComboBox<DoctorItem> cmbDoctors;
    private JComboBox<String> cmbTimeSlots;
    private JTextField txtReason;
    private JTextField txtDaysOffset; // 0 for today, 1 for tomorrow, etc.

    // Filters
    private JComboBox<String> cmbStatusFilter;

    public AppointmentPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
        refreshDropdowns();
        loadAppointments();
    }

    private void initComponents() {
        // Header
        add(UIConstants.createHeaderBanner(
            "Patient Appointment Management (LinkedList Queue)",
            "Schedule consultations, maintain FIFO queue order, and update visit statuses"
        ), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setOpaque(false);

        // --- LEFT: APPOINTMENT BOOKING FORM ---
        JPanel formCard = UIConstants.createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(380, 550));

        JLabel lblFormTitle = new JLabel("Schedule Consultation");
        lblFormTitle.setFont(UIConstants.FONT_HEADER);
        lblFormTitle.setForeground(UIConstants.PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        cmbPatients = new JComboBox<>();
        cmbPatients.setFont(UIConstants.FONT_BODY);

        cmbDoctors = new JComboBox<>();
        cmbDoctors.setFont(UIConstants.FONT_BODY);

        // Uses Array of time slots
        cmbTimeSlots = new JComboBox<>(HospitalService.APPOINTMENT_TIME_SLOTS);
        cmbTimeSlots.setFont(UIConstants.FONT_BODY);

        txtDaysOffset = new JTextField("0"); // 0 = Today
        UIConstants.styleTextField(txtDaysOffset);
        txtDaysOffset.setToolTipText("Enter 0 for Today, 1 for Tomorrow, 2 for 2 days later...");

        txtReason = new JTextField();
        UIConstants.styleTextField(txtReason);

        int row = 0;
        addFormRow(formGrid, gbc, row++, "Select Patient *:", cmbPatients);
        addFormRow(formGrid, gbc, row++, "Assign Doctor *:", cmbDoctors);
        addFormRow(formGrid, gbc, row++, "Time Slot:", cmbTimeSlots);
        addFormRow(formGrid, gbc, row++, "Days from Today:", txtDaysOffset);
        addFormRow(formGrid, gbc, row++, "Visit Reason / Chief Complaint:", txtReason);

        formCard.add(formGrid, BorderLayout.CENTER);

        JPanel formBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        formBtnRow.setOpaque(false);

        JButton btnBook = UIConstants.createPrimaryButton("Book Appointment (Enqueue)");
        btnBook.addActionListener(e -> handleBookAppointment());
        formBtnRow.add(btnBook);

        formCard.add(formBtnRow, BorderLayout.SOUTH);
        content.add(formCard, BorderLayout.WEST);

        // --- RIGHT: APPOINTMENTS QUEUE TABLE ---
        JPanel rightCard = UIConstants.createCardPanel();
        rightCard.setLayout(new BorderLayout(12, 12));

        // Filter and Action Bar
        JPanel toolBar = new JPanel(new BorderLayout(0, 10));
        toolBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Status:");
        lblFilter.setFont(UIConstants.FONT_BODY_BOLD);

        cmbStatusFilter = new JComboBox<>(new String[]{"All Statuses", "Scheduled", "In Progress", "Completed", "Cancelled"});
        cmbStatusFilter.setFont(UIConstants.FONT_BODY);
        cmbStatusFilter.addActionListener(e -> loadAppointments());

        filterPanel.add(lblFilter);
        filterPanel.add(cmbStatusFilter);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton btnComplete = UIConstants.createSuccessButton("Mark Completed");
        JButton btnCancel = UIConstants.createDangerButton("Cancel Appointment");
        JButton btnRefresh = UIConstants.createSecondaryButton("Refresh Queue");

        btnComplete.addActionListener(e -> updateStatusOfSelected("Completed"));
        btnCancel.addActionListener(e -> updateStatusOfSelected("Cancelled"));
        btnRefresh.addActionListener(e -> {
            refreshDropdowns();
            loadAppointments();
        });

        actionBtns.add(btnComplete);
        actionBtns.add(btnCancel);
        actionBtns.add(btnRefresh);

        toolBar.add(filterPanel, BorderLayout.NORTH);
        toolBar.add(actionBtns, BorderLayout.CENTER);
        rightCard.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Queue #", "Apt ID", "Patient Name", "Doctor", "Date", "Slot", "Status", "Reason"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        appointmentTable = new JTable(tableModel);
        UIConstants.styleTable(appointmentTable);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
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
        // Refresh Patients
        cmbPatients.removeAllItems();
        for (Patient p : service.getPatientsSortedByName()) {
            cmbPatients.addItem(new PatientItem(p.getId(), p.getName()));
        }

        // Refresh Doctors
        cmbDoctors.removeAllItems();
        for (Doctor d : service.getAllDoctors()) {
            cmbDoctors.addItem(new DoctorItem(d.getId(), d.getName(), d.getSpecialization()));
        }
    }

    public void loadAppointments() {
        tableModel.setRowCount(0);
        String filter = (String) cmbStatusFilter.getSelectedItem();
        LinkedList<Appointment> list = service.getAllAppointments();

        int queueIndex = 1;
        for (Appointment apt : list) {
            if (filter == null || filter.equals("All Statuses") || apt.getStatus().equalsIgnoreCase(filter)) {
                tableModel.addRow(new Object[]{
                    "#" + queueIndex,
                    apt.getAppointmentId(),
                    apt.getPatientName() + " (" + apt.getPatientId() + ")",
                    apt.getDoctorName(),
                    apt.getAppointmentDate(),
                    apt.getTimeSlot(),
                    apt.getStatus(),
                    apt.getReason()
                });
            }
            queueIndex++;
        }
    }

    private void handleBookAppointment() {
        PatientItem patient = (PatientItem) cmbPatients.getSelectedItem();
        DoctorItem doctor = (DoctorItem) cmbDoctors.getSelectedItem();
        String timeSlot = (String) cmbTimeSlots.getSelectedItem();
        String offsetStr = txtDaysOffset.getText().trim();
        String reason = txtReason.getText().trim();

        if (patient == null || doctor == null) {
            JOptionPane.showMessageDialog(this, "Please ensure at least one patient and one doctor exist.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (reason.isEmpty()) {
            reason = "General Consultation";
        }

        int offsetDays = 0;
        try {
            offsetDays = Integer.parseInt(offsetStr);
        } catch (NumberFormatException ex) {
            offsetDays = 0;
        }

        LocalDate aptDate = LocalDate.now().plusDays(offsetDays);
        String aptId = service.generateAppointmentId();

        Appointment appointment = new Appointment(
            aptId,
            patient.id,
            patient.name,
            doctor.id,
            doctor.name,
            aptDate,
            timeSlot,
            reason,
            "Scheduled"
        );

        service.scheduleAppointment(appointment);
        loadAppointments();
        txtReason.setText("");
        mainFrame.refreshDashboard();

        JOptionPane.showMessageDialog(this,
            "Appointment " + aptId + " successfully scheduled for " + patient.name + " with " + doctor.name + "!",
            "Appointment Booked", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatusOfSelected(String status) {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the queue.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aptId = (String) tableModel.getValueAt(row, 1);
        service.updateAppointmentStatus(aptId, status);
        loadAppointments();
        mainFrame.refreshDashboard();
        JOptionPane.showMessageDialog(this, "Appointment " + aptId + " marked as " + status + ".", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    // Helper wrapper classes for Comboboxes
    private static class PatientItem {
        final String id;
        final String name;

        PatientItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (" + id + ")";
        }
    }

    private static class DoctorItem {
        final String id;
        final String name;
        final String spec;

        DoctorItem(String id, String name, String spec) {
            this.id = id;
            this.name = name;
            this.spec = spec;
        }

        @Override
        public String toString() {
            return name + " - " + spec;
        }
    }
}
