package com.manager.storemanager.ui;

import com.manager.storemanager.model.Customer;
import com.manager.storemanager.service.CustomerService;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class CustomerPanel extends JPanel implements RefreshableView {

    private final CustomerService customerService;
    private final JTextField searchField = new JTextField(22);
    private final DefaultTableModel tableModel = TableUtils.nonEditableModel(
            "ID", "Nombre", "Telefono", "Documento", "Direccion", "Activo"
    );
    private final JTable table = new JTable(tableModel);
    private List<Customer> customers = new ArrayList<>();

    public CustomerPanel(CustomerService customerService) {
        this.customerService = customerService;
        setLayout(new BorderLayout(16, 16));
        setBackground(UIConstants.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIConstants.titleLabel("Clientes"), BorderLayout.WEST);

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
            customers = customerService.findCustomers(searchField.getText());
            tableModel.setRowCount(0);
            for (Customer customer : customers) {
                tableModel.addRow(new Object[]{
                    customer.getId(),
                    customer.getName(),
                    customer.getPhone(),
                    customer.getDocument(),
                    customer.getAddress(),
                    customer.isActive() ? "Si" : "No"
                });
            }
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible cargar los clientes.\n" + exception.getMessage());
        }
    }

    private void editSelected() {
        Customer customer = getSelectedCustomer();
        if (customer == null) {
            MessageUtils.showWarning(this, "Seleccione un cliente.");
            return;
        }
        openForm(customer);
    }

    private Customer getSelectedCustomer() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return customers.get(table.convertRowIndexToModel(viewRow));
    }

    private void openForm(Customer existing) {
        CustomerFormDialog dialog = new CustomerFormDialog(existing);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }
        try {
            Customer customer = dialog.buildCustomer();
            if (existing == null) {
                customerService.save(customer);
                MessageUtils.showInfo(this, "Cliente registrado.");
            } else {
                customer.setId(existing.getId());
                customerService.update(customer);
                MessageUtils.showInfo(this, "Cliente actualizado.");
            }
            reloadData();
        } catch (IllegalArgumentException exception) {
            MessageUtils.showWarning(this, exception.getMessage());
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible guardar el cliente.\n" + exception.getMessage());
        }
    }

    private final class CustomerFormDialog extends JDialog {

        private final JTextField nameField = new JTextField(20);
        private final JTextField phoneField = new JTextField(20);
        private final JTextField documentField = new JTextField(20);
        private final JTextField addressField = new JTextField(20);
        private final JCheckBox activeCheck = new JCheckBox("Activo", true);
        private boolean confirmed;

        private CustomerFormDialog(Customer existing) {
            super((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(CustomerPanel.this), true);
            setTitle(existing == null ? "Nuevo cliente" : "Editar cliente");
            buildLayout();
            if (existing != null) {
                nameField.setText(existing.getName());
                phoneField.setText(existing.getPhone());
                documentField.setText(existing.getDocument());
                addressField.setText(existing.getAddress());
                activeCheck.setSelected(existing.isActive());
            }
            pack();
            setLocationRelativeTo(CustomerPanel.this);
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
            addRow(form, gbc, 2, "Documento", documentField);
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

        private Customer buildCustomer() {
            Customer customer = new Customer();
            customer.setName(nameField.getText().trim());
            customer.setPhone(phoneField.getText().trim());
            customer.setDocument(documentField.getText().trim());
            customer.setAddress(addressField.getText().trim());
            customer.setActive(activeCheck.isSelected());
            return customer;
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
