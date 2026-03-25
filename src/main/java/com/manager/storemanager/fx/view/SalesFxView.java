package com.manager.storemanager.fx.view;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Customer;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.model.SaleDetail;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.CustomerService;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.SaleService;
import com.manager.storemanager.util.CurrencyUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SalesFxView implements FxView {

    private final User currentUser;
    private final ProductService productService;
    private final CustomerService customerService;
    private final SaleService saleService;
    private final VBox root = new VBox(18);
    private final ObservableList<Product> productRows = FXCollections.observableArrayList();
    private final ObservableList<SaleDetail> cartRows = FXCollections.observableArrayList();
    private final TableView<Product> productTable = new TableView<>(productRows);
    private final TableView<SaleDetail> cartTable = new TableView<>(cartRows);
    private final TextField searchField = new TextField();
    private final ComboBox<Customer> customerBox = new ComboBox<>();
    private final ComboBox<String> paymentBox = new ComboBox<>(FXCollections.observableArrayList("Efectivo", "Tarjeta", "Transferencia", "Mixto"));
    private final TextArea observationArea = new TextArea();
    private final Label subtotalValue = new Label(CurrencyUtils.format(BigDecimal.ZERO));
    private final Label taxValue = new Label(CurrencyUtils.format(BigDecimal.ZERO));
    private final Label totalValue = new Label(CurrencyUtils.format(BigDecimal.ZERO));

    public SalesFxView(User currentUser, ProductService productService, CustomerService customerService, SaleService saleService) {
        this.currentUser = currentUser;
        this.productService = productService;
        this.customerService = customerService;
        this.saleService = saleService;
        root.getStyleClass().add("module-root");
        root.getChildren().addAll(buildHeader(), buildBody());
        buildTables();
        paymentBox.getSelectionModel().selectFirst();
    }

    @Override
    public String getName() {
        return "Ventas";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            productRows.setAll(productService.findProductsForSale(searchField.getText()));
            Customer empty = new Customer();
            empty.setName("Sin cliente");
            customerBox.getItems().setAll(empty);
            customerBox.getItems().addAll(customerService.findCustomers(""));
            customerBox.getSelectionModel().selectFirst();
            clearCart();
        } catch (Exception exception) {
            FxSupport.showError("Ventas", exception.getMessage());
        }
    }

    private Node buildHeader() {
        HBox header = new HBox(10);
        header.getChildren().add(FxSupport.pageHeader("Ventas", "Carrito de venta, pago y ticket."));
        searchField.setPromptText("Buscar producto");
        Button searchButton = button("Buscar", "button-secondary", event -> loadProductsOnly());
        Button addButton = button("Agregar", "button-primary", event -> addSelectedProduct());
        Button removeButton = button("Quitar", "button-secondary", event -> removeSelectedItem());
        header.getChildren().addAll(FxSupport.spacer(), searchField, searchButton, addButton, removeButton);
        return header;
    }

    private Node buildBody() {
        VBox productsPane = new VBox(12, new Label("Productos disponibles"), productTable);
        productsPane.getStyleClass().add("surface-pane");
        VBox.setVgrow(productTable, Priority.ALWAYS);

        VBox orderPane = new VBox(14);
        orderPane.getStyleClass().add("surface-pane");

        customerBox.setMaxWidth(Double.MAX_VALUE);
        paymentBox.setMaxWidth(Double.MAX_VALUE);
        observationArea.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        addFormRow(form, 0, "Cliente", customerBox);
        addFormRow(form, 1, "Metodo de pago", paymentBox);
        addFormRow(form, 2, "Observacion", observationArea);

        VBox cartPane = new VBox(10, new Label("Carrito"), cartTable);
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        VBox summaryPane = new VBox(8,
                summaryRow("Subtotal", subtotalValue),
                summaryRow("Impuesto", taxValue),
                summaryRow("Total", totalValue)
        );

        HBox actions = new HBox(10);
        Button clearButton = button("Limpiar", "button-secondary", event -> clearCart());
        Button processButton = button("Procesar venta", "button-primary", event -> processSale());
        actions.getChildren().addAll(clearButton, processButton);

        orderPane.getChildren().addAll(form, cartPane, summaryPane, actions);
        VBox.setVgrow(cartPane, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane(productsPane, orderPane);
        splitPane.setDividerPositions(0.52);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        return splitPane;
    }

    private void buildTables() {
        productTable.getColumns().add(productColumn("Codigo", Product::getCode));
        productTable.getColumns().add(productColumn("Producto", Product::getName));
        productTable.getColumns().add(productColumn("Precio", product -> CurrencyUtils.format(product.getSalePrice())));
        productTable.getColumns().add(productColumn("Stock", product -> String.valueOf(product.getStock())));
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        cartTable.getColumns().add(cartColumn("Codigo", detail -> detail.getProduct().getCode()));
        cartTable.getColumns().add(cartColumn("Producto", detail -> detail.getProduct().getName()));
        cartTable.getColumns().add(cartColumn("Cantidad", detail -> String.valueOf(detail.getQuantity())));
        cartTable.getColumns().add(cartColumn("Precio", detail -> CurrencyUtils.format(detail.getUnitPrice())));
        cartTable.getColumns().add(cartColumn("Subtotal", detail -> CurrencyUtils.format(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))));
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadProductsOnly() {
        try {
            productRows.setAll(productService.findProductsForSale(searchField.getText()));
        } catch (Exception exception) {
            FxSupport.showError("Ventas", exception.getMessage());
        }
    }

    private void addSelectedProduct() {
        Product product = productTable.getSelectionModel().getSelectedItem();
        if (product == null) {
            FxSupport.showError("Ventas", "Selecciona un producto.");
            return;
        }
        try {
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Cantidad");
            dialog.setHeaderText(null);
            dialog.setContentText("Cantidad a vender:");
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/storemanager.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("app-root");
            String value = dialog.showAndWait().orElse(null);
            if (value == null) {
                return;
            }
            int quantity = Integer.parseInt(value.trim());
            if (quantity <= 0 || quantity > product.getStock()) {
                throw new IllegalArgumentException("La cantidad supera el stock disponible.");
            }
            SaleDetail existing = findDetail(product);
            if (existing != null) {
                int newQuantity = existing.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    throw new IllegalArgumentException("La cantidad acumulada supera el stock.");
                }
                existing.setQuantity(newQuantity);
                cartTable.refresh();
            } else {
                SaleDetail detail = new SaleDetail();
                detail.setProduct(product);
                detail.setQuantity(quantity);
                detail.setUnitPrice(product.getSalePrice());
                cartRows.add(detail);
            }
            refreshSummary();
        } catch (Exception exception) {
            FxSupport.showError("Ventas", exception.getMessage());
        }
    }

    private void removeSelectedItem() {
        SaleDetail selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxSupport.showError("Ventas", "Selecciona un item del carrito.");
            return;
        }
        cartRows.remove(selected);
        refreshSummary();
    }

    private void processSale() {
        try {
            Customer customer = customerBox.getValue();
            if (customer != null && customer.getId() == null) {
                customer = null;
            }
            Sale sale = saleService.prepareSale(currentUser, customer, new ArrayList<>(cartRows), paymentBox.getValue(), observationArea.getText());
            Long saleId = saleService.createSale(sale);
            sale.setId(saleId);
            sale.setSaleDate(LocalDateTime.now());
            showTicket(sale);
            loadProductsOnly();
            clearCart();
        } catch (Exception exception) {
            FxSupport.showError("Ventas", exception.getMessage());
        }
    }

    private void showTicket(Sale sale) {
        StringBuilder builder = new StringBuilder();
        builder.append("STOREMANAGER\n");
        builder.append("Venta #").append(sale.getId()).append('\n');
        builder.append("Fecha: ").append(sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER)).append('\n');
        builder.append("Cajero: ").append(currentUser.getFullName()).append('\n');
        builder.append("Cliente: ").append(sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName()).append("\n\n");
        for (SaleDetail detail : sale.getDetails()) {
            builder.append(detail.getProduct().getName()).append(" x").append(detail.getQuantity()).append("  ").append(CurrencyUtils.format(detail.getLineTotal())).append('\n');
        }
        builder.append("\nTotal: ").append(CurrencyUtils.format(sale.getTotal()));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ticket");
        alert.setHeaderText("Venta procesada");
        TextArea area = new TextArea(builder.toString());
        area.setEditable(false);
        area.setPrefRowCount(16);
        alert.getDialogPane().setContent(area);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/storemanager.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("app-root");
        alert.showAndWait();
    }

    private void clearCart() {
        cartRows.clear();
        observationArea.clear();
        refreshSummary();
    }

    private void refreshSummary() {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (SaleDetail detail : cartRows) {
            BigDecimal lineSubtotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            BigDecimal lineTax = lineSubtotal.multiply(AppConfig.TAX_RATE);
            subtotal = subtotal.add(lineSubtotal);
            tax = tax.add(lineTax);
            total = total.add(lineSubtotal.add(lineTax));
        }
        subtotalValue.setText(CurrencyUtils.format(subtotal));
        taxValue.setText(CurrencyUtils.format(tax));
        totalValue.setText(CurrencyUtils.format(total));
    }

    private SaleDetail findDetail(Product product) {
        for (SaleDetail detail : cartRows) {
            if (detail.getProduct().getId().equals(product.getId())) {
                return detail;
            }
        }
        return null;
    }

    private HBox summaryRow(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("summary-label");
        valueLabel.getStyleClass().add("summary-value");
        return new HBox(10, label, FxSupport.spacer(), valueLabel);
    }

    private void addFormRow(GridPane grid, int row, String labelText, Node field) {
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

    private TableColumn<Product, String> productColumn(String title, java.util.function.Function<Product, String> mapper) {
        TableColumn<Product, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(mapper.apply(cell.getValue())));
        return column;
    }

    private TableColumn<SaleDetail, String> cartColumn(String title, java.util.function.Function<SaleDetail, String> mapper) {
        TableColumn<SaleDetail, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(mapper.apply(cell.getValue())));
        return column;
    }
}
