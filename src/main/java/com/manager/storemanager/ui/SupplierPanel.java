package com.manager.storemanager.ui;

import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.service.SupplierService;
import com.manager.storemanager.util.MessageUtils;
import com.manager.storemanager.util.TableUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class SupplierPanel extends JPanel implements RefreshableView {

    private final SupplierService supplierService;
    private final JTextField searchField = new JTextField(22);
    private final DefaultTableModel tableModel = TableUtils.nonEditableModel(
            "ID", "Nombre", "Telefono", "Correo", "Direccion", "Activo"
    );
    private final JTable table = new JTable(tableModel);
    private List<Supplier> suppliers = new ArrayList<>();

    public SupplierPanel(SupplierService supplierService) {
        this.supplierService = supplierService;
        setLayout(new BorderLayout(16, 16));
        setBackground(UIConstants.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIConstants.titleLabel("Proveedores"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton searchButton = new JButton("Buscar");
        JButton addButton = new JButton("Nuevo");
        JButton editButton = new JButton("Editar");
        JButton refreshButton = new JButton("Actualizar");
        UIConstants.styleSecondaryButton(searchButton);
        UIConstants.stylePrimaryButton(addButton);
        UIConstants.styleSecondaryButton(editButton);
        UIConstants.styleSecondaryButton(refreshButton);
        searchButton.addActionListener(event -> reloadData());
        addButton.addActionListener(event -> openForm(null));
        editButton.addActionListener(event -> editSelected());
        refreshButton.addActionListener(event -> reloadData());
        searchField.addActionListener(event -> reloadData());
        actions.add(new JLabel("Buscar"));
        actions.add(searchField);
        actions.add(searchButton);
        actions.add(addButton);
        actions.add(editButton);
        actions.add(refreshButton);
        header.add(actions, BorderLayout.EAST);

        TableUtils.configureTable(table);
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void reloadData() {
        try {
            suppliers = supplierService.findSuppliers(searchField.getText());
            tableModel.setRowCount(0);
            for (Supplier supplier : suppliers) {
                tableModel.addRow(new Object[]{
                    supplier.getId(),
                    supplier.getName(),
                    supplier.getPhone(),
                    supplier.getEmail(),
                    supplier.getAddress(),
                    supplier.isActive() ? "Si" : "No"
                });
            }
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar los proveedores.\n" + exception.getMessage());
        }
    }

    private void editSelected() {
        Supplier supplier = getSelectedSupplier();
        if (supplier == null) {
            MessageUtils.showWarning(this, "Seleccione un proveedor.");
            return;
        }
        openForm(supplier);
    }

    private Supplier getSelectedSupplier() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return suppliers.get(table.convertRowIndexToModel(viewRow));
    }

    private void openForm(Supplier existing) {
        SupplierFormDialog dialog = new SupplierFormDialog(existing);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }
        try {
            Supplier supplier = dialog.buildSupplier();
            if (existing == null) {
                supplierService.save(supplier);
                MessageUtils.showInfo(this, "Proveedor registrado.");
            } else {
                supplier.setId(existing.getId());
                supplierService.update(supplier);
                MessageUtils.showInfo(this, "Proveedor actualizado.");
            }
            reloadData();
        } catch (IllegalArgumentException exception) {
            MessageUtils.showWarning(this, exception.getMessage());
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible guardar el proveedor.\n" + exception.getMessage());
        }
    }

    private final class SupplierFormDialog extends JDialog {

        private final JTextField nameField = new JTextField(20);
        private final JTextField phoneField = new JTextField(20);
        private final JTextField emailField = new JTextField(20);
        private final JTextField addressField = new JTextField(20);
        private final JCheckBox activeCheck = new JCheckBox("Activo", true);
        private boolean confirmed;

        private SupplierFormDialog(Supplier existing) {
            super((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(SupplierPanel.this), true);
            setTitle(existing == null ? "Nuevo proveedor" : "Editar proveedor");
            buildLayout();
            if (existing != null) {
                nameField.setText(existing.getName());
                phoneField.setText(existing.getPhone());
                emailField.setText(existing.getEmail());
                addressField.setText(existing.getAddress());
                activeCheck.setSelected(existing.isActive());
            }
            pack();
            setLocationRelativeTo(SupplierPanel.this);
        }

        private void buildLayout() {
            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UIConstants.SURFACE);
            form.setBorder(UIConstants.cardBorder());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            addRow(form, gbc, 0, "Nombre", nameField);
            addRow(form, gbc, 1, "Telefono", phoneField);
            addRow(form, gbc, 2, "Correo", emailField);
            addRow(form, gbc, 3, "Direccion", addressField);
            gbc.gridx = 1;
            gbc.gridy = 4;
            form.add(activeCheck, gbc);

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

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(footer, BorderLayout.SOUTH);
        }

        private boolean isConfirmed() {
            return confirmed;
        }

        private Supplier buildSupplier() {
            Supplier supplier = new Supplier();
            supplier.setName(nameField.getText().trim());
            supplier.setPhone(phoneField.getText().trim());
            supplier.setEmail(emailField.getText().trim());
            supplier.setAddress(addressField.getText().trim());
            supplier.setActive(activeCheck.isSelected());
            return supplier;
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
