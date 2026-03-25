package com.manager.storemanager.ui.dialog;

import com.manager.storemanager.model.Category;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.ui.UIConstants;
import com.manager.storemanager.util.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ProductDialog extends JDialog {

    private final ProductService productService;
    private final Product source;
    private Product result;
    private JTextField codeField;
    private JTextField nameField;
    private JComboBox<Category> categoryBox;
    private JComboBox<Supplier> supplierBox;
    private JTextArea descriptionArea;
    private JTextField purchasePriceField;
    private JTextField salePriceField;
    private JTextField stockField;
    private JTextField minimumStockField;
    private JComboBox<String> statusBox;

    public ProductDialog(Frame owner, ProductService productService, Product source) {
        super(owner, true);
        this.productService = productService;
        this.source = source;
        setTitle(source == null ? "Nuevo producto" : "Editar producto");
        setSize(560, 520);
        setLocationRelativeTo(owner);
        initComponents();
        loadReferenceData();
        if (source != null) {
            populate(source);
        }
    }

    public Product getResult() {
        return result;
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(UIConstants.cardBorder());
        form.setBackground(UIConstants.SURFACE);

        codeField = new JTextField();
        nameField = new JTextField();
        categoryBox = new JComboBox<>();
        supplierBox = new JComboBox<>();
        descriptionArea = new JTextArea(4, 24);
        purchasePriceField = new JTextField();
        salePriceField = new JTextField();
        stockField = new JTextField();
        minimumStockField = new JTextField();
        statusBox = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addField(form, gbc, 0, "Código", codeField);
        addField(form, gbc, 1, "Nombre", nameField);
        addField(form, gbc, 2, "Categoría", categoryBox);
        addField(form, gbc, 3, "Proveedor", supplierBox);
        addField(form, gbc, 4, "Descripción", descriptionArea);
        addField(form, gbc, 5, "Precio compra", purchasePriceField);
        addField(form, gbc, 6, "Precio venta", salePriceField);
        addField(form, gbc, 7, "Stock", stockField);
        addField(form, gbc, 8, "Stock mínimo", minimumStockField);
        addField(form, gbc, 9, "Estado", statusBox);

        JButton saveButton = new JButton("Guardar");
        UIConstants.stylePrimaryButton(saveButton);
        saveButton.addActionListener(event -> save());

        JButton cancelButton = new JButton("Cancelar");
        UIConstants.styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(event -> dispose());

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.add(cancelButton);
        actions.add(saveButton);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component component) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(component, gbc);
    }

    private void loadReferenceData() {
        try {
            List<Category> categories = productService.findCategories();
            for (Category category : categories) {
                categoryBox.addItem(category);
            }
            supplierBox.addItem(null);
            for (Supplier supplier : productService.findSuppliers()) {
                supplierBox.addItem(supplier);
            }
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar las referencias.\n" + exception.getMessage());
        }
    }

    private void populate(Product product) {
        codeField.setText(product.getCode());
        nameField.setText(product.getName());
        categoryBox.setSelectedItem(product.getCategory());
        supplierBox.setSelectedItem(product.getSupplier());
        descriptionArea.setText(product.getDescription());
        purchasePriceField.setText(product.getPurchasePrice().toPlainString());
        salePriceField.setText(product.getSalePrice().toPlainString());
        stockField.setText(String.valueOf(product.getStock()));
        minimumStockField.setText(String.valueOf(product.getMinimumStock()));
        statusBox.setSelectedItem(product.getStatus());
    }

    private void save() {
        try {
            Product product = source == null ? new Product() : source;
            product.setCode(codeField.getText().trim());
            product.setName(nameField.getText().trim());
            product.setCategory((Category) categoryBox.getSelectedItem());
            product.setSupplier((Supplier) supplierBox.getSelectedItem());
            product.setDescription(descriptionArea.getText().trim());
            product.setPurchasePrice(new BigDecimal(purchasePriceField.getText().trim()));
            product.setSalePrice(new BigDecimal(salePriceField.getText().trim()));
            product.setStock(Integer.parseInt(stockField.getText().trim()));
            product.setMinimumStock(Integer.parseInt(minimumStockField.getText().trim()));
            product.setStatus((String) statusBox.getSelectedItem());
            result = product;
            dispose();
        } catch (NumberFormatException exception) {
            MessageUtils.showWarning(this, "Verifique precios y cantidades.");
        }
    }
}
