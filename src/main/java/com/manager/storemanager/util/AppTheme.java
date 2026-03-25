package com.manager.storemanager.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

public final class AppTheme {

    public static final Color BACKGROUND = new Color(243, 245, 247);
    public static final Color SURFACE = Color.WHITE;
    public static final Color PANEL = new Color(224, 229, 233);
    public static final Color PRIMARY = new Color(28, 63, 85);
    public static final Color PRIMARY_DARK = new Color(21, 49, 66);
    public static final Color ACCENT = new Color(18, 102, 94);
    public static final Color DANGER = new Color(170, 43, 56);
    public static final Color TEXT = new Color(35, 40, 45);
    public static final Color MUTED = new Color(105, 112, 118);

    private AppTheme() {
    }

    public static void configure() {
        // FlatLaf automatically handles most global properties
    }

    public static JPanel contentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }

    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PANEL),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(SURFACE);
        button.setForeground(TEXT);
        return button;
    }

    public static JButton dangerButton(String text) {
        JButton button = primaryButton(text);
        button.setBackground(DANGER);
        return button;
    }

    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        return label;
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    public static void styleTextField(JTextField field) {
        field.setBackground(Color.WHITE);
    }

    public static void styleTable(JTable table, JScrollPane scrollPane) {
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
    }
}
