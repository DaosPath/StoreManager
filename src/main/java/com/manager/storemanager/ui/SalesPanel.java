package com.manager.storemanager.ui;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.model.Customer;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.model.SaleDetail;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.CustomerService;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.SaleService;
import com.manager.storemanager.util.CurrencyUtils;
import com.manager.storemanager.util.MessageUtils;
import com.manager.storemanager.util.TableUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class SalesPanel extends JPanel implements RefreshableView {

    private final User currentUser;
    private final ProductService productService;
    private final CustomerService customerService;
    private final SaleService saleService;
    private final List<Product> products = new ArrayList<>();
    private final List<SaleDetail> cart = new ArrayList<>();
    private final DefaultTableModel productsModel = TableUtils.nonEditableModel(
            "ID", "Código", "Producto", "Precio", "Stock"
    );
    private final DefaultTableModel cartModel = TableUtils.nonEditableModel(
            "ID", "Código", "Producto", "Cantidad", "Precio", "Subtotal"
    );
    private JTable productsTable;
    private JTable cartTable;
    private JTextField searchField;
    private JComboBox<Customer> customerBox;
    private JComboBox<String> paymentBox;
    private JTextArea observationArea;
    private javax.swing.JLabel subtotalLabel;
    private javax.swing.JLabel taxLabel;
    private javax.swing.JLabel totalLabel;

    public SalesPanel(User currentUser, ProductService productService, CustomerService customerService, SaleService saleService) {
        this.currentUser = currentUser;
        this.productService = productService;
        this.customerService = customerService;
        this.saleService = saleService;
        setLayout(new BorderLayout(16, 16));
        setBackground(UIConstants.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UIConstants.titleLabel("Ventas"), BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setOpaque(false);
        searchField = new JTextField(16);
        JButton searchButton = new JButton("Buscar");
        JButton addButton = new JButton("Agregar al carrito");
        JButton removeButton = new JButton("Quitar");
        JButton refreshButton = new JButton("Recargar");
        UIConstants.styleSecondaryButton(searchButton);
        UIConstants.stylePrimaryButton(addButton);
        UIConstants.styleSecondaryButton(removeButton);
        UIConstants.styleSecondaryButton(refreshButton);
        searchButton.addActionListener(event -> loadProducts());
        refreshButton.addActionListener(event -> reloadData());
        addButton.addActionListener(event -> addSelectedProduct());
        removeButton.addActionListener(event -> removeSelectedItem());
        controls.add(searchField);
        controls.add(searchButton);
        controls.add(addButton);
        controls.add(removeButton);
        controls.add(refreshButton);
        top.add(controls, BorderLayout.EAST);

        productsTable = new JTable(productsModel);
        cartTable = new JTable(cartModel);
        TableUtils.configureTable(productsTable);
        TableUtils.configureTable(cartTable);

        JPanel rightPanel = new JPanel(new BorderLayout(12, 12));
        rightPanel.setBackground(UIConstants.SURFACE);
        rightPanel.setBorder(UIConstants.cardBorder());

        JPanel form = new JPanel(new java.awt.GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        customerBox = new JComboBox<>();
        paymentBox = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia", "Mixto"});
        observationArea = new JTextArea(4, 18);
        subtotalLabel = UIConstants.titleLabel(CurrencyUtils.format(BigDecimal.ZERO));
        taxLabel = UIConstants.titleLabel(CurrencyUtils.format(BigDecimal.ZERO));
        totalLabel = UIConstants.titleLabel(CurrencyUtils.format(BigDecimal.ZERO));

        form.add(UIConstants.sectionLabel("Cliente"));
        form.add(customerBox);
        form.add(UIConstants.sectionLabel("Método de pago"));
        form.add(paymentBox);
        form.add(UIConstants.sectionLabel("Observación"));
        form.add(new JScrollPane(observationArea));

        JPanel summaryPanel = new JPanel(new java.awt.GridLayout(0, 1, 6, 6));
        summaryPanel.setOpaque(false);
        summaryPanel.add(UIConstants.sectionLabel("Subtotal"));
        summaryPanel.add(subtotalLabel);
        summaryPanel.add(UIConstants.sectionLabel("Impuesto"));
        summaryPanel.add(taxLabel);
        summaryPanel.add(UIConstants.sectionLabel("Total"));
        summaryPanel.add(totalLabel);

        JButton processButton = new JButton("Procesar venta");
        JButton clearButton = new JButton("Limpiar carrito");
        UIConstants.stylePrimaryButton(processButton);
        UIConstants.styleSecondaryButton(clearButton);
        processButton.addActionListener(event -> processSale());
        clearButton.addActionListener(event -> clearCart());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(clearButton);
        actions.add(processButton);

        rightPanel.add(form, BorderLayout.NORTH);
        rightPanel.add(summaryPanel, BorderLayout.CENTER);
        rightPanel.add(actions, BorderLayout.SOUTH);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(productsTable),
                new JScrollPane(cartTable));
        centerSplit.setResizeWeight(0.58);

        add(top, BorderLayout.NORTH);
        add(centerSplit, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    @Override
    public void reloadData() {
        loadProducts();
        loadCustomers();
        clearCart();
    }

    private void loadProducts() {
        try {
            products.clear();
            products.addAll(productService.findProductsForSale(searchField.getText()));
            productsModel.setRowCount(0);
            for (Product product : products) {
                productsModel.addRow(new Object[]{
                    product.getId(),
                    product.getCode(),
                    product.getName(),
                    CurrencyUtils.format(product.getSalePrice()),
                    product.getStock()
                });
            }
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar productos para venta.\n" + exception.getMessage());
        }
    }

    private void loadCustomers() {
        try {
            DefaultComboBoxModel<Customer> comboModel = new DefaultComboBoxModel<>();
            Customer noCustomer = new Customer();
            noCustomer.setName("Sin cliente");
            comboModel.addElement(noCustomer);
            for (Customer customer : customerService.findCustomers("")) {
                comboModel.addElement(customer);
            }
            customerBox.setModel(comboModel);
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar clientes.\n" + exception.getMessage());
        }
    }

    private void addSelectedProduct() {
        Product selectedProduct = getSelectedProduct();
        if (selectedProduct == null) {
            MessageUtils.showWarning(this, "Seleccione un producto.");
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Cantidad", "Agregar producto", JOptionPane.QUESTION_MESSAGE);
        if (input == null) {
            return;
        }
        try {
            int quantity = Integer.parseInt(input.trim());
            if (quantity <= 0 || quantity > selectedProduct.getStock()) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero y menor o igual al stock.");
            }
            SaleDetail existing = findDetail(selectedProduct.getId());
            int newQuantity = quantity;
            if (existing != null) {
                newQuantity += existing.getQuantity();
                if (newQuantity > selectedProduct.getStock()) {
                    throw new IllegalArgumentException("La cantidad total supera el stock disponible.");
                }
                existing.setQuantity(newQuantity);
            } else {
                SaleDetail detail = new SaleDetail();
                detail.setProduct(selectedProduct);
                detail.setQuantity(quantity);
                detail.setUnitPrice(selectedProduct.getSalePrice());
                cart.add(detail);
            }
            refreshCartTable();
        } catch (NumberFormatException exception) {
            MessageUtils.showWarning(this, "La cantidad debe ser numérica.");
        } catch (IllegalArgumentException exception) {
            MessageUtils.showWarning(this, exception.getMessage());
        }
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            MessageUtils.showWarning(this, "Seleccione un item del carrito.");
            return;
        }
        cart.remove(cartTable.convertRowIndexToModel(selectedRow));
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (SaleDetail detail : cart) {
            BigDecimal lineSubtotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            BigDecimal lineTax = lineSubtotal.multiply(AppConfig.TAX_RATE);
            BigDecimal lineTotal = lineSubtotal.add(lineTax);
            cartModel.addRow(new Object[]{
                detail.getProduct().getId(),
                detail.getProduct().getCode(),
                detail.getProduct().getName(),
                detail.getQuantity(),
                CurrencyUtils.format(detail.getUnitPrice()),
                CurrencyUtils.format(lineSubtotal)
            });
            subtotal = subtotal.add(lineSubtotal);
            tax = tax.add(lineTax);
            total = total.add(lineTotal);
        }
        subtotalLabel.setText(CurrencyUtils.format(subtotal));
        taxLabel.setText(CurrencyUtils.format(tax));
        totalLabel.setText(CurrencyUtils.format(total));
    }

    private void processSale() {
        try {
            Customer customer = (Customer) customerBox.getSelectedItem();
            if (customer != null && customer.getId() == null) {
                customer = null;
            }
            Sale sale = saleService.prepareSale(
                    currentUser,
                    customer,
                    new ArrayList<>(cart),
                    (String) paymentBox.getSelectedItem(),
                    observationArea.getText()
            );
            Long saleId = saleService.createSale(sale);
            sale.setId(saleId);
            sale.setSaleDate(LocalDateTime.now());
            showTicket(sale);
            loadProducts();
            clearCart();
            MessageUtils.showInfo(this, "Venta registrada correctamente.");
        } catch (IllegalArgumentException | SQLException exception) {
            MessageUtils.showError(this, exception.getMessage());
        }
    }

    private void clearCart() {
        cart.clear();
        observationArea.setText("");
        refreshCartTable();
    }

    private void showTicket(Sale sale) {
        StringBuilder ticket = new StringBuilder();
        ticket.append("STOREMANAGER").append('\n');
        ticket.append("Venta #").append(sale.getId()).append('\n');
        ticket.append("Fecha: ").append(sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER)).append('\n');
        ticket.append("Cajero: ").append(currentUser.getFullName()).append('\n');
        ticket.append("Cliente: ").append(sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName()).append("\n\n");
        for (SaleDetail detail : sale.getDetails()) {
            ticket.append(detail.getProduct().getName())
                    .append(" x").append(detail.getQuantity())
                    .append("  ").append(CurrencyUtils.format(detail.getLineTotal()))
                    .append('\n');
        }
        ticket.append("\nSubtotal: ").append(CurrencyUtils.format(sale.getSubtotal())).append('\n');
        ticket.append("Impuesto: ").append(CurrencyUtils.format(sale.getTax())).append('\n');
        ticket.append("Total: ").append(CurrencyUtils.format(sale.getTotal())).append('\n');
        ticket.append("Pago: ").append(sale.getPaymentMethod()).append('\n');
        JTextArea area = new JTextArea(ticket.toString());
        area.setEditable(false);
        area.setRows(18);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Ticket de venta", JOptionPane.INFORMATION_MESSAGE);
    }

    private Product getSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        return products.get(productsTable.convertRowIndexToModel(selectedRow));
    }

    private SaleDetail findDetail(Long productId) {
        for (SaleDetail detail : cart) {
            if (detail.getProduct().getId().equals(productId)) {
                return detail;
            }
        }
        return null;
    }
}
