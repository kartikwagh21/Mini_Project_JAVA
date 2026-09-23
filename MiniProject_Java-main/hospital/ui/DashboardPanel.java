package hospital.ui;

import hospital.model.Appointment;
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
 * Dashboard Panel providing high-level overview metrics, quick shortcuts,
 * and real-time queues for hospital administration.
 */
public class DashboardPanel extends JPanel {
    private final HospitalService service = HospitalService.getInstance();
    private final MainFrame mainFrame;

    private JLabel lblTotalPatients;
    private JLabel lblTotalDoctors;
    private JLabel lblTodayAppointments;
    private JLabel lblPendingBills;
    private DefaultTableModel appointmentTableModel;
    private DefaultTableModel recentPatientsTableModel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Top Header
        add(UIConstants.createHeaderBanner(
            "Hospital Overview & Analytics Dashboard",
            "Real-time operational summary, patient traffic, and appointments schedule"
        ), BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // 1. KPI Cards Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        kpiRow.setPreferredSize(new Dimension(800, 110));

        lblTotalPatients = new JLabel("0");
        lblTotalDoctors = new JLabel("0");
        lblTodayAppointments = new JLabel("0");
        lblPendingBills = new JLabel("₹0.00");

        kpiRow.add(createKpiCard("Registered Patients", lblTotalPatients, "Total Active Patients", UIConstants.PRIMARY, UIConstants.PRIMARY_LIGHT));
        kpiRow.add(createKpiCard("Specialist Doctors", lblTotalDoctors, "Medical Staff on Roster", UIConstants.SUCCESS, UIConstants.SUCCESS_LIGHT));
        kpiRow.add(createKpiCard("Today's Appointments", lblTodayAppointments, "Scheduled Appointments", UIConstants.PURPLE, UIConstants.PURPLE_LIGHT));
        kpiRow.add(createKpiCard("Pending Invoices", lblPendingBills, "Outstanding Revenue", UIConstants.WARNING, UIConstants.WARNING_LIGHT));

        centerPanel.add(kpiRow);
        centerPanel.add(Box.createVerticalStrut(20));

        // 2. Quick Action Bar
        JPanel quickActionCard = UIConstants.createCardPanel();
        quickActionCard.setLayout(new BorderLayout(12, 12));
        quickActionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lblQuick = new JLabel("Quick Administrative Actions:");
        lblQuick.setFont(UIConstants.FONT_BODY_BOLD);
        lblQuick.setForeground(UIConstants.TEXT_MAIN);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnNewPatient = UIConstants.createPrimaryButton("+ Register New Patient");
        JButton btnBookApt = UIConstants.createButton("+ Schedule Appointment", UIConstants.PURPLE, Color.WHITE);
        JButton btnNewBill = UIConstants.createButton("+ Create Invoice", UIConstants.SUCCESS, Color.WHITE);
        JButton btnSortedView = UIConstants.createSecondaryButton("View Sorted Directory");

        btnNewPatient.addActionListener(e -> mainFrame.navigateTo("Patients"));
        btnBookApt.addActionListener(e -> mainFrame.navigateTo("Appointments"));
        btnNewBill.addActionListener(e -> mainFrame.navigateTo("Billing"));
        btnSortedView.addActionListener(e -> mainFrame.navigateTo("Sorted Records"));

        btnRow.add(btnNewPatient);
        btnRow.add(btnBookApt);
        btnRow.add(btnNewBill);
        btnRow.add(btnSortedView);

        quickActionCard.add(lblQuick, BorderLayout.WEST);
        quickActionCard.add(btnRow, BorderLayout.EAST);

        centerPanel.add(quickActionCard);
        centerPanel.add(Box.createVerticalStrut(20));

        // 3. Two-column Tables: Today's Appointments (LinkedList) & Recent Registrations (Array)
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setOpaque(false);

        // Left: Appointments
        JPanel leftCard = UIConstants.createCardPanel();
        leftCard.setLayout(new BorderLayout(10, 10));
        JLabel lblLeft = new JLabel("Upcoming Appointments Queue");
        lblLeft.setFont(UIConstants.FONT_SUBHEADER);
        lblLeft.setForeground(UIConstants.TEXT_MAIN);
        leftCard.add(lblLeft, BorderLayout.NORTH);

        String[] aptCols = {"ID", "Patient", "Doctor", "Time", "Status"};
        appointmentTableModel = new DefaultTableModel(aptCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable aptTable = new JTable(appointmentTableModel);
        UIConstants.styleTable(aptTable);
        JScrollPane aptScroll = new JScrollPane(aptTable);
        aptScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        leftCard.add(aptScroll, BorderLayout.CENTER);

        // Right: Recent Patients (Fixed Array Buffer)
        JPanel rightCard = UIConstants.createCardPanel();
        rightCard.setLayout(new BorderLayout(10, 10));
        JLabel lblRight = new JLabel("Recently Registered Patient IDs");
        lblRight.setFont(UIConstants.FONT_SUBHEADER);
        lblRight.setForeground(UIConstants.TEXT_MAIN);
        rightCard.add(lblRight, BorderLayout.NORTH);

        String[] recentCols = {"Slot", "Patient ID", "Patient Name", "Age / Gender", "Blood Group"};
        recentPatientsTableModel = new DefaultTableModel(recentCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable recentTable = new JTable(recentPatientsTableModel);
        UIConstants.styleTable(recentTable);
        JScrollPane recentScroll = new JScrollPane(recentTable);
        recentScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        rightCard.add(recentScroll, BorderLayout.CENTER);

        tablesPanel.add(leftCard);
        tablesPanel.add(rightCard);

        centerPanel.add(tablesPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, String subtitle, Color accentColor, Color bgHighlight) {
        JPanel card = UIConstants.createCardPanel();
        card.setLayout(new BorderLayout(8, 6));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.FONT_BODY_BOLD);
        titleLbl.setForeground(UIConstants.TEXT_MUTED);

        valueLabel.setFont(UIConstants.FONT_KPI_NUMBER);
        valueLabel.setForeground(accentColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UIConstants.FONT_SMALL);
        subLbl.setForeground(UIConstants.TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    public void refreshData() {
        lblTotalPatients.setText(String.valueOf(service.getPatientCount()));
        lblTotalDoctors.setText(String.valueOf(service.getDoctorCount()));
        lblTodayAppointments.setText(String.valueOf(service.getTodayAppointmentCount()));
        lblPendingBills.setText("₹" + String.format("%.2f", service.getPendingRevenue()));

        // Populate Appointments from LinkedList
        appointmentTableModel.setRowCount(0);
        LinkedList<Appointment> appointments = service.getAllAppointments();
        for (Appointment apt : appointments) {
            appointmentTableModel.addRow(new Object[]{
                apt.getAppointmentId(),
                apt.getPatientName(),
                apt.getDoctorName(),
                apt.getTimeSlot(),
                apt.getStatus()
            });
        }

        // Populate Recent Patients from Fixed Array Buffer
        recentPatientsTableModel.setRowCount(0);
        String[] recentIds = service.getRecentPatientIdsArray();
        for (int i = 0; i < recentIds.length; i++) {
            String pid = recentIds[i];
            if (pid != null) {
                // O(1) HashMap lookup to fetch details
                Patient p = service.getPatientById(pid);
                if (p != null) {
                    recentPatientsTableModel.addRow(new Object[]{
                        "Slot " + (i + 1),
                        p.getId(),
                        p.getName(),
                        p.getAge() + " yrs / " + p.getGender(),
                        p.getBloodGroup()
                    });
                }
            } else {
                recentPatientsTableModel.addRow(new Object[]{
                    "Slot " + (i + 1),
                    "[Empty]",
                    "-",
                    "-",
                    "-"
                });
            }
        }
    }
}
