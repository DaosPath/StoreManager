package com.manager.storemanager.fx;

import com.manager.storemanager.fx.view.CustomerFxView;
import com.manager.storemanager.fx.view.DashboardFxView;
import com.manager.storemanager.fx.view.ProductFxView;
import com.manager.storemanager.fx.view.ReportsFxView;
import com.manager.storemanager.fx.view.SalesFxView;
import com.manager.storemanager.fx.view.StockManagementFxView;
import com.manager.storemanager.fx.view.SupplierFxView;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.CustomerService;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.service.SaleService;
import com.manager.storemanager.service.StockService;
import com.manager.storemanager.service.SupplierService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class MainShell extends BorderPane {

    private final Map<String, Supplier<FxView>> viewFactories = new LinkedHashMap<>();
    private final Map<String, FxView> views = new HashMap<>();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final StackPane contentHost = new StackPane();

    public MainShell(User currentUser) {
        getStyleClass().add("app-root");
        buildViews(currentUser);
        setLeft(buildSidebar(currentUser));
        contentHost.getStyleClass().add("content-host");
        setCenter(contentHost);
        Platform.runLater(() -> showView("Dashboard"));
    }

    private void buildViews(User currentUser) {
        ProductService productService = new ProductService();
        CustomerService customerService = new CustomerService();
        SupplierService supplierService = new SupplierService();
        StockService stockService = new StockService();
        SaleService saleService = new SaleService();
        ReportService reportService = new ReportService();

        registerView("Dashboard", () -> new DashboardFxView(reportService));
        registerView("Productos", () -> new ProductFxView(productService));
        registerView("Ventas", () -> new SalesFxView(currentUser, productService, customerService, saleService));
        registerView("Gestion de stock", () -> new StockManagementFxView(currentUser, stockService, productService, supplierService));
        registerView("Clientes", () -> new CustomerFxView(customerService));
        registerView("Proveedores", () -> new SupplierFxView(supplierService));
        registerView("Reportes", () -> new ReportsFxView(productService, saleService, reportService));
    }

    private void registerView(String name, Supplier<FxView> factory) {
        viewFactories.put(name, factory);
    }

    private Node buildSidebar(User currentUser) {
        VBox sidebar = new VBox(18);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(232);
        sidebar.setPadding(new Insets(16, 14, 14, 14));

        Label title = new Label("StoreManager");
        title.getStyleClass().add("sidebar-title");

        Label subtitle = new Label("Operacion local");
        subtitle.getStyleClass().add("sidebar-subtitle");

        VBox headerBox = new VBox(4, title, subtitle);

        VBox navBox = new VBox(8);
        navBox.setPadding(new Insets(14, 0, 0, 0));
        for (String key : viewFactories.keySet()) {
            Button button = createNavButton(key);
            navButtons.put(key, button);
            navBox.getChildren().add(button);
        }
        VBox.setVgrow(navBox, Priority.ALWAYS);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox userFooter = createUserFooter(currentUser);

        sidebar.getChildren().addAll(headerBox, navBox, spacer, userFooter);
        return sidebar;
    }

    private Button createNavButton(String name) {
        Button button = new Button();
        button.getStyleClass().addAll("button", "nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(52);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphic(createNavGraphic(name));
        button.setOnAction(event -> showView(name));
        return button;
    }

    private Node createNavGraphic(String name) {
        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("nav-icon-box");
        iconBox.setMinSize(20, 20);
        iconBox.setPrefSize(20, 20);
        iconBox.getChildren().add(createNavIcon(name));

        Label text = new Label(name);
        text.getStyleClass().add("nav-text");

        HBox row = new HBox(12, iconBox, text);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node createNavIcon(String name) {
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("nav-icon");
        icon.setContent(iconPath(name));
        return icon;
    }

    private HBox createUserFooter(User currentUser) {
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("sidebar-avatar");
        avatar.setMinSize(38, 38);
        avatar.setPrefSize(38, 38);

        Label avatarLetter = new Label(currentUser.getUsername().substring(0, 1).toUpperCase());
        avatarLetter.getStyleClass().add("sidebar-avatar-letter");
        avatar.getChildren().add(avatarLetter);

        Label userLabel = new Label(currentUser.getRole().getName().toUpperCase());
        userLabel.getStyleClass().add("sidebar-footer-user");

        Label chevron = new Label(">");
        chevron.getStyleClass().add("sidebar-chevron");

        HBox row = new HBox(12, avatar, userLabel, chevron);
        row.getStyleClass().add("sidebar-user-footer");
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(userLabel, Priority.ALWAYS);
        return row;
    }

    private void showView(String name) {
        FxView view = getOrCreateView(name);
        if (view == null) {
            return;
        }
        view.refresh();
        contentHost.getChildren().setAll(view.getContent());
        navButtons.forEach((key, button) -> {
            button.getStyleClass().remove("nav-button-active");
            if (key.equals(name)) {
                button.getStyleClass().add("nav-button-active");
            }
        });
    }

    private FxView getOrCreateView(String name) {
        FxView view = views.get(name);
        if (view != null) {
            return view;
        }

        Supplier<FxView> factory = viewFactories.get(name);
        if (factory == null) {
            return null;
        }

        try {
            view = factory.get();
            views.put(name, view);
            return view;
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            FxSupport.showError(name, "No se pudo cargar el modulo.");
            return null;
        }
    }

    private String iconPath(String name) {
        return switch (name) {
            case "Dashboard" -> "M4 4h6v6H4z M14 4h6v6h-6z M4 14h6v6H4z M14 14h6v6h-6z";
            case "Productos" -> "M12 2 4 6v12l8 4 8-4V6L12 2z M12 4.2 17.8 7 12 9.8 6.2 7 12 4.2z M6 9.2l5 2.6v7.5L6 16.8V9.2z M18 9.2v7.6l-5 2.5v-7.5L18 9.2z";
            case "Ventas" -> "M3 5h2l2.4 8.2c.2.7.8 1.3 1.7 1.3h7.9c.8 0 1.5-.5 1.7-1.3L21 8H8.1 M10 18a1.7 1.7 0 1 0 0 3.4A1.7 1.7 0 0 0 10 18zm8 0a1.7 1.7 0 1 0 0 3.4A1.7 1.7 0 0 0 18 18z";
            case "Gestion de stock" -> "M5 7h14l1 3v9H4v-9l1-3zm2 3v7h10v-7H7zm3-6h4l1 2H9l1-2z";
            case "Clientes" -> "M9 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zm7.5 1a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z M3.5 20a5.5 5.5 0 0 1 11 0v1h-11v-1zm11.7 1a4.3 4.3 0 0 1 4.3-4.1A4.3 4.3 0 0 1 23 21z";
            case "Proveedores" -> "M9 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zm8 0a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7z M2.5 21a6 6 0 0 1 13 0v1h-13v-1zm9.5 1a6 6 0 0 1 11 0";
            case "Reportes" -> "M5 4h10l4 4v12H5V4zm9 1.5V9h3.5L14 5.5z M8 13h8v1.8H8zm0 3.6h8v1.8H8zm0-7.2h5v1.8H8z";
            default -> "M4 4h16v16H4z";
        };
    }
}
