package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Category;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.util.CurrencyUtils;
import java.math.BigDecimal;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ProductFxView implements FxView {

    private final ProductService productService;
    private final VBox root = new VBox(18);
    private final ObservableList<Product> rows = FXCollections.observableArrayList();
    private final TableView<Product> table = new TableView<>(rows);
    private final TextField searchField = new TextField();

    public ProductFxView(ProductService productService) {
        this.productService = productService;
        root.getStyleClass().add("module-root");
        root.getChildren().addAll(buildHeader(), buildTablePane());
        buildTable();
    }

    @Override
    public String getName() {
        return "Productos";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            rows.setAll(productService.findProducts(searchField.getText()));
        } catch (Exception exception) {
            FxSupport.showError("Productos", exception.getMessage());
        }
    }

    private Node buildHeader() {
        HBox header = new HBox(10);
        VBox title = FxSupport.pageHeader("Productos", "Registro, edicion y desactivacion del catalogo.");

        searchField.setPromptText("Buscar por nombre o codigo");
        searchField.setPrefWidth(220);

        Button searchButton = button("Buscar", "button-secondary", event -> refresh());
        Button addButton = button("Nuevo", "button-primary", event -> openDialog(null));
        Button editButton = button("Editar", "button-secondary", event -> openDialog(table.getSelectionModel().getSelectedItem()));
        Button deleteButton = button("Desactivar", "button-danger", event -> deactivateSelected());

        header.getChildren().addAll(title, FxSupport.spacer(), searchField, searchButton, addButton, editButton, deleteButton);
        return header;
    }

    private Node buildTablePane() {
        VBox pane = new VBox(table);
        pane.getStyleClass().add("surface-pane");
        VBox.setVgrow(table, Priority.ALWAYS);
        return pane;
    }

    private void buildTable() {
        table.getColumns().add(textColumn("Codigo", product -> product.getCode()));
        table.getColumns().add(textColumn("Nombre", Product::getName));
        table.getColumns().add(textColumn("Categoria", product -> product.getCategory() == null ? "" : product.getCategory().getName()));
        table.getColumns().add(textColumn("Proveedor", product -> product.getSupplier() == null ? "" : product.getSupplier().getName()));
        table.getColumns().add(textColumn("Compra", product -> CurrencyUtils.format(product.getPurchasePrice())));
        table.getColumns().add(textColumn("Venta", product -> CurrencyUtils.format(product.getSalePrice())));
        table.getColumns().add(textColumn("Stock", product -> String.valueOf(product.getStock())));
        table.getColumns().add(textColumn("Minimo", product -> String.valueOf(product.getMinimumStock())));
        table.getColumns().add(textColumn("Estado", Product::getStatus));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void openDialog(Product existing) {
        try {
            Product result = showProductDialog(existing);
            if (result == null) {
                return;
            }
            if (result.getId() == null) {
                productService.save(result);
                FxSupport.showInfo("Productos", "Producto registrado.");
            } else {
                productService.update(result);
                FxSupport.showInfo("Productos", "Producto actualizado.");
            }
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Productos", exception.getMessage());
        }
    }

    private void deactivateSelected() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxSupport.showError("Productos", "Selecciona un producto.");
            return;
        }
        if (!FxSupport.confirm("Productos", "Se desactivara el producto " + selected.getName() + ".")) {
            return;
        }
        try {
            productService.deactivate(selected.getId());
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Productos", exception.getMessage());
        }
    }

    private Product showProductDialog(Product existing) throws Exception {
        List<Category> categories = productService.findCategories();
        List<Supplier> suppliers = productService.findSuppliers();

        Supplier noSupplier = new Supplier();
        noSupplier.setName("Sin proveedor");

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo producto" : "Editar producto");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/storemanager.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("app-root");

        ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField codeField = new TextField();
        TextField nameField = new TextField();
        ComboBox<Category> categoryBox = new ComboBox<>(FXCollections.observableArrayList(categories));
        ComboBox<Supplier> supplierBox = new ComboBox<>();
        supplierBox.getItems().add(noSupplier);
        supplierBox.getItems().addAll(suppliers);
        TextArea descriptionArea = new TextArea();
        TextField purchaseField = new TextField();
        TextField saleField = new TextField();
        TextField stockField = new TextField();
        TextField minimumField = new TextField();
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("ACTIVO", "INACTIVO"));
        statusBox.getSelectionModel().select("ACTIVO");

        if (existing != null) {
            codeField.setText(existing.getCode());
            nameField.setText(existing.getName());
            descriptionArea.setText(existing.getDescription());
            purchaseField.setText(existing.getPurchasePrice().toPlainString());
            saleField.setText(existing.getSalePrice().toPlainString());
            stockField.setText(String.valueOf(existing.getStock()));
            minimumField.setText(String.valueOf(existing.getMinimumStock()));
            statusBox.getSelectionModel().select(existing.getStatus());
            selectCategory(categoryBox, existing.getCategory());
            selectSupplier(supplierBox, existing.getSupplier(), noSupplier);
        } else {
            if (!categories.isEmpty()) {
                categoryBox.getSelectionModel().selectFirst();
            }
            supplierBox.getSelectionModel().select(noSupplier);
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(14));
        addRow(form, 0, "Codigo", codeField);
        addRow(form, 1, "Nombre", nameField);
        addRow(form, 2, "Categoria", categoryBox);
        addRow(form, 3, "Proveedor", supplierBox);
        addRow(form, 4, "Descripcion", descriptionArea);
        addRow(form, 5, "Precio compra", purchaseField);
        addRow(form, 6, "Precio venta", saleField);
        addRow(form, 7, "Stock", stockField);
        addRow(form, 8, "Stock minimo", minimumField);
        addRow(form, 9, "Estado", statusBox);

        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(button -> {
            if (button != saveType) {
                return null;
            }
            Product product = existing == null ? new Product() : existing;
            product.setCode(codeField.getText().trim());
            product.setName(nameField.getText().trim());
            product.setCategory(categoryBox.getValue());
            product.setSupplier(supplierBox.getValue() == noSupplier ? null : supplierBox.getValue());
            product.setDescription(descriptionArea.getText().trim());
            product.setPurchasePrice(new BigDecimal(purchaseField.getText().trim()));
            product.setSalePrice(new BigDecimal(saleField.getText().trim()));
            product.setStock(Integer.parseInt(stockField.getText().trim()));
            product.setMinimumStock(Integer.parseInt(minimumField.getText().trim()));
            product.setStatus(statusBox.getValue());
            return product;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void selectCategory(ComboBox<Category> comboBox, Category current) {
        if (current == null) {
            return;
        }
        for (Category category : comboBox.getItems()) {
            if (category.getId().equals(current.getId())) {
                comboBox.getSelectionModel().select(category);
                return;
            }
        }
    }

    private void selectSupplier(ComboBox<Supplier> comboBox, Supplier current, Supplier noSupplier) {
        if (current == null || current.getId() == null) {
            comboBox.getSelectionModel().select(noSupplier);
            return;
        }
        for (Supplier supplier : comboBox.getItems()) {
            if (supplier != null && supplier.getId() != null && supplier.getId().equals(current.getId())) {
                comboBox.getSelectionModel().select(supplier);
                return;
            }
        }
        comboBox.getSelectionModel().select(noSupplier);
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
}
