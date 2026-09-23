package hospital.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Application Frame containing the sidebar navigation and content view area.
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new HashMap<>();

    // Panels
    private DashboardPanel dashboardPanel;
    private PatientPanel patientPanel;
    private DoctorPanel doctorPanel;
    private AppointmentPanel appointmentPanel;
    private MedicalRecordPanel medicalRecordPanel;
    private BillingPanel billingPanel;
    private SortedRecordsPanel sortedRecordsPanel;

    private String currentView = "Dashboard";

    public MainFrame() {
        setTitle("CarePulse - Hospital Patient Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UIConstants.BG_MAIN);

        // 1. Sidebar Navigation (West)
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // 2. Content Panels (Center)
        dashboardPanel = new DashboardPanel(this);
        patientPanel = new PatientPanel(this);
        doctorPanel = new DoctorPanel(this);
        appointmentPanel = new AppointmentPanel(this);
        medicalRecordPanel = new MedicalRecordPanel(this);
        billingPanel = new BillingPanel(this);
        sortedRecordsPanel = new SortedRecordsPanel(this);

        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(patientPanel, "Patients");
        contentPanel.add(doctorPanel, "Doctors");
        contentPanel.add(appointmentPanel, "Appointments");
        contentPanel.add(medicalRecordPanel, "Medical Records");
        contentPanel.add(billingPanel, "Billing");
        contentPanel.add(sortedRecordsPanel, "Sorted Records");

        rootPanel.add(contentPanel, BorderLayout.CENTER);

        add(rootPanel);
        navigateTo("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 800));
        sidebar.setBackground(UIConstants.SIDEBAR_BG);

        // Brand Logo & Title
        JPanel brandPanel = new JPanel(new BorderLayout(8, 4));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel lblLogo = new JLabel("⚕ CarePulse");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Hospital Management System");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        brandPanel.add(lblLogo, BorderLayout.NORTH);
        brandPanel.add(lblSub, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        String[] navItems = {
            "Dashboard",
            "Patients",
            "Doctors",
            "Appointments",
            "Medical Records",
            "Billing",
            "Sorted Records"
        };

        for (int i = 0; i < navItems.length; i++) {
            String name = navItems[i];
            JButton btn = createNavButton(name, name);
            navButtons.put(name, btn);
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(6));
        }

        sidebar.add(menuPanel, BorderLayout.CENTER);


        return sidebar;
    }

    private JButton createNavButton(String label, String viewName) {
        JButton btn = new JButton(label);
        btn.setFont(UIConstants.FONT_BODY_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UIConstants.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(216, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));

        btn.addActionListener(e -> navigateTo(viewName));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!viewName.equals(currentView)) {
                    btn.setBackground(UIConstants.SIDEBAR_HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!viewName.equals(currentView)) {
                    btn.setBackground(UIConstants.SIDEBAR_BG);
                }
            }
        });

        return btn;
    }

    public void navigateTo(String viewName) {
        currentView = viewName;
        cardLayout.show(contentPanel, viewName);

        // Update button active states
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(viewName)) {
                btn.setBackground(UIConstants.SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(UIConstants.SIDEBAR_BG);
                btn.setForeground(new Color(203, 213, 225));
            }
        }

        // View-specific refreshes
        switch (viewName) {
            case "Dashboard":
                dashboardPanel.refreshData();
                break;
            case "Patients":
                patientPanel.loadPatients(null);
                break;
            case "Doctors":
                doctorPanel.loadDoctors("All");
                break;
            case "Appointments":
                appointmentPanel.refreshDropdowns();
                appointmentPanel.loadAppointments();
                break;
            case "Medical Records":
                medicalRecordPanel.refreshDropdowns();
                medicalRecordPanel.loadRecords(null);
                break;
            case "Billing":
                billingPanel.refreshPatientsDropdown();
                billingPanel.loadBills();
                break;
            case "Sorted Records":
                sortedRecordsPanel.applySorting();
                break;
        }
    }

    public void refreshDashboard() {
        if (dashboardPanel != null) {
            dashboardPanel.refreshData();
        }
    }
}
