package com.manager.storemanager.fx;

import com.manager.storemanager.fx.view.CustomerFxView;
import com.manager.storemanager.fx.view.DashboardFxView;
import com.manager.storemanager.fx.view.InventoryFxView;
import com.manager.storemanager.fx.view.ProductFxView;
import com.manager.storemanager.fx.view.ReportsFxView;
import com.manager.storemanager.fx.view.SalesFxView;
import com.manager.storemanager.fx.view.SupplierFxView;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.CustomerService;
import com.manager.storemanager.service.InventoryService;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.service.SaleService;
import com.manager.storemanager.service.SupplierService;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainShell extends BorderPane {

    private final Map<String, FxView> views = new LinkedHashMap<>();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final StackPane contentHost = new StackPane();

    public MainShell(User currentUser) {
        getStyleClass().add("app-root");
        buildViews(currentUser);
        setLeft(buildSidebar(currentUser));
        contentHost.getStyleClass().add("content-host");
        setCenter(contentHost);
        showView("Dashboard");
    }

    private void buildViews(User currentUser) {
        ProductService productService = new ProductService();
        CustomerService customerService = new CustomerService();
        SupplierService supplierService = new SupplierService();
        InventoryService inventoryService = new InventoryService();
        SaleService saleService = new SaleService();
        ReportService reportService = new ReportService();

        registerView(new DashboardFxView(reportService));
        registerView(new ProductFxView(productService));
        registerView(new SalesFxView(currentUser, productService, customerService, saleService));
        registerView(new InventoryFxView(currentUser, inventoryService));
        registerView(new CustomerFxView(customerService));
        registerView(new SupplierFxView(supplierService));
        registerView(new ReportsFxView(productService, saleService, reportService));
    }

    private void registerView(FxView view) {
        views.put(view.getName(), view);
    }

    private Node buildSidebar(User currentUser) {
        VBox sidebar = new VBox(14);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(248);
        sidebar.setPadding(new Insets(24));

        Label title = new Label("StoreManager");
        title.getStyleClass().add("sidebar-title");

        Label subtitle = new Label("JavaFX desktop");
        subtitle.getStyleClass().add("sidebar-subtitle");

        VBox userCard = new VBox(4);
        userCard.getStyleClass().add("sidebar-user-card");
        Label userName = new Label(currentUser.getFullName());
        userName.getStyleClass().add("sidebar-user-name");
        Label role = new Label(currentUser.getRole().getName().toUpperCase());
        role.getStyleClass().add("sidebar-user-role");
        userCard.getChildren().addAll(userName, role);

        VBox navBox = new VBox(10);
        navBox.setPadding(new Insets(14, 0, 0, 0));
        for (String key : views.keySet()) {
            Button button = new Button(key);
            button.getStyleClass().addAll("button", "nav-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setOnAction(event -> showView(key));
            navButtons.put(key, button);
            navBox.getChildren().add(button);
        }
        VBox.setVgrow(navBox, Priority.ALWAYS);

        Label footer = new Label("Ventas, stock y reportes");
        footer.getStyleClass().add("sidebar-footer");

        sidebar.getChildren().addAll(title, subtitle, userCard, navBox, footer);
        return sidebar;
    }

    private void showView(String name) {
        FxView view = views.get(name);
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
}
