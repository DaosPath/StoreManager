package com.manager.storemanager.ui;

import com.manager.storemanager.model.User;
import com.manager.storemanager.service.CustomerService;
import com.manager.storemanager.service.InventoryService;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.service.SaleService;
import com.manager.storemanager.service.SupplierService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, RefreshableView> views = new LinkedHashMap<>();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final User currentUser;

    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("StoreManager | " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 820);
        setMinimumSize(new Dimension(1240, 760));
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        ProductService productService = new ProductService();
        CustomerService customerService = new CustomerService();
        SupplierService supplierService = new SupplierService();
        InventoryService inventoryService = new InventoryService();
        SaleService saleService = new SaleService();
        ReportService reportService = new ReportService();

        registerView("Dashboard", new DashboardPanel(reportService));
        registerView("Productos", new ProductPanel(productService));
        registerView("Ventas", new SalesPanel(currentUser, productService, customerService, saleService));
        registerView("Inventario", new InventoryPanel(currentUser, inventoryService));
        registerView("Clientes", new CustomerPanel(customerService));
        registerView("Proveedores", new SupplierPanel(supplierService));
        registerView("Reportes", new ReportsPanel(productService, saleService, reportService));

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(UIConstants.BACKGROUND);
        shell.add(buildSidebar(), BorderLayout.WEST);
        shell.add(buildContentWrapper(), BorderLayout.CENTER);
        setContentPane(shell);
        showView("Dashboard");
    }

    private void registerView(String name, RefreshableView view) {
        views.put(name, view);
        contentPanel.add((Component) view, name);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIConstants.SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 16, 22, 16));

        JLabel appTitle = new JLabel("StoreManager");
        appTitle.setForeground(Color.WHITE);
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel profileCard = UIConstants.roundedPanel(UIConstants.SIDEBAR_ACCENT, 22);
        profileCard.setLayout(new BorderLayout(12, 0));
        profileCard.setMaximumSize(new Dimension(200, 72));
        profileCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avatar = new JLabel(currentUser.getFullName().substring(0, 1).toUpperCase(), SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(255, 255, 255, 26));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avatar.setPreferredSize(new Dimension(42, 42));

        JPanel profileText = new JPanel();
        profileText.setOpaque(false);
        profileText.setLayout(new BoxLayout(profileText, BoxLayout.Y_AXIS));
        JLabel userName = new JLabel(currentUser.getFullName());
        userName.setForeground(Color.WHITE);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel roleLabel = new JLabel(currentUser.getRole().getName().toUpperCase());
        roleLabel.setForeground(new Color(171, 187, 211));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        profileText.add(userName);
        profileText.add(Box.createVerticalStrut(4));
        profileText.add(roleLabel);

        profileCard.add(avatar, BorderLayout.WEST);
        profileCard.add(profileText, BorderLayout.CENTER);

        sidebar.add(appTitle);
        sidebar.add(Box.createVerticalStrut(18));
        sidebar.add(profileCard);
        sidebar.add(Box.createVerticalStrut(18));

        for (String viewName : views.keySet()) {
            JButton button = new JButton(viewName);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(200, 40));
            UIConstants.styleSidebarButton(button, false);
            button.addActionListener(event -> showView(viewName));
            navButtons.put(viewName, button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JPanel buildContentWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BACKGROUND);
        wrapper.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        contentPanel.setOpaque(false);
        wrapper.add(contentPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void showView(String name) {
        RefreshableView view = views.get(name);
        if (view != null) {
            view.reloadData();
        }
        cardLayout.show(contentPanel, name);
        updateNavigationState(name);
    }

    private void updateNavigationState(String selectedView) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            UIConstants.styleSidebarButton(entry.getValue(), entry.getKey().equals(selectedView));
        }
    }
}
