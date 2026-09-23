package hospital.ui;

import hospital.model.Patient;
import hospital.service.HospitalService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Sorted Records & Data Structures Inspector Panel.
 * Demonstrates TreeMap sorting features (O(log N) operations) and displays
 * an educational comparison of Arrays, LinkedList, HashMap, and TreeMap.
 */
public class SortedRecordsPanel extends JPanel {
    private final HospitalService service = HospitalService.getInstance();
    private final MainFrame mainFrame;

    private DefaultTableModel tableModel;
    private JTable sortedTable;
    private JComboBox<String> cmbSortCriteria;
    private JLabel lblSortExplanation;

    public SortedRecordsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIConstants.BG_MAIN);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initComponents();
        applySorting();
    }

    private void initComponents() {
        // Top Header
        add(UIConstants.createHeaderBanner(
            "Sorted Patient Records",
            "Explore and filter patient records using custom sorting"
        ), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);

        // --- TOP CONTROLS & EXPLANATION BANNER ---
        JPanel topCard = UIConstants.createCardPanel();
        topCard.setLayout(new BorderLayout(12, 12));

        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlRow.setOpaque(false);

        JLabel lblSelect = new JLabel("Select Sort Criterion:");
        lblSelect.setFont(UIConstants.FONT_BODY_BOLD);

        cmbSortCriteria = new JComboBox<>(new String[]{
            "Alphabetical by Patient Name",
            "Natural Order by Patient ID",
            "Youngest to Oldest (Age Ascending)",
            "Oldest to Youngest (Age Descending)",
            "Most Recent Registrations First (Date Descending)"
        });
        cmbSortCriteria.setFont(UIConstants.FONT_BODY);
        cmbSortCriteria.addActionListener(e -> applySorting());

        JButton btnRefresh = UIConstants.createPrimaryButton("Re-apply Sort");
        btnRefresh.addActionListener(e -> applySorting());

        controlRow.add(lblSelect);
        controlRow.add(cmbSortCriteria);
        controlRow.add(btnRefresh);

        lblSortExplanation = new JLabel("Active Sorting: TreeMap<String, Patient> maintains entries sorted automatically.");
        lblSortExplanation.setFont(UIConstants.FONT_BODY);
        lblSortExplanation.setForeground(UIConstants.PRIMARY_DARK);

        topCard.add(controlRow, BorderLayout.NORTH);
        topCard.add(lblSortExplanation, BorderLayout.SOUTH);

        content.add(topCard, BorderLayout.NORTH);

        // --- CENTER: SORTED TABLE ---
        JPanel tableCard = UIConstants.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Index", "Patient ID", "Full Name", "Age", "Gender", "Blood Group", "Contact Number", "Reg. Date", "BP"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        sortedTable = new JTable(tableModel);
        UIConstants.styleTable(sortedTable);

        JScrollPane scrollPane = new JScrollPane(sortedTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);



        add(content, BorderLayout.CENTER);
    }

    public void applySorting() {
        int index = cmbSortCriteria.getSelectedIndex();
        List<Patient> list;

        switch (index) {
            case 0:
                list = service.getPatientsSortedByName();
                lblSortExplanation.setText("Sorted alphabetically by Patient Name.");
                break;
            case 1:
                list = service.getPatientsSortedById();
                lblSortExplanation.setText("Sorted naturally by Unique Patient ID.");
                break;
            case 2:
                list = service.getPatientsSortedByAge(true);
                lblSortExplanation.setText("Sorted dynamically by Age (Ascending: Youngest to Oldest).");
                break;
            case 3:
                list = service.getPatientsSortedByAge(false);
                lblSortExplanation.setText("Sorted dynamically by Age (Descending: Senior citizens first).");
                break;
            case 4:
                list = service.getPatientsSortedByRegistrationDate(true);
                lblSortExplanation.setText("Sorted by Registration Date (Most recent admissions first).");
                break;
            default:
                list = service.getPatientsSortedByName();
                break;
        }

        tableModel.setRowCount(0);
        int rank = 1;
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                rank++,
                p.getId(),
                p.getName(),
                p.getAge() + " yrs",
                p.getGender(),
                p.getBloodGroup(),
                p.getContactNumber(),
                p.getRegistrationDate(),
                p.getBloodPressure()
            });
        }
    }
}
