package hospital.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UI Constants and styling utilities for a clean, modern Swing GUI.
 */
public class UIConstants {
    // Primary Colors
    public static final Color PRIMARY = new Color(24, 119, 242);       // Medical Royal Blue
    public static final Color PRIMARY_DARK = new Color(17, 85, 180);   // Darker Blue for hover/accent
    public static final Color PRIMARY_LIGHT = new Color(235, 243, 255);// Soft blue background highlight
    public static final Color ACCENT = new Color(14, 165, 233);        // Cyan Accent
    
    // Status Colors
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald Green
    public static final Color SUCCESS_LIGHT = new Color(209, 250, 229);
    public static final Color WARNING = new Color(245, 158, 11);       // Amber Orange
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color DANGER = new Color(239, 68, 68);         // Crimson Red
    public static final Color DANGER_LIGHT = new Color(254, 226, 226);
    public static final Color PURPLE = new Color(139, 92, 246);
    public static final Color PURPLE_LIGHT = new Color(237, 233, 254);

    // Neutral Grays & Surfaces
    public static final Color BG_MAIN = new Color(248, 250, 252);      // Light Slate Gray background
    public static final Color SURFACE = Color.WHITE;
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);      // Deep Navy / Slate 900
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);   // Slate 800
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235); // Active Blue
    public static final Color TEXT_MAIN = new Color(15, 23, 42);       // Dark Slate
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Medium Slate Gray
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);   // Light Gray text
    public static final Color BORDER = new Color(226, 232, 240);       // Clean Border Gray

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_KPI_NUMBER = new Font("Segoe UI", Font.BOLD, 26);

    /**
     * Creates a styled modern button.
     */
    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, new Color(241, 245, 249), TEXT_MAIN);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER, Color.WHITE);
    }

    /**
     * Creates a styled card container panel with a subtle border and padding.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }

    /**
     * Styles a JTable with modern healthcare UI styling.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_MAIN);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // Center render for ID and Date columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Padding for standard cells
        DefaultTableCellRenderer padRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSelected, hasFocus, r, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return comp;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(padRenderer);
        }
    }

    /**
     * Styles a JTextField.
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(FONT_BODY);
        textField.setForeground(TEXT_MAIN);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
    }

    /**
     * Creates a header banner panel.
     */
    public static JPanel createHeaderBanner(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_MAIN);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_BODY);
        subLabel.setForeground(TEXT_MUTED);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subLabel, BorderLayout.CENTER);
        return panel;
    }
}
