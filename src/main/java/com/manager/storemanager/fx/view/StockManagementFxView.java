package com.manager.storemanager.fx.view;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.StockEntryItem;
import com.manager.storemanager.model.StockEntryRequest;
import com.manager.storemanager.model.StockMovement;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.StockService;
import com.manager.storemanager.service.SupplierService;
import com.manager.storemanager.util.CurrencyUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.geometry.Side;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class StockManagementFxView implements FxView {

    private enum WorkspaceMode {
        ENTRY,
        HISTORY
    }

    private enum MovementMode {
        PURCHASE,
        MANUAL
    }

    private final User currentUser;
    private final StockService stockService;
    private final ProductService productService;
    private final SupplierService supplierService;

    private final VBox root = new VBox(16);
    private final BorderPane topBar = new BorderPane();
    private final StackPane contentHost = new StackPane();

    private final ObservableList<Product> stockRows = FXCollections.observableArrayList();
    private final ObservableList<StockMovement> movementRows = FXCollections.observableArrayList();
    private final ObservableList<Product> productOptions = FXCollections.observableArrayList();
    private final ObservableList<Supplier> supplierOptions = FXCollections.observableArrayList();
    private final FilteredList<Product> filteredStockRows = new FilteredList<>(stockRows, product -> true);

    private final TableView<Product> stockTable = new TableView<>(filteredStockRows);
    private final TableView<StockMovement> movementTable = new TableView<>(movementRows);

    private final ComboBox<Supplier> supplierBox = new ComboBox<>(supplierOptions);
    private final ComboBox<String> documentTypeBox = new ComboBox<>(FXCollections.observableArrayList(
            "FACTURA", "BOLETA", "GUIA", "NOTA"));
    private final ComboBox<String> purchaseDestinationBox = new ComboBox<>(FXCollections.observableArrayList(
            "Almacen principal", "Bodega secundaria", "Piso de venta"));
    private final ComboBox<String> manualDestinationBox = new ComboBox<>(FXCollections.observableArrayList(
            "Almacen principal", "Bodega secundaria", "Piso de venta"));
    private final ComboBox<String> adjustmentTypeBox = new ComboBox<>(FXCollections.observableArrayList(
            "Conteo", "Merma", "Devolucion", "Regularizacion"));

    private final TextField seriesField = new TextField();
    private final TextField correlativeField = new TextField();
    private final TextField manualReasonField = new TextField();
    private final TextArea noteArea = new TextArea();

    private final VBox detailRowsBox = new VBox(14);
    private final List<EntryRow> detailRows = new ArrayList<>();

    private final Label previewBadge = new Label();
    private final Label previewTypeValue = new Label();
    private final Label previewReferenceValue = new Label();
    private final Label previewSupplierValue = new Label();
    private final Label previewDestinationValue = new Label();
    private final Label previewRowsValue = new Label();

    private final Label rowsSummaryValue = new Label("0 filas");
    private final Label unitsSummaryValue = new Label("0 unidades");
    private final Label amountSummaryValue = new Label(CurrencyUtils.format(BigDecimal.ZERO));

    private final Button entryBackButton = new Button();
    private final Button entryButton = new Button("Nueva entrada");
    private final Button historyButton = new Button("Historial");
    private final Button refreshHistoryButton = new Button("Actualizar");
    private final Button historyRefreshButton = new Button();
    private final Button historyReportButton = new Button("Resumen\nstock");
    private final Button backToEntryButton = new Button("Nueva\nentrada");
    private final Button addRowButton = new Button("Anadir fila");
    private final Button saveButton = new Button("Guardar movimiento");
    private final TextField stockSearchField = new TextField();

    private final VBox purchaseModeCard = movementCard(
            MovementMode.PURCHASE,
            "Compra a proveedor",
            "Modo activo",
            "Registra una compra con multiples productos y deja trazabilidad del documento.",
            purchaseGlyph()
    );
    private final VBox manualModeCard = movementCard(
            MovementMode.MANUAL,
            "Ajuste manual",
            "Uso puntual",
            "Aplica correcciones por conteo, merma, devolucion o regularizacion.",
            adjustmentGlyph()
    );

    private final VBox purchaseForm = buildPurchaseForm();
    private final VBox manualForm = buildManualForm();
    private final StackPane formHost = new StackPane();
    private final ScrollPane entryScroll = new ScrollPane();
    private final Node historyView = buildHistoryView();

    private WorkspaceMode workspaceMode = WorkspaceMode.ENTRY;
    private MovementMode movementMode = MovementMode.PURCHASE;

    public StockManagementFxView(User currentUser, StockService stockService, ProductService productService, SupplierService supplierService) {
        this.currentUser = currentUser;
        this.stockService = stockService;
        this.productService = productService;
        this.supplierService = supplierService;

        root.getStyleClass().addAll("module-root", "stock-root");
        contentHost.getStyleClass().add("stock-content-host");
        VBox.setVgrow(contentHost, Priority.ALWAYS);

        buildTables();
        configureInputs();
        configureHistorySearch();
        buildTopBar();
        buildEntryView();
        bindPreview();
        addEntryRow();
        updateMovementMode();
        updateSummary();
        switchWorkspace(WorkspaceMode.ENTRY);

        root.getChildren().addAll(topBar, contentHost);
    }

    @Override
    public String getName() {
        return "Gestion de stock";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            stockRows.setAll(stockService.findCurrentStock());
            movementRows.setAll(stockService.findMovements());
            supplierOptions.setAll(activeSuppliers(supplierService.findSuppliers("")));
            productOptions.setAll(activeProducts(productService.findProducts("")));
            for (EntryRow row : detailRows) {
                row.refreshOptions();
            }
            updatePreview();
            updateSummary();
        } catch (Exception exception) {
            FxSupport.showError("Gestion de stock", exception.getMessage());
        }
    }

    private void buildTopBar() {
        topBar.getStyleClass().addAll("stock-topbar", "stock-entry-topbar");

        entryBackButton.getStyleClass().addAll("button", "button-secondary", "stock-entry-back");
        entryBackButton.setGraphic(backGlyph());
        entryBackButton.setOnAction(event -> switchWorkspace(WorkspaceMode.HISTORY));

        addRowButton.getStyleClass().addAll("button", "stock-inline-action");
        saveButton.getStyleClass().addAll("button", "button-primary", "stock-save-button");
        saveButton.setGraphic(saveGlyph());

        addRowButton.setGraphic(smallPlusGlyph());
        refreshHistoryButton.setOnAction(event -> refresh());
        addRowButton.setOnAction(event -> addEntryRow());
        saveButton.setOnAction(event -> saveMovement());

        HBox left = new HBox(14, entryBackButton, FxSupport.pageHeader("Nueva Entrada", "Registra el ingreso de productos a tu inventario."));
        left.getStyleClass().add("stock-entry-header-wrap");
        left.setAlignment(Pos.CENTER_LEFT);

        topBar.setLeft(left);
        topBar.setCenter(null);
        topBar.setRight(null);
    }

    private void buildEntryView() {
        VBox content = new VBox(18,
                movementSection(),
                movementDataSection(),
                detailSection()
        );
        content.getStyleClass().add("stock-entry-content");

        entryScroll.getStyleClass().add("stock-entry-scroll");
        entryScroll.setFitToWidth(true);
        entryScroll.setPannable(true);
        entryScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        entryScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        entryScroll.setContent(content);
        FxSupport.enhanceScrollPane(entryScroll, 2.35);
    }

    private Node movementSection() {
        VBox section = sectionCard(
                "Elige el tipo de movimiento",
                "Selecciona si vas a registrar una compra completa o un ajuste puntual de stock."
        );

        HBox modes = new HBox(18, purchaseModeCard, manualModeCard);
        modes.getStyleClass().add("stock-mode-grid");
        HBox.setHgrow(purchaseModeCard, Priority.ALWAYS);
        HBox.setHgrow(manualModeCard, Priority.ALWAYS);

        section.getChildren().add(modes);
        return section;
    }

    private Node movementDataSection() {
        VBox section = sectionCard(
                "Datos del movimiento",
                "Define los datos base antes de cargar los productos que van a entrar al stock."
        );

        formHost.getStyleClass().add("stock-form-host");
        HBox.setHgrow(formHost, Priority.ALWAYS);

        VBox previewCard = previewCard();
        previewCard.setPrefWidth(320);
        previewCard.setMinWidth(320);
        previewCard.setMaxWidth(320);

        HBox body = new HBox(18, formHost, previewCard);
        body.getStyleClass().add("stock-data-layout");
        HBox.setHgrow(formHost, Priority.ALWAYS);

        section.getChildren().add(body);
        return section;
    }

    private Node detailSection() {
        VBox card = new VBox(16);
        card.getStyleClass().add("stock-section-card");

        VBox titles = new VBox(4);
        titles.getStyleClass().add("stock-section-head");
        Label title = new Label("Detalle de productos");
        title.getStyleClass().add("stock-section-title");
        Label subtitle = new Label("Carga cantidades, costo unitario y referencias por cada producto.");
        subtitle.getStyleClass().add("stock-section-subtitle");
        titles.getChildren().addAll(title, subtitle);

        HBox head = new HBox(12, titles, FxSupport.spacer(), addRowButton);
        head.setAlignment(Pos.CENTER_LEFT);

        detailRowsBox.getStyleClass().add("stock-row-list");

        VBox rowsChip = summaryChip("Filas", rowsSummaryValue);
        VBox unitsChip = summaryChip("Unidades", unitsSummaryValue);
        VBox amountChip = summaryChip("Monto total", amountSummaryValue);
        amountChip.getStyleClass().add("stock-summary-chip-emphasis");

        HBox summary = new HBox(10,
                rowsChip,
                unitsChip,
                amountChip,
                FxSupport.spacer(),
                saveButton
        );
        summary.getStyleClass().add("stock-summary-bar");
        summary.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(head, detailRowsBox, summary);
        return card;
    }

    private Node buildHistoryView() {
        VBox wrapper = new VBox(18);
        wrapper.getStyleClass().add("stock-history-content");

        historyRefreshButton.getStyleClass().addAll("button", "button-secondary", "stock-history-action-icon");
        historyRefreshButton.setGraphic(refreshGlyph());
        historyRefreshButton.setOnAction(event -> refresh());

        historyReportButton.getStyleClass().addAll("button", "button-secondary", "stock-history-action-report");
        historyReportButton.setGraphic(historyGlyph());
        historyReportButton.setOnAction(event -> showHistoryReport());

        backToEntryButton.getStyleClass().addAll("button", "button-primary", "stock-history-action-primary");
        backToEntryButton.setGraphic(lightPlusGlyph());
        backToEntryButton.setOnAction(event -> switchWorkspace(WorkspaceMode.ENTRY));

        HBox head = new HBox(12);
        head.getChildren().addAll(
                FxSupport.pageHeader("Historial y stock actual", "Consulta el stock disponible y los ultimos movimientos registrados."),
                FxSupport.spacer(),
                historyRefreshButton,
                historyReportButton,
                backToEntryButton
        );
        head.setAlignment(Pos.CENTER_LEFT);

        HBox stockHeader = new HBox(12);
        stockHeader.setAlignment(Pos.CENTER_LEFT);
        HBox stockTitle = new HBox(10, stockPanelGlyph(), sectionLabel("Stock Actual"));
        stockTitle.getStyleClass().add("stock-history-panel-title-wrap");
        HBox.setHgrow(stockSearchField, Priority.ALWAYS);
        HBox searchShell = new HBox(10, historySearchGlyph(), stockSearchField);
        searchShell.getStyleClass().add("stock-history-search-shell");
        searchShell.setMaxWidth(240);
        stockHeader.getChildren().addAll(stockTitle, FxSupport.spacer(), searchShell);

        VBox stockPane = new VBox(12, stockHeader, stockTable);
        stockPane.getStyleClass().addAll("surface-pane", "stock-history-panel");
        stockPane.getStyleClass().add("stock-history-panel-stock");

        HBox movementHeader = new HBox(10, movementPanelGlyph(), sectionLabel("Ultimos Movimientos"));
        movementHeader.getStyleClass().add("stock-history-panel-title-wrap");
        movementHeader.setAlignment(Pos.CENTER_LEFT);

        VBox movementPane = new VBox(12, movementHeader, movementTable);
        movementPane.getStyleClass().addAll("surface-pane", "stock-history-panel");
        movementPane.getStyleClass().add("stock-history-panel-movement");

        VBox.setVgrow(stockPane, Priority.ALWAYS);
        VBox.setVgrow(movementPane, Priority.ALWAYS);
        VBox.setVgrow(stockTable, Priority.ALWAYS);
        VBox.setVgrow(movementTable, Priority.ALWAYS);

        wrapper.getChildren().addAll(head, stockPane, movementPane);
        return wrapper;
    }

    private VBox buildPurchaseForm() {
        GridPane grid = formGrid();
        grid.add(fieldGroup("Proveedor", supplierBox), 0, 0, 2, 1);
        grid.add(fieldGroup("Tipo comprobante", documentTypeBox), 0, 1);
        grid.add(fieldGroup("Almacen destino", purchaseDestinationBox), 1, 1);
        grid.add(fieldGroup("Serie", seriesField), 0, 2);
        grid.add(fieldGroup("Correlativo", correlativeField), 1, 2);

        VBox box = new VBox(grid);
        box.getStyleClass().add("stock-form-block");
        return box;
    }

    private VBox buildManualForm() {
        GridPane grid = formGrid();
        grid.add(fieldGroup("Tipo de ajuste", adjustmentTypeBox), 0, 0);
        grid.add(fieldGroup("Almacen destino", manualDestinationBox), 1, 0);
        grid.add(fieldGroup("Motivo general", manualReasonField), 0, 1, 2, 1);
        grid.add(fieldGroup("Observacion", noteArea), 0, 2, 2, 1);

        VBox box = new VBox(grid);
        box.getStyleClass().add("stock-form-block");
        return box;
    }

    private VBox previewCard() {
        VBox card = new VBox(14);
        card.getStyleClass().addAll("stock-preview-card", "surface-pane");

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4);
        Label title = new Label("Documento");
        title.getStyleClass().add("stock-preview-title");
        Label subtitle = new Label("Vista previa del movimiento que vas a registrar.");
        subtitle.getStyleClass().add("stock-preview-copy");
        titles.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titles, Priority.ALWAYS);
        StackPane iconWrap = new StackPane(documentPreviewGlyph());
        iconWrap.getStyleClass().add("stock-preview-icon-wrap");
        previewBadge.getStyleClass().add("stock-preview-badge");
        previewBadge.setMinWidth(78);
        previewBadge.setMaxWidth(78);
        head.getChildren().addAll(titles, FxSupport.spacer(), previewBadge, iconWrap);

        VBox focusBox = new VBox(10,
                previewMetric("Tipo", previewTypeValue),
                previewMetric("Referencia", previewReferenceValue)
        );
        focusBox.getStyleClass().add("stock-preview-focus");

        VBox meta = new VBox(10,
                previewRow("Proveedor", previewSupplierValue),
                previewRow("Destino", previewDestinationValue),
                previewRow("Filas", previewRowsValue)
        );
        meta.getStyleClass().add("stock-preview-meta");

        card.getChildren().addAll(head, focusBox, new Separator(), meta);
        return card;
    }

    private VBox sectionCard(String titleText, String subtitleText) {
        VBox card = new VBox(18);
        card.getStyleClass().add("stock-section-card");
        card.getChildren().add(sectionHeader(titleText, subtitleText));
        return card;
    }

    private VBox sectionHeader(String titleText, String subtitleText) {
        VBox box = new VBox(4);
        box.getStyleClass().add("stock-section-head");
        Label title = new Label(titleText);
        title.getStyleClass().add("stock-section-title");
        Label subtitle = new Label(subtitleText);
        subtitle.getStyleClass().add("stock-section-subtitle");
        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("stock-form-grid");
        grid.setHgap(18);
        grid.setVgap(16);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        left.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        right.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().setAll(left, right);
        return grid;
    }

    private VBox fieldGroup(String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("stock-field-label");
        VBox box = new VBox(8, label, field);
        box.getStyleClass().add("stock-field-group");
        return box;
    }

    private VBox summaryChip(String titleText, Label valueLabel) {
        Label title = new Label(titleText);
        title.getStyleClass().add("stock-summary-title");
        valueLabel.getStyleClass().add("stock-summary-value");
        VBox chip = new VBox(2, title, valueLabel);
        chip.getStyleClass().add("stock-summary-chip");
        return chip;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("stock-table-title");
        return label;
    }

    private VBox previewMetric(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("stock-preview-label");
        valueLabel.getStyleClass().add("stock-preview-value");
        VBox box = new VBox(6, label, valueLabel);
        box.getStyleClass().add("stock-preview-metric");
        return box;
    }

    private HBox previewRow(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("stock-preview-row-label");
        valueLabel.getStyleClass().add("stock-preview-row-value");
        HBox row = new HBox(10, label, FxSupport.spacer(), valueLabel);
        row.getStyleClass().add("stock-preview-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox movementCard(MovementMode mode, String titleText, String stateText, String copyText, Node icon) {
        StackPane iconBox = new StackPane(icon);
        iconBox.getStyleClass().add("stock-mode-icon-box");

        VBox titles = new VBox(4);
        Label title = new Label(titleText);
        title.getStyleClass().add("stock-mode-title");
        Label state = new Label(stateText);
        state.getStyleClass().add("stock-mode-state");
        titles.getChildren().addAll(title, state);

        StackPane radio = new StackPane();
        radio.getStyleClass().add("stock-mode-radio");

        HBox top = new HBox(12, iconBox, titles, FxSupport.spacer(), radio);
        top.setAlignment(Pos.CENTER_LEFT);

        Label copy = new Label(copyText);
        copy.getStyleClass().add("stock-mode-copy");
        copy.setWrapText(true);

        VBox card = new VBox(16, top, copy);
        card.getStyleClass().add("stock-mode-card");
        card.setOnMouseClicked(event -> {
            movementMode = mode;
            updateMovementMode();
        });
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void configureInputs() {
        styleCombo(supplierBox, supplier -> supplier == null ? "" : supplier.getName());
        styleCombo(documentTypeBox, value -> value);
        styleCombo(purchaseDestinationBox, value -> value);
        styleCombo(manualDestinationBox, value -> value);
        styleCombo(adjustmentTypeBox, value -> value);

        styleTextField(seriesField, "1313");
        styleTextField(correlativeField, "14124");
        styleTextField(manualReasonField, "Conteo o ajuste puntual");

        noteArea.getStyleClass().add("stock-note-area");
        noteArea.setPromptText("Detalle interno del ajuste");
        noteArea.setPrefRowCount(3);
        noteArea.setWrapText(true);

        documentTypeBox.getSelectionModel().selectFirst();
        purchaseDestinationBox.getSelectionModel().selectFirst();
        manualDestinationBox.getSelectionModel().selectFirst();
        adjustmentTypeBox.getSelectionModel().selectFirst();
    }

    private void bindPreview() {
        supplierBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        documentTypeBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        purchaseDestinationBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        manualDestinationBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        adjustmentTypeBox.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        seriesField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        correlativeField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        manualReasonField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        noteArea.textProperty().addListener((obs, oldValue, newValue) -> updatePreview());
    }

    private void buildTables() {
        stockTable.getStyleClass().addAll("stock-history-table", "stock-current-table");
        stockTable.getColumns().add(stockCodeColumn());
        stockTable.getColumns().add(stockProductColumn());
        stockTable.getColumns().add(stockValueColumn("Stock", product -> safeInt(product.getStock()), "stock-stock-pill"));
        stockTable.getColumns().add(stockPlainValueColumn("Minimo", product -> safeInt(product.getMinimumStock()), "stock-min-value"));
        stockTable.getColumns().add(stockStatusColumn());
        stockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        stockTable.setPlaceholder(emptyTable("Sin productos cargados."));
        stockTable.setFixedCellSize(58);
        FxSupport.enhanceTableView(stockTable, 1.65);

        movementTable.getStyleClass().addAll("stock-history-table", "stock-movement-table");
        movementTable.getColumns().add(movementDateColumn());
        movementTable.getColumns().add(movementProductColumn());
        movementTable.getColumns().add(movementTypeColumn());
        movementTable.getColumns().add(movementQuantityColumn());
        movementTable.getColumns().add(movementStockColumn());
        movementTable.getColumns().add(movementReasonColumn());
        movementTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        movementTable.setPlaceholder(emptyTable("Sin movimientos registrados."));
        movementTable.setFixedCellSize(66);
        FxSupport.enhanceTableView(movementTable, 1.65);
    }

    private void switchWorkspace(WorkspaceMode mode) {
        workspaceMode = mode;
        contentHost.getChildren().setAll(mode == WorkspaceMode.ENTRY ? entryScroll : historyView);
        topBar.setManaged(mode == WorkspaceMode.ENTRY);
        topBar.setVisible(mode == WorkspaceMode.ENTRY);
    }

    private void updateMovementMode() {
        purchaseModeCard.getStyleClass().remove("stock-mode-card-active");
        manualModeCard.getStyleClass().remove("stock-mode-card-active");
        if (movementMode == MovementMode.PURCHASE) {
            purchaseModeCard.getStyleClass().add("stock-mode-card-active");
            formHost.getChildren().setAll(purchaseForm);
        } else {
            manualModeCard.getStyleClass().add("stock-mode-card-active");
            formHost.getChildren().setAll(manualForm);
        }
        updatePreview();
    }

    private void updatePreview() {
        boolean previewReady;

        if (movementMode == MovementMode.PURCHASE) {
            previewTypeValue.setText(safeText(documentTypeBox.getValue(), "FACTURA"));
            String reference = safeText(seriesField.getText(), "").trim();
            String correlative = safeText(correlativeField.getText(), "").trim();
            if (reference.isBlank() && correlative.isBlank()) {
                previewReferenceValue.setText("Pendiente");
            } else if (reference.isBlank()) {
                previewReferenceValue.setText(correlative);
            } else if (correlative.isBlank()) {
                previewReferenceValue.setText(reference);
            } else {
                previewReferenceValue.setText(reference + "-" + correlative);
            }
            Supplier supplier = supplierBox.getValue();
            previewSupplierValue.setText(supplier == null ? "Sin proveedor" : safeText(supplier.getName(), "Sin proveedor"));
            previewReady = supplier != null && !reference.isBlank() && !correlative.isBlank();
        } else {
            previewTypeValue.setText(safeText(adjustmentTypeBox.getValue(), "AJUSTE"));
            previewReferenceValue.setText(safeText(manualReasonField.getText(), "Ajuste puntual"));
            previewSupplierValue.setText("No aplica");
            previewReady = !safeText(manualReasonField.getText(), "").trim().isBlank();
        }

        previewBadge.setText(previewReady ? "Completo" : "Pendiente");
        previewBadge.getStyleClass().removeAll(
                "stock-preview-badge-manual",
                "stock-preview-badge-purchase",
                "stock-preview-badge-ready",
                "stock-preview-badge-waiting"
        );
        previewBadge.getStyleClass().add(previewReady ? "stock-preview-badge-ready" : "stock-preview-badge-waiting");
        previewDestinationValue.setText(safeText(currentDestination(), "Almacen principal"));
        previewRowsValue.setText(detailRows.stream().filter(EntryRow::hasProduct).count() + " cargadas");
    }

    private void addEntryRow() {
        EntryRow row = new EntryRow(detailRows.size() + 1);
        detailRows.add(row);
        detailRowsBox.getChildren().add(row.container);
        refreshRowTitles();
        updatePreview();
        updateSummary();
    }

    private void removeEntryRow(EntryRow row) {
        if (detailRows.size() == 1) {
            row.clear();
            return;
        }
        detailRows.remove(row);
        detailRowsBox.getChildren().remove(row.container);
        refreshRowTitles();
        updatePreview();
        updateSummary();
    }

    private void refreshRowTitles() {
        for (int index = 0; index < detailRows.size(); index++) {
            detailRows.get(index).setIndex(index + 1);
        }
    }

    private void updateSummary() {
        int rows = 0;
        int units = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (EntryRow row : detailRows) {
            if (!row.hasProduct()) {
                continue;
            }
            rows++;
            units += row.quantityValue();
            total = total.add(row.subtotal());
        }
        rowsSummaryValue.setText(rows + " filas");
        unitsSummaryValue.setText(units + " unidades");
        amountSummaryValue.setText(CurrencyUtils.format(total));
        updatePreview();
    }

    private void saveMovement() {
        try {
            List<EntryRow> activeRows = detailRows.stream().filter(EntryRow::hasProduct).toList();
            if (activeRows.isEmpty()) {
                throw new IllegalArgumentException("Agrega al menos una fila con producto.");
            }
            if (movementMode == MovementMode.PURCHASE && supplierBox.getValue() == null) {
                throw new IllegalArgumentException("Selecciona un proveedor.");
            }
            if (movementMode == MovementMode.PURCHASE && safeText(seriesField.getText(), "").trim().isBlank()) {
                throw new IllegalArgumentException("Ingresa la serie del documento.");
            }
            if (movementMode == MovementMode.PURCHASE && safeText(correlativeField.getText(), "").trim().isBlank()) {
                throw new IllegalArgumentException("Ingresa el correlativo del documento.");
            }
            if (movementMode == MovementMode.MANUAL && safeText(manualReasonField.getText(), "").trim().isBlank()) {
                throw new IllegalArgumentException("Ingresa el motivo general del ajuste.");
            }

            for (EntryRow row : activeRows) {
                row.validate();
            }

            StockEntryRequest request = new StockEntryRequest();
            request.setUser(currentUser);
            request.setEntryMode(movementMode == MovementMode.PURCHASE ? "COMPRA" : "AJUSTE");
            request.setSupplier(supplierBox.getValue());
            request.setDocumentType(documentTypeBox.getValue());
            request.setSeries(safeText(seriesField.getText(), "").trim());
            request.setCorrelative(safeText(correlativeField.getText(), "").trim());
            request.setDestination(safeText(currentDestination(), "Almacen principal"));
            request.setAdjustmentType(adjustmentTypeBox.getValue());
            request.setGeneralReason(safeText(manualReasonField.getText(), "").trim());
            request.setNote(safeText(noteArea.getText(), "").trim());
            for (EntryRow row : activeRows) {
                request.getItems().add(row.toEntryItem());
            }

            stockService.registerStockEntry(request);

            FxSupport.showInfo("Gestion de stock", "Movimiento registrado.");
            clearEntryForm();
            refresh();
            switchWorkspace(WorkspaceMode.ENTRY);
        } catch (Exception exception) {
            FxSupport.showError("Gestion de stock", exception.getMessage());
        }
    }

    private void clearEntryForm() {
        supplierBox.getSelectionModel().clearSelection();
        documentTypeBox.getSelectionModel().selectFirst();
        purchaseDestinationBox.getSelectionModel().selectFirst();
        manualDestinationBox.getSelectionModel().selectFirst();
        adjustmentTypeBox.getSelectionModel().selectFirst();
        seriesField.clear();
        correlativeField.clear();
        manualReasonField.clear();
        noteArea.clear();
        detailRows.clear();
        detailRowsBox.getChildren().clear();
        addEntryRow();
        movementMode = MovementMode.PURCHASE;
        updateMovementMode();
    }

    private String currentDestination() {
        return movementMode == MovementMode.PURCHASE
                ? purchaseDestinationBox.getValue()
                : manualDestinationBox.getValue();
    }

    private List<Supplier> activeSuppliers(List<Supplier> suppliers) {
        List<Supplier> filtered = new ArrayList<>();
        for (Supplier supplier : suppliers) {
            if (supplier != null && supplier.isActive()) {
                filtered.add(supplier);
            }
        }
        return filtered;
    }

    private List<Product> activeProducts(List<Product> products) {
        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (product != null && !"INACTIVO".equalsIgnoreCase(product.getStatus())) {
                filtered.add(product);
            }
        }
        return filtered;
    }

    private String formatProductOption(Product product) {
        if (product == null) {
            return "";
        }
        return safeText(product.getCode(), "SIN-CODIGO") + " - " + safeText(product.getName(), "Sin nombre");
    }

    private String formatProductMeta(Product product) {
        if (product == null) {
            return "";
        }
        List<String> chunks = new ArrayList<>();
        if (!safeText(product.getCode(), "").isBlank()) {
            chunks.add(product.getCode());
        }
        if (!safeText(product.getCategoryName(), "").isBlank()) {
            chunks.add(product.getCategoryName());
        }
        if (!safeText(product.getSupplierName(), "").isBlank()) {
            chunks.add(product.getSupplierName());
        }
        return String.join("  |  ", chunks);
    }

    private String normalizeSearch(String value) {
        return safeText(value, "").trim().toLowerCase(Locale.ROOT);
    }

    private boolean productMatches(Product product, String query) {
        if (product == null) {
            return false;
        }
        String normalized = normalizeSearch(query);
        if (normalized.isBlank()) {
            return true;
        }
        return normalizeSearch(product.getCode()).contains(normalized)
                || normalizeSearch(product.getName()).contains(normalized)
                || normalizeSearch(product.getCategoryName()).contains(normalized)
                || normalizeSearch(product.getSupplierName()).contains(normalized)
                || normalizeSearch(product.getDescription()).contains(normalized);
    }

    private Product findProductById(Long productId) {
        if (productId == null) {
            return null;
        }
        for (Product product : productOptions) {
            if (product != null && productId.equals(product.getId())) {
                return product;
            }
        }
        return null;
    }

    private Product firstMatchingProduct(String query) {
        String normalized = normalizeSearch(query);
        if (normalized.isBlank()) {
            return null;
        }
        for (Product product : productOptions) {
            if (product == null) {
                continue;
            }
            if (normalized.equals(normalizeSearch(product.getCode()))
                    || normalized.equals(normalizeSearch(product.getName()))
                    || normalized.equals(normalizeSearch(formatProductOption(product)))) {
                return product;
            }
        }
        for (Product product : productOptions) {
            if (productMatches(product, normalized)) {
                return product;
            }
        }
        return null;
    }

    private void syncProductEditor(ComboBox<Product> comboBox) {
        if (!comboBox.isEditable()) {
            return;
        }
        String text = comboBox.getValue() == null ? "" : formatProductOption(comboBox.getValue());
        comboBox.getEditor().setText(text);
        comboBox.getEditor().positionCaret(text.length());
    }

    private void updateProductComboRowCount(ComboBox<Product> comboBox, FilteredList<Product> filteredProducts) {
        int rows = Math.max(1, Math.min(filteredProducts.size(), 6));
        comboBox.setVisibleRowCount(rows);
        Platform.runLater(() -> {
            Node popupList = comboBox.lookup(".list-view");
            if (!(popupList instanceof ListView<?> listView)) {
                return;
            }
            double cellHeight = listView.getFixedCellSize() > 0 ? listView.getFixedCellSize() : 52;
            double popupHeight = (rows * cellHeight) + 12;
            listView.setMinHeight(popupHeight);
            listView.setPrefHeight(popupHeight);
            listView.setMaxHeight(popupHeight);
        });
    }

    private void commitProductSelection(ComboBox<Product> comboBox, FilteredList<Product> filteredProducts) {
        Product selectedItem = comboBox.getSelectionModel().getSelectedItem();
        Product currentValue = comboBox.getValue();
        String query = safeText(comboBox.getEditor().getText(), "").trim();

        Product resolved = selectedItem != null ? selectedItem : currentValue;
        if (resolved == null && !query.isBlank()) {
            resolved = firstMatchingProduct(query);
        }

        comboBox.setValue(resolved);
        if (resolved == null) {
            comboBox.getSelectionModel().clearSelection();
        } else {
            comboBox.getSelectionModel().select(resolved);
        }

        filteredProducts.setPredicate(product -> true);
        updateProductComboRowCount(comboBox, filteredProducts);
        syncProductEditor(comboBox);
    }

    private void styleTextField(TextField field, String promptText) {
        field.getStyleClass().add("stock-text-field");
        field.setPromptText(promptText);
    }

    private <T> void styleCombo(ComboBox<T> comboBox, Function<T, String> formatter) {
        comboBox.getStyleClass().add("stock-combo");
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setVisibleRowCount(8);
        comboBox.setCellFactory(ignored -> new StockComboCell<>(true, formatter));
        comboBox.setButtonCell(new StockComboCell<>(false, formatter));
    }

    private void styleProductSearchCombo(ComboBox<Product> comboBox, FilteredList<Product> filteredProducts) {
        comboBox.getStyleClass().addAll("stock-combo", "stock-product-combo");
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setEditable(true);
        comboBox.setItems(filteredProducts);
        comboBox.setPromptText("Seleccionar producto");
        comboBox.getEditor().getStyleClass().add("stock-product-editor");
        comboBox.getEditor().setPromptText("Buscar por codigo o nombre");
        updateProductComboRowCount(comboBox, filteredProducts);
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product product) {
                return formatProductOption(product);
            }

            @Override
            public Product fromString(String string) {
                return firstMatchingProduct(string);
            }
        });
        comboBox.setCellFactory(ignored -> new StockProductCell(true));
        comboBox.setButtonCell(new StockProductCell(false));

        final boolean[] syncingEditor = {false};
        Runnable syncEditor = () -> {
            syncingEditor[0] = true;
            syncProductEditor(comboBox);
            syncingEditor[0] = false;
        };

        comboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            filteredProducts.setPredicate(product -> true);
            updateProductComboRowCount(comboBox, filteredProducts);
            syncEditor.run();
        });

        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            Platform.runLater(() -> {
                comboBox.setValue(newValue);
                filteredProducts.setPredicate(product -> true);
                updateProductComboRowCount(comboBox, filteredProducts);
                syncEditor.run();
                comboBox.hide();
            });
        });

        comboBox.setOnAction(event -> {
            if (comboBox.getValue() != null) {
                filteredProducts.setPredicate(product -> true);
                updateProductComboRowCount(comboBox, filteredProducts);
                syncEditor.run();
            }
        });

        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (syncingEditor[0]) {
                return;
            }
            filteredProducts.setPredicate(product -> productMatches(product, newValue));
            updateProductComboRowCount(comboBox, filteredProducts);
            if ((comboBox.isFocused() || comboBox.getEditor().isFocused()) && !comboBox.isShowing()) {
                Platform.runLater(() -> {
                    if ((comboBox.isFocused() || comboBox.getEditor().isFocused()) && !comboBox.isShowing()) {
                        comboBox.show();
                    }
                });
            }
        });

        comboBox.getEditor().focusedProperty().addListener((obs, oldValue, focused) -> {
            if (focused) {
                comboBox.getEditor().selectAll();
                filteredProducts.setPredicate(product -> productMatches(product, comboBox.getEditor().getText()));
                updateProductComboRowCount(comboBox, filteredProducts);
                Platform.runLater(() -> {
                    if (!comboBox.isShowing()) {
                        comboBox.show();
                    }
                });
                return;
            }

            Platform.runLater(() -> {
                commitProductSelection(comboBox, filteredProducts);
                comboBox.hide();
            });
        });

        comboBox.getEditor().setOnAction(event -> {
            commitProductSelection(comboBox, filteredProducts);
            comboBox.hide();
        });

        comboBox.setOnShowing(event -> {
            String query = comboBox.getEditor().isFocused() ? comboBox.getEditor().getText() : "";
            filteredProducts.setPredicate(product -> productMatches(product, query));
            updateProductComboRowCount(comboBox, filteredProducts);
        });

        comboBox.setOnHidden(event -> {
            if (!comboBox.getEditor().isFocused()) {
                commitProductSelection(comboBox, filteredProducts);
            }
        });
    }

    private void configureHistorySearch() {
        stockSearchField.setPromptText("Buscar producto...");
        stockSearchField.getStyleClass().add("stock-history-search-field");
        stockSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyStockHistoryFilter(newValue));
    }

    private void applyStockHistoryFilter(String rawQuery) {
        String query = safeText(rawQuery, "").trim().toLowerCase(Locale.ROOT);
        if (query.isBlank()) {
            filteredStockRows.setPredicate(product -> true);
            return;
        }
        filteredStockRows.setPredicate(product -> {
            String source = String.join(" ",
                    safeText(product.getCode(), ""),
                    safeText(product.getName(), ""),
                    safeText(product.getCategoryName(), ""),
                    safeText(product.getSupplierName(), "")
            ).toLowerCase(Locale.ROOT);
            return source.contains(query);
        });
    }

    private void showHistoryReport() {
        int totalProducts = filteredStockRows.size();
        int totalUnits = filteredStockRows.stream().mapToInt(product -> safeInt(product.getStock())).sum();
        int lowStock = (int) filteredStockRows.stream()
                .filter(product -> safeInt(product.getStock()) <= safeInt(product.getMinimumStock()))
                .count();
        int movementCount = movementRows.size();

        Stage modal = buildStockSummaryStage();

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("stock-summary-overlay");

        VBox card = new VBox(18);
        card.getStyleClass().add("stock-summary-dialog-card");
        card.setPrefWidth(392);
        card.setMaxWidth(392);

        HBox header = new HBox(12);
        header.getStyleClass().add("stock-summary-dialog-head");

        StackPane infoWrap = new StackPane(stockSummaryInfoGlyph());
        infoWrap.getStyleClass().add("stock-summary-dialog-info-wrap");

        Button closeButton = new Button();
        closeButton.getStyleClass().add("stock-summary-dialog-close");
        closeButton.setGraphic(closeGlyph());
        closeButton.setOnAction(event -> modal.close());

        header.getChildren().addAll(infoWrap, FxSupport.spacer(), closeButton);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        Label title = new Label("Resumen de stock");
        title.getStyleClass().add("stock-summary-dialog-title");
        Label subtitle = new Label("Informacion actual de los productos listados.");
        subtitle.getStyleClass().add("stock-summary-dialog-subtitle");
        titles.getChildren().addAll(title, subtitle);

        GridPane metrics = new GridPane();
        metrics.getStyleClass().add("stock-summary-dialog-grid");
        metrics.setHgap(12);
        metrics.setVgap(12);

        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(50);
        leftColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(50);
        rightColumn.setHgrow(Priority.ALWAYS);
        metrics.getColumnConstraints().setAll(leftColumn, rightColumn);

        metrics.add(summaryMetricCard("PRODUCTOS", String.valueOf(totalProducts), stockSummaryProductsGlyph(), false), 0, 0);
        metrics.add(summaryMetricCard("UNIDADES", String.valueOf(totalUnits), stockSummaryUnitsGlyph(), true), 1, 0);
        metrics.add(summaryMetricCard("REPOSICION", String.valueOf(lowStock), refreshGlyph(), false), 0, 1);
        metrics.add(summaryMetricCard("MOVIMIENTOS", String.valueOf(movementCount), historyGlyph(), false), 1, 1);

        Separator divider = new Separator();
        divider.getStyleClass().add("stock-summary-dialog-divider");

        HBox actions = new HBox();
        actions.getStyleClass().add("stock-summary-dialog-actions");
        Button acceptButton = new Button("Aceptar");
        acceptButton.getStyleClass().addAll("button", "button-primary", "stock-summary-dialog-accept");
        acceptButton.setOnAction(event -> modal.close());
        actions.getChildren().addAll(FxSupport.spacer(), acceptButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(header, titles, metrics, divider, actions);
        overlay.getChildren().add(card);

        Scene scene = new Scene(overlay);
        scene.setFill(Color.TRANSPARENT);
        FxSupport.applyTheme(scene);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                modal.close();
                event.consume();
            }
        });

        installWindowDrag(modal, header);

        modal.setScene(scene);
        modal.showAndWait();
    }

    private VBox summaryMetricCard(String labelText, String valueText, Node icon, boolean highlight) {
        Label label = new Label(labelText);
        label.getStyleClass().add("stock-summary-metric-label");

        Label value = new Label(valueText);
        value.getStyleClass().add("stock-summary-metric-value");

        HBox top = new HBox(8, icon, label);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(16, top, value);
        card.getStyleClass().add("stock-summary-metric-card");
        if (highlight) {
            card.getStyleClass().add("stock-summary-metric-card-highlight");
            label.getStyleClass().add("stock-summary-metric-label-highlight");
            value.getStyleClass().add("stock-summary-metric-value-highlight");
        }
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private Stage buildStockSummaryStage() {
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

    private TableColumn<Product, Product> stockCodeColumn() {
        TableColumn<Product, Product> column = new TableColumn<>("Codigo");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label label = new Label();
            {
                label.getStyleClass().add("stock-code-text");
            }
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : label);
                if (!empty && item != null) {
                    label.setText(safeText(item.getCode(), "-"));
                }
            }
        });
        column.setMinWidth(120);
        column.setPrefWidth(120);
        column.setMaxWidth(120);
        return column;
    }

    private TableColumn<Product, Product> stockProductColumn() {
        TableColumn<Product, Product> column = new TableColumn<>("Producto");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label name = new Label();
            private final Label meta = new Label();
            private final VBox box = new VBox(2, name, meta);
            {
                name.getStyleClass().add("stock-product-name-cell");
                meta.getStyleClass().add("stock-product-meta-cell");
            }
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : box);
                if (!empty && item != null) {
                    name.setText(safeText(item.getName(), "-"));
                    meta.setText(safeText(item.getCategoryName(), ""));
                }
            }
        });
        column.setMinWidth(280);
        column.setPrefWidth(320);
        return column;
    }

    private TableColumn<Product, Integer> stockValueColumn(String title, Function<Product, Integer> mapper, String styleClass) {
        TableColumn<Product, Integer> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(mapper.apply(cell.getValue())));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label label = new Label();
            private final StackPane wrap = new StackPane(label);
            {
                wrap.getStyleClass().add(styleClass);
                label.getStyleClass().add("stock-pill-value");
            }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : wrap);
                if (!empty && item != null) {
                    label.setText(String.valueOf(item));
                }
            }
        });
        column.setStyle("-fx-alignment: CENTER;");
        column.setMinWidth(104);
        column.setPrefWidth(104);
        column.setMaxWidth(104);
        return column;
    }

    private TableColumn<Product, Integer> stockPlainValueColumn(String title, Function<Product, Integer> mapper, String styleClass) {
        TableColumn<Product, Integer> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(mapper.apply(cell.getValue())));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label label = new Label();
            {
                label.getStyleClass().add(styleClass);
            }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : label);
                if (!empty && item != null) {
                    label.setText(String.valueOf(item));
                }
            }
        });
        column.setStyle("-fx-alignment: CENTER;");
        column.setMinWidth(96);
        column.setPrefWidth(96);
        column.setMaxWidth(96);
        return column;
    }

    private TableColumn<Product, Product> stockStatusColumn() {
        TableColumn<Product, Product> column = new TableColumn<>("Estado");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                boolean active = !"INACTIVO".equalsIgnoreCase(safeText(item.getStatus(), ""));
                Label label = new Label(active ? "ACTIVO" : "INACTIVO");
                label.getStyleClass().addAll("stock-status-pill", active ? "stock-status-pill-active" : "stock-status-pill-inactive");
                setGraphic(label);
            }
        });
        column.setStyle("-fx-alignment: CENTER;");
        column.setMinWidth(124);
        column.setPrefWidth(124);
        column.setMaxWidth(124);
        return column;
    }

    private TableColumn<StockMovement, StockMovement> movementDateColumn() {
        TableColumn<StockMovement, StockMovement> column = new TableColumn<>("Fecha");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label date = new Label();
            private final Label time = new Label();
            private final VBox box = new VBox(3, date, time);
            {
                date.getStyleClass().add("stock-movement-date");
                time.getStyleClass().add("stock-movement-time");
            }
            @Override
            protected void updateItem(StockMovement item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : box);
                if (!empty && item != null) {
                    if (item.getMovementDate() == null) {
                        date.setText("-");
                        time.setText("");
                    } else {
                        date.setText(item.getMovementDate().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                        time.setText(item.getMovementDate().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US)));
                    }
                }
            }
        });
        column.setMinWidth(132);
        column.setPrefWidth(132);
        column.setMaxWidth(132);
        return column;
    }

    private TableColumn<StockMovement, StockMovement> movementProductColumn() {
        TableColumn<StockMovement, StockMovement> column = new TableColumn<>("Producto");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label label = new Label();
            {
                label.getStyleClass().add("stock-movement-product");
            }
            @Override
            protected void updateItem(StockMovement item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : label);
                if (!empty && item != null) {
                    label.setText(item.getProduct() == null ? safeText(item.getProductName(), "-") : safeText(item.getProduct().getName(), "-"));
                }
            }
        });
        column.setMinWidth(170);
        column.setPrefWidth(190);
        return column;
    }

    private TableColumn<StockMovement, StockMovement> movementTypeColumn() {
        TableColumn<StockMovement, StockMovement> column = new TableColumn<>("Tipo");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(StockMovement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                boolean incoming = safeInt(item.getQuantity()) >= 0;
                SVGPath icon = incoming ? entryHistoryGlyph() : exitHistoryGlyph();
                Label text = new Label(safeText(item.getMovementType(), incoming ? "ENTRADA" : "SALIDA").toUpperCase(Locale.ROOT));
                text.getStyleClass().add("stock-movement-type-text");
                HBox badge = new HBox(6, icon, text);
                badge.getStyleClass().addAll("stock-movement-type-pill", incoming ? "stock-movement-type-pill-entry" : "stock-movement-type-pill-exit");
                badge.setAlignment(Pos.CENTER_LEFT);
                setGraphic(badge);
            }
        });
        column.setMinWidth(132);
        column.setPrefWidth(132);
        column.setMaxWidth(132);
        return column;
    }

    private TableColumn<StockMovement, StockMovement> movementQuantityColumn() {
        TableColumn<StockMovement, StockMovement> column = new TableColumn<>("Cant.");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label label = new Label();
            @Override
            protected void updateItem(StockMovement item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : label);
                if (!empty && item != null) {
                    int quantity = safeInt(item.getQuantity());
                    label.getStyleClass().setAll("stock-movement-quantity", quantity >= 0 ? "stock-movement-quantity-positive" : "stock-movement-quantity-negative");
                    label.setText((quantity >= 0 ? "+" : "") + quantity);
                }
            }
        });
        column.setStyle("-fx-alignment: CENTER;");
        column.setMinWidth(84);
        column.setPrefWidth(84);
        column.setMaxWidth(84);
        return column;
    }

    private TableColumn<StockMovement, StockMovement> movementStockColumn() {
        TableColumn<StockMovement, StockMovement> column = new TableColumn<>("Stock");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label label = new Label();
            {
                label.getStyleClass().add("stock-movement-stock");
            }
            @Override
            protected void updateItem(StockMovement item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : label);
                if (!empty && item != null) {
                    label.setText(safeInt(item.getPreviousStock()) + " \u2192 " + safeInt(item.getNewStock()));
                }
            }
        });
        column.setMinWidth(98);
        column.setPrefWidth(98);
        column.setMaxWidth(98);
        return column;
    }

    private TableColumn<StockMovement, StockMovement> movementReasonColumn() {
        TableColumn<StockMovement, StockMovement> column = new TableColumn<>("Motivo / Usuario");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label reason = new Label();
            private final Label user = new Label();
            private final VBox box = new VBox(3, reason, user);
            {
                reason.getStyleClass().add("stock-movement-reason");
                user.getStyleClass().add("stock-movement-user");
            }
            @Override
            protected void updateItem(StockMovement item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : box);
                if (!empty && item != null) {
                    reason.setText(safeText(item.getReason(), "Sin motivo"));
                    user.setText(item.getUser() == null ? safeText(item.getUsername(), "") : safeText(item.getUser().getFullName(), ""));
                }
            }
        });
        column.setMinWidth(250);
        column.setPrefWidth(320);
        return column;
    }

    private Node emptyTable(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("stock-empty-table");
        return new StackPane(label);
    }

    private int parseInt(String rawValue, String label) {
        String value = safeText(rawValue, "").trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Ingresa " + label + ".");
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException("La " + label + " debe ser mayor a cero.");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("La " + label + " no es valida.");
        }
    }

    private BigDecimal parseMoney(String rawValue, String label) {
        String value = safeText(rawValue, "").trim()
                .replace(" ", "")
                .replace("$", "")
                .replace("S/", "")
                .replace(",", ".");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Ingresa " + label + ".");
        }
        try {
            BigDecimal parsed = new BigDecimal(value);
            if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El " + label + " no puede ser negativo.");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El " + label + " no es valido.");
        }
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Button iconButton(Node icon, String styleClass, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button();
        button.getStyleClass().addAll("button", styleClass);
        button.setGraphic(icon);
        button.setOnAction(action);
        return button;
    }

    private Node purchaseGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M2 6h3l1.3 6.5h8.4L16.8 7H6.4M7.2 15.5A1.3 1.3 0 1 1 4.6 15.5A1.3 1.3 0 0 1 7.2 15.5ZM15.6 15.5A1.3 1.3 0 1 1 13 15.5A1.3 1.3 0 0 1 15.6 15.5ZM18 5V2m-1.5 1.5h3");
        path.getStyleClass().add("stock-mode-icon");
        return path;
    }

    private Node adjustmentGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 4h3v13H4zm6 0h3v8h-3zm6 0h3v16h-3zM3 19h17");
        path.getStyleClass().add("stock-mode-icon");
        return path;
    }

    private Node trashGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M6 7h8m-7 0V5.8c0-.7.6-1.3 1.3-1.3h3.8c.7 0 1.3.6 1.3 1.3V7m-7 0-.5 8.2c0 .7.5 1.3 1.2 1.3h4.5c.7 0 1.2-.6 1.2-1.3L14 7M9 9.2v5.2m3-5.2v5.2");
        path.getStyleClass().add("stock-remove-icon");
        return path;
    }

    private Node smallPlusGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M10 4v12M4 10h12");
        path.getStyleClass().add("stock-plus-icon");
        return path;
    }

    private Node backGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M11.5 3.5 5.5 9.5l6 6");
        path.getStyleClass().add("stock-entry-back-icon");
        return path;
    }

    private Node saveGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 8.5 7 11.5 14 4.5");
        path.getStyleClass().add("stock-save-icon");
        return path;
    }

    private Node documentPreviewGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M5 3h5.8L14 6.2v7.5c0 1.2-.9 2.1-2.1 2.1H5.1C3.9 15.8 3 14.9 3 13.7V5.1C3 3.9 3.9 3 5.1 3zm5.1 1.4v2.3h2.3L10.2 4.4zM6 9.2h5.4M6 11.7h5.4");
        path.getStyleClass().add("stock-preview-doc-icon");
        return path;
    }

    private Node lightPlusGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M10 4v12M4 10h12");
        path.getStyleClass().add("stock-history-plus-icon");
        return path;
    }

    private Node refreshGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M13.5 5.8V2.7M13.5 2.7h-3.2M13.5 2.7A5.8 5.8 0 1 0 15.6 7M2.5 10.2v3.1m0 0h3.2m-3.2 0A5.8 5.8 0 0 0 13 13");
        path.getStyleClass().add("stock-history-header-icon");
        return path;
    }

    private Node historyGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 2.3a5.7 5.7 0 1 1-4 1.6M8 4v4.2l2.6 1.5M4.2 2.3H1.8v2.4");
        path.getStyleClass().add("stock-history-header-icon");
        return path;
    }

    private Node stockPanelGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 2.2 13.2 5v6L8 13.8 2.8 11V5L8 2.2Zm0 1.7L4.3 5.5 8 7.2l3.7-1.7L8 3.9Zm-4 2.8v3.2l3.2 1.8V8.4L4 6.7Zm8 0-3.2 1.7v3.3l3.2-1.8V6.7Z");
        path.getStyleClass().add("stock-history-panel-icon");
        return path;
    }

    private Node movementPanelGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 2.2a5.8 5.8 0 1 1 0 11.6A5.8 5.8 0 0 1 8 2.2Zm0 1.4a4.4 4.4 0 1 0 0 8.8a4.4 4.4 0 0 0 0-8.8Zm-.7 1.9h1.4v2.8h2.2v1.3H7.3V5.5Z");
        path.getStyleClass().add("stock-history-panel-icon");
        return path;
    }

    private Node historySearchGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M10.5 10.5 15 15M12 6.8A5.2 5.2 0 1 1 1.6 6.8A5.2 5.2 0 0 1 12 6.8Z");
        path.getStyleClass().add("stock-history-search-icon");
        return path;
    }

    private SVGPath entryHistoryGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 8h7M8 4l4 4-4 4");
        path.getStyleClass().add("stock-movement-type-icon");
        return path;
    }

    private SVGPath exitHistoryGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M12 8H5m4 4L5 8l4-4");
        path.getStyleClass().add("stock-movement-type-icon");
        return path;
    }

    private Node stockSummaryInfoGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 2.1a5.9 5.9 0 1 1 0 11.8a5.9 5.9 0 0 1 0-11.8Zm0 2.1a.9.9 0 1 0 0 1.8a.9.9 0 0 0 0-1.8Zm-.7 3.4h1.4v4.1H7.3V7.6Z");
        path.getStyleClass().add("stock-summary-dialog-info-icon");
        return path;
    }

    private Node stockSummaryProductsGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 2.2 13.2 5v6L8 13.8 2.8 11V5L8 2.2Zm0 1.7L4.3 5.5 8 7.2l3.7-1.7L8 3.9Zm-4 2.8v3.2l3.2 1.8V8.4L4 6.7Zm8 0-3.2 1.7v3.3l3.2-1.8V6.7Z");
        path.getStyleClass().add("stock-summary-metric-icon");
        return path;
    }

    private Node stockSummaryUnitsGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 5.2 8 3l4 2.2M4 5.2V9.6L8 12l4-2.4V5.2M4 5.2 8 7.6l4-2.4M6 10.8 10 13l4-2.2M6 10.8V15.2L10 17.6l4-2.4v-4.4M6 10.8 10 13l4-2.2");
        path.getStyleClass().addAll("stock-summary-metric-icon", "stock-summary-metric-icon-highlight");
        return path;
    }

    private Node closeGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 4 12 12M12 4 4 12");
        path.getStyleClass().add("stock-summary-dialog-close-icon");
        return path;
    }

    private final class ProductPicker extends VBox {

        private final ObjectProperty<Product> value = new SimpleObjectProperty<>();
        private final FilteredList<Product> filteredProducts = new FilteredList<>(productOptions, product -> true);
        private final HBox shell = new HBox(10);
        private final TextField searchField = new TextField();
        private final StackPane arrowWrap = new StackPane();
        private final SVGPath arrowIcon = new SVGPath();
        private final ListView<Product> resultsView = new ListView<>(filteredProducts);
        private final ContextMenu popupMenu = new ContextMenu();
        private boolean syncingDisplay;

        private ProductPicker() {
            getStyleClass().add("stock-product-picker");
            setFillWidth(true);
            setMaxWidth(Double.MAX_VALUE);

            shell.getStyleClass().add("stock-product-picker-shell");
            shell.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(shell, Priority.ALWAYS);

            searchField.getStyleClass().add("stock-product-picker-field");
            searchField.setPromptText("Seleccionar producto");
            HBox.setHgrow(searchField, Priority.ALWAYS);

            arrowIcon.setContent("M3 5.5 8 10.5 13 5.5");
            arrowIcon.getStyleClass().add("stock-product-picker-arrow");
            arrowWrap.getStyleClass().add("stock-product-picker-arrow-wrap");
            arrowWrap.getChildren().add(arrowIcon);
            arrowWrap.setOnMouseClicked(event -> {
                searchField.requestFocus();
                if (popupMenu.isShowing()) {
                    popupMenu.hide();
                } else {
                    filteredProducts.setPredicate(product -> productMatches(product, searchField.getText()));
                    updateProductPopupMetrics();
                    showPopup();
                }
            });

            shell.getChildren().addAll(searchField, arrowWrap);
            getChildren().add(shell);

            Label emptyLabel = new Label("Sin coincidencias");
            emptyLabel.getStyleClass().add("stock-product-placeholder");
            resultsView.setPlaceholder(new StackPane(emptyLabel));
            resultsView.getStyleClass().add("stock-product-list");
            resultsView.setCellFactory(ignored -> new StockProductCell(true));
            resultsView.setFixedCellSize(52);
            resultsView.setFocusTraversable(false);
            FxSupport.enhanceListView(resultsView, 1.55);

            CustomMenuItem menuItem = new CustomMenuItem(resultsView, false);
            menuItem.getStyleClass().add("stock-product-menu-item");
            popupMenu.getItems().add(menuItem);
            popupMenu.getStyleClass().add("stock-product-menu");
            popupMenu.setAutoHide(true);
            popupMenu.setHideOnEscape(true);

            value.addListener((obs, oldValue, newValue) -> renderSelection());

            searchField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (syncingDisplay) {
                    return;
                }
                if (value.get() != null && !normalizeSearch(formatProductOption(value.get())).equals(normalizeSearch(newValue))) {
                    value.set(null);
                }
                filteredProducts.setPredicate(product -> productMatches(product, newValue));
                updateProductPopupMetrics();
                if (searchField.isFocused()) {
                    showPopup();
                }
                if (!filteredProducts.isEmpty()) {
                    resultsView.getSelectionModel().selectFirst();
                } else {
                    resultsView.getSelectionModel().clearSelection();
                }
            });

            searchField.focusedProperty().addListener((obs, oldValue, focused) -> {
                togglePickerActive(focused || popupMenu.isShowing());
                if (focused) {
                    filteredProducts.setPredicate(product -> productMatches(product, searchField.getText()));
                    updateProductPopupMetrics();
                    if (!searchField.getText().isBlank()) {
                        searchField.selectAll();
                    }
                    showPopup();
                    return;
                }
                Platform.runLater(() -> {
                    if (!popupMenu.isShowing()) {
                        renderSelection();
                        togglePickerActive(false);
                    }
                });
            });

            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.DOWN) {
                    showPopup();
                    if (resultsView.getSelectionModel().isEmpty() && !filteredProducts.isEmpty()) {
                        resultsView.getSelectionModel().selectFirst();
                    } else {
                        resultsView.getSelectionModel().selectNext();
                    }
                    resultsView.scrollTo(resultsView.getSelectionModel().getSelectedIndex());
                    event.consume();
                    return;
                }
                if (event.getCode() == KeyCode.UP) {
                    showPopup();
                    resultsView.getSelectionModel().selectPrevious();
                    resultsView.scrollTo(resultsView.getSelectionModel().getSelectedIndex());
                    event.consume();
                    return;
                }
                if (event.getCode() == KeyCode.ENTER) {
                    Product selected = resultsView.getSelectionModel().getSelectedItem();
                    if (selected == null && !filteredProducts.isEmpty()) {
                        selected = filteredProducts.get(0);
                    }
                    commitSelection(selected);
                    event.consume();
                    return;
                }
                if (event.getCode() == KeyCode.ESCAPE) {
                    popupMenu.hide();
                    renderSelection();
                    event.consume();
                }
            });

            resultsView.setOnMouseClicked(event -> {
                Product selected = resultsView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    commitSelection(selected);
                }
            });

            popupMenu.setOnShowing(event -> {
                if (getScene() != null && popupMenu.getScene() != null) {
                    popupMenu.getScene().getStylesheets().setAll(getScene().getStylesheets());
                }
                togglePickerActive(true);
                updateProductPopupMetrics();
            });

            popupMenu.setOnHidden(event -> {
                filteredProducts.setPredicate(product -> true);
                updateProductPopupMetrics();
                renderSelection();
                togglePickerActive(searchField.isFocused());
            });
        }

        private ObjectProperty<Product> valueProperty() {
            return value;
        }

        private Product getValue() {
            return value.get();
        }

        private void setValue(Product product) {
            value.set(product);
        }

        private void refreshOptions() {
            Product current = getValue();
            if (current == null) {
                filteredProducts.setPredicate(product -> true);
                updateProductPopupMetrics();
                return;
            }
            Product refreshed = findProductById(current.getId());
            setValue(refreshed);
            filteredProducts.setPredicate(product -> true);
            updateProductPopupMetrics();
        }

        private void clear() {
            setValue(null);
            filteredProducts.setPredicate(product -> true);
            resultsView.getSelectionModel().clearSelection();
            updateProductPopupMetrics();
        }

        private void commitSelection(Product product) {
            setValue(product);
            if (product == null) {
                resultsView.getSelectionModel().clearSelection();
            } else {
                resultsView.getSelectionModel().select(product);
            }
            popupMenu.hide();
        }

        private void renderSelection() {
            syncingDisplay = true;
            String text = getValue() == null ? "" : formatProductOption(getValue());
            searchField.setText(text);
            searchField.positionCaret(text.length());
            syncingDisplay = false;
        }

        private void updateProductPopupMetrics() {
            int visibleRows = Math.max(1, Math.min(filteredProducts.size(), 6));
            double cellHeight = resultsView.getFixedCellSize() > 0 ? resultsView.getFixedCellSize() : 52;
            double popupHeight = filteredProducts.isEmpty() ? 72 : (visibleRows * cellHeight) + 14;
            double popupWidth = Math.max(shell.getWidth(), 280);
            resultsView.setMinWidth(popupWidth);
            resultsView.setPrefWidth(popupWidth);
            resultsView.setMaxWidth(popupWidth);
            resultsView.setMinHeight(popupHeight);
            resultsView.setPrefHeight(popupHeight);
            resultsView.setMaxHeight(popupHeight);
        }

        private void showPopup() {
            if (getScene() == null || getScene().getWindow() == null) {
                return;
            }
            updateProductPopupMetrics();
            if (!popupMenu.isShowing()) {
                popupMenu.show(shell, Side.BOTTOM, 0, 6);
            }
        }

        private void togglePickerActive(boolean active) {
            shell.getStyleClass().remove("stock-product-picker-shell-active");
            if (active) {
                shell.getStyleClass().add("stock-product-picker-shell-active");
            }
        }
    }

    private final class EntryRow {

        private final VBox container = new VBox(14);
        private final Label titleLabel = new Label();
        private final ProductPicker productPicker = new ProductPicker();
        private final TextField quantityField = new TextField("1");
        private final TextField unitCostField = new TextField();
        private final TextField lotField = new TextField();
        private final DatePicker expirationPicker = new DatePicker();
        private final Label subtotalValue = new Label(CurrencyUtils.format(BigDecimal.ZERO));

        private EntryRow(int index) {
            container.getStyleClass().add("stock-entry-card");

            styleTextField(quantityField, "0");
            styleTextField(unitCostField, "0.00");
            styleTextField(lotField, "Nro. lote");

            expirationPicker.getStyleClass().add("stock-date-picker");
            expirationPicker.setPromptText("dd/mm/aaaa");
            expirationPicker.setMaxWidth(Double.MAX_VALUE);

            productPicker.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && safeText(unitCostField.getText(), "").trim().isBlank() && newValue.getPurchasePrice() != null) {
                    unitCostField.setText(newValue.getPurchasePrice().stripTrailingZeros().toPlainString());
                }
                updateSubtotal();
                updateSummary();
            });
            quantityField.textProperty().addListener((obs, oldValue, newValue) -> {
                updateSubtotal();
                updateSummary();
            });
            unitCostField.textProperty().addListener((obs, oldValue, newValue) -> {
                updateSubtotal();
                updateSummary();
            });
            lotField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview());
            expirationPicker.valueProperty().addListener((obs, oldValue, newValue) -> updatePreview());

            Button removeButton = iconButton(trashGlyph(), "stock-remove-button", event -> removeEntryRow(this));

            HBox head = new HBox(10, titleLabel, FxSupport.spacer(), removeButton);
            head.getStyleClass().add("stock-entry-head");
            head.setAlignment(Pos.CENTER_LEFT);

            GridPane grid = new GridPane();
            grid.getStyleClass().add("stock-row-grid");
            grid.setHgap(16);
            grid.setVgap(14);
            ColumnConstraints colA = new ColumnConstraints();
            colA.setPercentWidth(36);
            colA.setHgrow(Priority.ALWAYS);
            ColumnConstraints colB = new ColumnConstraints();
            colB.setPercentWidth(22);
            colB.setHgrow(Priority.ALWAYS);
            ColumnConstraints colC = new ColumnConstraints();
            colC.setPercentWidth(20);
            colC.setHgrow(Priority.ALWAYS);
            ColumnConstraints colD = new ColumnConstraints();
            colD.setPercentWidth(22);
            colD.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().setAll(colA, colA, colB, colD);

            grid.add(fieldGroup("PRODUCTO", productPicker), 0, 0, 2, 1);
            grid.add(fieldGroup("CANTIDAD", quantityField), 2, 0);
            grid.add(fieldGroup("COSTO UNIT. ($)", unitCostField), 3, 0);
            grid.add(fieldGroup("LOTE (OPCIONAL)", lotField), 0, 1, 2, 1);
            grid.add(fieldGroup("VENCIMIENTO", expirationPicker), 2, 1);
            grid.add(subtotalBox(), 3, 1);

            container.getChildren().addAll(head, grid);
            setIndex(index);
            refreshOptions();
            updateSubtotal();
        }

        private void setIndex(int index) {
            titleLabel.setText("Producto " + index);
            titleLabel.getStyleClass().setAll("stock-entry-title");
        }

        private void refreshOptions() {
            productPicker.refreshOptions();
        }

        private Node subtotalBox() {
            Label label = new Label("SUBTOTAL");
            label.getStyleClass().add("stock-field-label");
            subtotalValue.getStyleClass().add("stock-subtotal-value");
            VBox box = new VBox(8, label, subtotalValue);
            box.getStyleClass().add("stock-subtotal-box");
            return box;
        }

        private void updateSubtotal() {
            subtotalValue.setText(CurrencyUtils.format(subtotal()));
        }

        private boolean hasProduct() {
            return productPicker.getValue() != null;
        }

        private Product product() {
            return productPicker.getValue();
        }

        private int quantityValue() {
            if (safeText(quantityField.getText(), "").trim().isBlank()) {
                return 0;
            }
            try {
                return Integer.parseInt(quantityField.getText().trim());
            } catch (NumberFormatException exception) {
                return 0;
            }
        }

        private BigDecimal subtotal() {
            BigDecimal cost = BigDecimal.ZERO;
            try {
                cost = parseMoney(unitCostField.getText(), "costo unitario");
            } catch (IllegalArgumentException exception) {
                if (productPicker.getValue() != null && productPicker.getValue().getPurchasePrice() != null && safeText(unitCostField.getText(), "").trim().isBlank()) {
                    cost = productPicker.getValue().getPurchasePrice();
                }
            }
            return cost.multiply(BigDecimal.valueOf(Math.max(quantityValue(), 0L)));
        }

        private String unitCostText() {
            return safeText(unitCostField.getText(), "").trim();
        }

        private BigDecimal unitCostValue() {
            try {
                return parseMoney(unitCostField.getText(), "costo unitario");
            } catch (IllegalArgumentException exception) {
                if (productPicker.getValue() != null && productPicker.getValue().getPurchasePrice() != null
                        && safeText(unitCostField.getText(), "").trim().isBlank()) {
                    return productPicker.getValue().getPurchasePrice();
                }
                return BigDecimal.ZERO;
            }
        }

        private String lot() {
            return safeText(lotField.getText(), "").trim();
        }

        private String expiration() {
            LocalDate date = expirationPicker.getValue();
            return date == null ? "" : date.toString();
        }

        private LocalDate expirationDate() {
            return expirationPicker.getValue();
        }

        private StockEntryItem toEntryItem() {
            StockEntryItem item = new StockEntryItem();
            item.setProduct(product());
            item.setQuantity(quantityValue());
            item.setUnitCost(unitCostValue());
            item.setLot(lot());
            item.setExpirationDate(expirationDate());
            return item;
        }

        private void validate() {
            if (productPicker.getValue() == null) {
                throw new IllegalArgumentException("Selecciona un producto en cada fila activa.");
            }
            parseInt(quantityField.getText(), "cantidad");
            parseMoney(unitCostField.getText(), "costo unitario");
        }

        private void clear() {
            productPicker.clear();
            quantityField.setText("1");
            unitCostField.clear();
            lotField.clear();
            expirationPicker.setValue(null);
            updateSubtotal();
            updateSummary();
        }
    }

    private static final class StockComboCell<T> extends ListCell<T> {

        private final boolean popupCell;
        private final Function<T, String> formatter;

        private StockComboCell(boolean popupCell, Function<T, String> formatter) {
            this.popupCell = popupCell;
            this.formatter = formatter;
            getStyleClass().add(popupCell ? "stock-combo-popup-cell" : "stock-combo-button-cell");
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            setText(formatter.apply(item));
            setGraphic(null);
        }
    }

    private final class StockProductCell extends ListCell<Product> {

        private final boolean popupCell;
        private final Label nameLabel = new Label();
        private final Label metaLabel = new Label();
        private final VBox content = new VBox(3, nameLabel, metaLabel);

        private StockProductCell(boolean popupCell) {
            this.popupCell = popupCell;
            if (popupCell) {
                getStyleClass().add("stock-product-popup-cell");
                content.getStyleClass().add("stock-product-popup-content");
                nameLabel.getStyleClass().add("stock-product-popup-name");
                metaLabel.getStyleClass().add("stock-product-popup-meta");
            } else {
                getStyleClass().add("stock-combo-button-cell");
            }
        }

        @Override
        protected void updateItem(Product item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            if (popupCell) {
                nameLabel.setText(safeText(item.getName(), "Sin nombre"));
                metaLabel.setText(formatProductMeta(item));
                setText(null);
                setGraphic(content);
                return;
            }
            setText(formatProductOption(item));
            setGraphic(null);
        }
    }
}
