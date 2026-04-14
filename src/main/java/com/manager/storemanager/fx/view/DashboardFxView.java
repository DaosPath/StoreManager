package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.DailySalesTotal;
import com.manager.storemanager.model.DashboardSummary;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.util.CurrencyUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class DashboardFxView implements FxView {

    private final ReportService reportService;
    private final VBox root = new VBox(14);

    private final Label salesValue = label("dashboard-sales-value");
    private final Label salesMessage = label("dashboard-card-copy");
    private final Label productsValue = label("dashboard-metric-value");
    private final Label customersValue = label("dashboard-metric-value");
    private final Label suppliersValue = label("dashboard-metric-value");
    private final Label lowStockValue = label("dashboard-metric-value", "dashboard-warning-value");
    private final Label stockFlag = label("dashboard-tag", "dashboard-tag-warning");
    private final Label stockMessage = label("dashboard-card-copy");
    private final Label overviewTotal = label("dashboard-table-total");
    private final Label factsCustomers = label("dashboard-mini-value");
    private final Label factsProducts = label("dashboard-mini-value");
    private final Label factsSuppliers = label("dashboard-mini-value");
    private final Label factsSuppliersSecondary = label("dashboard-mini-value");
    private final Label summaryProducts = label("dashboard-summary-item");
    private final Label summaryCustomers = label("dashboard-summary-item");
    private final Label summarySuppliers = label("dashboard-summary-item");
    private final Label summaryProductsMeta = label("dashboard-summary-pill");
    private final Label summaryCustomersMeta = label("dashboard-summary-pill");
    private final Label summarySuppliersMeta = label("dashboard-summary-pill");
    private final Label checkSales = label("dashboard-task-text");
    private final Label checkStock = label("dashboard-task-text");
    private final Label checkCatalog = label("dashboard-task-text");
    private final SalesTrendChart trendChart = new SalesTrendChart();

    public DashboardFxView(ReportService reportService) {
        this.reportService = reportService;
        root.getStyleClass().addAll("module-root", "dashboard-root");
        root.getChildren().addAll(buildHeader(), buildGrid(), buildTrendCard());
        applyEmptyState();
    }

    @Override
    public String getName() {
        return "Dashboard";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            LocalDate to = LocalDate.now();
            LocalDate from = to.minusDays(29);
            render(reportService.loadDashboardSummary());
            renderTrend(reportService.findDailyTotals(from, to), from, to);
        } catch (Exception exception) {
            FxSupport.showError("Dashboard", exception.getMessage());
        }
    }

    private Node buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("dashboard-header");

        Label title = new Label("Dashboard");
        title.getStyleClass().add("dashboard-title");
        Label subtitle = new Label("Resumen general y metricas de tu negocio.");
        subtitle.getStyleClass().add("dashboard-subtitle");
        VBox copy = new VBox(4, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane statusIcon = new StackPane(refreshGlyph());
        statusIcon.getStyleClass().add("dashboard-status-icon");

        Label statusTitle = new Label("Base local");
        statusTitle.getStyleClass().add("dashboard-status-title");

        Label statusSubtitle = new Label("Sincronizado");
        statusSubtitle.getStyleClass().add("dashboard-status-subtitle");

        VBox statusCopy = new VBox(1, statusTitle, statusSubtitle);
        HBox statusCard = new HBox(12, statusIcon, statusCopy);
        statusCard.getStyleClass().add("dashboard-status-card");

        header.getChildren().addAll(copy, spacer, statusCard);
        return header;
    }

    private Node buildGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("dashboard-grid");
        grid.setHgap(14);
        grid.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(32);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(23);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(22);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(23);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        grid.add(buildSalesCard(), 0, 0);
        grid.add(buildMetricCard("Productos\nactivos", productsValue, "dashboard-card-green", productsGlyph()), 1, 0);
        grid.add(buildMetricCard("Clientes", customersValue, "dashboard-card-green", customersGlyph()), 2, 0);
        grid.add(buildMetricCard("Proveedores", suppliersValue, "dashboard-card-green", suppliersGlyph()), 3, 0);
        grid.add(buildStockCard(), 0, 1);
        grid.add(buildDataCard(), 1, 1, 2, 2);
        grid.add(buildSummaryCard(), 0, 2);
        grid.add(buildPrioritiesCard(), 3, 1, 1, 2);
        return grid;
    }

    private Node buildTrendCard() {
        Label title = new Label("Tendencia de ventas");
        title.getStyleClass().add("dashboard-chart-title");
        Label subtitle = new Label("Ultimos 30 dias");
        subtitle.getStyleClass().add("dashboard-chart-subtitle");
        VBox copy = new VBox(1, title, subtitle);

        HBox legend = new HBox(12,
                legendItem("Diaria", "dashboard-legend-dot-primary"),
                legendItem("Promedio", "dashboard-legend-dot-secondary"),
                legendItem("Tendencia", "dashboard-legend-dot-tertiary")
        );
        legend.getStyleClass().add("dashboard-legend");

        HBox head = new HBox(copy, FxSupport.spacer(), legend);
        head.getStyleClass().add("dashboard-chart-head");

        VBox card = card("dashboard-card", "dashboard-chart-card");
        card.setPrefHeight(192);
        VBox.setVgrow(trendChart, Priority.ALWAYS);
        card.getChildren().addAll(head, trendChart);
        return card;
    }

    private Node buildSalesCard() {
        VBox copy = new VBox(2);
        Label title = new Label("Ventas del dia");
        title.getStyleClass().add("dashboard-card-title");
        copy.getChildren().addAll(title, salesMessage);

        Label badge = new Label("Caja");
        badge.getStyleClass().addAll("dashboard-tag", "dashboard-tag-green");

        HBox head = new HBox(copy, FxSupport.spacer(), badge);
        head.getStyleClass().add("dashboard-card-head");

        StackPane trend = new StackPane(trendGlyph());
        trend.getStyleClass().add("dashboard-trend-wrap");

        HBox body = new HBox(salesValue, FxSupport.spacer(), trend);
        body.getStyleClass().add("dashboard-sales-body");

        VBox card = card("dashboard-card", "dashboard-card-sales");
        card.setPrefHeight(124);
        card.getChildren().addAll(head, body);
        return card;
    }

    private Node buildMetricCard(String titleText, Label valueLabel, String accentStyle, Node icon) {
        Label title = new Label(titleText);
        title.getStyleClass().add("dashboard-card-title");
        StackPane iconWrap = new StackPane(icon);
        iconWrap.getStyleClass().add("dashboard-metric-icon-wrap");
        HBox top = new HBox(title, FxSupport.spacer(), iconWrap);
        top.getStyleClass().add("dashboard-metric-head");

        VBox card = card("dashboard-card", "dashboard-card-metric", accentStyle);
        card.setPrefHeight(124);
        card.getChildren().addAll(top, valueLabel);
        return card;
    }

    private Node buildStockCard() {
        VBox copy = new VBox(2);
        Label title = new Label("Stock bajo");
        title.getStyleClass().add("dashboard-card-title");
        copy.getChildren().addAll(title, stockMessage);

        HBox head = new HBox(copy, FxSupport.spacer(), stockFlag);
        head.getStyleClass().add("dashboard-card-head");

        VBox card = card("dashboard-card", "dashboard-card-metric", "dashboard-card-warning");
        card.setPrefHeight(124);
        card.getChildren().addAll(head, lowStockValue);
        return card;
    }

    private Node buildDataCard() {
        VBox copy = new VBox(2);
        Label title = new Label("Datos operativos");
        title.getStyleClass().add("dashboard-card-title");
        Label subtitle = new Label("Conteos base.");
        subtitle.getStyleClass().add("dashboard-card-copy");
        copy.getChildren().addAll(title, subtitle);

        Label badge = new Label("Base");
        badge.getStyleClass().addAll("dashboard-tag", "dashboard-tag-outline");

        HBox head = new HBox(copy, FxSupport.spacer(), badge);
        head.getStyleClass().add("dashboard-card-head");

        VBox rows = new VBox(6,
                miniRow("Base de clientes", factsCustomers, false),
                miniRow("Base de productos", factsProducts, true),
                miniRow("Abastecimiento", factsSuppliers, false),
                miniRow("Proveedores", factsSuppliersSecondary, false)
        );

        VBox card = card("dashboard-card", "dashboard-card-data");
        card.setPrefHeight(244);
        card.getChildren().addAll(head, overviewTotal, rows);
        return card;
    }

    private Node buildSummaryCard() {
        Label title = new Label("Resumen operativo");
        title.getStyleClass().add("dashboard-card-title");
        VBox list = new VBox(10,
                summaryRow(summaryGlyph("product"), summaryProducts, summaryProductsMeta),
                summaryRow(summaryGlyph("customer"), summaryCustomers, summaryCustomersMeta),
                summaryRow(summaryGlyph("supplier"), summarySuppliers, summarySuppliersMeta)
        );
        VBox card = card("dashboard-card", "dashboard-card-summary");
        card.setPrefHeight(156);
        card.getChildren().addAll(title, list);
        return card;
    }

    private Node buildPrioritiesCard() {
        Label title = new Label("Prioridades");
        title.getStyleClass().add("dashboard-card-title");

        VBox tasks = new VBox(10,
                taskItem(checkSales, false),
                taskItem(checkStock, false),
                taskItem(checkCatalog, false)
        );

        VBox card = card("dashboard-card", "dashboard-card-priorities");
        card.setPrefHeight(244);
        card.getChildren().addAll(title, tasks);
        return card;
    }

    private Node miniRow(String labelText, Label valueLabel, boolean active) {
        Label text = new Label(labelText);
        text.getStyleClass().add("dashboard-mini-label");
        HBox row = new HBox(text, FxSupport.spacer(), valueLabel);
        row.getStyleClass().add(active ? "dashboard-mini-row-active" : "dashboard-mini-row");
        row.setPadding(new Insets(8, 10, 8, 10));
        return row;
    }

    private Node summaryRow(Node icon, Label textLabel, Label metaLabel) {
        StackPane iconWrap = new StackPane(icon);
        iconWrap.getStyleClass().add("dashboard-summary-icon-wrap");
        HBox row = new HBox(10, iconWrap, textLabel, FxSupport.spacer(), metaLabel);
        row.getStyleClass().add("dashboard-summary-row");
        row.setPadding(new Insets(0, 0, 0, 0));
        row.setMinHeight(24);
        return row;
    }

    private Node taskItem(Label textLabel, boolean active) {
        Label checkbox = new Label();
        checkbox.getStyleClass().add(active ? "dashboard-task-check-active" : "dashboard-task-check");
        HBox row = new HBox(10, checkbox, textLabel);
        row.getStyleClass().add(active ? "dashboard-task-item-active" : "dashboard-task-item");
        row.setPadding(new Insets(10, 12, 10, 12));
        return row;
    }

    private Node legendItem(String name, String dotStyle) {
        Region dot = new Region();
        dot.getStyleClass().addAll("dashboard-legend-dot", dotStyle);
        Label text = new Label(name);
        text.getStyleClass().add("dashboard-legend-text");
        HBox row = new HBox(6, dot, text);
        row.getStyleClass().add("dashboard-legend-item");
        return row;
    }

    private VBox card(String... styleClasses) {
        VBox box = new VBox(12);
        box.getStyleClass().addAll(styleClasses);
        return box;
    }

    private Label label(String... styleClasses) {
        Label label = new Label();
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    private SVGPath refreshGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M15.5 3.5v4h-4 M15 8a5 5 0 1 0 1.2 5.2");
        path.getStyleClass().add("dashboard-status-glyph");
        return path;
    }

    private SVGPath trendGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3 16 9 10 13 13 22 4 M16 4h6v6");
        path.getStyleClass().add("dashboard-trend-icon");
        return path;
    }

    private SVGPath productsGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("m7.5 4.27 9 5.15M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16ZM3.3 7l8.7 5 8.7-5M12 22V12");
        path.getStyleClass().add("dashboard-metric-icon");
        return path;
    }

    private SVGPath customersGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8a4 4 0 0 0 0 8zm13 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75");
        path.getStyleClass().add("dashboard-metric-icon");
        return path;
    }

    private SVGPath suppliersGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M10 17h4V5H2v12h3M20 17h2v-3.34a4 4 0 0 0-1.17-2.83L19 9h-5v8h2M7.5 20a2.5 2.5 0 1 0 0-5a2.5 2.5 0 0 0 0 5zm10 0a2.5 2.5 0 1 0 0-5a2.5 2.5 0 0 0 0 5z");
        path.getStyleClass().add("dashboard-metric-icon");
        return path;
    }

    private SVGPath summaryGlyph(String kind) {
        SVGPath path = new SVGPath();
        path.getStyleClass().add("dashboard-summary-icon");
        path.setContent(switch (kind) {
            case "product" -> "m7.5 4.27 9 5.15M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16ZM3.3 7l8.7 5 8.7-5M12 22V12";
            case "customer" -> "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8a4 4 0 0 0 0 8z";
            default -> "M10 17h4V5H2v12h3M20 17h2v-3.34a4 4 0 0 0-1.17-2.83L19 9h-5v8h2M7.5 20a2.5 2.5 0 1 0 0-5a2.5 2.5 0 0 0 0 5zm10 0a2.5 2.5 0 1 0 0-5a2.5 2.5 0 0 0 0 5z";
        });
        return path;
    }

    private void applyEmptyState() {
        salesValue.setText(CurrencyUtils.format(BigDecimal.ZERO));
        salesMessage.setText("Sin ventas registradas hoy.");
        productsValue.setText("0");
        customersValue.setText("0");
        suppliersValue.setText("0");
        lowStockValue.setText("0");
        stockFlag.setText("Normal");
        stockMessage.setText("No hay productos con reposicion urgente.");
        overviewTotal.setText("0");
        factsCustomers.setText("0");
        factsProducts.setText("0");
        factsSuppliers.setText("0");
        factsSuppliersSecondary.setText("0");
        summaryProducts.setText("Catalogo");
        summaryCustomers.setText("Base de clientes");
        summarySuppliers.setText("Abastecimiento");
        summaryProductsMeta.setText("0 productos");
        summaryCustomersMeta.setText("0 registros");
        summarySuppliersMeta.setText("0 proveedores");
        checkSales.setText("Verificar apertura y registro de ventas.");
        checkStock.setText("El nivel de stock se mantiene estable por ahora.");
        checkCatalog.setText("Mantener catalogo, clientes y proveedores consistentes.");
        trendChart.setSeries(List.of(
                new TrendSeries(Color.web("#e8ea52"), zeros(30)),
                new TrendSeries(Color.web("#f3bf4c"), zeros(30)),
                new TrendSeries(Color.web("#65d1c2"), zeros(30))
        ));
    }

    private void render(DashboardSummary summary) {
        int products = summary.getProductCount();
        int customers = summary.getCustomerCount();
        int suppliers = summary.getSupplierCount();
        int lowStock = summary.getLowStockCount();
        boolean hasSales = summary.getTodaySalesTotal() != null && summary.getTodaySalesTotal().signum() > 0;

        salesValue.setText(CurrencyUtils.format(summary.getTodaySalesTotal()));
        salesMessage.setText(hasSales ? "Hay ventas registradas." : "Sin ventas registradas hoy.");
        productsValue.setText(String.valueOf(products));
        customersValue.setText(String.valueOf(customers));
        suppliersValue.setText(String.valueOf(suppliers));
        lowStockValue.setText(String.valueOf(lowStock));
        overviewTotal.setText(String.valueOf(products + customers + suppliers + lowStock));
        factsCustomers.setText(String.valueOf(customers));
        factsProducts.setText(String.valueOf(products));
        factsSuppliers.setText(String.valueOf(suppliers));
        factsSuppliersSecondary.setText(String.valueOf(suppliers));
        summaryProducts.setText("Catalogo");
        summaryCustomers.setText("Base de clientes");
        summarySuppliers.setText("Abastecimiento");
        summaryProductsMeta.setText(products + " productos");
        summaryCustomersMeta.setText(customers + " registros");
        summarySuppliersMeta.setText(suppliers + " proveedores");

        if (lowStock > 0) {
            stockFlag.setText("Atencion");
            stockMessage.setText(lowStock + " productos requieren reposicion.");
            checkStock.setText("Priorizar la compra de " + lowStock + " productos con stock bajo.");
        } else {
            stockFlag.setText("Normal");
            stockMessage.setText("No hay productos con reposicion urgente.");
            checkStock.setText("El nivel de stock se mantiene estable por ahora.");
        }

        checkSales.setText(hasSales
                ? "Revisar tickets emitidos y validar cierre."
                : "Verificar apertura y registro de ventas.");
        checkCatalog.setText(products == 0
                ? "Cargar productos antes de habilitar la operacion comercial."
                : "Mantener catalogo, clientes y proveedores consistentes.");
    }

    private void renderTrend(List<DailySalesTotal> rows, LocalDate from, LocalDate to) {
        Map<LocalDate, BigDecimal> byDate = new HashMap<>();
        for (DailySalesTotal row : rows) {
            byDate.put(row.getDate(), row.getTotal() == null ? BigDecimal.ZERO : row.getTotal());
        }

        List<Double> actual = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            actual.add(byDate.getOrDefault(cursor, BigDecimal.ZERO).doubleValue());
            cursor = cursor.plusDays(1);
        }

        trendChart.setSeries(List.of(
                new TrendSeries(Color.web("#e8ea52"), actual),
                new TrendSeries(Color.web("#f3bf4c"), movingAverage(actual, 5)),
                new TrendSeries(Color.web("#65d1c2"), regressionLine(actual))
        ));
    }

    private List<Double> movingAverage(List<Double> values, int window) {
        List<Double> result = new ArrayList<>(values.size());
        for (int index = 0; index < values.size(); index++) {
            int start = Math.max(0, index - window + 1);
            double total = 0;
            int count = 0;
            for (int cursor = start; cursor <= index; cursor++) {
                total += values.get(cursor);
                count++;
            }
            result.add(count == 0 ? 0 : total / count);
        }
        return result;
    }

    private List<Double> regressionLine(List<Double> values) {
        int size = values.size();
        if (size == 0) {
            return List.of();
        }

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        for (int index = 0; index < size; index++) {
            double x = index;
            double y = values.get(index);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = size * sumX2 - sumX * sumX;
        double slope = denominator == 0 ? 0 : (size * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / size;

        List<Double> result = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            result.add(Math.max(0, intercept + slope * index));
        }
        return result;
    }

    private List<Double> zeros(int size) {
        List<Double> values = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            values.add(0d);
        }
        return values;
    }
}

