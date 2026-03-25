package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.DashboardSummary;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.util.CurrencyUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardFxView implements FxView {

    private final ReportService reportService;
    private final VBox root = new VBox(18);
    private final Label productsValue = metricValue();
    private final Label customersValue = metricValue();
    private final Label suppliersValue = metricValue();
    private final Label lowStockValue = metricValue();
    private final Label salesValue = metricValue();

    public DashboardFxView(ReportService reportService) {
        this.reportService = reportService;
        root.getStyleClass().add("module-root");
        root.getChildren().addAll(buildHeader(), buildCards());
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
            DashboardSummary summary = reportService.loadDashboardSummary();
            productsValue.setText(String.valueOf(summary.getProductCount()));
            customersValue.setText(String.valueOf(summary.getCustomerCount()));
            suppliersValue.setText(String.valueOf(summary.getSupplierCount()));
            lowStockValue.setText(String.valueOf(summary.getLowStockCount()));
            salesValue.setText(CurrencyUtils.format(summary.getTodaySalesTotal()));
        } catch (Exception exception) {
            FxSupport.showError("Dashboard", exception.getMessage());
        }
    }

    private Node buildHeader() {
        HBox header = new HBox(12);
        header.getChildren().add(FxSupport.pageHeader("Dashboard", "Vista general del negocio, ventas y abastecimiento."));
        Region spacer = FxSupport.spacer();
        Button refreshButton = new Button("Actualizar");
        refreshButton.getStyleClass().addAll("button", "button-secondary");
        refreshButton.setOnAction(event -> refresh());
        header.getChildren().addAll(spacer, refreshButton);
        return header;
    }

    private Node buildCards() {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.add(metricCard("Productos activos", "Catalogo disponible para venta.", productsValue), 0, 0);
        grid.add(metricCard("Clientes", "Clientes listos para facturacion.", customersValue), 1, 0);
        grid.add(metricCard("Proveedores", "Base de abastecimiento actual.", suppliersValue), 2, 0);
        grid.add(metricCard("Stock bajo", "Productos que requieren reposicion.", lowStockValue), 0, 1);
        grid.add(metricCard("Ventas del dia", "Total vendido en la jornada actual.", salesValue), 1, 1);
        grid.add(tipCard(), 2, 1);
        GridPane.setHgrow(grid, Priority.ALWAYS);
        return grid;
    }

    private VBox metricCard(String title, String subtitle, Label valueLabel) {
        VBox card = new VBox(16);
        card.getStyleClass().add("metric-card");
        card.setPadding(new Insets(20));
        card.setPrefHeight(220);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("metric-subtitle");
        subtitleLabel.setWrapText(true);
        card.getChildren().addAll(titleLabel, subtitleLabel, valueLabel);
        return card;
    }

    private VBox tipCard() {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("metric-card");
        card.setStyle("-fx-background-color: linear-gradient(to bottom, #15233d, #101a2e);");
        card.setPadding(new Insets(20));
        Label title = new Label("Control diario");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 700;");
        Label body = new Label("Usa Inventario para registrar entradas de mercaderia y Reportes para revisar ventas por rango.");
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: #c5d3e6; -fx-font-size: 12px;");
        Label tag = new Label("Operacion estable");
        tag.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 999; -fx-text-fill: white; -fx-padding: 8 12 8 12;");
        card.getChildren().addAll(title, body, tag);
        return card;
    }

    private Label metricValue() {
        Label label = new Label("0");
        label.getStyleClass().add("metric-value");
        return label;
    }
}
