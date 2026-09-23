package hospital.ui;

import hospital.model.Doctor;
import hospital.service.HospitalService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Doctor Directory Panel displaying doctor profiles, specializations,
 * schedules, consultation charges, and doctor registration.
 */
public class DoctorPanel extends JPanel {
    private final HospitalService service = HospitalService.getInstance();
    private final MainFrame mainFrame;

    private DefaultTableModel tableModel;
    private JTable doctorTable;
    private JComboBox<String> cmbSpecializationFilter;

    public DoctorPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
        loadDoctors("All");
    }

    private void initComponents() {
        // Top Header
        add(UIConstants.createHeaderBanner(
            "Medical Staff & Specialist Doctor Directory",
            "Manage hospital physicians, clinical specialties, consultation schedules, and fees"
        ), BorderLayout.NORTH);

        // Center Card Panel
        JPanel card = UIConstants.createCardPanel();
        card.setLayout(new BorderLayout(14, 14));

        // Top Filter & Action Bar
        JPanel toolBar = new JPanel(new BorderLayout(0, 10));
        toolBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter by Specialization:");
        lblFilter.setFont(UIConstants.FONT_BODY_BOLD);

        String[] deptFilterOptions = new String[HospitalService.DEPARTMENTS.length + 1];
        deptFilterOptions[0] = "All";
        System.arraycopy(HospitalService.DEPARTMENTS, 0, deptFilterOptions, 1, HospitalService.DEPARTMENTS.length);

        cmbSpecializationFilter = new JComboBox<>(deptFilterOptions);
        cmbSpecializationFilter.setFont(UIConstants.FONT_BODY);
        cmbSpecializationFilter.addActionListener(e -> {
            String selected = (String) cmbSpecializationFilter.getSelectedItem();
            loadDoctors(selected);
        });

        filterPanel.add(lblFilter);
        filterPanel.add(cmbSpecializationFilter);

        JButton btnAddDoctor = UIConstants.createPrimaryButton("+ Add New Specialist");
        btnAddDoctor.addActionListener(e -> showAddDoctorDialog());

        toolBar.add(filterPanel, BorderLayout.NORTH);
        toolBar.add(btnAddDoctor, BorderLayout.CENTER);
        card.add(toolBar, BorderLayout.NORTH);

        // Doctor Table
        String[] cols = {
            "Doctor ID", "Doctor Name", "Specialization", "Qualification",
            "Consultation Fee (₹)", "Room No", "Available Schedule", "Contact Phone"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        doctorTable = new JTable(tableModel);
        UIConstants.styleTable(doctorTable);

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadDoctors(String specialization) {
        tableModel.setRowCount(0);
        List<Doctor> doctors = service.getDoctorsBySpecialization(specialization);
        for (Doctor d : doctors) {
            tableModel.addRow(new Object[]{
                d.getId(),
                d.getName(),
                d.getSpecialization(),
                d.getQualification(),
                String.format("₹%.2f", d.getConsultationFee()),
                d.getRoomNumber(),
                d.getAvailableDays(),
                d.getContactNumber()
            });
        }
    }

    private void showAddDoctorDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Medical Specialist", true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField txtDocId = new JTextField(service.generateDoctorId());
        txtDocId.setEditable(false);
        txtDocId.setBackground(new Color(241, 245, 249));

        JTextField txtName = new JTextField();
        JComboBox<String> cmbDept = new JComboBox<>(HospitalService.DEPARTMENTS);
        JTextField txtQual = new JTextField("MBBS, MD");
        JTextField txtPhone = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtFee = new JTextField("1000.00");
        JTextField txtRoom = new JTextField("Room 101");
        JTextField txtDays = new JTextField("Mon, Wed, Fri");

        int row = 0;
        addDialogRow(content, gbc, row++, "Doctor ID:", txtDocId);
        addDialogRow(content, gbc, row++, "Doctor Name *:", txtName);
        addDialogRow(content, gbc, row++, "Specialization:", cmbDept);
        addDialogRow(content, gbc, row++, "Qualification:", txtQual);
        addDialogRow(content, gbc, row++, "Contact Phone *:", txtPhone);
        addDialogRow(content, gbc, row++, "Email Address:", txtEmail);
        addDialogRow(content, gbc, row++, "Consultation Fee (₹):", txtFee);
        addDialogRow(content, gbc, row++, "Room No:", txtRoom);
        addDialogRow(content, gbc, row++, "Available Schedule:", txtDays);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancel = UIConstants.createSecondaryButton("Cancel");
        JButton btnSave = UIConstants.createPrimaryButton("Save Doctor");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String feeStr = txtFee.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in Doctor Name and Contact Phone.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double fee = 1000.0;
            try {
                fee = Double.parseDouble(feeStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric fee.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Doctor newDoc = new Doctor(
                txtDocId.getText().trim(),
                name,
                (String) cmbDept.getSelectedItem(),
                txtQual.getText().trim(),
                phone,
                txtEmail.getText().trim(),
                fee,
                txtRoom.getText().trim(),
                txtDays.getText().trim()
            );

            service.addDoctor(newDoc);
            loadDoctors("All");
            mainFrame.refreshDashboard();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Doctor " + name + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDialogRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
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
}
