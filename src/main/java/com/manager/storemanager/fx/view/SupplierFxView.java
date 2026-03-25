package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.service.SupplierService;
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

public class SupplierFxView implements FxView {

    private final SupplierService supplierService;
    private final VBox root = new VBox(18);
    private final ObservableList<Supplier> rows = FXCollections.observableArrayList();
    private final TableView<Supplier> table = new TableView<>(rows);
    private final TextField searchField = new TextField();

    public SupplierFxView(SupplierService supplierService) {
        this.supplierService = supplierService;
        root.getStyleClass().add("module-root");
        root.getChildren().addAll(buildHeader(), buildPane());
        buildTable();
    }

    @Override
    public String getName() {
        return "Proveedores";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            rows.setAll(supplierService.findSuppliers(searchField.getText()));
        } catch (Exception exception) {
            FxSupport.showError("Proveedores", exception.getMessage());
        }
    }

    private Node buildHeader() {
        HBox header = new HBox(10);
        header.getChildren().add(FxSupport.pageHeader("Proveedores", "Gestion de proveedores y contactos de compra."));
        searchField.setPromptText("Buscar por nombre, telefono o correo");
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
        table.getColumns().add(textColumn("Nombre", Supplier::getName));
        table.getColumns().add(textColumn("Telefono", Supplier::getPhone));
        table.getColumns().add(textColumn("Correo", Supplier::getEmail));
        table.getColumns().add(textColumn("Direccion", Supplier::getAddress));
        table.getColumns().add(textColumn("Activo", supplier -> supplier.isActive() ? "Si" : "No"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void openDialog(Supplier existing) {
        try {
            Dialog<Supplier> dialog = new Dialog<>();
            dialog.setTitle(existing == null ? "Nuevo proveedor" : "Editar proveedor");
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/storemanager.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("app-root");

            ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField nameField = new TextField();
            TextField phoneField = new TextField();
            TextField emailField = new TextField();
            TextField addressField = new TextField();
            CheckBox activeBox = new CheckBox("Activo");
            activeBox.setSelected(true);

            if (existing != null) {
                nameField.setText(existing.getName());
                phoneField.setText(existing.getPhone());
                emailField.setText(existing.getEmail());
                addressField.setText(existing.getAddress());
                activeBox.setSelected(existing.isActive());
            }

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.setPadding(new Insets(14));
            addRow(form, 0, "Nombre", nameField);
            addRow(form, 1, "Telefono", phoneField);
            addRow(form, 2, "Correo", emailField);
            addRow(form, 3, "Direccion", addressField);
            addRow(form, 4, "Estado", activeBox);
            dialog.getDialogPane().setContent(form);

            dialog.setResultConverter(button -> {
                if (button != saveType) {
                    return null;
                }
                Supplier supplier = existing == null ? new Supplier() : existing;
                supplier.setName(nameField.getText().trim());
                supplier.setPhone(phoneField.getText().trim());
                supplier.setEmail(emailField.getText().trim());
                supplier.setAddress(addressField.getText().trim());
                supplier.setActive(activeBox.isSelected());
                return supplier;
            });

            Supplier supplier = dialog.showAndWait().orElse(null);
            if (supplier == null) {
                return;
            }
            if (supplier.getId() == null) {
                supplierService.save(supplier);
            } else {
                supplierService.update(supplier);
            }
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Proveedores", exception.getMessage());
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

    private TableColumn<Supplier, String> textColumn(String title, java.util.function.Function<Supplier, String> mapper) {
        TableColumn<Supplier, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(mapper.apply(cell.getValue())));
        return column;
    }
}
