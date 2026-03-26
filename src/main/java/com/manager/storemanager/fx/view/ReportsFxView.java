package com.manager.storemanager.fx.view;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.DailySalesTotal;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.service.SaleService;
import com.manager.storemanager.util.CurrencyUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.StringConverter;

public class ReportsFxView implements FxView {

    private static final String TAB_TODAY = "today";
    private static final String TAB_RANGE = "range";
    private static final String TAB_LOW_STOCK = "low_stock";
    private static final String TAB_CATALOG = "catalog";
    private static final String TAB_MONTH_TOTAL = "month_total";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "CO"));
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "CO"));

    private final ProductService productService;
    private final SaleService saleService;
    private final ReportService reportService;

    private final VBox root = new VBox(18);
    private final VBox reportCard = new VBox(16);
    private final HBox presetBar = new HBox(8);
    private final HBox tabBar = new HBox(8);
    private final Map<String, Button> tabButtons = new LinkedHashMap<>();
    private final Map<String, Button> presetButtons = new LinkedHashMap<>();
    private final DatePicker fromPicker = new DatePicker(LocalDate.now().minusDays(7));
    private final DatePicker toPicker = new DatePicker(LocalDate.now());
    private final Label reportChip = new Label("Centro analitico");
    private final Label reportTitle = new Label("Ventas del dia");
    private final Label reportSubtitle = new Label("Movimientos registrados para la fecha actual.");
    private final Label summaryCount = new Label("0");
    private final Label summaryText = new Label("registros encontrados.");
    private final Label reportNote = new Label("Usa este reporte para validar el cierre de caja y auditar las ventas exactas procesadas durante el turno actual.");
    private final Label emptyTitle = new Label("No hay datos para mostrar");
    private final Label emptyMessage = new Label("No se encontraron registros para la vista seleccionada.");
    private final Button emptyAction = new Button("Cambiar fechas");
    private final TableView<ReportRow> reportTable = new TableView<>();
    private final VBox emptyState = new VBox(10);
    private final StackPane bodyStack = new StackPane();
    private String activeTab = TAB_TODAY;
    private String activeRangePreset = "7_days";

    public ReportsFxView(ProductService productService, SaleService saleService, ReportService reportService) {
        this.productService = productService;
        this.saleService = saleService;
        this.reportService = reportService;

        root.getStyleClass().add("reports-root");
        root.getStylesheets().add(css("/css/reports-native.css"));
        root.setFillWidth(true);
        reportTable.getStyleClass().add("reports-table");
        reportTable.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().addAll(buildHeader(), buildCard());
        VBox.setVgrow(reportCard, Priority.ALWAYS);
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reportTable.setFixedCellSize(38);
        FxSupport.enhanceTableView(reportTable, 1.15);

        emptyAction.setOnAction(event -> {
            activeTab = TAB_RANGE;
            updateTabSelection();
            fromPicker.requestFocus();
            fromPicker.show();
            showSalesByRange();
        });

        initPresetButtons();
        initTabs();
        configureDatePicker(fromPicker);
        configureDatePicker(toPicker);
        showTodaySales();
    }

    @Override
    public String getName() {
        return "Reportes";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        loadActiveReport();
    }

    private Node buildHeader() {
        VBox header = new VBox(12);
        header.getStyleClass().add("reports-header");

        HBox topRow = new HBox(16);
        topRow.getStyleClass().add("reports-top-row");
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBlock = FxSupport.pageHeader("Reportes", "Ventas, stock bajo, catalogo y totales del negocio.");

        HBox filters = new HBox(8);
        filters.getStyleClass().add("reports-filters");
        filters.setAlignment(Pos.CENTER_RIGHT);

        Button apply = new Button("Aplicar filtros");
        apply.getStyleClass().addAll("button", "reports-apply-button");
        apply.setOnAction(event -> {
            activeRangePreset = detectRangePreset(fromPicker.getValue(), toPicker.getValue());
            activeTab = TAB_RANGE;
            updateTabSelection();
            showSalesByRange();
        });

        filters.getChildren().addAll(fromPicker, toPicker, apply);
        topRow.getChildren().addAll(titleBlock, FxSupport.spacer(), filters);

        tabBar.getStyleClass().add("reports-tabs");
        header.getChildren().addAll(topRow, tabBar, presetBar);
        return header;
    }

    private Node buildCard() {
        reportCard.getStyleClass().add("reports-card");

        HBox cardHeader = new HBox(16);
        cardHeader.getStyleClass().add("reports-card-header");
        cardHeader.setAlignment(Pos.TOP_LEFT);

        VBox heading = new VBox(8);
        heading.getStyleClass().add("reports-heading");
        reportChip.getStyleClass().add("reports-chip");
        reportTitle.getStyleClass().add("reports-title");
        reportSubtitle.getStyleClass().add("reports-subtitle");
        heading.getChildren().addAll(reportChip, reportTitle, reportSubtitle);

        VBox summary = new VBox(6);
        summary.getStyleClass().add("reports-summary-card");
        Label summaryLabel = new Label("Resumen");
        summaryLabel.getStyleClass().add("reports-summary-label");
        HBox summaryLine = new HBox(6);
        summaryLine.setAlignment(Pos.BASELINE_LEFT);
        summaryCount.getStyleClass().add("reports-summary-count");
        summaryText.getStyleClass().add("reports-summary-text");
        summaryLine.getChildren().addAll(summaryCount, summaryText);
        summary.getChildren().addAll(summaryLabel, summaryLine);

        cardHeader.getChildren().addAll(heading, FxSupport.spacer(), summary);

        HBox note = new HBox(10);
        note.getStyleClass().add("reports-note");
        note.setAlignment(Pos.TOP_LEFT);
        Label noteIcon = new Label("i");
        noteIcon.getStyleClass().add("reports-note-icon");
        reportNote.getStyleClass().add("reports-note-text");
        note.getChildren().addAll(noteIcon, reportNote);

        StackPane tableShell = new StackPane(reportTable);
        tableShell.getStyleClass().add("reports-table-shell");
        VBox.setVgrow(tableShell, Priority.ALWAYS);

        emptyState.getStyleClass().add("reports-empty-state");
        emptyState.setAlignment(Pos.CENTER);
        Label emptyIcon = new Label("[]");
        emptyIcon.getStyleClass().add("reports-empty-icon");
        emptyTitle.getStyleClass().add("reports-empty-title");
        emptyMessage.getStyleClass().add("reports-empty-message");
        emptyAction.getStyleClass().addAll("button", "reports-empty-button");
        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyMessage, emptyAction);

        bodyStack.getChildren().addAll(tableShell, emptyState);
        VBox.setVgrow(bodyStack, Priority.ALWAYS);

        reportCard.getChildren().addAll(cardHeader, note, bodyStack);
        return reportCard;
    }

    private void initTabs() {
        tabBar.getChildren().setAll(
                createTab(TAB_TODAY, "Ventas del dia", tabCalendarIcon(), this::showTodaySales),
                createTab(TAB_RANGE, "Por periodo", tabTrendIcon(), this::showSalesByRange),
                createTab(TAB_LOW_STOCK, "Stock bajo", tabInfoIcon(), this::showLowStock),
                createTab(TAB_CATALOG, "Catalogo", tabBoxIcon(), this::showProducts),
                createTab(TAB_MONTH_TOTAL, "Total por mes", tabSigmaIcon(), this::showMonthlyTotals)
        );
    }

    private void initPresetButtons() {
        presetBar.getStyleClass().add("reports-preset-bar");
        addPresetButton("Ayer", "yesterday");
        addPresetButton("7 dias", "7_days");
        addPresetButton("30 dias", "30_days");
        addPresetButton("180 dias", "180_days");
        addPresetButton("360 dias", "360_days");
        addPresetButton("Total", "total");
        updatePresetSelection();
    }

    private void addPresetButton(String text, String preset) {
        Button button = new Button(text);
        button.getStyleClass().add("reports-preset-button");
        button.setOnAction(event -> applyRangePreset(preset));
        presetButtons.put(preset, button);
        presetBar.getChildren().add(button);
    }

    private void configureDatePicker(DatePicker picker) {
        picker.getStyleClass().add("reports-date-picker");
        picker.setPrefWidth(140);
        picker.setEditable(false);
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate value) {
                return value == null ? "" : DISPLAY_DATE_FORMATTER.format(value);
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.isBlank()) {
                    return null;
                }
                return LocalDate.parse(string, DISPLAY_DATE_FORMATTER);
            }
        });
    }

    private Button createTab(String key, String text, Node icon, Runnable action) {
        Button button = new Button(text, icon);
        button.getStyleClass().add("reports-tab-button");
        button.setOnAction(event -> {
            activeTab = key;
            updateTabSelection();
            action.run();
        });
        tabButtons.put(key, button);
        return button;
    }

    private void updateTabSelection() {
        tabButtons.forEach((key, button) -> button.getStyleClass().remove("reports-tab-button-active"));
        Button activeButton = tabButtons.get(activeTab);
        if (activeButton != null) {
            activeButton.getStyleClass().add("reports-tab-button-active");
        }
        updatePresetVisibility();
    }

    private void updatePresetVisibility() {
        boolean visible = TAB_RANGE.equals(activeTab) || TAB_MONTH_TOTAL.equals(activeTab);
        presetBar.setVisible(visible);
        presetBar.setManaged(visible);
    }

    private void updatePresetSelection() {
        presetButtons.forEach((preset, button) -> button.getStyleClass().remove("reports-preset-button-active"));
        Button active = presetButtons.get(activeRangePreset);
        if (active != null) {
            active.getStyleClass().add("reports-preset-button-active");
        }
    }

    private void loadActiveReport() {
        switch (activeTab) {
            case TAB_RANGE -> showSalesByRange();
            case TAB_LOW_STOCK -> showLowStock();
            case TAB_CATALOG -> showProducts();
            case TAB_MONTH_TOTAL -> showMonthlyTotals();
            default -> showTodaySales();
        }
    }

    private void showTodaySales() {
        activeTab = TAB_TODAY;
        updateTabSelection();
        try {
            List<Sale> sales = saleService.findTodaySales();
            renderReport(
                    "Centro analitico",
                    "Ventas del dia",
                    "Movimientos registrados para la fecha actual.",
                    List.of("ID", "Fecha", "Cajero", "Cliente", "Pago", "Total"),
                    sales.stream().map(sale -> new String[]{
                        String.valueOf(sale.getId()),
                        sale.getSaleDate() == null ? "" : sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER),
                        sale.getUser() == null ? "" : sale.getUser().getFullName(),
                        sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName(),
                        sale.getPaymentMethod(),
                        CurrencyUtils.format(sale.getTotal())
                    }).toList(),
                    sales.size(),
                    "registros encontrados.",
                    "Usa este reporte para validar el cierre de caja y auditar las ventas exactas procesadas durante el turno actual.",
                    "No hay datos para mostrar",
                    "No se encontraron registros de ventas para la fecha actual.",
                    "Cambiar fechas",
                    false
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showSalesByRange() {
        activeTab = TAB_RANGE;
        updateTabSelection();
        try {
            List<Sale> sales = saleService.findSalesByDateRange(fromPicker.getValue(), toPicker.getValue());
            renderReport(
                    "Analisis por periodo",
                    "Ventas por periodo",
                    "Ventas entre el " + fromPicker.getValue().format(AppConfig.DATE_FORMATTER)
                            + " y el " + toPicker.getValue().format(AppConfig.DATE_FORMATTER) + ".",
                    List.of("ID", "Fecha", "Cajero", "Cliente", "Pago", "Subtotal", "Impuesto", "Total"),
                    sales.stream().map(sale -> new String[]{
                        String.valueOf(sale.getId()),
                        sale.getSaleDate() == null ? "" : sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER),
                        sale.getUser() == null ? "" : sale.getUser().getFullName(),
                        sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName(),
                        sale.getPaymentMethod(),
                        CurrencyUtils.format(sale.getSubtotal()),
                        CurrencyUtils.format(sale.getTax()),
                        CurrencyUtils.format(sale.getTotal())
                    }).toList(),
                    sales.size(),
                    "ventas encontradas.",
                    "Compara comportamiento comercial, cierres parciales y dias con mayor movimiento dentro del rango seleccionado.",
                    "No hay datos para mostrar",
                    "No se encontraron registros de ventas para el rango de fechas seleccionado.",
                    "Cambiar fechas",
                    true
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showLowStock() {
        activeTab = TAB_LOW_STOCK;
        updateTabSelection();
        try {
            List<Product> products = productService.findLowStockProducts();
            renderReport(
                    "Reposicion",
                    "Stock bajo",
                    "Productos que ya alcanzaron o quedaron cerca del minimo configurado.",
                    List.of("Codigo", "Producto", "Categoria", "Stock", "Minimo", "Estado"),
                    products.stream().map(product -> new String[]{
                        product.getCode(),
                        product.getName(),
                        product.getCategory() == null ? "" : product.getCategory().getName(),
                        String.valueOf(product.getStock()),
                        String.valueOf(product.getMinimumStock()),
                        product.getStatus()
                    }).toList(),
                    products.size(),
                    "productos con alerta.",
                    "Este reporte ayuda a priorizar compras y entradas de mercaderia antes de afectar el flujo de ventas.",
                    "Stock controlado",
                    "No hay productos por debajo del stock minimo configurado.",
                    "",
                    false
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showProducts() {
        activeTab = TAB_CATALOG;
        updateTabSelection();
        try {
            List<Product> products = productService.findProducts("");
            renderReport(
                    "Catalogo",
                    "Catalogo de productos",
                    "Vista general del catalogo activo con precios y disponibilidad.",
                    List.of("Codigo", "Producto", "Categoria", "Venta", "Stock", "Estado"),
                    products.stream().map(product -> new String[]{
                        product.getCode(),
                        product.getName(),
                        product.getCategory() == null ? "" : product.getCategory().getName(),
                        CurrencyUtils.format(product.getSalePrice()),
                        String.valueOf(product.getStock()),
                        product.getStatus()
                    }).toList(),
                    products.size(),
                    "productos listados.",
                    "Revisa rapidamente disponibilidad, categorias y precios activos del mini market.",
                    "No hay productos para mostrar",
                    "No se encontraron productos activos en el catalogo.",
                    "",
                    false
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showMonthlyTotals() {
        activeTab = TAB_MONTH_TOTAL;
        updateTabSelection();
        try {
            List<DailySalesTotal> totals = reportService.findDailyTotals(fromPicker.getValue(), toPicker.getValue());
            Map<YearMonth, BigDecimal> monthlyTotals = new LinkedHashMap<>();
            totals.forEach(row -> monthlyTotals.merge(YearMonth.from(row.getDate()), row.getTotal(), BigDecimal::add));

            renderReport(
                    "Totales",
                    "Total por mes",
                    "Acumulado mensual entre el " + fromPicker.getValue().format(AppConfig.DATE_FORMATTER)
                            + " y el " + toPicker.getValue().format(AppConfig.DATE_FORMATTER) + ".",
                    List.of("Mes", "Total vendido"),
                    monthlyTotals.entrySet().stream().map(entry -> new String[]{
                        entry.getKey().format(MONTH_FORMATTER),
                        CurrencyUtils.format(entry.getValue())
                    }).toList(),
                    monthlyTotals.size(),
                    "meses resumidos.",
                    "Usa esta vista para detectar meses fuertes, caidas de venta y tendencia acumulada del negocio.",
                    "No hay datos para mostrar",
                    "No hay ventas acumuladas para construir el total mensual del rango seleccionado.",
                    "Cambiar fechas",
                    true
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void renderReport(String chip,
                              String title,
                              String subtitle,
                              List<String> headers,
                              List<String[]> data,
                              int summaryCountValue,
                              String summaryTextValue,
                              String note,
                              String emptyTitleText,
                              String emptyMessageText,
                              String emptyActionLabel,
                              boolean showPresets) {
        reportChip.setText(chip);
        reportTitle.setText(title);
        reportSubtitle.setText(subtitle);
        summaryCount.setText(String.valueOf(summaryCountValue));
        summaryText.setText(summaryTextValue == null ? "registros encontrados." : summaryTextValue);
        reportNote.setText(note);

        presetBar.setVisible(showPresets);
        presetBar.setManaged(showPresets);
        updatePresetSelection();

        reportTable.getColumns().clear();
        reportTable.getItems().clear();
        reportTable.setVisible(true);
        reportTable.setManaged(true);

        if (data == null || data.isEmpty()) {
            emptyTitle.setText(emptyTitleText);
            emptyMessage.setText(emptyMessageText);
            emptyAction.setText(emptyActionLabel == null || emptyActionLabel.isBlank() ? "Cambiar fechas" : emptyActionLabel);
            emptyAction.setVisible(emptyActionLabel != null && !emptyActionLabel.isBlank());
            emptyAction.setManaged(emptyAction.isVisible());
            reportTable.setVisible(false);
            reportTable.setManaged(false);
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);
        emptyAction.setVisible(true);
        emptyAction.setManaged(true);

        ObservableList<ReportRow> rows = FXCollections.observableArrayList();
        data.forEach(row -> rows.add(new ReportRow(row)));

        for (int index = 0; index < headers.size(); index++) {
            String header = headers.get(index);
            TableColumn<ReportRow, String> column = new TableColumn<>(header);
            final int columnIndex = index;
            column.setCellValueFactory(cellData -> cellData.getValue().valueAt(columnIndex));
            column.getStyleClass().add("reports-table-column");
            if (isNumericColumn(header)) {
                column.getStyleClass().add("reports-table-column-numeric");
                column.setStyle("-fx-alignment: CENTER-RIGHT;");
            }
            column.setPrefWidth(preferredWidthFor(header, index));
            reportTable.getColumns().add(column);
        }

        reportTable.setItems(rows);
    }

    private boolean isNumericColumn(String header) {
        String normalized = header == null ? "" : header.toLowerCase(Locale.ROOT);
        return normalized.contains("total")
                || normalized.contains("subtotal")
                || normalized.contains("impuesto")
                || normalized.contains("venta")
                || normalized.contains("stock")
                || normalized.contains("minimo")
                || normalized.contains("cantidad");
    }

    private double preferredWidthFor(String header, int index) {
        if (index == 0) {
            return 96;
        }
        String normalized = header == null ? "" : header.toLowerCase(Locale.ROOT);
        if (normalized.contains("fecha")) {
            return 140;
        }
        if (normalized.contains("cliente") || normalized.contains("producto")) {
            return 180;
        }
        if (normalized.contains("observacion") || normalized.contains("motivo")) {
            return 220;
        }
        if (isNumericColumn(header)) {
            return 120;
        }
        return 140;
    }

    private void applyRangePreset(String preset) {
        LocalDate today = LocalDate.now();
        LocalDate from;
        LocalDate to;

        switch (preset) {
            case "yesterday" -> {
                from = today.minusDays(1);
                to = today.minusDays(1);
            }
            case "7_days" -> {
                from = today.minusDays(6);
                to = today;
            }
            case "30_days" -> {
                from = today.minusDays(29);
                to = today;
            }
            case "180_days" -> {
                from = today.minusDays(179);
                to = today;
            }
            case "360_days" -> {
                from = today.minusDays(359);
                to = today;
            }
            case "total" -> {
                from = earliestSaleDateOr(today);
                to = today;
            }
            default -> {
                return;
            }
        }

        activeRangePreset = preset;
        fromPicker.setValue(from);
        toPicker.setValue(to);
        activeTab = TAB_RANGE;
        updateTabSelection();
        showSalesByRange();
    }

    private String detectRangePreset(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return "";
        }
        LocalDate today = LocalDate.now();
        if (from.equals(today.minusDays(1)) && to.equals(today.minusDays(1))) {
            return "yesterday";
        }
        if (from.equals(today.minusDays(6)) && to.equals(today)) {
            return "7_days";
        }
        if (from.equals(today.minusDays(29)) && to.equals(today)) {
            return "30_days";
        }
        if (from.equals(today.minusDays(179)) && to.equals(today)) {
            return "180_days";
        }
        if (from.equals(today.minusDays(359)) && to.equals(today)) {
            return "360_days";
        }
        LocalDate earliest = earliestSaleDateOr(today);
        if (from.equals(earliest) && to.equals(today)) {
            return "total";
        }
        return "";
    }

    private LocalDate earliestSaleDateOr(LocalDate fallback) {
        try {
            LocalDate earliest = saleService.findEarliestSaleDate();
            return earliest == null ? fallback : earliest;
        } catch (Exception exception) {
            return fallback;
        }
    }

    private static String css(String path) {
        return Objects.requireNonNull(ReportsFxView.class.getResource(path)).toExternalForm();
    }

    private Node tabCalendarIcon() {
        return tabIcon("M8 2v2M16 2v2M3 7h18M5 4h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z");
    }

    private Node tabTrendIcon() {
        return tabIcon("M4 16l5-5 3 3 6-7M18 7h-6v6");
    }

    private Node tabInfoIcon() {
        SVGPath circle = new SVGPath();
        circle.setContent("M12 2a10 10 0 1 0 0 20a10 10 0 0 0 0-20");
        circle.getStyleClass().add("reports-tab-icon");

        SVGPath stem = new SVGPath();
        stem.setContent("M12 10v5");
        stem.getStyleClass().add("reports-tab-icon");

        SVGPath dot = new SVGPath();
        dot.setContent("M12 7h.01");
        dot.getStyleClass().add("reports-tab-icon");

        return new StackPane(circle, stem, dot);
    }

    private Node tabBoxIcon() {
        return tabIcon("m7.5 4.3 9 5.2M3 8l9 5 9-5M12 22V13M4 6.8l7-4a2 2 0 0 1 2 0l7 4A2 2 0 0 1 21 8v8a2 2 0 0 1-1 1.7l-7 4a2 2 0 0 1-2 0l-7-4A2 2 0 0 1 3 16V8a2 2 0 0 1 1-1.2Z");
    }

    private Node tabSigmaIcon() {
        Label label = new Label("\u03A3");
        label.getStyleClass().add("reports-tab-sigma");
        return label;
    }

    private Node tabIcon(String content) {
        SVGPath icon = new SVGPath();
        icon.setContent(content);
        icon.getStyleClass().add("reports-tab-icon");
        Region wrap = new Region();
        wrap.setMinSize(0, 0);
        return new StackPane(icon, wrap);
    }

    private static final class ReportRow {

        private final List<String> cells;

        private ReportRow(String[] values) {
            this.cells = List.of(values);
        }

        private javafx.beans.value.ObservableValue<String> valueAt(int index) {
            String value = index < cells.size() ? cells.get(index) : "";
            return new javafx.beans.property.ReadOnlyStringWrapper(value);
        }
    }
}
