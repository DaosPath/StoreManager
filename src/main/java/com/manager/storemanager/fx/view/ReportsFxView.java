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
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ReportsFxView implements FxView {

    private final ProductService productService;
    private final SaleService saleService;
    private final ReportService reportService;
    private final VBox root = new VBox(18);
    private final TableView<String[]> table = new TableView<>();
    private final ObservableList<String[]> rows = FXCollections.observableArrayList();
    private final DatePicker fromPicker = new DatePicker(LocalDate.now().minusDays(7));
    private final DatePicker toPicker = new DatePicker(LocalDate.now());

    public ReportsFxView(ProductService productService, SaleService saleService, ReportService reportService) {
        this.productService = productService;
        this.saleService = saleService;
        this.reportService = reportService;
        root.getStyleClass().add("module-root");
        root.getChildren().addAll(buildHeader(), buildPane());
        table.setItems(rows);
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
        showTodaySales();
    }

    private Node buildHeader() {
        HBox header = new HBox(10);
        header.getChildren().add(FxSupport.pageHeader("Reportes", "Ventas, stock bajo, catalogo y totales diarios."));
        Button todayButton = button("Ventas del dia", event -> showTodaySales());
        Button rangeButton = button("Por rango", event -> showSalesByRange());
        Button lowStockButton = button("Stock bajo", event -> showLowStock());
        Button productsButton = button("Productos", event -> showProducts());
        Button totalsButton = button("Total por dia", event -> showDailyTotals());
        header.getChildren().addAll(FxSupport.spacer(), fromPicker, toPicker, todayButton, rangeButton, lowStockButton, productsButton, totalsButton);
        return header;
    }

    private Node buildPane() {
        VBox pane = new VBox(table);
        pane.getStyleClass().add("surface-pane");
        VBox.setVgrow(table, Priority.ALWAYS);
        return pane;
    }

    private void showTodaySales() {
        try {
            List<Sale> sales = saleService.findTodaySales();
            setReport(
                    List.of("ID", "Fecha", "Cajero", "Cliente", "Pago", "Total"),
                    sales.stream().map(sale -> new String[]{
                        String.valueOf(sale.getId()),
                        sale.getSaleDate() == null ? "" : sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER),
                        sale.getUser() == null ? "" : sale.getUser().getFullName(),
                        sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName(),
                        sale.getPaymentMethod(),
                        CurrencyUtils.format(sale.getTotal())
                    }).toList()
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showSalesByRange() {
        try {
            List<Sale> sales = saleService.findSalesByDateRange(fromPicker.getValue(), toPicker.getValue());
            setReport(
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
                    }).toList()
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showLowStock() {
        try {
            List<Product> products = productService.findLowStockProducts();
            setReport(
                    List.of("Codigo", "Producto", "Categoria", "Stock", "Minimo", "Estado"),
                    products.stream().map(product -> new String[]{
                        product.getCode(),
                        product.getName(),
                        product.getCategory() == null ? "" : product.getCategory().getName(),
                        String.valueOf(product.getStock()),
                        String.valueOf(product.getMinimumStock()),
                        product.getStatus()
                    }).toList()
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showProducts() {
        try {
            List<Product> products = productService.findProducts("");
            setReport(
                    List.of("Codigo", "Producto", "Categoria", "Venta", "Stock", "Estado"),
                    products.stream().map(product -> new String[]{
                        product.getCode(),
                        product.getName(),
                        product.getCategory() == null ? "" : product.getCategory().getName(),
                        CurrencyUtils.format(product.getSalePrice()),
                        String.valueOf(product.getStock()),
                        product.getStatus()
                    }).toList()
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showDailyTotals() {
        try {
            List<DailySalesTotal> totals = reportService.findDailyTotals(fromPicker.getValue(), toPicker.getValue());
            setReport(
                    List.of("Fecha", "Total"),
                    totals.stream().map(row -> new String[]{
                        row.getDate().format(AppConfig.DATE_FORMATTER),
                        CurrencyUtils.format(row.getTotal())
                    }).toList()
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void setReport(List<String> headers, List<String[]> data) {
        table.getColumns().clear();
        for (int i = 0; i < headers.size(); i++) {
            final int index = i;
            TableColumn<String[], String> column = new TableColumn<>(headers.get(i));
            column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue()[index]));
            table.getColumns().add(column);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rows.setAll(data);
    }

    private Button button(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "button-secondary");
        button.setOnAction(action);
        return button;
    }
}
