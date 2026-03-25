package com.manager.storemanager.ui;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.model.InventoryMovement;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.InventoryService;
import com.manager.storemanager.util.MessageUtils;
import com.manager.storemanager.util.TableUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class InventoryPanel extends JPanel implements RefreshableView {

    private final User currentUser;
    private final InventoryService inventoryService;
    private final DefaultTableModel stockModel = TableUtils.nonEditableModel(
            "ID", "Codigo", "Nombre", "Categoria", "Stock", "Minimo", "Estado", "Alerta"
    );
    private final DefaultTableModel movementModel = TableUtils.nonEditableModel(
            "Fecha", "Tipo", "Producto", "Cantidad", "Anterior", "Nuevo", "Usuario", "Motivo"
    );
    private final JTable stockTable = new JTable(stockModel);
    private final JTable movementTable = new JTable(movementModel);
    private List<Product> currentStock = new ArrayList<>();

    public InventoryPanel(User currentUser, InventoryService inventoryService) {
        this.currentUser = currentUser;
        this.inventoryService = inventoryService;
        setLayout(new BorderLayout(16, 16));
        setBackground(UIConstants.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIConstants.titleLabel("Inventario"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton entryButton = new JButton("Entrada de mercaderia");
        JButton refreshButton = new JButton("Actualizar");
        UIConstants.stylePrimaryButton(entryButton);
        UIConstants.styleSecondaryButton(refreshButton);
        entryButton.addActionListener(event -> registerEntry());
        refreshButton.addActionListener(event -> reloadData());
        actions.add(entryButton);
        actions.add(refreshButton);
        header.add(actions, BorderLayout.EAST);

        TableUtils.configureTable(stockTable);
        TableUtils.configureTable(movementTable);

        JPanel center = new JPanel(new GridLayout(2, 1, 16, 16));
        center.setOpaque(false);
        center.add(wrapSection("Stock actual", new JScrollPane(stockTable)));
        center.add(wrapSection("Historial de movimientos", new JScrollPane(movementTable)));

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    @Override
    public void reloadData() {
        loadStock();
        loadMovements();
    }

    private void loadStock() {
        try {
            currentStock = inventoryService.findCurrentStock();
            stockModel.setRowCount(0);
            for (Product product : currentStock) {
                boolean lowStock = product.getStock() <= product.getMinimumStock();
                stockModel.addRow(new Object[]{
                    product.getId(),
                    product.getCode(),
                    product.getName(),
                    product.getCategory() == null ? "" : product.getCategory().getName(),
                    product.getStock(),
                    product.getMinimumStock(),
                    product.getStatus(),
                    lowStock ? "Stock bajo" : "OK"
                });
            }
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar el stock.\n" + exception.getMessage());
        }
    }

    private void loadMovements() {
        try {
            List<InventoryMovement> movements = inventoryService.findMovements();
            movementModel.setRowCount(0);
            for (InventoryMovement movement : movements) {
                movementModel.addRow(new Object[]{
                    movement.getMovementDate() == null ? "" : movement.getMovementDate().format(AppConfig.DATE_TIME_FORMATTER),
                    movement.getMovementType(),
                    movement.getProduct() == null ? "" : movement.getProduct().getName(),
                    movement.getQuantity(),
                    movement.getPreviousStock(),
                    movement.getNewStock(),
                    movement.getUser() == null ? "" : movement.getUser().getFullName(),
                    movement.getReason()
                });
            }
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar el historial.\n" + exception.getMessage());
        }
    }

    private void registerEntry() {
        Product product = getSelectedProduct();
        if (product == null) {
            MessageUtils.showWarning(this, "Seleccione un producto del stock.");
            return;
        }
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
        JTextField reasonField = new JTextField(20);
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Producto: " + product.getName()));
        panel.add(new JLabel("Cantidad"));
        panel.add(quantitySpinner);
        panel.add(new JLabel("Motivo"));
        panel.add(reasonField);

        int option = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Registrar entrada",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            inventoryService.registerStockEntry(product, (Integer) quantitySpinner.getValue(), reasonField.getText(), currentUser);
            MessageUtils.showInfo(this, "Entrada registrada.");
            reloadData();
        } catch (IllegalArgumentException exception) {
            MessageUtils.showWarning(this, exception.getMessage());
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible registrar la entrada.\n" + exception.getMessage());
        }
    }

    private Product getSelectedProduct() {
        int viewRow = stockTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return currentStock.get(stockTable.convertRowIndexToModel(viewRow));
    }

    private JPanel wrapSection(String title, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIConstants.SURFACE);
        panel.setBorder(UIConstants.cardBorder());
        panel.add(UIConstants.sectionLabel(title), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}
