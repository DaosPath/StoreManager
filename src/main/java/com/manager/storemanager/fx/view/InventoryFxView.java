package com.manager.storemanager.fx.view;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.InventoryMovement;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.InventoryService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class InventoryFxView implements FxView {

    private final User currentUser;
    private final InventoryService inventoryService;
    private final VBox root = new VBox(18);
    private final ObservableList<Product> stockRows = FXCollections.observableArrayList();
    private final ObservableList<InventoryMovement> movementRows = FXCollections.observableArrayList();
    private final TableView<Product> stockTable = new TableView<>(stockRows);
    private final TableView<InventoryMovement> movementTable = new TableView<>(movementRows);

    public InventoryFxView(User currentUser, InventoryService inventoryService) {
        this.currentUser = currentUser;
        this.inventoryService = inventoryService;
        root.getStyleClass().add("module-root");
        root.getChildren().addAll(buildHeader(), buildSplitPane());
        buildTables();
    }

    @Override
    public String getName() {
        return "Inventario";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            stockRows.setAll(inventoryService.findCurrentStock());
            movementRows.setAll(inventoryService.findMovements());
        } catch (Exception exception) {
            FxSupport.showError("Inventario", exception.getMessage());
        }
    }

    private Node buildHeader() {
        HBox header = new HBox(10);
        header.getChildren().add(FxSupport.pageHeader("Inventario", "Control de stock actual y movimientos registrados."));
        Button entryButton = button("Registrar entrada", "button-primary", event -> registerEntry());
        Button refreshButton = button("Actualizar", "button-secondary", event -> refresh());
        header.getChildren().addAll(FxSupport.spacer(), entryButton, refreshButton);
        return header;
    }

    private Node buildSplitPane() {
        VBox stockPane = new VBox(12, new Label("Stock actual"), stockTable);
        stockPane.getStyleClass().add("surface-pane");
        VBox.setVgrow(stockTable, Priority.ALWAYS);

        VBox movementPane = new VBox(12, new Label("Historial de movimientos"), movementTable);
        movementPane.getStyleClass().add("surface-pane");
        VBox.setVgrow(movementTable, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane(stockPane, movementPane);
        splitPane.setDividerPositions(0.46);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        return splitPane;
    }

    private void buildTables() {
        stockTable.getColumns().add(textColumn("Codigo", Product::getCode));
        stockTable.getColumns().add(textColumn("Producto", Product::getName));
        stockTable.getColumns().add(textColumn("Categoria", product -> product.getCategory() == null ? "" : product.getCategory().getName()));
        stockTable.getColumns().add(textColumn("Stock", product -> String.valueOf(product.getStock())));
        stockTable.getColumns().add(textColumn("Minimo", product -> String.valueOf(product.getMinimumStock())));
        stockTable.getColumns().add(textColumn("Estado", Product::getStatus));
        stockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        movementTable.getColumns().add(movementColumn("Fecha", movement -> movement.getMovementDate() == null ? "" : movement.getMovementDate().format(AppConfig.DATE_TIME_FORMATTER)));
        movementTable.getColumns().add(movementColumn("Producto", movement -> movement.getProduct() == null ? "" : movement.getProduct().getName()));
        movementTable.getColumns().add(movementColumn("Tipo", InventoryMovement::getMovementType));
        movementTable.getColumns().add(movementColumn("Cantidad", movement -> String.valueOf(movement.getQuantity())));
        movementTable.getColumns().add(movementColumn("Anterior", movement -> String.valueOf(movement.getPreviousStock())));
        movementTable.getColumns().add(movementColumn("Nuevo", movement -> String.valueOf(movement.getNewStock())));
        movementTable.getColumns().add(movementColumn("Motivo", InventoryMovement::getReason));
        movementTable.getColumns().add(movementColumn("Usuario", movement -> movement.getUser() == null ? "" : movement.getUser().getFullName()));
        movementTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void registerEntry() {
        Product selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxSupport.showError("Inventario", "Selecciona un producto.");
            return;
        }
        try {
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle("Registrar entrada");
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/storemanager.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("app-root");
            ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField quantityField = new TextField();
            TextField reasonField = new TextField();
            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.setPadding(new Insets(14));
            addRow(form, 0, "Cantidad", quantityField);
            addRow(form, 1, "Motivo", reasonField);
            dialog.getDialogPane().setContent(form);
            dialog.setResultConverter(button -> button == saveType ? new String[]{quantityField.getText().trim(), reasonField.getText().trim()} : null);

            String[] values = dialog.showAndWait().orElse(null);
            if (values == null) {
                return;
            }
            inventoryService.registerStockEntry(selected, Integer.parseInt(values[0]), values[1], currentUser);
            refresh();
            FxSupport.showInfo("Inventario", "Entrada registrada.");
        } catch (Exception exception) {
            FxSupport.showError("Inventario", exception.getMessage());
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

    private TableColumn<Product, String> textColumn(String title, java.util.function.Function<Product, String> mapper) {
        TableColumn<Product, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(mapper.apply(cell.getValue())));
        return column;
    }

    private TableColumn<InventoryMovement, String> movementColumn(String title, java.util.function.Function<InventoryMovement, String> mapper) {
        TableColumn<InventoryMovement, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(mapper.apply(cell.getValue())));
        return column;
    }
}