final class TrendSeries {

    private final Color color;
    private final List<Double> points;

    TrendSeries(Color color, List<Double> points) {
        this.color = color;
        this.points = points;
    }

    public Color getColor() {
        return color;
    }

    public List<Double> getPoints() {
        return points;
    }
}

final class SalesTrendChart extends StackPane {

    private final Canvas canvas = new Canvas();
    private List<TrendSeries> series = List.of();

    SalesTrendChart() {
        getStyleClass().add("dashboard-chart-pane");
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);
        setMinHeight(124);
        setPrefHeight(124);
        widthProperty().addListener((obs, oldValue, newValue) -> draw());
        heightProperty().addListener((obs, oldValue, newValue) -> draw());
    }

    void setSeries(List<TrendSeries> series) {
        this.series = series;
        draw();
    }

    private void draw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        if (width <= 0 || height <= 0) {
            return;
        }

        double left = 10;
        double right = width - 10;
        double top = 8;
        double bottom = height - 10;

        gc.setStroke(Color.rgb(255, 255, 255, 0.18));
        gc.setLineWidth(1);
        for (int line = 0; line < 4; line++) {
            double y = top + ((bottom - top) / 3d) * line;
            gc.strokeLine(left, y, right, y);
        }

        double max = 1;
        int count = 0;
        for (TrendSeries item : series) {
            count = Math.max(count, item.getPoints().size());
            for (double value : item.getPoints()) {
                max = Math.max(max, value);
            }
        }

        if (count < 2) {
            return;
        }

        for (TrendSeries item : series) {
            gc.setStroke(item.getColor());
            gc.setLineWidth(1.6);
            gc.beginPath();
            boolean started = false;
            for (int index = 0; index < item.getPoints().size(); index++) {
                double x = left + (right - left) * index / (count - 1d);
                double y = bottom - ((bottom - top) * item.getPoints().get(index) / max);
                if (!started) {
                    gc.moveTo(x, y);
                    started = true;
                } else {
                    gc.lineTo(x, y);
                }
            }
            gc.stroke();
        }
    }
}
