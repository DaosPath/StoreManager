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
import javafx.scene.Group;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
        sidebar.setPrefWidth(228);
        sidebar.setPadding(new Insets(16, 12, 14, 12));

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
        button.setMinHeight(50);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphic(createNavGraphic(name));
        button.setOnAction(event -> showView(name));
        return button;
    }

    private Node createNavGraphic(String name) {
        Region accent = new Region();
        accent.getStyleClass().add("nav-accent");
        accent.setMinWidth(4);
        accent.setPrefWidth(4);
        accent.setMaxWidth(4);

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("nav-icon-box");
        iconBox.setMinSize(20, 20);
        iconBox.setPrefSize(20, 20);
        iconBox.getChildren().add(createNavIcon(name));

        Label text = new Label(name);
        text.getStyleClass().add("nav-text");

        HBox row = new HBox(10, accent, iconBox, text);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node createNavIcon(String name) {
        Node icon = buildNavIcon(name);
        icon.getStyleClass().add("nav-icon");
        return icon;
    }

    private Node buildNavIcon(String name) {
        return switch (name) {
            case "Dashboard" -> dashboardIcon();
            case "Productos" -> productsIcon();
            case "Ventas" -> salesIcon();
            case "Gestion de stock" -> stockIcon();
            case "Clientes" -> customersIcon();
            case "Proveedores" -> suppliersIcon();
            case "Reportes" -> reportsIcon();
            default -> defaultIcon();
        };
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

    private Node dashboardIcon() {
        Group group = new Group(
                roundedRect(3, 3, 7, 7),
                roundedRect(14, 3, 7, 7),
                roundedRect(14, 14, 7, 7),
                roundedRect(3, 14, 7, 7)
        );
        return navIconGroup(group);
    }

    private Node productsIcon() {
        Group group = new Group(
                path("m7.5 4.27 9 5.15"),
                path("M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"),
                path("m3.3 7 8.7 5 8.7-5"),
                path("M12 22V12")
        );
        return navIconGroup(group);
    }

    private Node salesIcon() {
        Group group = new Group(
                strokedCircle(8, 21, 1),
                strokedCircle(19, 21, 1),
                path("M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12")
        );
        return navIconGroup(group);
    }

    private Node stockIcon() {
        Group group = new Group(
                roundedRect(2, 3, 20, 5),
                path("M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8"),
                path("M10 12h4")
        );
        return navIconGroup(group);
    }

    private Node customersIcon() {
        Group group = new Group(
                path("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"),
                strokedCircle(9, 7, 4),
                path("M22 21v-2a4 4 0 0 0-3-3.87"),
                path("M16 3.13a4 4 0 0 1 0 7.75")
        );
        return navIconGroup(group);
    }

    private Node suppliersIcon() {
        Group group = new Group(
                path("M10 17h4V5H2v12h3"),
                path("M20 17h2v-3.34a4 4 0 0 0-1.17-2.83L19 9h-5v8h2"),
                strokedCircle(7.5, 17.5, 2.5),
                strokedCircle(17.5, 17.5, 2.5)
        );
        return navIconGroup(group);
    }

    private Node reportsIcon() {
        Group group = new Group(
                path("M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"),
                path("M14 2v4a2 2 0 0 0 2 2h4"),
                path("M10 9H8"),
                path("M16 13H8"),
                path("M16 17H8")
        );
        return navIconGroup(group);
    }

    private Node defaultIcon() {
        return navIconGroup(new Group(roundedRect(4, 4, 16, 16)));
    }

    private Group navIconGroup(Group group) {
        group.setScaleX(0.76);
        group.setScaleY(0.76);
        return group;
    }

    private Rectangle roundedRect(double x, double y, double width, double height) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect.setArcWidth(2);
        rect.setArcHeight(2);
        rect.getStyleClass().add("nav-icon-stroke");
        return rect;
    }

    private Circle strokedCircle(double centerX, double centerY, double radius) {
        Circle circle = new Circle(centerX, centerY, radius);
        circle.getStyleClass().add("nav-icon-stroke");
        return circle;
    }

    private SVGPath path(String content) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.getStyleClass().add("nav-icon-stroke");
        return path;
    }
}
