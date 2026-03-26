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
import java.time.LocalDate;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ReportsFxView implements FxView {

    private final ProductService productService;
    private final SaleService saleService;
    private final ReportService reportService;
    private final VBox root = new VBox(18);
    private final WebViewBridge bridge = new WebViewBridge("/web/reports.html");
    private final DatePicker fromPicker = new DatePicker(LocalDate.now().minusDays(7));
    private final DatePicker toPicker = new DatePicker(LocalDate.now());

    public ReportsFxView(ProductService productService, SaleService saleService, ReportService reportService) {
        this.productService = productService;
        this.saleService = saleService;
        this.reportService = reportService;
        root.getStyleClass().add("module-root");
        root.getChildren().addAll(buildHeader(), buildPane());
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
        VBox pane = new VBox(bridge.getView());
        pane.getStyleClass().add("surface-pane");
        VBox.setVgrow(bridge.getView(), Priority.ALWAYS);
        return pane;
    }

    private void showTodaySales() {
        try {
            List<Sale> sales = saleService.findTodaySales();
            renderReport(
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
                    sales.size() + " registros encontrados.",
                    "Usa este reporte para validar el cierre de caja y las ventas del turno."
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showSalesByRange() {
        try {
            List<Sale> sales = saleService.findSalesByDateRange(fromPicker.getValue(), toPicker.getValue());
            renderReport(
                    "Ventas por rango",
                    "Periodo del " + fromPicker.getValue().format(AppConfig.DATE_FORMATTER)
                            + " al " + toPicker.getValue().format(AppConfig.DATE_FORMATTER) + ".",
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
                    sales.size() + " ventas dentro del rango seleccionado.",
                    "Sirve para comparar dias, cortes parciales y comportamiento comercial."
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showLowStock() {
        try {
            List<Product> products = productService.findLowStockProducts();
            renderReport(
                    "Productos con stock bajo",
                    "Items que ya alcanzaron o rozan el minimo configurado.",
                    List.of("Codigo", "Producto", "Categoria", "Stock", "Minimo", "Estado"),
                    products.stream().map(product -> new String[]{
                        product.getCode(),
                        product.getName(),
                        product.getCategory() == null ? "" : product.getCategory().getName(),
                        String.valueOf(product.getStock()),
                        String.valueOf(product.getMinimumStock()),
                        product.getStatus()
                    }).toList(),
                    products.size() + " productos con reposicion pendiente.",
                    "Revisa compras y entradas de mercaderia antes de afectar ventas."
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showProducts() {
        try {
            List<Product> products = productService.findProducts("");
            renderReport(
                    "Catalogo de productos",
                    "Vista general del catalogo activo con precio y disponibilidad.",
                    List.of("Codigo", "Producto", "Categoria", "Venta", "Stock", "Estado"),
                    products.stream().map(product -> new String[]{
                        product.getCode(),
                        product.getName(),
                        product.getCategory() == null ? "" : product.getCategory().getName(),
                        CurrencyUtils.format(product.getSalePrice()),
                        String.valueOf(product.getStock()),
                        product.getStatus()
                    }).toList(),
                    products.size() + " productos listados.",
                    "Este listado ayuda a revisar precios y disponibilidad del mini market."
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void showDailyTotals() {
        try {
            List<DailySalesTotal> totals = reportService.findDailyTotals(fromPicker.getValue(), toPicker.getValue());
            renderReport(
                    "Total vendido por dia",
                    "Acumulado diario entre las fechas seleccionadas.",
                    List.of("Fecha", "Total"),
                    totals.stream().map(row -> new String[]{
                        row.getDate().format(AppConfig.DATE_FORMATTER),
                        CurrencyUtils.format(row.getTotal())
                    }).toList(),
                    totals.size() + " dias resumidos.",
                    "Usa esta lectura para ver caidas, picos y tendencia reciente."
            );
        } catch (Exception exception) {
            FxSupport.showError("Reportes", exception.getMessage());
        }
    }

    private void renderReport(String title, String subtitle, List<String> headers, List<String[]> data,
                              String badge, String note) {
        String payload = "{"
                + "\"title\":" + WebViewBridge.jsString(title) + ","
                + "\"subtitle\":" + WebViewBridge.jsString(subtitle) + ","
                + "\"badge\":" + WebViewBridge.jsString(badge) + ","
                + "\"note\":" + WebViewBridge.jsString(note) + ","
                + "\"emptyMessage\":" + WebViewBridge.jsString("No hay datos para mostrar en este reporte.") + ","
                + "\"columns\":" + WebViewBridge.jsArray(headers) + ","
                + "\"rows\":" + WebViewBridge.jsMatrix(data)
                + "}";
        bridge.execute("window.renderReport(" + payload + ");");
    }

    private Button button(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "button-secondary");
        button.setOnAction(action);
        return button;
    }
}
