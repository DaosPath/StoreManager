package com.manager.storemanager.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public final class UIConstants {

    public static final Color BACKGROUND = new Color(243, 246, 251);
    public static final Color SURFACE = new Color(255, 255, 255);
    public static final Color SURFACE_MUTED = new Color(248, 250, 253);
    public static final Color SIDEBAR = new Color(18, 28, 47);
    public static final Color SIDEBAR_ACCENT = new Color(35, 49, 77);
    public static final Color SIDEBAR_SELECTED = new Color(44, 58, 87);
    public static final Color ACCENT = new Color(45, 106, 255);
    public static final Color ACCENT_DARK = new Color(29, 78, 216);
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    public static final Color TEXT_MUTED = new Color(107, 114, 128);
    public static final Color TEXT_SOFT = new Color(148, 163, 184);
    public static final Color BORDER = new Color(225, 231, 238);
    public static final Color DANGER = new Color(214, 69, 69);
    public static final Color DANGER_SOFT = new Color(254, 234, 234);
    public static final Color SUCCESS = new Color(28, 140, 95);
    public static final Color SUCCESS_SOFT = new Color(229, 247, 238);
    public static final Color INFO_SOFT = new Color(235, 241, 255);

    private UIConstants() {
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(20, 20, 20, 20)
        );
    }

    public static Border sectionBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 14, 14, 14)
        );
    }

    public static void stylePrimaryButton(JButton button) {
        baseButton(button, ACCENT, Color.WHITE);
    }

    public static void styleSecondaryButton(JButton button) {
        button.setBackground(SURFACE);
        button.setForeground(TEXT_PRIMARY);
    }

    public static void styleDangerButton(JButton button) {
        baseButton(button, DANGER, Color.WHITE);
    }

    public static void styleSidebarButton(JButton button, boolean selected) {
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(selected ? SIDEBAR_SELECTED : SIDEBAR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 15));
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static JLabel titleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel pageTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel subtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    public static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    public static void applyFieldSize(JComponent component) {
        component.setPreferredSize(new Dimension(220, 36));
    }

    public static JPanel roundedPanel(Color background, int radius) {
        return new RoundedPanel(background, radius);
    }

    private static void baseButton(JButton button, Color background, Color foreground) {
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private static final class RoundedPanel extends JPanel {
        private final Color background;
        private final int radius;

        private RoundedPanel(Color background, int radius) {
            this.background = background;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
