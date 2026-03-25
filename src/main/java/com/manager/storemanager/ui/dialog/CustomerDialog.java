package com.manager.storemanager.ui.dialog;

import com.manager.storemanager.model.Customer;
import com.manager.storemanager.ui.UIConstants;
import com.manager.storemanager.util.MessageUtils;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CustomerDialog extends JDialog {

    private final Customer source;
    private Customer result;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField documentField;
    private JTextField addressField;
    private JCheckBox activeBox;

    public CustomerDialog(Frame owner, Customer source) {
        super(owner, true);
        this.source = source;
        setTitle(source == null ? "Nuevo cliente" : "Editar cliente");
        setSize(460, 320);
        setLocationRelativeTo(owner);
        initComponents();
        if (source != null) {
            populate(source);
        }
    }

    public Customer getResult() {
        return result;
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(UIConstants.cardBorder());
        form.setBackground(UIConstants.SURFACE);

        nameField = new JTextField();
        phoneField = new JTextField();
        documentField = new JTextField();
        addressField = new JTextField();
        activeBox = new JCheckBox("Activo", true);
        activeBox.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addField(form, gbc, 0, "Nombre", nameField);
        addField(form, gbc, 1, "Teléfono", phoneField);
        addField(form, gbc, 2, "Documento", documentField);
        addField(form, gbc, 3, "Dirección", addressField);
        addField(form, gbc, 4, "Estado", activeBox);

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
        gbc.weightx = 0.30;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.70;
        panel.add(component, gbc);
    }

    private void populate(Customer customer) {
        nameField.setText(customer.getName());
        phoneField.setText(customer.getPhone());
        documentField.setText(customer.getDocument());
        addressField.setText(customer.getAddress());
        activeBox.setSelected(customer.isActive());
    }

    private void save() {
        if (nameField.getText().trim().isEmpty()) {
            MessageUtils.showWarning(this, "El nombre es obligatorio.");
            return;
        }
        Customer customer = source == null ? new Customer() : source;
        customer.setName(nameField.getText().trim());
        customer.setPhone(phoneField.getText().trim());
        customer.setDocument(documentField.getText().trim());
        customer.setAddress(addressField.getText().trim());
        customer.setActive(activeBox.isSelected());
        result = customer;
        dispose();
    }
}
