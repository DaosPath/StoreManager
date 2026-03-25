package com.manager.storemanager.ui;

import com.manager.storemanager.model.Category;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.util.CurrencyUtils;
import com.manager.storemanager.util.MessageUtils;
import com.manager.storemanager.util.TableUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class ProductPanel extends JPanel implements RefreshableView {

    private final ProductService productService;
    private final JTextField searchField = new JTextField(22);
    private final DefaultTableModel tableModel = TableUtils.nonEditableModel(
            "ID", "Codigo", "Nombre", "Categoria", "Proveedor", "Compra", "Venta", "Stock", "Minimo", "Estado"
    );
    private final JTable table = new JTable(tableModel);
    private List<Product> products = new ArrayList<>();

    public ProductPanel(ProductService productService) {
        this.productService = productService;
        setLayout(new BorderLayout(16, 16));
        setBackground(UIConstants.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        // ── Row 1: Title + Search + New button ──────────────────────────────
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);

        topRow.add(UIConstants.pageTitleLabel("Inventario de Productos"), BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);

        searchField.setPreferredSize(new java.awt.Dimension(240, 40));
        searchField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        searchField.setBackground(new java.awt.Color(245, 247, 250));
        searchField.putClientProperty("JTextField.placeholderText", "Buscar producto...");
        searchField.putClientProperty("FlatLaf.style",
                "arc: 20; borderWidth: 1; borderColor: #DDE3EB; focusWidth: 1");
        searchField.putClientProperty("JTextField.leadingIcon", createSearchIcon());
        searchField.addActionListener(event -> reloadData());

        JButton addButton = new JButton("Nuevo Producto");
        addButton.setBackground(new java.awt.Color(34, 170, 90));
        addButton.setForeground(java.awt.Color.WHITE);
        addButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        addButton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new java.awt.Dimension(160, 40));
        addButton.putClientProperty("FlatLaf.style", "arc: 20; focusWidth: 0");
        addButton.setIcon(createPlusIcon());
        addButton.setIconTextGap(6);
        addButton.addActionListener(event -> openForm(null));

        topRight.add(searchField);
        topRight.add(addButton);
        topRow.add(topRight, BorderLayout.EAST);

        // ── Row 2: Action buttons ───────────────────────────────────────────
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 4, 0));

        JButton editButton = createOutlinedButton("Editar", createPencilIcon());
        JButton disableButton = createOutlinedButton("Desactivar", createDisableIcon());
        JButton refreshButton = createOutlinedButton("Actualizar", createRefreshIcon());
        editButton.addActionListener(event -> editSelected());
        disableButton.addActionListener(event -> deactivateSelected());
        refreshButton.addActionListener(event -> reloadData());

        actionRow.add(editButton);
        actionRow.add(disableButton);
        actionRow.add(refreshButton);

        // ── Header assembly ─────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(topRow);
        header.add(actionRow);

        // ── Table ───────────────────────────────────────────────────────────
        table.setBackground(UIConstants.SURFACE);
        TableUtils.configureTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 235, 244)));
        scrollPane.getViewport().setBackground(UIConstants.SURFACE);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ── Button factory ──────────────────────────────────────────────────────

    private JButton createOutlinedButton(String text, javax.swing.Icon icon) {
        JButton button = new JButton(text);
        button.setBackground(java.awt.Color.WHITE);
        button.setForeground(UIConstants.TEXT_PRIMARY);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.putClientProperty("FlatLaf.style",
                "arc: 18; borderWidth: 1; borderColor: #DDE3EB; hoverBorderColor: #B0BACA; focusWidth: 0");
        button.setIcon(icon);
        button.setIconTextGap(5);
        return button;
    }

    // ── Painted icons ───────────────────────────────────────────────────────

    private javax.swing.Icon createSearchIcon() {
        return new javax.swing.Icon() {
            @Override public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(140, 155, 175));
                g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 1, y + 1, 10, 10);
                g2.drawLine(x + 10, y + 10, x + 14, y + 14);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
    }

    private javax.swing.Icon createPlusIcon() {
        return new javax.swing.Icon() {
            @Override public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(java.awt.Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 7, y + 2, x + 7, y + 12);
                g2.drawLine(x + 2, y + 7, x + 12, y + 7);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 14; }
            @Override public int getIconHeight() { return 14; }
        };
    }

    private javax.swing.Icon createPencilIcon() {
        return new javax.swing.Icon() {
            @Override public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(100, 115, 135));
                g2.setStroke(new java.awt.BasicStroke(1.4f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 2, y + 12, x + 11, y + 3);
                g2.drawLine(x + 11, y + 3, x + 13, y + 1);
                g2.drawLine(x + 13, y + 1, x + 15, y + 3);
                g2.drawLine(x + 15, y + 3, x + 6, y + 12);
                g2.drawLine(x + 2, y + 12, x + 6, y + 12);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 14; }
        };
    }

    private javax.swing.Icon createDisableIcon() {
        return new javax.swing.Icon() {
            @Override public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(100, 115, 135));
                g2.setStroke(new java.awt.BasicStroke(1.4f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 1, y + 3, 12, 9, 3, 3);
                g2.drawLine(x + 6, y + 1, x + 10, y + 1);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 15; }
            @Override public int getIconHeight() { return 14; }
        };
    }

    private javax.swing.Icon createRefreshIcon() {
        return new javax.swing.Icon() {
            @Override public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(100, 115, 135));
                g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.drawArc(x + 2, y + 2, 11, 11, 30, 300);
                g2.drawLine(x + 12, y + 3, x + 14, y + 3);
                g2.drawLine(x + 12, y + 3, x + 12, y + 1);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 15; }
        };
    }

    @Override
    public void reloadData() {
        try {
            products = productService.findProducts(searchField.getText());
            tableModel.setRowCount(0);
            for (Product product : products) {
                tableModel.addRow(new Object[]{
                    product.getId(),
                    product.getCode(),
                    product.getName(),
                    product.getCategory() == null ? "" : product.getCategory().getName(),
                    product.getSupplier() == null ? "" : product.getSupplier().getName(),
                    CurrencyUtils.format(product.getPurchasePrice()),
                    CurrencyUtils.format(product.getSalePrice()),
                    product.getStock(),
                    product.getMinimumStock(),
                    product.getStatus()
                });
            }
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar los productos.\n" + exception.getMessage());
        }
    }

    private void editSelected() {
        Product product = getSelectedProduct();
        if (product == null) {
            MessageUtils.showWarning(this, "Seleccione un producto.");
            return;
        }
        openForm(product);
    }

    private void deactivateSelected() {
        Product product = getSelectedProduct();
        if (product == null) {
            MessageUtils.showWarning(this, "Seleccione un producto.");
            return;
        }
        int option = JOptionPane.showConfirmDialog(
                this,
                "Se desactivara el producto " + product.getName() + ".",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            productService.deactivate(product.getId());
            reloadData();
            MessageUtils.showInfo(this, "Producto desactivado.");
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible desactivar el producto.\n" + exception.getMessage());
        }
    }

    private Product getSelectedProduct() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return products.get(modelRow);
    }

    private void openForm(Product existing) {
        try {
            List<Category> categories = productService.findCategories();
            List<Supplier> suppliers = new ArrayList<>();
            suppliers.add(null);
            suppliers.addAll(productService.findSuppliers());
            if (categories.isEmpty()) {
                MessageUtils.showWarning(this, "No hay categorias activas disponibles.");
                return;
            }

            ProductFormDialog dialog = new ProductFormDialog(existing, categories, suppliers);
            dialog.setVisible(true);
            if (!dialog.isConfirmed()) {
                return;
            }

            Product product = dialog.buildProduct();
            if (existing == null) {
                productService.save(product);
                MessageUtils.showInfo(this, "Producto registrado.");
            } else {
                product.setId(existing.getId());
                productService.update(product);
                MessageUtils.showInfo(this, "Producto actualizado.");
            }
            reloadData();
        } catch (IllegalArgumentException exception) {
            MessageUtils.showWarning(this, exception.getMessage());
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible guardar el producto.\n" + exception.getMessage());
        }
    }

    private final class ProductFormDialog extends JDialog {

        private final JTextField codeField = new JTextField(18);
        private final JTextField nameField = new JTextField(18);
        private final JComboBox<Category> categoryCombo;
        private final JComboBox<Supplier> supplierCombo;
        private final JTextField purchasePriceField = new JTextField(18);
        private final JTextField salePriceField = new JTextField(18);
        private final JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        private final JSpinner minimumStockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
        private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});
        private final JTextArea descriptionArea = new JTextArea(4, 18);
        private boolean confirmed;

        private ProductFormDialog(Product existing, List<Category> categories, List<Supplier> suppliers) {
            super((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(ProductPanel.this), true);
            setTitle(existing == null ? "Nuevo producto" : "Editar producto");
            categoryCombo = new JComboBox<>(categories.toArray(Category[]::new));
            supplierCombo = new JComboBox<>(suppliers.toArray(Supplier[]::new));
            buildLayout();
            if (existing != null) {
                fillForm(existing);
            }
            pack();
            setLocationRelativeTo(ProductPanel.this);
        }

        private void buildLayout() {
            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UIConstants.SURFACE);
            form.setBorder(UIConstants.cardBorder());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            addRow(form, gbc, 0, "Codigo", codeField);
            addRow(form, gbc, 1, "Nombre", nameField);
            addRow(form, gbc, 2, "Categoria", categoryCombo);
            addRow(form, gbc, 3, "Proveedor", supplierCombo);
            addRow(form, gbc, 4, "Precio compra", purchasePriceField);
            addRow(form, gbc, 5, "Precio venta", salePriceField);
            addRow(form, gbc, 6, "Stock", stockSpinner);
            addRow(form, gbc, 7, "Stock minimo", minimumStockSpinner);
            addRow(form, gbc, 8, "Estado", statusCombo);

            gbc.gridx = 0;
            gbc.gridy = 9;
            form.add(new JLabel("Descripcion"), gbc);
            gbc.gridx = 1;
            form.add(new JScrollPane(descriptionArea), gbc);

            JButton saveButton = new JButton("Guardar");
            JButton cancelButton = new JButton("Cancelar");
            UIConstants.stylePrimaryButton(saveButton);
            UIConstants.styleSecondaryButton(cancelButton);
            saveButton.addActionListener(event -> {
                confirmed = true;
                setVisible(false);
            });
            cancelButton.addActionListener(event -> setVisible(false));

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.setOpaque(false);
            footer.add(cancelButton);
            footer.add(saveButton);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(form, BorderLayout.CENTER);
            getContentPane().add(footer, BorderLayout.SOUTH);
        }

        private void fillForm(Product product) {
            codeField.setText(product.getCode());
            nameField.setText(product.getName());
            categoryCombo.setSelectedItem(product.getCategory());
            supplierCombo.setSelectedItem(product.getSupplier());
            purchasePriceField.setText(product.getPurchasePrice().toPlainString());
            salePriceField.setText(product.getSalePrice().toPlainString());
            stockSpinner.setValue(product.getStock());
            minimumStockSpinner.setValue(product.getMinimumStock());
            statusCombo.setSelectedItem(product.getStatus());
            descriptionArea.setText(product.getDescription());
        }

        private boolean isConfirmed() {
            return confirmed;
        }

        private Product buildProduct() {
            Product product = new Product();
            product.setCode(codeField.getText().trim());
            product.setName(nameField.getText().trim());
            product.setCategory((Category) categoryCombo.getSelectedItem());
            product.setSupplier((Supplier) supplierCombo.getSelectedItem());
            product.setPurchasePrice(new BigDecimal(purchasePriceField.getText().trim()));
            product.setSalePrice(new BigDecimal(salePriceField.getText().trim()));
            product.setStock((Integer) stockSpinner.getValue());
            product.setMinimumStock((Integer) minimumStockSpinner.getValue());
            product.setStatus((String) statusCombo.getSelectedItem());
            product.setDescription(descriptionArea.getText().trim());
            return product;
        }

        private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel(label), gbc);
            gbc.gridx = 1;
            panel.add(field, gbc);
        }
    }
}
