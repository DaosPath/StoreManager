package com.manager.storemanager.fx.view;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.fx.WebViewBridge;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import netscape.javascript.JSObject;

public class ReportsFxView implements FxView {

    private static final String TAB_TODAY = "today";
    private static final String TAB_RANGE = "range";
    private static final String TAB_LOW_STOCK = "low_stock";
    private static final String TAB_CATALOG = "catalog";
    private static final String TAB_MONTH_TOTAL = "month_total";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "CO"));

    private final ProductService productService;
    private final SaleService saleService;
    private final ReportService reportService;
    private final VBox root = new VBox(16);
    private final WebViewBridge bridge = new WebViewBridge("/web/reports.html");
    private final DatePicker fromPicker = new DatePicker(LocalDate.now().minusDays(7));
    private final DatePicker toPicker = new DatePicker(LocalDate.now());
    private final Map<String, Button> tabButtons = new LinkedHashMap<>();
    private final ReportWebActions webActions = new ReportWebActions();
    private String activeTab = TAB_TODAY;
    private String activeRangePreset = "7_days";

    public ReportsFxView(ProductService productService, SaleService saleService, ReportService reportService) {
        this.productService = productService;
        this.saleService = saleService;
        this.reportService = reportService;
        root.getStyleClass().addAll("module-root", "reports-root");
        root.getChildren().addAll(buildHeader(), buildPane());
        bridge.whenReady(() -> {
            installWebActions();
            showTodaySales();
        });
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

        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("reports-topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox filterBar = new HBox(8);
        filterBar.getStyleClass().add("reports-filterbar");
        filterBar.setAlignment(Pos.CENTER_RIGHT);

        configureDatePicker(fromPicker);
        configureDatePicker(toPicker);

        Button applyButton = new Button("Aplicar filtros");
        applyButton.getStyleClass().addAll("button", "reports-apply-button");
        applyButton.setOnAction(event -> {
            activeRangePreset = detectRangePreset(fromPicker.getValue(), toPicker.getValue());
            loadActiveReport();
        });

        filterBar.getChildren().addAll(fromPicker, toPicker, applyButton);
        topBar.getChildren().addAll(
                FxSupport.pageHeader("Reportes", "Ventas, stock bajo, catalogo y totales del negocio."),
                FxSupport.spacer(),
                filterBar
        );

        HBox tabs = new HBox(10);
        tabs.getStyleClass().add("reports-tabs");
        tabs.getChildren().addAll(
                createTab(TAB_TODAY, "Ventas del dia", tabCalendarIcon(), this::showTodaySales),
                createTab(TAB_RANGE, "Por periodo", tabTrendIcon(), this::showSalesByRange),
                createTab(TAB_LOW_STOCK, "Stock bajo", tabInfoIcon(), this::showLowStock),
                createTab(TAB_CATALOG, "Catalogo", tabBoxIcon(), this::showProducts),
                createTab(TAB_MONTH_TOTAL, "Total por mes", tabSigmaIcon(), this::showMonthlyTotals)
        );

        header.getChildren().addAll(topBar, tabs);
        return header;
    }

    private Node buildPane() {
        bridge.getView().getStyleClass().add("reports-webview");
        bridge.getView().setPrefHeight(760);
        StackPane pane = new StackPane(bridge.getView());
        pane.getStyleClass().add("reports-content-wrap");
        VBox.setVgrow(pane, Priority.ALWAYS);
        return pane;
    }

    private void configureDatePicker(DatePicker picker) {
        picker.getStyleClass().addAll("stock-date-picker", "reports-date-picker");
        picker.setPrefWidth(138);
    }

    private Button createTab(String key, String text, Node icon, Runnable action) {
        Button button = new Button(text, icon);
        button.getStyleClass().add("reports-tab-button");
        button.setOnAction(event -> {
            activeTab = key;
            updateActiveTabStyles();
            action.run();
        });
        tabButtons.put(key, button);
        updateActiveTabStyles();
        return button;
    }

    private void updateActiveTabStyles() {
        tabButtons.forEach((key, button) -> {
            button.getStyleClass().remove("reports-tab-button-active");
            if (key.equals(activeTab)) {
                button.getStyleClass().add("reports-tab-button-active");
            }
        });
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
        updateActiveTabStyles();
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
        updateActiveTabStyles();
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
        updateActiveTabStyles();
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
        updateActiveTabStyles();
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
        updateActiveTabStyles();
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
                              int summaryCount,
                              String summaryText,
                              String note,
                              String emptyTitle,
                              String emptyMessage,
                              String emptyActionLabel,
                              boolean showPresets) {
        String payload = "{"
                + "\"chip\":" + WebViewBridge.jsString(chip) + ","
                + "\"title\":" + WebViewBridge.jsString(title) + ","
                + "\"subtitle\":" + WebViewBridge.jsString(subtitle) + ","
                + "\"summaryCount\":" + WebViewBridge.jsString(String.valueOf(summaryCount)) + ","
                + "\"summaryText\":" + WebViewBridge.jsString(summaryText) + ","
                + "\"note\":" + WebViewBridge.jsString(note) + ","
                + "\"emptyTitle\":" + WebViewBridge.jsString(emptyTitle) + ","
                + "\"emptyMessage\":" + WebViewBridge.jsString(emptyMessage) + ","
                + "\"emptyActionLabel\":" + WebViewBridge.jsString(emptyActionLabel) + ","
                + "\"showPresets\":" + showPresets + ","
                + "\"activePreset\":" + WebViewBridge.jsString(activeRangePreset) + ","
                + "\"columns\":" + WebViewBridge.jsArray(headers) + ","
                + "\"rows\":" + WebViewBridge.jsMatrix(data)
                + "}";
        bridge.execute("window.renderReport(" + payload + ");");
    }

    private void installWebActions() {
        JSObject window = (JSObject) bridge.getEngine().executeScript("window");
        window.setMember("reportActions", webActions);
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

        if (!TAB_RANGE.equals(activeTab) && !TAB_MONTH_TOTAL.equals(activeTab)) {
            activeTab = TAB_RANGE;
            updateActiveTabStyles();
        }
        loadActiveReport();
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

    public final class ReportWebActions {

        public void applyPreset(String preset) {
            Platform.runLater(() -> applyRangePreset(preset));
        }
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
        circle.getStyleClass().add("reports-tab-icon-stroke");

        SVGPath stem = new SVGPath();
        stem.setContent("M12 10v5");
        stem.getStyleClass().add("reports-tab-icon-stroke");

        SVGPath dot = new SVGPath();
        dot.setContent("M12 7h.01");
        dot.getStyleClass().add("reports-tab-icon-stroke");

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
        icon.getStyleClass().add("reports-tab-icon-stroke");
        Region wrap = new Region();
        wrap.setMinSize(0, 0);
        StackPane container = new StackPane(icon, wrap);
        container.getStyleClass().add("reports-tab-icon-wrap");
        return container;
    }
}
