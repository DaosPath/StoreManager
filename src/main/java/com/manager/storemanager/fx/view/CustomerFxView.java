package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Customer;
import com.manager.storemanager.service.CustomerService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CustomerFxView implements FxView {

    private final CustomerService customerService;
    private final VBox root = new VBox(18);
    private final ObservableList<Customer> rows = FXCollections.observableArrayList();
    private final TableView<Customer> table = new TableView<>(rows);
    private final TextField searchField = new TextField();

    public CustomerFxView(CustomerService customerService) {
        this.customerService = customerService;
        root.getStyleClass().add("module-root");
        root.getChildren().addAll(buildHeader(), buildPane());
        buildTable();
    }

    @Override
    public String getName() {
        return "Clientes";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            rows.setAll(customerService.findCustomers(searchField.getText()));
        } catch (Exception exception) {
            FxSupport.showError("Clientes", exception.getMessage());
        }
    }

    private Node buildHeader() {
        HBox header = new HBox(10);
        header.getChildren().add(FxSupport.pageHeader("Clientes", "Gestion de clientes y datos de facturacion."));
        searchField.setPromptText("Buscar por nombre, telefono o documento");
        Button searchButton = button("Buscar", "button-secondary", event -> refresh());
        Button addButton = button("Nuevo", "button-primary", event -> openDialog(null));
        Button editButton = button("Editar", "button-secondary", event -> openDialog(table.getSelectionModel().getSelectedItem()));
        header.getChildren().addAll(FxSupport.spacer(), searchField, searchButton, addButton, editButton);
        return header;
    }

    private Node buildPane() {
        VBox pane = new VBox(table);
        pane.getStyleClass().add("surface-pane");
        VBox.setVgrow(table, Priority.ALWAYS);
        return pane;
    }

    private void buildTable() {
        table.getColumns().add(textColumn("Nombre", Customer::getName));
        table.getColumns().add(textColumn("Telefono", Customer::getPhone));
        table.getColumns().add(textColumn("Documento", Customer::getDocument));
        table.getColumns().add(textColumn("Direccion", Customer::getAddress));
        table.getColumns().add(textColumn("Activo", customer -> customer.isActive() ? "Si" : "No"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void openDialog(Customer existing) {
        try {
            Dialog<Customer> dialog = new Dialog<>();
            dialog.setTitle(existing == null ? "Nuevo cliente" : "Editar cliente");
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/storemanager.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("app-root");

            ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField nameField = new TextField();
            TextField phoneField = new TextField();
            TextField documentField = new TextField();
            TextField addressField = new TextField();
            CheckBox activeBox = new CheckBox("Activo");
            activeBox.setSelected(true);

            if (existing != null) {
                nameField.setText(existing.getName());
                phoneField.setText(existing.getPhone());
                documentField.setText(existing.getDocument());
                addressField.setText(existing.getAddress());
                activeBox.setSelected(existing.isActive());
            }

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.setPadding(new Insets(14));
            addRow(form, 0, "Nombre", nameField);
            addRow(form, 1, "Telefono", phoneField);
            addRow(form, 2, "Documento", documentField);
            addRow(form, 3, "Direccion", addressField);
            addRow(form, 4, "Estado", activeBox);
            dialog.getDialogPane().setContent(form);

            dialog.setResultConverter(button -> {
                if (button != saveType) {
                    return null;
                }
                Customer customer = existing == null ? new Customer() : existing;
                customer.setName(nameField.getText().trim());
                customer.setPhone(phoneField.getText().trim());
                customer.setDocument(documentField.getText().trim());
                customer.setAddress(addressField.getText().trim());
                customer.setActive(activeBox.isSelected());
                return customer;
            });

            Customer customer = dialog.showAndWait().orElse(null);
            if (customer == null) {
                return;
            }
            if (customer.getId() == null) {
                customerService.save(customer);
            } else {
                customerService.update(customer);
            }
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Clientes", exception.getMessage());
        }
    }

    private void addRow(GridPane grid, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private Button button(String text, String styleClass, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", styleClass);
        button.setOnAction(action);
        return button;
    }

    private TableColumn<Customer, String> textColumn(String title, java.util.function.Function<Customer, String> mapper) {
        TableColumn<Customer, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(mapper.apply(cell.getValue())));
        return column;
    }
}
