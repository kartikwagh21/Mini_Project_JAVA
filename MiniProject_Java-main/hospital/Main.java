package hospital;

import hospital.ui.MainFrame;

import javax.swing.*;

/**
 * Entry point for the Hospital Patient Management System.
 */
public class Main {
    public static void main(String[] args) {
        // Set modern System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback gracefully
        }

        // Launch GUI safely on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
