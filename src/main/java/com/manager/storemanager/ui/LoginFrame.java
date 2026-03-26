package com.manager.storemanager.ui;

import com.manager.storemanager.model.User;
import com.manager.storemanager.service.AuthService;
import com.manager.storemanager.util.MessageUtils;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private JTextField usernameField;
    private JPasswordField passwordField;

    // ── Brand palette ───────────────────────────────────────────────────────
    private static final Color GRAD_TOP_LEFT = new Color(12, 25, 56);
    private static final Color GRAD_BOT_RIGHT = new Color(32, 72, 155);
    private static final Color SHAPE_A = new Color(45, 90, 180, 100);
    private static final Color SHAPE_B = new Color(55, 115, 210, 60);
    private static final Color SHAPE_C = new Color(70, 130, 230, 35);

    // ── Form palette ────────────────────────────────────────────────────────
    private static final Color PAGE_BG = new Color(244, 246, 251);
    private static final Color CARD_SHADOW = new Color(30, 50, 90, 22);
    private static final Color FIELD_BG = new Color(240, 242, 247);
    private static final Color FIELD_ICON_COLOR = new Color(135, 150, 172);
    private static final Color LABEL_COLOR = new Color(30, 40, 60);

    public LoginFrame() {
        setTitle("StoreManager | Inicio de sesion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(PAGE_BG);
        container.add(buildBrandingPanel(), BorderLayout.WEST);
        container.add(buildRightSide(), BorderLayout.CENTER);
        setContentPane(container);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  LEFT PANEL — Gradient + angular geometric shapes
    // ═══════════════════════════════════════════════════════════════════════════

    private JPanel buildBrandingPanel() {
        JPanel branding = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Gradient
                g2.setPaint(new GradientPaint(0, 0, GRAD_TOP_LEFT, w, h, GRAD_BOT_RIGHT));
                g2.fillRect(0, 0, w, h);

                // Angular planes (triangular shapes like the reference)
                drawAngularPlane(g2, SHAPE_A,
                        w * 0.55, 0,
                        w * 1.05, h * 0.25,
                        w * 0.85, h * 0.55,
                        w * 0.30, h * 0.30);

                drawAngularPlane(g2, SHAPE_B,
                        0, h * 0.45,
                        w * 0.50, h * 0.35,
                        w * 0.70, h * 0.75,
                        w * 0.10, h * 0.90);

                drawAngularPlane(g2, SHAPE_C,
                        w * 0.60, h * 0.60,
                        w * 1.10, h * 0.50,
                        w * 1.00, h * 1.05,
                        w * 0.40, h * 0.95);

                g2.dispose();
            }
        };
        branding.setPreferredSize(new Dimension(445, 650));
        branding.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // ── Logo: badge + text ──────────────────────────────────────────────
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoRow.setMaximumSize(new Dimension(380, 55));

        JPanel logoBadge = UIConstants.roundedPanel(new Color(55, 115, 215), 14);
        logoBadge.setLayout(new BorderLayout());
        logoBadge.setPreferredSize(new Dimension(44, 44));
        JLabel logoS = new JLabel("S", SwingConstants.CENTER);
        logoS.setForeground(Color.WHITE);
        logoS.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoBadge.add(logoS, BorderLayout.CENTER);

        JLabel appName = new JLabel("StoreManager");
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("Segoe UI", Font.BOLD, 28));

        logoRow.add(logoBadge);
        logoRow.add(appName);

        // ── Tagline ─────────────────────────────────────────────────────────
        JLabel tagline = new JLabel("Tu gestion integral en un solo lugar");
        tagline.setForeground(new Color(175, 195, 225));
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Feature icons ───────────────────────────────────────────────────
        JPanel featuresRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 36, 0));
        featuresRow.setOpaque(false);
        featuresRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        featuresRow.setMaximumSize(new Dimension(400, 90));

        featuresRow.add(createFeatureItem("Ventas", FeatureIcon.CART));
        featuresRow.add(createFeatureItem("Gestion de stock", FeatureIcon.CLIPBOARD));
        featuresRow.add(createFeatureItem("Reportes", FeatureIcon.CHART));

        content.add(logoRow);
        content.add(Box.createVerticalStrut(12));
        content.add(tagline);
        content.add(Box.createVerticalStrut(56));
        content.add(featuresRow);

        branding.add(content);
        return branding;
    }

    private void drawAngularPlane(Graphics2D g2, Color c,
                                   double x1, double y1, double x2, double y2,
                                   double x3, double y3, double x4, double y4) {
        Path2D p = new Path2D.Double();
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        p.lineTo(x3, y3);
        p.lineTo(x4, y4);
        p.closePath();
        g2.setColor(c);
        g2.fill(p);
    }

    private enum FeatureIcon { CART, CLIPBOARD, CHART }

    private JPanel createFeatureItem(String text, FeatureIcon type) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));

        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;

                switch (type) {
                    case CART -> {
                        // Cart body
                        g2.drawLine(cx - 10, cy - 8, cx - 7, cy - 8);
                        g2.drawLine(cx - 7, cy - 8, cx - 4, cy + 3);
                        g2.drawLine(cx - 4, cy + 3, cx + 9, cy + 3);
                        g2.drawLine(cx + 10, cy - 6, cx - 6, cy - 6);
                        g2.drawLine(cx + 10, cy - 6, cx + 9, cy + 3);
                        // Wheels
                        g2.fillOval(cx - 3, cy + 5, 4, 4);
                        g2.fillOval(cx + 5, cy + 5, 4, 4);
                    }
                    case CLIPBOARD -> {
                        // Board
                        g2.drawRoundRect(cx - 8, cy - 9, 16, 20, 3, 3);
                        // Clip
                        g2.drawRoundRect(cx - 4, cy - 12, 8, 6, 3, 3);
                        // Lines
                        g2.drawLine(cx - 4, cy - 1, cx + 4, cy - 1);
                        g2.drawLine(cx - 4, cy + 3, cx + 4, cy + 3);
                        g2.drawLine(cx - 4, cy + 7, cx + 2, cy + 7);
                    }
                    case CHART -> {
                        // Bars
                        g2.fillRoundRect(cx - 9, cy + 1, 5, 9, 2, 2);
                        g2.fillRoundRect(cx - 2, cy - 5, 5, 15, 2, 2);
                        g2.fillRoundRect(cx + 5, cy - 2, 5, 12, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(54, 54));
        circle.setMaximumSize(new Dimension(54, 54));
        circle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(text);
        label.setForeground(new Color(195, 212, 238));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        item.add(circle);
        item.add(Box.createVerticalStrut(10));
        item.add(label);
        return item;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  RIGHT SIDE — Form card with shadow + credentials footer
    // ═══════════════════════════════════════════════════════════════════════════

    private JPanel buildRightSide() {
        JPanel right = new JPanel(new BorderLayout(0, 0));
        right.setBackground(PAGE_BG);

        // ── Form card centered ──────────────────────────────────────────────
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);

        // Shadow wrapper
        JPanel shadowCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(CARD_SHADOW);
                g2.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 10, 26, 26);
                // Card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 24, 24);
                g2.dispose();
            }
        };
        shadowCard.setOpaque(false);
        shadowCard.setBorder(BorderFactory.createEmptyBorder(48, 48, 40, 48));
        shadowCard.setPreferredSize(new Dimension(430, 420));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;

        // Title
        JLabel title = new JLabel("Bienvenido", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(LABEL_COLOR);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        form.add(title, gbc);

        // Subtitle
        JLabel sub = new JLabel("Ingresa tus credenciales para continuar", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(UIConstants.TEXT_MUTED);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 32, 0);
        form.add(sub, gbc);

        // ── Username ────────────────────────────────────────────────────────
        JLabel userLabel = new JLabel("Usuario");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(LABEL_COLOR);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 7, 0);
        form.add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(330, 52));
        usernameField.setMinimumSize(new Dimension(330, 52));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBackground(FIELD_BG);
        usernameField.putClientProperty("JTextField.placeholderText", "Ingresa tu usuario");
        usernameField.putClientProperty("JTextField.showClearButton", true);
        usernameField.putClientProperty("JTextField.leadingIcon", createUserIcon());
        usernameField.putClientProperty("FlatLaf.style",
                "arc: 16; borderWidth: 0; focusWidth: 2; innerFocusWidth: 0; margin: 4,8,4,8");

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        form.add(usernameField, gbc);

        // ── Password ────────────────────────────────────────────────────────
        JLabel passLabel = new JLabel("Contrasena");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setForeground(LABEL_COLOR);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 7, 0);
        form.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(330, 52));
        passwordField.setMinimumSize(new Dimension(330, 52));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBackground(FIELD_BG);
        passwordField.putClientProperty("JTextField.placeholderText", "Ingresa tu contrasena");
        passwordField.putClientProperty("JTextField.showRevealButton", true);
        passwordField.putClientProperty("JTextField.showCapsLock", true);
        passwordField.putClientProperty("JTextField.leadingIcon", createLockIcon());
        passwordField.putClientProperty("FlatLaf.style",
                "arc: 16; borderWidth: 0; focusWidth: 2; innerFocusWidth: 0; margin: 4,8,4,8");

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 28, 0);
        form.add(passwordField, gbc);

        // ── Login button ────────────────────────────────────────────────────
        JButton loginBtn = new JButton("Iniciar Sesion");
        loginBtn.setBackground(UIConstants.ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(330, 48));
        loginBtn.putClientProperty("JButton.buttonType", "roundRect");
        loginBtn.addActionListener(e -> authenticate());
        passwordField.addActionListener(e -> authenticate());

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(loginBtn, gbc);

        shadowCard.add(form, BorderLayout.CENTER);
        cardWrapper.add(shadowCard);

        // ── Credentials footer ──────────────────────────────────────────────
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(14, 0, 28, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(222, 228, 237));
        sep.setMaximumSize(new Dimension(420, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel credTitle = new JLabel("Credenciales de prueba", SwingConstants.CENTER);
        credTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        credTitle.setForeground(LABEL_COLOR);
        credTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel credRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 48, 0));
        credRow.setOpaque(false);
        credRow.setMaximumSize(new Dimension(420, 48));
        credRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        credRow.add(createCredBlock("Admin", "admin / admin123"));
        credRow.add(createCredBlock("Cajero", "cajero / cajero123"));

        footer.add(sep);
        footer.add(Box.createVerticalStrut(16));
        footer.add(credTitle);
        footer.add(Box.createVerticalStrut(10));
        footer.add(credRow);

        right.add(cardWrapper, BorderLayout.CENTER);
        right.add(footer, BorderLayout.SOUTH);
        return right;
    }

    // ── Custom painted icons for text fields ────────────────────────────────

    private javax.swing.Icon createUserIcon() {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_ICON_COLOR);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Head circle
                g2.drawOval(x + 5, y + 1, 8, 8);
                // Shoulders arc
                g2.drawArc(x + 1, y + 10, 16, 10, 0, 180);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
        };
    }

    private javax.swing.Icon createLockIcon() {
        return new javax.swing.Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_ICON_COLOR);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Shackle arc
                g2.drawArc(x + 4, y + 1, 10, 10, 0, 180);
                // Body rounded rect
                g2.fillRoundRect(x + 2, y + 8, 14, 11, 5, 5);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
        };
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JPanel createCredBlock(String role, String creds) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

        JLabel r = new JLabel(role);
        r.setFont(new Font("Segoe UI", Font.BOLD, 13));
        r.setForeground(LABEL_COLOR);
        r.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel c = new JLabel(creds);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setForeground(UIConstants.TEXT_MUTED);
        c.setAlignmentX(Component.CENTER_ALIGNMENT);

        block.add(r);
        block.add(Box.createVerticalStrut(3));
        block.add(c);
        return block;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  AUTH
    // ═══════════════════════════════════════════════════════════════════════════

    private void authenticate() {
        try {
            User user = authService.login(usernameField.getText(), passwordField.getPassword());
            new MainFrame(user).setVisible(true);
            dispose();
        } catch (IllegalArgumentException exception) {
            MessageUtils.showWarning(this, exception.getMessage());
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible iniciar sesion.\n" + exception.getMessage());
        } catch (RuntimeException exception) {
            MessageUtils.showError(this, "Error de infraestructura.\n" + extractMessage(exception));
        }
    }

    private String extractMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
