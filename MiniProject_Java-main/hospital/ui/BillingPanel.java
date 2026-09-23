package hospital.ui;

import hospital.model.BillingRecord;
import hospital.model.Patient;
import hospital.service.HospitalService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Hospital Billing & Invoicing Panel for generating patient invoices,
 * tracking payment statuses, itemized revenue calculations, and fast HashMap lookup.
 */
public class BillingPanel extends JPanel {
    private final HospitalService service = HospitalService.getInstance();
    private final MainFrame mainFrame;

    private DefaultTableModel tableModel;
    private JTable billingTable;

    // Form inputs
    private JComboBox<PatientItem> cmbPatient;
    private JTextField txtConsultFee;
    private JTextField txtLabFee;
    private JTextField txtMedicineFee;
    private JTextField txtRoomFee;
    private JTextField txtTaxPercent;
    private JTextField txtDiscount;
    private JLabel lblCalculatedTotal;
    private JComboBox<String> cmbPaymentMethod;
    private JComboBox<String> cmbPaymentStatus;

    // Filters
    private JComboBox<String> cmbStatusFilter;

    public BillingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
        refreshPatientsDropdown();
        loadBills();
    }

    private void initComponents() {
        // Header
        add(UIConstants.createHeaderBanner(
            "Hospital Billing & Financial Invoicing (HashMap Lookup)",
            "Generate itemized hospital bills, process patient payments, and track revenue"
        ), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setOpaque(false);

        // --- LEFT FORM: CREATE BILL ---
        JPanel formCard = UIConstants.createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setPreferredSize(new Dimension(380, 580));

        JLabel lblFormTitle = new JLabel("Generate Patient Invoice");
        lblFormTitle.setFont(UIConstants.FONT_HEADER);
        lblFormTitle.setForeground(UIConstants.PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        cmbPatient = new JComboBox<>();
        cmbPatient.setFont(UIConstants.FONT_BODY);

        txtConsultFee = new JTextField("1000.00");
        txtLabFee = new JTextField("0.00");
        txtMedicineFee = new JTextField("0.00");
        txtRoomFee = new JTextField("0.00");
        txtTaxPercent = new JTextField("5.0");
        txtDiscount = new JTextField("0.00");

        UIConstants.styleTextField(txtConsultFee);
        UIConstants.styleTextField(txtLabFee);
        UIConstants.styleTextField(txtMedicineFee);
        UIConstants.styleTextField(txtRoomFee);
        UIConstants.styleTextField(txtTaxPercent);
        UIConstants.styleTextField(txtDiscount);

        // Real-time calculation listener
        java.awt.event.KeyAdapter calcListener = new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateLiveTotal();
            }
        };
        txtConsultFee.addKeyListener(calcListener);
        txtLabFee.addKeyListener(calcListener);
        txtMedicineFee.addKeyListener(calcListener);
        txtRoomFee.addKeyListener(calcListener);
        txtTaxPercent.addKeyListener(calcListener);
        txtDiscount.addKeyListener(calcListener);

        lblCalculatedTotal = new JLabel("₹1050.00");
        lblCalculatedTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCalculatedTotal.setForeground(UIConstants.SUCCESS);

        // Uses Array of payment methods
        cmbPaymentMethod = new JComboBox<>(HospitalService.PAYMENT_METHODS);
        cmbPaymentMethod.setFont(UIConstants.FONT_BODY);

        cmbPaymentStatus = new JComboBox<>(new String[]{"Pending", "Paid"});
        cmbPaymentStatus.setFont(UIConstants.FONT_BODY);

        int row = 0;
        addFormRow(formGrid, gbc, row++, "Patient *:", cmbPatient);
        addFormRow(formGrid, gbc, row++, "Doctor Consultation (₹):", txtConsultFee);
        addFormRow(formGrid, gbc, row++, "Lab / Diagnostic (₹):", txtLabFee);
        addFormRow(formGrid, gbc, row++, "Pharmacy / Meds (₹):", txtMedicineFee);
        addFormRow(formGrid, gbc, row++, "Room / Ward Charges (₹):", txtRoomFee);
        addFormRow(formGrid, gbc, row++, "Tax Rate (%):", txtTaxPercent);
        addFormRow(formGrid, gbc, row++, "Discount (₹):", txtDiscount);
        addFormRow(formGrid, gbc, row++, "Net Payable Total:", lblCalculatedTotal);
        addFormRow(formGrid, gbc, row++, "Payment Method:", cmbPaymentMethod);
        addFormRow(formGrid, gbc, row++, "Payment Status:", cmbPaymentStatus);

        formCard.add(formGrid, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.setOpaque(false);

        JButton btnGenerate = UIConstants.createSuccessButton("Generate Invoice");
        btnGenerate.addActionListener(e -> handleGenerateInvoice());
        btnRow.add(btnGenerate);

        formCard.add(btnRow, BorderLayout.SOUTH);
        content.add(formCard, BorderLayout.WEST);

        // --- RIGHT: INVOICE TABLE ---
        JPanel rightCard = UIConstants.createCardPanel();
        rightCard.setLayout(new BorderLayout(12, 12));

        JPanel toolBar = new JPanel(new BorderLayout(0, 10));
        toolBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Status:");
        lblFilter.setFont(UIConstants.FONT_BODY_BOLD);

        cmbStatusFilter = new JComboBox<>(new String[]{"All Bills", "Pending", "Paid"});
        cmbStatusFilter.setFont(UIConstants.FONT_BODY);
        cmbStatusFilter.addActionListener(e -> loadBills());

        filterPanel.add(lblFilter);
        filterPanel.add(cmbStatusFilter);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        JButton btnMarkPaid = UIConstants.createSuccessButton("Mark as Paid");
        JButton btnViewInvoice = UIConstants.createButton("Print / View Invoice", UIConstants.PURPLE, Color.WHITE);

        btnMarkPaid.addActionListener(e -> handleMarkPaid());
        btnViewInvoice.addActionListener(e -> viewSelectedInvoice());

        actionBtns.add(btnMarkPaid);
        actionBtns.add(btnViewInvoice);

        toolBar.add(filterPanel, BorderLayout.NORTH);
        toolBar.add(actionBtns, BorderLayout.CENTER);
        rightCard.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Bill ID", "Patient Name", "Consult Fee", "Lab Fee", "Meds Fee", "Total (₹)", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        billingTable = new JTable(tableModel);
        UIConstants.styleTable(billingTable);
        billingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(billingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        rightCard.add(scrollPane, BorderLayout.CENTER);

        content.add(rightCard, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        updateLiveTotal();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(comp, gbc);
    }

    private void updateLiveTotal() {
        try {
            double c = parseDouble(txtConsultFee.getText());
            double l = parseDouble(txtLabFee.getText());
            double m = parseDouble(txtMedicineFee.getText());
            double r = parseDouble(txtRoomFee.getText());
            double tax = parseDouble(txtTaxPercent.getText());
            double disc = parseDouble(txtDiscount.getText());

            double sub = c + l + m + r;
            double taxAmt = sub * (tax / 100.0);
            double total = Math.max(0.0, (sub + taxAmt) - disc);

            lblCalculatedTotal.setText(String.format("₹%.2f", total));
        } catch (Exception ignored) {
            lblCalculatedTotal.setText("₹0.00");
        }
    }

    private double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void refreshPatientsDropdown() {
        cmbPatient.removeAllItems();
        for (Patient p : service.getPatientsSortedByName()) {
            cmbPatient.addItem(new PatientItem(p.getId(), p.getName()));
        }
    }

    public void loadBills() {
        tableModel.setRowCount(0);
        String filter = (String) cmbStatusFilter.getSelectedItem();
        List<BillingRecord> list = service.getAllBills();
        for (BillingRecord b : list) {
            if (filter == null || filter.equals("All Bills") || b.getPaymentStatus().equalsIgnoreCase(filter)) {
                tableModel.addRow(new Object[]{
                    b.getBillId(),
                    b.getPatientName() + " (" + b.getPatientId() + ")",
                    String.format("₹%.2f", b.getConsultationFee()),
                    String.format("₹%.2f", b.getLabCharges()),
                    String.format("₹%.2f", b.getMedicineCharges()),
                    String.format("₹%.2f", b.getTotalAmount()),
                    b.getPaymentStatus(),
                    b.getBillingDate()
                });
            }
        }
    }

    private void handleGenerateInvoice() {
        PatientItem patient = (PatientItem) cmbPatient.getSelectedItem();
        if (patient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double c = parseDouble(txtConsultFee.getText());
        double l = parseDouble(txtLabFee.getText());
        double m = parseDouble(txtMedicineFee.getText());
        double r = parseDouble(txtRoomFee.getText());
        double tax = parseDouble(txtTaxPercent.getText());
        double disc = parseDouble(txtDiscount.getText());
        String status = (String) cmbPaymentStatus.getSelectedItem();
        String method = (String) cmbPaymentMethod.getSelectedItem();

        String billId = service.generateBillId();
        BillingRecord bill = new BillingRecord(
            billId,
            patient.id,
            patient.name,
            "APT-N/A",
            c, l, m, r, tax, disc,
            status,
            method,
            LocalDate.now()
        );

        service.addBillingRecord(bill);
        loadBills();
        mainFrame.refreshDashboard();

        JOptionPane.showMessageDialog(this,
            "Invoice " + billId + " for ₹" + String.format("%.2f", bill.getTotalAmount()) + " generated successfully!",
            "Invoice Created", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleMarkPaid() {
        int row = billingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = (String) tableModel.getValueAt(row, 0);
        BillingRecord b = service.getBillById(billId);
        if (b != null) {
            if ("Paid".equalsIgnoreCase(b.getPaymentStatus())) {
                JOptionPane.showMessageDialog(this, "This bill is already marked as Paid.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String method = (String) JOptionPane.showInputDialog(
                this,
                "Select Payment Method:",
                "Process Payment - " + billId,
                JOptionPane.QUESTION_MESSAGE,
                null,
                HospitalService.PAYMENT_METHODS,
                HospitalService.PAYMENT_METHODS[0]
            );

            if (method != null) {
                service.updateBillPaymentStatus(billId, "Paid", method);
                loadBills();
                mainFrame.refreshDashboard();
                JOptionPane.showMessageDialog(this, "Payment of ₹" + String.format("%.2f", b.getTotalAmount()) + " confirmed via " + method + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void viewSelectedInvoice() {
        int row = billingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table to preview.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String billId = (String) tableModel.getValueAt(row, 0);
        BillingRecord b = service.getBillById(billId);
        if (b == null) return;

        Patient p = service.getPatientById(b.getPatientId());

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("              CITY GENERAL HOSPITAL & RESEARCH         \n");
        sb.append("                 PATIENT OFFICIAL INVOICE               \n");
        sb.append("========================================================\n");
        sb.append(String.format("Invoice Number: %-20s Date: %s\n", b.getBillId(), b.getBillingDate()));
        sb.append(String.format("Patient ID    : %-20s Name: %s\n", b.getPatientId(), b.getPatientName()));
        if (p != null) {
            sb.append(String.format("Contact Phone : %-20s Blood: %s\n", p.getContactNumber(), p.getBloodGroup()));
        }
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("  %-35s  %12s\n", "ITEM / SERVICE DESCRIPTION", "AMOUNT (₹)"));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("  %-35s  %12.2f\n", "1. Physician Consultation Charge", b.getConsultationFee()));
        sb.append(String.format("  %-35s  %12.2f\n", "2. Clinical Laboratory & Diagnostics", b.getLabCharges()));
        sb.append(String.format("  %-35s  %12.2f\n", "3. Pharmacy / Prescribed Medication", b.getMedicineCharges()));
        sb.append(String.format("  %-35s  %12.2f\n", "4. Room / Inpatient Care Charge", b.getRoomCharges()));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("  %-35s  %12.2f\n", "SUBTOTAL", b.getSubtotal()));
        sb.append(String.format("  %-35s  %12.2f\n", "TAX (" + b.getTaxRatePercent() + "%)", b.getTaxAmount()));
        sb.append(String.format("  %-35s -%11.2f\n", "DISCOUNT / CONCESSION", b.getDiscountAmount()));
        sb.append("========================================================\n");
        sb.append(String.format("  %-35s  %12.2f\n", "TOTAL NET PAYABLE", b.getTotalAmount()));
        sb.append("========================================================\n");
        sb.append("Payment Status : ").append(b.getPaymentStatus().toUpperCase()).append("\n");
        sb.append("Payment Method : ").append(b.getPaymentMethod()).append("\n");
        if (b.getPaymentDate() != null) {
            sb.append("Payment Date   : ").append(b.getPaymentDate()).append("\n");
        }
        sb.append("========================================================\n");
        sb.append("   Thank you for choosing City General Hospital.   \n");
        sb.append("========================================================\n");

        JTextArea area = new JTextArea(sb.toString(), 22, 50);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);

        JOptionPane.showMessageDialog(this, scroll, "Official Hospital Invoice - " + b.getBillId(), JOptionPane.PLAIN_MESSAGE);
    }

    private static class PatientItem {
        final String id;
        final String name;
        PatientItem(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name + " (" + id + ")"; }
    }
}
