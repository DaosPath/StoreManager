package com.manager.storemanager.ui;

import com.manager.storemanager.model.DailySalesTotal;
import com.manager.storemanager.model.DashboardSummary;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.util.CurrencyUtils;
import com.manager.storemanager.util.MessageUtils;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardPanel extends JPanel implements RefreshableView {

    private final ReportService reportService;
    private final JLabel productsValue = buildValueLabel();
    private final JLabel customersValue = buildValueLabel();
    private final JLabel suppliersValue = buildValueLabel();
    private final JLabel lowStockValue = buildValueLabel();
    private final JLabel salesValue = buildValueLabel();
    private SalesChartPanel chartPanel;

    public DashboardPanel(ReportService reportService) {
        this.reportService = reportService;
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(UIConstants.pageTitleLabel("Dashboard"));
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(UIConstants.subtitleLabel("Resumen operativo del mini market para caja, stock y abastecimiento."));

        JButton refreshButton = new JButton("Actualizar");
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(UIConstants.TEXT_PRIMARY);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshButton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        refreshButton.putClientProperty("FlatLaf.style",
                "arc: 20; borderWidth: 1; borderColor: #DDE3EB; hoverBorderColor: #B0BACA; focusWidth: 0");
        refreshButton.setIcon(new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.ACCENT);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(x + 2, y + 2, 12, 12, 30, 300);
                // Arrow tip
                int ax = x + 12, ay = y + 4;
                g2.drawLine(ax, ay, ax + 3, ay);
                g2.drawLine(ax, ay, ax, ay - 3);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 16; }
        });
        refreshButton.setIconTextGap(6);
        refreshButton.addActionListener(event -> reloadData());

        JPanel actionWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionWrapper.setOpaque(false);
        actionWrapper.add(refreshButton);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(actionWrapper, BorderLayout.EAST);

        // ── Top row: 3 metric cards ─────────────────────────────────────────
        JPanel topCards = new JPanel(new GridLayout(1, 3, 16, 0));
        topCards.setOpaque(false);
        topCards.add(createMetricCard("Productos Activos", "Catalogo disponible para venta.",
                productsValue, "P", new Color(235, 241, 255), new Color(45, 106, 255)));
        topCards.add(createMetricCard("Total Clientes", "Clientes registrados y listos para facturacion.",
                customersValue, "C", new Color(240, 245, 250), UIConstants.TEXT_MUTED));
        topCards.add(createMetricCard("Proveedores", "Aliados de compra y reposicion.",
                suppliersValue, "S", new Color(230, 247, 238), new Color(28, 140, 95)));

        // ── Middle: Sales chart ─────────────────────────────────────────────
        chartPanel = new SalesChartPanel();

        // ── Bottom row: 2 metric cards + hint card ──────────────────────────
        JPanel bottomCards = new JPanel(new GridLayout(1, 3, 16, 0));
        bottomCards.setOpaque(false);
        bottomCards.add(createMetricCard("Stock Bajo", "Productos que requieren reposicion.",
                lowStockValue, "!", UIConstants.DANGER_SOFT, UIConstants.DANGER));
        bottomCards.add(createMetricCard("Ventas del Dia", "Total vendido en la jornada actual.",
                salesValue, "$", UIConstants.SUCCESS_SOFT, UIConstants.SUCCESS));
        bottomCards.add(createHintCard());

        // ── Assembly: vertical stack ────────────────────────────────────────
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(topCards);
        center.add(Box.createVerticalStrut(16));
        center.add(chartPanel);
        center.add(Box.createVerticalStrut(16));
        center.add(bottomCards);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    // ── Metric card ─────────────────────────────────────────────────────────

    private JPanel createMetricCard(String title, String subtitle, JLabel valueLabel,
                                    String badgeText, Color badgeBg, Color badgeFg) {
        JPanel card = UIConstants.roundedPanel(UIConstants.SURFACE, 20);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("<html><div style='width:140px; line-height:120%'>" + subtitle + "</div></html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(UIConstants.TEXT_MUTED);

        textStack.add(titleLabel);
        textStack.add(Box.createVerticalStrut(4));
        textStack.add(subtitleLabel);

        // Circular badge
        JPanel badgePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        badgePanel.setOpaque(false);
        badgePanel.setLayout(new BorderLayout());
        badgePanel.setPreferredSize(new Dimension(42, 42));

        JLabel badge = new JLabel(badgeText, SwingConstants.CENTER);
        badge.setForeground(badgeFg);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 17));
        badgePanel.add(badge, BorderLayout.CENTER);

        top.add(textStack, BorderLayout.CENTER);
        top.add(badgePanel, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(UIConstants.TEXT_PRIMARY);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ── Hint card (dark) ────────────────────────────────────────────────────

    private JPanel createHintCard() {
        JPanel card = UIConstants.roundedPanel(UIConstants.SIDEBAR, 20);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel title = new JLabel("Control Diario");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel body = new JLabel("<html><div style='width:200px; line-height:130%'>Usa Reportes para revisar ventas por rango y el modulo Gestion de stock para registrar nuevas entradas de mercaderia.</div></html>");
        body.setForeground(new Color(185, 200, 222));
        body.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel tagPanel = UIConstants.roundedPanel(new Color(40, 180, 120), 12);
        tagPanel.setLayout(new BorderLayout());
        tagPanel.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        JLabel tag = new JLabel("Operacion Estable");
        tag.setForeground(Color.WHITE);
        tag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tagPanel.add(tag, BorderLayout.CENTER);

        JPanel tagWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tagWrapper.setOpaque(false);
        tagWrapper.add(tagPanel);

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(tagWrapper, BorderLayout.SOUTH);
        return card;
    }

    // ── Sales chart panel ───────────────────────────────────────────────────

    private static final class SalesChartPanel extends JPanel {

        private static final Color CHART_LINE = new Color(28, 160, 120);
        private static final Color CHART_FILL_TOP = new Color(28, 160, 120, 60);
        private static final Color CHART_FILL_BOT = new Color(28, 160, 120, 5);
        private static final Color GRID_LINE = new Color(235, 240, 248);
        private static final Color AXIS_TEXT = new Color(130, 140, 155);


        private List<DailySalesTotal> data = new ArrayList<>();

        SalesChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 240));
        }

        void setData(List<DailySalesTotal> data) {
            this.data = data != null ? data : new ArrayList<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // Card background
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, 20, 20);

            int padL = 60, padR = 30, padT = 50, padB = 36;
            int cw = w - padL - padR, ch = h - padT - padB;

            // ── Title + subtitle ────────────────────────────────────────────
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.setColor(UIConstants.TEXT_PRIMARY);
            g2.drawString("Sales Activity", padL, 30);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(AXIS_TEXT);
            String period = "Ultimo mas 7 dias";
            int tw = g2.getFontMetrics().stringWidth(period);
            g2.drawString(period, w - padR - tw, 30);

            if (data.isEmpty() || cw <= 0 || ch <= 0) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(AXIS_TEXT);
                g2.drawString("Sin datos de ventas recientes", padL + 20, padT + ch / 2);
                g2.dispose();
                return;
            }

            // ── Compute values ──────────────────────────────────────────────
            double maxVal = 0;
            for (DailySalesTotal d : data) {
                double v = d.getTotal().doubleValue();
                if (v > maxVal) maxVal = v;
            }
            if (maxVal == 0) maxVal = 100;
            maxVal = Math.ceil(maxVal / 20) * 20; // round up to nearest 20

            int n = data.size();
            int[] px = new int[n], py = new int[n];
            for (int i = 0; i < n; i++) {
                px[i] = padL + (n == 1 ? cw / 2 : i * cw / (n - 1));
                double v = data.get(i).getTotal().doubleValue();
                py[i] = padT + ch - (int) (v / maxVal * ch);
            }

            // ── Grid lines + Y axis labels ──────────────────────────────────
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            int gridCount = 5;
            for (int i = 0; i <= gridCount; i++) {
                int y = padT + i * ch / gridCount;
                g2.setColor(GRID_LINE);
                g2.drawLine(padL, y, padL + cw, y);
                int val = (int) (maxVal * (gridCount - i) / gridCount);
                g2.setColor(AXIS_TEXT);
                String label = String.valueOf(val);
                int lw = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, padL - lw - 8, y + 4);
            }

            // ── Area fill ───────────────────────────────────────────────────
            int[] fillX = new int[n + 2], fillY = new int[n + 2];
            System.arraycopy(px, 0, fillX, 0, n);
            System.arraycopy(py, 0, fillY, 0, n);
            fillX[n] = px[n - 1];
            fillY[n] = padT + ch;
            fillX[n + 1] = px[0];
            fillY[n + 1] = padT + ch;

            g2.setPaint(new GradientPaint(0, padT, CHART_FILL_TOP, 0, padT + ch, CHART_FILL_BOT));
            g2.fillPolygon(fillX, fillY, n + 2);

            // ── Line ────────────────────────────────────────────────────────
            g2.setColor(CHART_LINE);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);
            }

            // ── Dots + X axis labels ────────────────────────────────────────
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i < n; i++) {
                // dot
                g2.setColor(Color.WHITE);
                g2.fillOval(px[i] - 5, py[i] - 5, 10, 10);
                g2.setColor(CHART_LINE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(px[i] - 5, py[i] - 5, 10, 10);

                // x label
                g2.setColor(AXIS_TEXT);
                String xlabel = (i + 1) + " dias";
                int xlw = g2.getFontMetrics().stringWidth(xlabel);
                g2.drawString(xlabel, px[i] - xlw / 2, padT + ch + 22);
            }

            g2.dispose();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JLabel buildValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 36));
        label.setForeground(UIConstants.TEXT_PRIMARY);
        return label;
    }

    @Override
    public void reloadData() {
        try {
            DashboardSummary summary = reportService.loadDashboardSummary();
            productsValue.setText(String.valueOf(summary.getProductCount()));
            customersValue.setText(String.valueOf(summary.getCustomerCount()));
            suppliersValue.setText(String.valueOf(summary.getSupplierCount()));
            lowStockValue.setText(String.valueOf(summary.getLowStockCount()));
            salesValue.setText(CurrencyUtils.format(summary.getTodaySalesTotal()));

            // Load chart data for last 7 days
            LocalDate today = LocalDate.now();
            List<DailySalesTotal> dailyData = reportService.findDailyTotals(today.minusDays(6), today);
            chartPanel.setData(dailyData);
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar el dashboard.\n" + exception.getMessage());
        }
    }
}
