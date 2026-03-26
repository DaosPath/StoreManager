package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Category;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.util.CurrencyUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class ProductFxView implements FxView {

    private final ProductService productService;
    private final VBox root = new VBox(14);
    private final ObservableList<Product> visibleRows = FXCollections.observableArrayList();
    private final ListView<Product> listView = new ListView<>(visibleRows);
    private final TextField searchField = new TextField();
    private final Button filterButton = new Button("Filtros");
    private final Button editButton = button("Editar", "button-secondary", event -> openSelected());
    private final VBox filterPanel = new VBox(12);
    private final ComboBox<Category> filterCategoryBox = new ComboBox<>();
    private final ComboBox<String> filterStatusBox = new ComboBox<>();
    private final Category allFilterCategory = createAllFilterCategory();
    private final EventHandler<MouseEvent> sceneSelectionHandler = this::handleSceneClick;

    private List<Product> loadedProducts = new ArrayList<>();
    private Long filterCategoryId;
    private String filterStatus;
    private boolean filtersLoaded;
    private Product activeProduct;

    public ProductFxView(ProductService productService) {
        this.productService = productService;
        root.getStyleClass().addAll("module-root", "product-module");
        root.getChildren().addAll(
                FxSupport.pageHeader("Products", "Registro, edicion y desactivacion del catalogo."),
                buildToolbar(),
                buildFilterPanel(),
                buildListPane()
        );
        configureList();
        installSelectionBehavior();
        updateActionState();
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
            loadedProducts = productService.findProducts(searchField.getText());
            applyClientFilters();
        } catch (Exception exception) {
            FxSupport.showError("Productos", exception.getMessage());
        }
    }

    private Node buildToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.getStyleClass().add("product-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        HBox searchBox = new HBox(10);
        searchBox.getStyleClass().add("product-search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPrefWidth(338);

        StackPane searchIcon = new StackPane(searchGlyph());
        searchIcon.getStyleClass().add("product-search-icon-wrap");
        searchField.getStyleClass().add("product-search-field");
        searchField.setPromptText("Buscar por nombre o codigo");
        searchField.setOnAction(event -> refresh());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIcon, searchField);

        filterButton.getStyleClass().addAll("button", "button-secondary", "product-filter-button");
        filterButton.setGraphic(filterGlyph());
        filterButton.setOnAction(event -> toggleFilterPanel());

        Button addButton = button("Nuevo", "button-primary", event -> openDialog(null));
        editButton.setOnAction(event -> openSelected());

        toolbar.getChildren().addAll(searchBox, filterButton, FxSupport.spacer(), addButton, editButton);
        return toolbar;
    }

    private Node buildFilterPanel() {
        filterPanel.getStyleClass().add("product-filter-panel");
        filterPanel.setManaged(false);
        filterPanel.setVisible(false);

        configureInlineFilterCombo(filterCategoryBox);
        configureInlineFilterCombo(filterStatusBox);
        filterCategoryBox.setOnAction(event -> applyFilterSelections());
        filterStatusBox.setOnAction(event -> applyFilterSelections());

        VBox categoryField = filterField("Categoria", filterCategoryBox);
        VBox statusField = filterField("Estado", filterStatusBox);
        HBox.setHgrow(categoryField, Priority.ALWAYS);
        HBox.setHgrow(statusField, Priority.ALWAYS);

        Button clearButton = button("Limpiar", "button-secondary", event -> clearFilters());
        clearButton.getStyleClass().add("product-filter-clear");

        HBox row = new HBox(12, categoryField, statusField, clearButton);
        row.getStyleClass().add("product-filter-row");
        row.setAlignment(Pos.CENTER_LEFT);

        filterPanel.getChildren().add(row);
        return filterPanel;
    }

    private Node buildListPane() {
        VBox pane = new VBox(listView);
        pane.getStyleClass().add("product-list-pane");
        VBox.setVgrow(listView, Priority.ALWAYS);
        return pane;
    }

    private void configureList() {
        listView.getStyleClass().add("product-list-view");
        listView.setFocusTraversable(false);
        listView.setPlaceholder(emptyState());
        listView.setCellFactory(ignored -> new ProductCardCell());
        listView.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleListBackgroundClick);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateActionState());
    }

    private void updateActionState() {
        boolean hasSelection = getActiveProduct() != null;
        editButton.setDisable(!hasSelection);
    }

    private void applyClientFilters() {
        Product selectedProduct = activeProduct;
        visibleRows.setAll(
                loadedProducts.stream()
                        .filter(this::matchesCategory)
                        .filter(this::matchesStatus)
                        .toList()
        );
        restoreActiveProduct(selectedProduct);
    }

    private boolean matchesCategory(Product product) {
        if (filterCategoryId == null) {
            return true;
        }
        return product.getCategory() != null && filterCategoryId.equals(product.getCategory().getId());
    }

    private boolean matchesStatus(Product product) {
        if (filterStatus == null || filterStatus.isBlank()) {
            return true;
        }
        return filterStatus.equalsIgnoreCase(product.getStatus());
    }

    private void openSelected() {
        Product selected = getActiveProduct();
        if (selected == null) {
            FxSupport.showError("Productos", "Selecciona un producto.");
            return;
        }
        openDialog(selected);
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

    private void updateProductStatus(Product product, boolean active) {
        if (product == null || product.getId() == null) {
            return;
        }
        try {
            setActiveProduct(product);
            String newStatus = active ? "ACTIVO" : "INACTIVO";
            productService.updateStatus(product.getId(), newStatus);
            product.setStatus(newStatus);
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Productos", exception.getMessage());
            refresh();
        }
    }

    private Product showProductDialog(Product existing) throws Exception {
        List<Category> categories = productService.findCategories();
        List<Supplier> suppliers = productService.findSuppliers();

        Supplier noSupplier = new Supplier();
        noSupplier.setName("Sin proveedor");

        TextField codeField = new TextField();
        codeField.setPromptText("E.g., 123456");
        TextField nameField = new TextField();
        nameField.setPromptText("E.g., Galletas Maria");
        ComboBox<Category> categoryBox = new ComboBox<>(FXCollections.observableArrayList(categories));
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        configureDialogCombo(categoryBox);
        ComboBox<Supplier> supplierBox = new ComboBox<>();
        supplierBox.getItems().add(noSupplier);
        supplierBox.getItems().addAll(suppliers);
        supplierBox.setMaxWidth(Double.MAX_VALUE);
        configureDialogCombo(supplierBox);
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("E.g., Galletas saladas paquete de 200g");
        TextField purchaseField = new TextField();
        purchaseField.setPromptText("E.g., 15.50");
        TextField saleField = new TextField();
        saleField.setPromptText("E.g., 20.00");
        TextField stockField = new TextField();
        stockField.setPromptText("E.g., 100");
        TextField minimumField = new TextField();
        minimumField.setPromptText("E.g., 10");
        if (existing != null) {
            codeField.setText(existing.getCode());
            nameField.setText(existing.getName());
            descriptionField.setText(existing.getDescription());
            purchaseField.setText(existing.getPurchasePrice().toPlainString());
            saleField.setText(existing.getSalePrice().toPlainString());
            stockField.setText(String.valueOf(existing.getStock()));
            minimumField.setText(String.valueOf(existing.getMinimumStock()));
            selectCategory(categoryBox, existing.getCategory());
            selectSupplier(supplierBox, existing.getSupplier(), noSupplier);
        } else {
            if (!categories.isEmpty()) {
                categoryBox.getSelectionModel().selectFirst();
            }
            supplierBox.getSelectionModel().select(noSupplier);
        }

        Label feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("product-dialog-feedback");
        feedbackLabel.setManaged(false);
        feedbackLabel.setVisible(false);

        Product[] resultHolder = new Product[1];
        Stage modal = buildProductStage();

        GridPane form = new GridPane();
        form.getStyleClass().add("product-dialog-form-grid");
        form.setHgap(18);
        form.setVgap(14);
        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(50);
        leftColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(50);
        rightColumn.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().setAll(leftColumn, rightColumn);

        addDialogField(form, dialogField("Codigo", barcodeGlyph(), codeField), 0, 0);
        addDialogField(form, dialogField("Nombre", tagGlyph(), nameField), 1, 0);
        addDialogField(form, dialogField("Categoria", folderGlyph(), categoryBox), 0, 1);
        addDialogField(form, dialogField("Proveedor", userGlyph(), supplierBox), 1, 1);
        addDialogField(form, dialogField("Descripcion", documentGlyph(), descriptionField), 0, 2, 2);
        addDialogField(form, dialogField("Precio compra", currencyGlyph(), purchaseField), 0, 3);
        addDialogField(form, dialogField("Precio venta", currencyGlyph(), saleField), 1, 3);
        addDialogField(form, dialogField("Stock", cubeGlyph(), stockField), 0, 4);
        addDialogField(form, dialogField("Stock minimo", cubeGlyph(), minimumField), 1, 4);

        Button closeButton = new Button();
        closeButton.getStyleClass().add("product-dialog-close");
        closeButton.setGraphic(closeGlyph());
        closeButton.setOnAction(event -> modal.close());

        Button saveButton = button("Guardar", "button-primary", event -> {
            try {
                Product product = buildProduct(existing, noSupplier, codeField, nameField, categoryBox, supplierBox,
                        descriptionField, purchaseField, saleField, stockField, minimumField);
                hideDialogFeedback(feedbackLabel);
                resultHolder[0] = product;
                modal.close();
            } catch (IllegalArgumentException exception) {
                showDialogFeedback(feedbackLabel, exception.getMessage());
            }
        });
        saveButton.getStyleClass().add("product-dialog-save");
        saveButton.setDefaultButton(true);

        Button cancelButton = button("Cancelar", "button-secondary", event -> modal.close());
        cancelButton.getStyleClass().add("product-dialog-cancel");
        cancelButton.setCancelButton(true);

        HBox titleRow = new HBox(12);
        titleRow.getStyleClass().add("product-dialog-head");
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(existing == null ? "Nuevo producto" : "Editar producto");
        titleLabel.getStyleClass().add("product-dialog-title");
        titleRow.getChildren().addAll(titleLabel, FxSupport.spacer(), closeButton);
        installWindowDrag(modal, titleRow);

        HBox actions = new HBox(10, saveButton, cancelButton);
        actions.getStyleClass().add("product-dialog-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(18, titleRow, form, feedbackLabel, actions);
        card.getStyleClass().add("product-dialog-card");
        card.setPrefWidth(760);
        card.setMaxWidth(760);

        StackPane overlay = new StackPane(card);
        overlay.getStyleClass().add("product-dialog-overlay");
        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                modal.close();
            }
        });

        StackPane shell = new StackPane(overlay);
        shell.getStyleClass().add("product-dialog-shell");

        Scene scene = new Scene(shell);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().setAll(
                getClass().getResource("/css/base.css").toExternalForm(),
                getClass().getResource("/css/main.css").toExternalForm()
        );
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                modal.close();
                event.consume();
            }
        });

        modal.setScene(scene);
        modal.showAndWait();
        return resultHolder[0];
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

    private Product buildProduct(Product existing, Supplier noSupplier, TextField codeField, TextField nameField,
            ComboBox<Category> categoryBox, ComboBox<Supplier> supplierBox, TextField descriptionField,
            TextField purchaseField, TextField saleField, TextField stockField, TextField minimumField) {
        String code = required(codeField.getText(), "Ingresa el codigo.");
        String name = required(nameField.getText(), "Ingresa el nombre.");
        Category category = categoryBox.getValue();
        if (category == null) {
            throw new IllegalArgumentException("Selecciona una categoria.");
        }

        BigDecimal purchasePrice = parseDecimal(purchaseField.getText(), "precio de compra");
        BigDecimal salePrice = parseDecimal(saleField.getText(), "precio de venta");
        int stock = parseInteger(stockField.getText(), "stock");
        int minimumStock = parseInteger(minimumField.getText(), "stock minimo");

        if (purchasePrice.signum() < 0 || salePrice.signum() < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos.");
        }
        if (stock < 0 || minimumStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }

        Product product = existing == null ? new Product() : existing;
        product.setCode(code);
        product.setName(name);
        product.setCategory(category);
        product.setSupplier(supplierBox.getValue() == noSupplier ? null : supplierBox.getValue());
        product.setDescription(trimToEmpty(descriptionField.getText()));
        product.setPurchasePrice(purchasePrice);
        product.setSalePrice(salePrice);
        product.setStock(stock);
        product.setMinimumStock(minimumStock);
        product.setStatus(existing == null ? "ACTIVO" : existing.getStatus());
        return product;
    }

    private Node dialogField(String labelText, Node icon, Node input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("product-dialog-label");

        StackPane iconWrap = new StackPane(icon);
        iconWrap.getStyleClass().add("product-dialog-icon-wrap");

        HBox shell = new HBox(10, iconWrap, input);
        shell.getStyleClass().add("product-dialog-field-shell");
        shell.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(input, Priority.ALWAYS);

        if (input instanceof TextField textField) {
            textField.getStyleClass().add("product-dialog-input");
        }
        if (input instanceof ComboBox<?> comboBox) {
            comboBox.getStyleClass().add("product-dialog-combo");
        }

        VBox wrapper = new VBox(8, label, shell);
        wrapper.getStyleClass().add("product-dialog-field");
        return wrapper;
    }

    private void addDialogField(GridPane grid, Node field, int column, int row) {
        addDialogField(grid, field, column, row, 1);
    }

    private void addDialogField(GridPane grid, Node field, int column, int row, int columnSpan) {
        grid.add(field, column, row, columnSpan, 1);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private void toggleFilterPanel() {
        boolean open = !filterPanel.isVisible();
        if (open) {
            ensureFilterOptionsLoaded();
            syncFilterControls();
        }
        filterPanel.setManaged(open);
        filterPanel.setVisible(open);
        filterButton.getStyleClass().remove("product-filter-button-active");
        if (open) {
            filterButton.getStyleClass().add("product-filter-button-active");
        }
    }

    private void ensureFilterOptionsLoaded() {
        if (filtersLoaded) {
            return;
        }
        try {
            List<Category> categories = productService.findCategories();
            filterCategoryBox.getItems().setAll(allFilterCategory);
            filterCategoryBox.getItems().addAll(categories);
            filterStatusBox.getItems().setAll("Todos", "ACTIVO", "INACTIVO");
            filtersLoaded = true;
        } catch (Exception exception) {
            FxSupport.showError("Productos", exception.getMessage());
        }
    }

    private void syncFilterControls() {
        if (!filtersLoaded) {
            return;
        }

        filterCategoryBox.getSelectionModel().select(allFilterCategory);
        if (filterCategoryId != null) {
            for (Category category : filterCategoryBox.getItems()) {
                if (category.getId() != null && category.getId().equals(filterCategoryId)) {
                    filterCategoryBox.getSelectionModel().select(category);
                    break;
                }
            }
        }

        filterStatusBox.getSelectionModel().select(filterStatus == null ? "Todos" : filterStatus);
    }

    private void applyFilterSelections() {
        Category selectedCategory = filterCategoryBox.getValue();
        filterCategoryId = selectedCategory == null || selectedCategory.getId() == null ? null : selectedCategory.getId();
        String selectedStatus = filterStatusBox.getValue();
        filterStatus = selectedStatus == null || "Todos".equals(selectedStatus) ? null : selectedStatus;
        applyClientFilters();
    }

    private void clearFilters() {
        filterCategoryId = null;
        filterStatus = null;
        if (filtersLoaded) {
            filterCategoryBox.getSelectionModel().select(allFilterCategory);
            filterStatusBox.getSelectionModel().select("Todos");
        }
        applyClientFilters();
    }

    private VBox filterField(String labelText, ComboBox<?> comboBox) {
        Label label = new Label(labelText);
        label.getStyleClass().add("product-filter-label");

        VBox field = new VBox(6, label, comboBox);
        field.getStyleClass().add("product-filter-field");
        return field;
    }

    private <T> void configureInlineFilterCombo(ComboBox<T> comboBox) {
        configureDialogCombo(comboBox);
        comboBox.getStyleClass().add("product-inline-filter-combo");
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }

    private <T> void configureDialogCombo(ComboBox<T> comboBox) {
        comboBox.setVisibleRowCount(6);
        comboBox.setCellFactory(ignored -> new ProductDialogComboCell<>(true));
        comboBox.setButtonCell(new ProductDialogComboCell<>(false));
    }

    private Stage buildProductStage() {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.setResizable(false);
        return stage;
    }

    private void installWindowDrag(Stage stage, Node dragHandle) {
        final double[] offset = new double[2];
        dragHandle.setOnMousePressed(event -> {
            offset[0] = event.getSceneX();
            offset[1] = event.getSceneY();
        });
        dragHandle.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - offset[0]);
            stage.setY(event.getScreenY() - offset[1]);
        });
    }

    private Category createAllFilterCategory() {
        Category category = new Category();
        category.setName("Todas");
        return category;
    }

    private void installSelectionBehavior() {
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, sceneSelectionHandler);
            }
            if (newScene != null) {
                newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneSelectionHandler);
            }
        });
    }

    private void handleSceneClick(MouseEvent event) {
        if (!(event.getTarget() instanceof Node node)) {
            return;
        }
        if (hasAncestor(node, ListCell.class)) {
            return;
        }
        if (isProtectedActionTarget(node)) {
            return;
        }
        clearActiveProduct();
    }

    private void handleListBackgroundClick(MouseEvent event) {
        if (!(event.getTarget() instanceof Node node)) {
            return;
        }
        if (hasAncestor(node, ListCell.class) || hasAncestor(node, ScrollBar.class)) {
            return;
        }
        clearActiveProduct();
        event.consume();
    }

    private boolean hasAncestor(Node node, Class<?> type) {
        Node current = node;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private boolean isProtectedActionTarget(Node node) {
        return isDescendantOf(node, editButton);
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        Node current = node;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void showDialogFeedback(Label label, String message) {
        label.setText(message);
        label.setManaged(true);
        label.setVisible(true);
    }

    private void hideDialogFeedback(Label label) {
        label.setText("");
        label.setManaged(false);
        label.setVisible(false);
    }

    private String required(String value, String message) {
        String trimmed = trimToEmpty(value);
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        try {
            return new BigDecimal(required(value, "Ingresa el " + fieldName + "."));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El " + fieldName + " no es valido.");
        }
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(required(value, "Ingresa el " + fieldName + "."));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El " + fieldName + " no es valido.");
        }
    }

    private Node emptyState() {
        Label label = new Label("No hay productos para mostrar.");
        label.getStyleClass().add("product-empty-state");
        StackPane empty = new StackPane(label);
        empty.getStyleClass().add("product-empty-wrap");
        return empty;
    }

    private void restoreActiveProduct(Product selectedProduct) {
        if (visibleRows.isEmpty()) {
            clearActiveProduct();
            return;
        }

        if (selectedProduct != null) {
            for (Product product : visibleRows) {
                if (sameProduct(product, selectedProduct)) {
                    activeProduct = product;
                    listView.refresh();
                    updateActionState();
                    return;
                }
            }
        }

        clearActiveProduct();
    }

    private Product getActiveProduct() {
        return activeProduct;
    }

    private void setActiveProduct(Product product) {
        activeProduct = product;
        listView.getSelectionModel().clearSelection();
        listView.refresh();
        updateActionState();
    }

    private void clearActiveProduct() {
        activeProduct = null;
        listView.getSelectionModel().clearSelection();
        listView.refresh();
        updateActionState();
    }

    private boolean sameProduct(Product left, Product right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.getId() != null && right.getId() != null) {
            return left.getId().equals(right.getId());
        }
        if (left.getCode() != null && right.getCode() != null) {
            return left.getCode().equalsIgnoreCase(right.getCode());
        }
        return left == right;
    }

    private Button button(String text, String styleClass, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", styleClass);
        button.setOnAction(action);
        return button;
    }

    private Node searchGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M10.5 3.5a7 7 0 1 0 4.58 12.3l4.31 4.31 1.4-1.4-4.31-4.31A7 7 0 0 0 10.5 3.5zm0 2a5 5 0 1 1 0 10 5 5 0 0 1 0-10z");
        path.getStyleClass().add("product-search-icon");
        return path;
    }

    private Node filterGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 5h16v2H4zm3 6h10v2H7zm3 6h4v2h-4z");
        path.getStyleClass().add("product-filter-icon");
        return path;
    }

    private Node barcodeGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3 4h1.5v16H3zm3 0h1v16H6zm3 0h2v16H9zm4 0h1v16h-1zm3 0h1.5v16H16z");
        path.getStyleClass().add("product-dialog-icon");
        return path;
    }

    private Node tagGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 6.5V12l7.8 7.8a1.5 1.5 0 0 0 2.1 0l5.9-5.9a1.5 1.5 0 0 0 0-2.1L12 4H6.5A2.5 2.5 0 0 0 4 6.5zm3 1.5a1.5 1.5 0 1 1 0 3 1.5 1.5 0 0 1 0-3z");
        path.getStyleClass().add("product-dialog-icon");
        return path;
    }

    private Node folderGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3.5 6A2.5 2.5 0 0 1 6 3.5h3l2 2H18A2.5 2.5 0 0 1 20.5 8v8A2.5 2.5 0 0 1 18 18.5H6A2.5 2.5 0 0 1 3.5 16V6zm2 1a1 1 0 0 0-1 1v8a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V8a1 1 0 0 0-1-1h-7.6l-2-2H6a1 1 0 0 0-1 1z");
        path.getStyleClass().add("product-dialog-icon");
        return path;
    }

    private Node userGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M12 4.5a3.5 3.5 0 1 1 0 7 3.5 3.5 0 0 1 0-7zm0 9c-3.6 0-6.5 2.4-6.5 5.5v1h2v-1c0-1.8 1.9-3.5 4.5-3.5s4.5 1.7 4.5 3.5v1h2v-1c0-3.1-2.9-5.5-6.5-5.5z");
        path.getStyleClass().add("product-dialog-icon");
        return path;
    }

    private Node documentGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M6 3.5h7l4 4V19a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 5 19V5A1.5 1.5 0 0 1 6.5 3.5H6zm6 1.8V8h2.7L12 5.3zM7 10h8v1.5H7zm0 4h8v1.5H7z");
        path.getStyleClass().add("product-dialog-icon");
        return path;
    }

    private Node currencyGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M11 3.5h2V6c2.5.2 4 1.6 4 3.7h-2c0-1.2-.9-1.9-2.5-2v4.1c3.2.6 4.8 1.8 4.8 4.3 0 2.4-1.9 3.9-4.8 4.1v2.3h-2V20c-3-.2-4.9-1.8-5-4.4h2c.1 1.5 1.1 2.3 3 2.5v-4.2c-3-.6-4.6-1.7-4.6-4.1 0-2.3 1.8-3.8 4.6-4V3.5zm0 3.8c-1.5.2-2.3.9-2.3 2 0 1 .7 1.6 2.3 2V7.3zm2 10.8c1.7-.2 2.7-.9 2.7-2.1 0-1.1-.8-1.7-2.7-2.1v4.2z");
        path.getStyleClass().add("product-dialog-icon");
        return path;
    }

    private Node cubeGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M12 3.5 5 7v10l7 3.5 7-3.5V7l-7-3.5zm0 1.8 4.8 2.4L12 10.1 7.2 7.7 12 5.3zm-5 4 4 2v5.5l-4-2V9.3zm6 7.5v-5.5l4-2v5.5l-4 2z");
        path.getStyleClass().add("product-dialog-icon");
        return path;
    }

    private Node closeGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M6 6l12 12M18 6 6 18");
        path.getStyleClass().add("product-dialog-close-icon");
        return path;
    }

    private static final class ProductDialogComboCell<T> extends ListCell<T> {

        private final boolean popupCell;

        private ProductDialogComboCell(boolean popupCell) {
            this.popupCell = popupCell;
            getStyleClass().add(popupCell ? "product-dialog-popup-cell" : "product-dialog-button-cell");
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            setText(String.valueOf(item));
            setGraphic(null);
        }
    }

    private final class ProductCardCell extends ListCell<Product> {

        private ProductCardCell() {
            getStyleClass().add("product-card-cell");
            setOnMouseClicked(event -> {
                if (!isEmpty() && getItem() != null) {
                    setActiveProduct(getItem());
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(Product product, boolean empty) {
            super.updateItem(product, empty);
            if (empty || product == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            HBox row = new HBox(14);
            row.getStyleClass().add("product-card-row");
            row.setAlignment(Pos.CENTER_LEFT);
            if (isActiveProduct(product)) {
                row.getStyleClass().add("product-card-row-active");
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(
                    column(product.getCode(), "product-col-code", 84),
                    column(product.getName(), "product-col-name", 160),
                    column(product.getCategory() == null ? "" : product.getCategory().getName(), "product-col-text", 110),
                    column(product.getSupplier() == null ? "" : product.getSupplier().getName(), "product-col-text", 152),
                    column(CurrencyUtils.format(product.getPurchasePrice()), "product-col-money", 94),
                    column(CurrencyUtils.format(product.getSalePrice()), "product-col-money", 94),
                    column(String.valueOf(product.getStock()), "product-col-small", 56),
                    column(String.valueOf(product.getMinimumStock()), "product-col-small", 56),
                    spacer,
                    statusSwitchNode(product)
            );

            setText(null);
            setGraphic(row);
        }

        private Node column(String value, String styleClass, double width) {
            Label label = new Label(value);
            label.getStyleClass().add(styleClass);
            label.setMinWidth(width);
            label.setPrefWidth(width);
            label.setMaxWidth(width);
            return label;
        }

        private Node statusSwitchNode(Product product) {
            boolean enabled = !"INACTIVO".equalsIgnoreCase(product.getStatus());
            BooleanProperty activeStatus = new SimpleBooleanProperty(enabled);

            Region track = new Region();
            track.getStyleClass().add("product-row-switch-track");
            track.setMouseTransparent(true);

            Region thumb = new Region();
            thumb.getStyleClass().add("product-row-switch-thumb");
            thumb.setMouseTransparent(true);

            StackPane toggle = new StackPane(track, thumb);
            toggle.getStyleClass().add("product-row-switch");

            Label label = new Label();
            label.getStyleClass().add("product-row-switch-label");

            Runnable syncState = () -> {
                boolean active = activeStatus.get();
                track.getStyleClass().remove("product-row-switch-track-active");
                label.getStyleClass().remove("product-row-switch-label-active");
                thumb.setTranslateX(active ? 9 : -9);
                label.setText(active ? "Activo" : "Inactivo");
                if (active) {
                    track.getStyleClass().add("product-row-switch-track-active");
                    label.getStyleClass().add("product-row-switch-label-active");
                }
            };
            syncState.run();
            activeStatus.addListener((obs, oldValue, newValue) -> syncState.run());

            EventHandler<MouseEvent> toggleHandler = event -> {
                boolean nextState = !activeStatus.get();
                activeStatus.set(nextState);
                updateProductStatus(product, nextState);
                event.consume();
            };
            toggle.setOnMouseClicked(toggleHandler);
            label.setOnMouseClicked(toggleHandler);

            HBox box = new HBox(8, toggle, label);
            box.getStyleClass().add("product-row-switch-wrap");
            box.setAlignment(Pos.CENTER_LEFT);
            box.setMinWidth(118);
            box.setPrefWidth(118);
            box.setMaxWidth(118);
            return box;
        }
    }

    private boolean isActiveProduct(Product product) {
        return sameProduct(product, activeProduct);
    }
}
