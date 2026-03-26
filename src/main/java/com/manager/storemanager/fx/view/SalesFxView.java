package com.manager.storemanager.fx.view;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.fx.SaleTicketWindow;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class SalesFxView implements FxView {

    private static final String ALL_CATEGORIES = "Todas";

    private final User currentUser;
    private final ProductService productService;
    private final CustomerService customerService;
    private final SaleService saleService;
    private final VBox root = new VBox(18);
    private final List<Product> loadedProducts = new ArrayList<>();
    private final ObservableList<SaleDetail> cartRows = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();
    private final FlowPane categoryChips = new FlowPane(10, 10);
    private final TilePane productGrid = new TilePane();
    private final ScrollPane productScroll = new ScrollPane(productGrid);
    private final StackPane productContent = new StackPane();
    private final StackPane cartContent = new StackPane();
    private final VBox cartItemsBox = new VBox(8);
    private final ComboBox<Customer> customerBox = new ComboBox<>();
    private final ComboBox<String> paymentBox = new ComboBox<>(FXCollections.observableArrayList(
            "Efectivo", "Tarjeta", "Transferencia", "Mixto"));
    private final TextArea observationArea = new TextArea();
    private final Label visibleCountLabel = new Label("0 visibles");
    private final Label subtotalValue = new Label(CurrencyUtils.format(BigDecimal.ZERO));
    private final Label taxValue = new Label(CurrencyUtils.format(BigDecimal.ZERO));
    private final Label totalValue = new Label(CurrencyUtils.format(BigDecimal.ZERO));
    private final Button clearButton = button("Limpiar orden", "button-secondary", event -> clearCart());
    private final Button processButton = button("Cobrar " + CurrencyUtils.format(BigDecimal.ZERO), "button-primary", event -> processSale());

    private String activeCategory = ALL_CATEGORIES;

    public SalesFxView(User currentUser, ProductService productService, CustomerService customerService, SaleService saleService) {
        this.currentUser = currentUser;
        this.productService = productService;
        this.customerService = customerService;
        this.saleService = saleService;

        root.getStyleClass().addAll("module-root", "sales-pos-root");
        Node shell = buildShell();
        root.getChildren().add(shell);
        VBox.setVgrow(shell, Priority.ALWAYS);

        searchField.setPromptText("Buscar por nombre o codigo");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> refreshCatalogView());
        searchField.setOnAction(event -> refreshCatalogView());

        categoryChips.getStyleClass().add("sales-chip-bar");

        productGrid.getStyleClass().add("sales-product-grid");
        productGrid.setHgap(14);
        productGrid.setVgap(14);
        productGrid.setPrefColumns(3);
        productGrid.setPrefTileWidth(210);
        productGrid.setTileAlignment(Pos.TOP_LEFT);

        productScroll.getStyleClass().add("sales-product-scroll");
        productScroll.setFitToWidth(true);
        productScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        productScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        productScroll.setContent(productGrid);
        FxSupport.enhanceScrollPane(productScroll, 1.9);
        productContent.getStyleClass().add("sales-product-content");
        productContent.getChildren().add(productScroll);
        VBox.setVgrow(productContent, Priority.ALWAYS);

        cartItemsBox.getStyleClass().add("sales-cart-list");
        cartContent.getStyleClass().add("sales-cart-content");

        customerBox.getStyleClass().add("sales-select");
        paymentBox.getStyleClass().add("sales-select");
        customerBox.setMaxWidth(Double.MAX_VALUE);
        paymentBox.setMaxWidth(Double.MAX_VALUE);
        configureSalesCombo(customerBox);
        configureSalesCombo(paymentBox);
        paymentBox.getSelectionModel().selectFirst();

        observationArea.getStyleClass().add("sales-note-area");
        observationArea.setPromptText("Observacion de la venta");
        observationArea.setPrefRowCount(2);
        observationArea.setWrapText(true);

        clearButton.getStyleClass().add("sales-clear-button");
        processButton.getStyleClass().add("sales-process-button");

        refreshCartView();
        refreshSummary();
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
            loadedProducts.clear();
            loadedProducts.addAll(productService.findProducts(""));
            loadCustomers();
            paymentBox.getSelectionModel().selectFirst();
            activeCategory = ALL_CATEGORIES;
            clearCart();
            rebuildCategoryChips();
            refreshCatalogView();
        } catch (Exception exception) {
            FxSupport.showError("Ventas", exception.getMessage());
        }
    }

    private Node buildShell() {
        HBox shell = new HBox(18);
        shell.getStyleClass().add("sales-pos-shell");
        shell.setFillHeight(true);

        VBox catalogPanel = buildCatalogPanel();
        VBox orderPanel = buildOrderPanel();
        HBox.setHgrow(catalogPanel, Priority.ALWAYS);
        HBox.setHgrow(orderPanel, Priority.NEVER);

        shell.getChildren().addAll(catalogPanel, orderPanel);
        return shell;
    }

    private VBox buildCatalogPanel() {
        VBox panel = new VBox(18);
        panel.getStyleClass().addAll("sales-panel", "sales-catalog-panel");

        Label title = new Label("Catalogo de venta");
        title.getStyleClass().add("sales-panel-title");
        Label subtitle = new Label("Busca por nombre o categoria para encontrar productos mas rapido.");
        subtitle.getStyleClass().add("sales-panel-subtitle");

        visibleCountLabel.getStyleClass().add("sales-count-pill");

        VBox titleBlock = new VBox(4, title, subtitle);
        HBox head = new HBox(12, titleBlock, FxSupport.spacer(), visibleCountLabel);
        head.getStyleClass().add("sales-panel-head");
        head.setAlignment(Pos.CENTER_LEFT);

        ScrollPane chipScroll = new ScrollPane(categoryChips);
        chipScroll.getStyleClass().add("sales-chip-scroll");
        chipScroll.setFitToHeight(true);
        chipScroll.setFitToWidth(true);
        chipScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chipScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        FxSupport.enhanceScrollPane(chipScroll, 1.65);

        panel.getChildren().addAll(head, searchShell(), chipScroll, productContent);
        VBox.setVgrow(productContent, Priority.ALWAYS);
        return panel;
    }

    private VBox buildOrderPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().addAll("sales-panel", "sales-order-panel");
        panel.setPrefWidth(404);
        panel.setMinWidth(404);

        Label title = new Label("Carrito de venta");
        title.getStyleClass().add("sales-panel-title");
        Label subtitle = new Label("Revisa cantidades, pago y observaciones antes de confirmar.");
        subtitle.getStyleClass().add("sales-panel-subtitle");

        VBox titleBlock = new VBox(4, title, subtitle);
        titleBlock.getStyleClass().add("sales-panel-head");

        HBox cartHead = new HBox(10);
        cartHead.setAlignment(Pos.CENTER_LEFT);
        Label cartLabel = new Label("Orden actual");
        cartLabel.getStyleClass().add("sales-section-title");
        cartHead.getChildren().addAll(cartLabel, FxSupport.spacer(), clearButton);

        VBox cartShell = new VBox(10, cartHead, cartContent);
        cartShell.getStyleClass().add("sales-cart-shell");

        VBox summary = new VBox(10,
                summaryRow("Subtotal", subtotalValue),
                summaryRow("Impuestos", taxValue),
                new Separator(),
                totalRow()
        );
        summary.getStyleClass().add("sales-summary-box");

        processButton.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(18, titleBlock, cartShell, summary, processButton);
        content.getStyleClass().add("sales-order-body");
        VBox.setVgrow(cartShell, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("sales-order-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        FxSupport.enhanceScrollPane(scrollPane, 2.0);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().add(scrollPane);
        return panel;
    }

    private Node searchShell() {
        HBox shell = new HBox(10);
        shell.getStyleClass().add("sales-search-shell");
        shell.setAlignment(Pos.CENTER_LEFT);
        shell.getChildren().addAll(searchGlyph(), searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        return shell;
    }

    private VBox fieldBlock(String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("sales-field-label");
        VBox box = new VBox(6, label, field);
        box.getStyleClass().add("sales-field-block");
        return box;
    }

    private <T> void configureSalesCombo(ComboBox<T> comboBox) {
        comboBox.setVisibleRowCount(6);
        comboBox.setCellFactory(ignored -> new SalesComboCell<>(true));
        comboBox.setButtonCell(new SalesComboCell<>(false));
    }

    private void loadCustomers() throws Exception {
        Customer empty = new Customer();
        empty.setName("Sin cliente");
        customerBox.getItems().setAll(empty);
        customerBox.getItems().addAll(customerService.findCustomers(""));
        customerBox.getSelectionModel().selectFirst();
    }

    private void rebuildCategoryChips() {
        List<String> categories = availableCategories();
        if (!categories.contains(activeCategory)) {
            activeCategory = ALL_CATEGORIES;
        }

        categoryChips.getChildren().clear();
        categoryChips.getChildren().add(categoryChip(ALL_CATEGORIES));
        for (String category : categories) {
            categoryChips.getChildren().add(categoryChip(category));
        }
    }

    private List<String> availableCategories() {
        Set<String> categories = new LinkedHashSet<>();
        for (Product product : loadedProducts) {
            if (!isCatalogVisible(product)) {
                continue;
            }
            categories.add(safeText(product.getCategoryName(), "Sin categoria"));
        }
        return new ArrayList<>(categories);
    }

    private Node categoryChip(String category) {
        Button chip = new Button(category);
        chip.getStyleClass().add("sales-chip");
        if (category.equals(activeCategory)) {
            chip.getStyleClass().add("sales-chip-active");
        }
        chip.setOnAction(event -> {
            activeCategory = category;
            rebuildCategoryChips();
            refreshCatalogView();
        });
        return chip;
    }

    private void refreshCatalogView() {
        List<Product> filtered = filteredCatalogProducts();
        int totalVisible = 0;
        for (Product product : loadedProducts) {
            if (isCatalogVisible(product)) {
                totalVisible++;
            }
        }
        visibleCountLabel.setText(filtered.size() + " de " + totalVisible + " visibles");

        if (filtered.isEmpty()) {
            productContent.getChildren().setAll(emptyCatalogState());
            return;
        }

        productGrid.getChildren().clear();
        for (Product product : filtered) {
            productGrid.getChildren().add(productCard(product));
        }
        productContent.getChildren().setAll(productScroll);
    }

    private List<Product> filteredCatalogProducts() {
        String search = safeText(searchField.getText(), "").toLowerCase();
        List<Product> filtered = new ArrayList<>();
        for (Product product : loadedProducts) {
            if (!isCatalogVisible(product)) {
                continue;
            }
            String category = safeText(product.getCategoryName(), "Sin categoria");
            boolean categoryMatch = ALL_CATEGORIES.equals(activeCategory) || activeCategory.equals(category);
            if (!categoryMatch) {
                continue;
            }
            String searchable = (safeText(product.getCode(), "") + " " + safeText(product.getName(), "") + " " + category).toLowerCase();
            if (search.isBlank() || searchable.contains(search)) {
                filtered.add(product);
            }
        }
        return filtered;
    }

    private boolean isCatalogVisible(Product product) {
        return product != null && !"INACTIVO".equalsIgnoreCase(product.getStatus());
    }

    private Node productCard(Product product) {
        VBox card = new VBox(12);
        card.getStyleClass().add("sales-product-card");
        card.setPrefWidth(210);
        card.setMinWidth(210);
        card.setMaxWidth(210);

        StackPane iconWrap = new StackPane(boxGlyph());
        iconWrap.getStyleClass().add("sales-product-icon");

        Label categoryBadge = new Label(safeText(product.getCategoryName(), "Sin categoria"));
        categoryBadge.getStyleClass().add("sales-card-badge");

        HBox top = new HBox(8, iconWrap, FxSupport.spacer(), categoryBadge);
        top.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(safeText(product.getName(), "Producto"));
        name.getStyleClass().add("sales-card-name");
        name.setWrapText(true);

        Label supplier = new Label(safeText(product.getSupplierName(), "Sin proveedor"));
        supplier.getStyleClass().add("sales-card-meta");
        supplier.setWrapText(true);

        Label price = new Label(CurrencyUtils.format(product.getSalePrice()));
        price.getStyleClass().add("sales-card-price");

        Label stockBadge = new Label(product.getStock() <= 0 ? "Sin stock" : "Stock " + product.getStock());
        stockBadge.getStyleClass().add("sales-stock-badge");
        if (product.getStock() > 0) {
            stockBadge.getStyleClass().add("sales-stock-badge-ok");
        } else {
            stockBadge.getStyleClass().add("sales-stock-badge-out");
        }

        Button addButton = button(product.getStock() > 0 ? "Agregar" : "Sin stock",
                product.getStock() > 0 ? "button-primary" : "button-secondary",
                event -> {
                    addProductToCart(product);
                    event.consume();
                });
        addButton.getStyleClass().add("sales-add-button");
        addButton.setDisable(product.getStock() <= 0);

        HBox stockRow = new HBox(stockBadge);
        stockRow.setAlignment(Pos.CENTER_LEFT);
        stockRow.getStyleClass().add("sales-card-stock-row");

        VBox footer = new VBox(6, price, stockRow);
        footer.getStyleClass().add("sales-card-footer");

        card.getChildren().addAll(top, name, supplier, footer, addButton);

        if (product.getStock() > 0) {
            card.setOnMouseClicked(event -> addProductToCart(product));
        } else {
            card.getStyleClass().add("sales-product-card-disabled");
        }

        return card;
    }

    private void addProductToCart(Product product) {
        if (product == null || product.getStock() == null || product.getStock() <= 0) {
            FxSupport.showError("Ventas", "El producto no tiene stock disponible.");
            return;
        }
        try {
            SaleDetail existing = findDetail(product);
            if (existing != null) {
                int newQuantity = existing.getQuantity() + 1;
                if (newQuantity > product.getStock()) {
                    throw new IllegalArgumentException("La cantidad acumulada supera el stock disponible.");
                }
                existing.setQuantity(newQuantity);
            } else {
                SaleDetail detail = new SaleDetail();
                detail.setProduct(product);
                detail.setQuantity(1);
                detail.setUnitPrice(product.getSalePrice());
                cartRows.add(detail);
            }
            refreshCartView();
            refreshSummary();
        } catch (Exception exception) {
            FxSupport.showError("Ventas", exception.getMessage());
        }
    }

    private void refreshCartView() {
        if (cartRows.isEmpty()) {
            cartContent.getChildren().setAll(emptyCartState());
        } else {
            cartItemsBox.getChildren().clear();
            for (SaleDetail detail : cartRows) {
                cartItemsBox.getChildren().add(cartRow(detail));
            }
            cartContent.getChildren().setAll(cartItemsBox);
        }

        boolean empty = cartRows.isEmpty();
        clearButton.setDisable(empty);
        processButton.setDisable(empty);
    }

    private Node emptyCartState() {
        StackPane iconWrap = new StackPane(cartGlyph());
        iconWrap.getStyleClass().add("sales-empty-icon");

        Label title = new Label("Orden vacia");
        title.getStyleClass().add("sales-empty-title");

        Label subtitle = new Label("Agrega productos del catalogo para empezar una venta.");
        subtitle.getStyleClass().add("sales-empty-subtitle");
        subtitle.setWrapText(true);

        VBox box = new VBox(12, iconWrap, title, subtitle);
        box.getStyleClass().add("sales-empty-state");
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Node emptyCatalogState() {
        Label title = new Label("No hay productos para mostrar.");
        title.getStyleClass().add("sales-empty-title");
        Label subtitle = new Label("Prueba otra busqueda o cambia la categoria activa.");
        subtitle.getStyleClass().add("sales-empty-subtitle");

        VBox box = new VBox(8, title, subtitle);
        box.getStyleClass().add("sales-empty-state");
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Node cartRow(SaleDetail detail) {
        Label name = new Label(safeText(detail.getProduct().getName(), "Producto"));
        name.getStyleClass().add("sales-cart-name");
        name.setWrapText(true);
        HBox.setHgrow(name, Priority.ALWAYS);

        Label meta = new Label(detail.getQuantity() + " x " + CurrencyUtils.format(detail.getUnitPrice()));
        meta.getStyleClass().add("sales-cart-meta");

        Button minusButton = miniButton("-", event -> updateCartQuantity(detail, -1));
        Button plusButton = miniButton("+", event -> updateCartQuantity(detail, 1));
        Label quantityLabel = new Label(String.valueOf(detail.getQuantity()));
        quantityLabel.getStyleClass().add("sales-qty-value");
        HBox quantityBox = new HBox(6, minusButton, quantityLabel, plusButton);
        quantityBox.getStyleClass().add("sales-qty-box");
        quantityBox.setAlignment(Pos.CENTER);
        quantityBox.setMinWidth(110);
        quantityBox.setPrefWidth(110);

        Label lineTotal = new Label(CurrencyUtils.format(lineSubtotal(detail)));
        lineTotal.getStyleClass().add("sales-cart-total");

        Button removeButton = new Button("Quitar");
        removeButton.getStyleClass().addAll("button", "button-secondary", "sales-remove-button");
        removeButton.setOnAction(event -> removeDetail(detail));
        removeButton.setMinWidth(78);
        removeButton.setPrefWidth(78);

        HBox top = new HBox(10, name, FxSupport.spacer(), lineTotal);
        top.getStyleClass().add("sales-cart-top");
        top.setAlignment(Pos.CENTER_LEFT);

        HBox bottom = new HBox(10, meta, FxSupport.spacer(), quantityBox, removeButton);
        bottom.getStyleClass().add("sales-cart-bottom");
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox row = new VBox(10, top, bottom);
        row.getStyleClass().add("sales-cart-row");
        return row;
    }

    private void updateCartQuantity(SaleDetail detail, int delta) {
        Product product = detail.getProduct();
        int nextQuantity = detail.getQuantity() + delta;
        if (nextQuantity <= 0) {
            cartRows.remove(detail);
        } else {
            if (product.getStock() != null && nextQuantity > product.getStock()) {
                FxSupport.showError("Ventas", "La cantidad supera el stock disponible.");
                return;
            }
            detail.setQuantity(nextQuantity);
        }
        refreshCartView();
        refreshSummary();
    }

    private void removeDetail(SaleDetail detail) {
        cartRows.remove(detail);
        refreshCartView();
        refreshSummary();
    }

    private void processSale() {
        try {
            Sale sale = showCheckoutPopup();
            if (sale == null) {
                return;
            }
            Long saleId = saleService.createSale(sale);
            sale.setId(saleId);
            sale.setSaleDate(LocalDateTime.now());
            showTicket(sale);
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Ventas", exception.getMessage());
        }
    }

    private Sale prepareSalePreview() {
        Customer customer = customerBox.getValue();
        if (customer != null && customer.getId() == null) {
            customer = null;
        }
        return saleService.prepareSale(currentUser, customer, new ArrayList<>(cartRows), paymentBox.getValue(), observationArea.getText());
    }

    private Sale showCheckoutPopup() {
        Sale preview = prepareSalePreview();
        Stage modal = buildModalStage();
        Sale[] confirmedSale = new Sale[1];

        Label title = new Label("Confirmar venta");
        title.getStyleClass().add("sales-popup-title");

        Label subtitle = new Label("Revisa el cobro antes de registrar la venta y emitir el ticket.");
        subtitle.getStyleClass().add("sales-popup-subtitle");

        Button closeButton = new Button();
        closeButton.getStyleClass().add("sales-popup-close");
        closeButton.setGraphic(closeGlyph());
        closeButton.setOnAction(event -> modal.close());

        HBox titleRow = new HBox(12, title, FxSupport.spacer(), closeButton);
        titleRow.getStyleClass().add("sales-popup-head");
        titleRow.setAlignment(Pos.CENTER_LEFT);
        installWindowDrag(modal, titleRow);

        VBox form = new VBox(12,
                fieldBlock("Cliente", customerBox),
                fieldBlock("Metodo de pago", paymentBox),
                fieldBlock("Observacion", observationArea)
        );
        form.getStyleClass().add("sales-popup-form");

        VBox items = new VBox(6);
        items.getStyleClass().add("sales-popup-items");
        int visibleItems = Math.min(preview.getDetails().size(), 5);
        for (int index = 0; index < visibleItems; index++) {
            SaleDetail detail = preview.getDetails().get(index);
            items.getChildren().add(popupInfoRow(
                    detail.getProduct().getName() + " x" + detail.getQuantity(),
                    CurrencyUtils.format(detail.getLineTotal())));
        }
        if (preview.getDetails().size() > visibleItems) {
            Label more = new Label("+" + (preview.getDetails().size() - visibleItems) + " productos mas");
            more.getStyleClass().add("sales-popup-note");
            items.getChildren().add(more);
        }

        VBox totals = new VBox(8,
                popupInfoRow("Subtotal", CurrencyUtils.format(preview.getSubtotal())),
                popupInfoRow("Impuestos", CurrencyUtils.format(preview.getTax())),
                new Separator(),
                popupTotalRow(CurrencyUtils.format(preview.getTotal()))
        );
        totals.getStyleClass().add("sales-popup-totals");

        Button cancelButton = button("Cancelar", "button-secondary", event -> modal.close());
        Button confirmButton = button("Confirmar venta", "button-primary", event -> {
            try {
                confirmedSale[0] = prepareSalePreview();
                modal.close();
            } catch (Exception exception) {
                FxSupport.showError("Ventas", exception.getMessage());
            }
        });

        HBox actions = new HBox(10, cancelButton, confirmButton);
        actions.getStyleClass().add("sales-popup-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(14, titleRow, subtitle, form, items, totals, actions);
        card.getStyleClass().add("sales-popup-card");
        card.setMaxWidth(520);

        ScrollPane scroll = new ScrollPane(card);
        scroll.getStyleClass().add("sales-popup-scroll");
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setMaxWidth(560);
        FxSupport.enhanceScrollPane(scroll, 2.0);

        StackPane overlay = new StackPane(scroll);
        overlay.getStyleClass().add("sales-popup-overlay");
        overlay.setPadding(new Insets(24));
        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                modal.close();
            }
        });

        Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        double popupHeight = 700;
        if (owner != null) {
            popupHeight = Math.max(620, Math.min(owner.getHeight() - 32, 760));
        }

        Scene scene = new Scene(overlay, 620, popupHeight);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        FxSupport.applyTheme(scene);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE -> modal.close();
                case ENTER -> confirmButton.fire();
                default -> {
                }
            }
        });

        modal.setScene(scene);
        modal.centerOnScreen();
        modal.showAndWait();
        return confirmedSale[0];
    }

    private HBox popupInfoRow(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("sales-popup-row-label");

        Label value = new Label(valueText);
        value.getStyleClass().add("sales-popup-row-value");

        HBox row = new HBox(12, label, FxSupport.spacer(), value);
        row.getStyleClass().add("sales-popup-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox popupTotalRow(String totalText) {
        Label label = new Label("Total");
        label.getStyleClass().add("sales-popup-total-label");

        Label value = new Label(totalText);
        value.getStyleClass().add("sales-popup-total-value");

        HBox row = new HBox(12, label, FxSupport.spacer(), value);
        row.getStyleClass().add("sales-popup-total-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void showTicket(Sale sale) {
        Window owner = root.getScene() == null ? null : root.getScene().getWindow();
        SaleTicketWindow.show(owner, currentUser, sale);
    }

    private void clearCart() {
        cartRows.clear();
        observationArea.clear();
        refreshCartView();
        refreshSummary();
    }

    private void refreshSummary() {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (SaleDetail detail : cartRows) {
            BigDecimal lineSubtotal = lineSubtotal(detail);
            BigDecimal lineTax = lineSubtotal.multiply(AppConfig.TAX_RATE);
            subtotal = subtotal.add(lineSubtotal);
            tax = tax.add(lineTax);
            total = total.add(lineSubtotal.add(lineTax));
        }

        subtotalValue.setText(CurrencyUtils.format(subtotal));
        taxValue.setText(CurrencyUtils.format(tax));
        totalValue.setText(CurrencyUtils.format(total));
        processButton.setText("Cobrar " + CurrencyUtils.format(total));
        clearButton.setDisable(cartRows.isEmpty());
        processButton.setDisable(cartRows.isEmpty());
    }

    private HBox summaryRow(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("sales-summary-label");
        valueLabel.getStyleClass().add("sales-summary-value");

        HBox row = new HBox(10, label, FxSupport.spacer(), valueLabel);
        row.getStyleClass().add("sales-summary-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox totalRow() {
        Label label = new Label("Total");
        label.getStyleClass().add("sales-total-label");
        totalValue.getStyleClass().add("sales-total-value");
        HBox row = new HBox(10, label, FxSupport.spacer(), totalValue);
        row.getStyleClass().add("sales-total-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Button miniButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().add("sales-qty-button");
        Label glyph = new Label(text);
        glyph.getStyleClass().add("sales-qty-glyph");
        button.setText("");
        button.setGraphic(glyph);
        button.setFocusTraversable(false);
        button.setOnAction(action);
        return button;
    }

    private Button button(String text, String styleClass, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", styleClass);
        button.setOnAction(action);
        return button;
    }

    private SaleDetail findDetail(Product product) {
        for (SaleDetail detail : cartRows) {
            if (detail.getProduct().getId().equals(product.getId())) {
                return detail;
            }
        }
        return null;
    }

    private BigDecimal lineSubtotal(SaleDetail detail) {
        return detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Stage buildModalStage() {
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

    private Node searchGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M11 11L15 15M13.5 7.5A6 6 0 1 1 1.5 7.5A6 6 0 0 1 13.5 7.5Z");
        path.getStyleClass().add("sales-search-icon");
        return path;
    }

    private Node boxGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 1L14 4.5V11.5L8 15L2 11.5V4.5L8 1ZM4 5.4V10.3L7.2 12.2V7.3L4 5.4ZM8.8 12.2L12 10.3V5.4L8.8 7.3V12.2ZM8 2.8L5.1 4.4L8 6L10.9 4.4L8 2.8Z");
        path.getStyleClass().add("sales-card-icon");
        return path;
    }

    private Node cartGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M1 2H3L4.2 9H11.8L13.3 4H5.1M6 12.5A1 1 0 1 1 4 12.5A1 1 0 0 1 6 12.5ZM12 12.5A1 1 0 1 1 10 12.5A1 1 0 0 1 12 12.5Z");
        path.getStyleClass().add("sales-empty-cart-icon");
        return path;
    }

    private Node closeGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M6 6l12 12M18 6 6 18");
        path.getStyleClass().add("sales-popup-close-icon");
        return path;
    }

    private final class SalesComboCell<T> extends ListCell<T> {

        private final boolean popupCell;

        private SalesComboCell(boolean popupCell) {
            this.popupCell = popupCell;
            getStyleClass().add(popupCell ? "sales-select-popup-cell" : "sales-select-button-cell");
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            setText(formatSalesComboItem(item));
        }
    }

    private String formatSalesComboItem(Object item) {
        if (item == null) {
            return "";
        }
        if (item instanceof Customer customer) {
            return safeText(customer.getName(), "Sin cliente");
        }
        return String.valueOf(item);
    }
}
