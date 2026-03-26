package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Customer;
import com.manager.storemanager.service.CustomerService;
import java.util.Locale;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class CustomerFxView implements FxView {

    private final CustomerService customerService;
    private final VBox root = new VBox(18);
    private final ObservableList<Customer> rows = FXCollections.observableArrayList();
    private final ObjectProperty<Customer> selectedCustomer = new SimpleObjectProperty<>();
    private final TextField searchField = new TextField();
    private final Label resultsLabel = new Label("Mostrando 0 de 0 clientes");
    private final Button editButton = buildToolbarButton("Editar", editGlyph(), "button-secondary");
    private final Button addButton = buildToolbarButton("Nuevo", plusGlyph(), "button-primary");
    private final Button previousButton = buildFooterButton("Anterior");
    private final Button nextButton = buildFooterButton("Siguiente");
    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(220));
    private final FlowPane cardsPane = new FlowPane();
    private final ScrollPane cardsScroll = new ScrollPane(cardsPane);

    public CustomerFxView(CustomerService customerService) {
        this.customerService = customerService;

        root.getStyleClass().addAll("module-root", "customer-module");
        configureSearch();
        configureActions();
        configureCardsPane();
        root.getChildren().addAll(buildHeader(), buildPane());
    }

    @Override
    public String getName() {
        return "Clientes";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            Long selectedId = selectedCustomer.get() == null ? null : selectedCustomer.get().getId();
            rows.setAll(customerService.findCustomers(searchField.getText()));
            selectedCustomer.set(findCustomerById(selectedId));
            renderCards();
            resultsLabel.setText("Mostrando " + rows.size() + " de " + rows.size() + " clientes");
            previousButton.setDisable(true);
            nextButton.setDisable(true);
        } catch (Exception exception) {
            FxSupport.showError("Clientes", exception.getMessage());
        }
    }

    private void configureSearch() {
        searchField.setPromptText("Buscar por nombre, telefono...");
        searchField.getStyleClass().add("customer-search-field");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> searchDelay.playFromStart());
        searchField.setOnAction(event -> refresh());
        searchDelay.setOnFinished(event -> refresh());
    }

    private void configureActions() {
        editButton.setDisable(true);
        editButton.setOnAction(event -> openDialog(selectedCustomer.get()));
        addButton.setOnAction(event -> openDialog(null));
        selectedCustomer.addListener((obs, oldValue, newValue) -> {
            editButton.setDisable(newValue == null);
            renderCards();
        });
    }

    private void configureCardsPane() {
        cardsPane.getStyleClass().add("customer-cards");
        cardsPane.setHgap(16);
        cardsPane.setVgap(16);
        cardsPane.setPrefWrapLength(760);

        cardsScroll.getStyleClass().add("customer-card-scroll");
        cardsScroll.setFitToWidth(true);
        cardsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cardsScroll.setContent(cardsPane);
        VBox.setVgrow(cardsScroll, Priority.ALWAYS);
        cardsScroll.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> cardsPane.setPrefWrapLength(Math.max(320, newBounds.getWidth() - 8)));
    }

    private Node buildHeader() {
        HBox header = new HBox(14);
        header.getStyleClass().add("customer-header");

        HBox searchShell = new HBox(10, searchGlyphWrap(), searchField);
        searchShell.getStyleClass().add("customer-search-shell");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox.setHgrow(searchShell, Priority.ALWAYS);

        HBox actions = new HBox(10, searchShell, editButton, addButton);
        actions.getStyleClass().add("customer-toolbar-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(430);
        HBox.setHgrow(actions, Priority.ALWAYS);

        header.getChildren().addAll(FxSupport.pageHeader("Clientes", "Gestion de clientes y datos de facturacion."), FxSupport.spacer(), actions);
        return header;
    }

    private Node buildPane() {
        VBox shell = new VBox(0);
        shell.getStyleClass().addAll("surface-pane", "customer-board-shell");
        VBox.setVgrow(shell, Priority.ALWAYS);
        shell.getChildren().addAll(cardsScroll, buildFooter());
        return shell;
    }

    private Node buildFooter() {
        HBox footer = new HBox(12, resultsLabel, FxSupport.spacer(), previousButton, nextButton);
        footer.getStyleClass().add("customer-footer");
        footer.setAlignment(Pos.CENTER_LEFT);
        resultsLabel.getStyleClass().add("customer-results");
        return footer;
    }

    private void renderCards() {
        cardsPane.getChildren().clear();
        if (rows.isEmpty()) {
            cardsPane.getChildren().add(emptyState("No hay clientes para mostrar"));
            return;
        }
        for (Customer customer : rows) {
            cardsPane.getChildren().add(customerCard(customer));
        }
    }

    private Node customerCard(Customer customer) {
        VBox card = new VBox(14);
        card.getStyleClass().add("customer-card");
        if (isSelected(customer)) {
            card.getStyleClass().add("customer-card-active");
        }
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("customer-avatar");
        Label initials = new Label(initials(customer.getName()));
        initials.getStyleClass().add("customer-avatar-text");
        avatar.getChildren().add(initials);

        VBox identity = new VBox(6);
        Label name = new Label(fallback(customer.getName(), "-"));
        name.getStyleClass().add("customer-card-name");
        identity.getChildren().addAll(name, statusBadge(customer.isActive()));

        Button moreButton = new Button();
        moreButton.getStyleClass().addAll("button", "customer-card-menu");
        moreButton.setGraphic(ellipsisGlyph());
        moreButton.setOnAction(event -> {
            selectedCustomer.set(customer);
            openDialog(customer);
        });

        HBox head = new HBox(12, avatar, identity, FxSupport.spacer(), moreButton);
        head.getStyleClass().add("customer-card-head");
        head.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(identity, Priority.ALWAYS);

        VBox body = new VBox(12,
                infoRow(phoneGlyph(), fallback(customer.getPhone(), "No registrado"), isBlank(customer.getPhone())),
                infoRow(documentGlyph(), fallback(customer.getDocument(), "-"), false),
                infoRow(locationGlyph(), fallback(customer.getAddress(), "-"), false)
        );
        body.getStyleClass().add("customer-card-body");

        card.getChildren().addAll(head, body);
        card.setOnMouseClicked(event -> {
            selectedCustomer.set(customer);
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                openDialog(customer);
            }
        });
        return card;
    }

    private Node infoRow(Node icon, String value, boolean muted) {
        StackPane iconBox = new StackPane(icon);
        iconBox.getStyleClass().add("customer-info-icon-box");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll("customer-info-value");
        if (muted) {
            valueLabel.getStyleClass().add("customer-info-value-muted");
        }

        if (value.startsWith("CF-") || value.startsWith("CC") || value.equals("-")) {
            StackPane badge = new StackPane(valueLabel);
            badge.getStyleClass().add("customer-document-badge");
            if (value.equals("-")) {
                badge.getStyleClass().add("customer-document-badge-empty");
            }
            HBox row = new HBox(12, iconBox, badge);
            row.getStyleClass().add("customer-info-row");
            row.setAlignment(Pos.CENTER_LEFT);
            return row;
        }

        HBox row = new HBox(12, iconBox, valueLabel);
        row.getStyleClass().add("customer-info-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node statusBadge(boolean active) {
        Region dot = new Region();
        Label label = new Label(active ? "Activo" : "Inactivo");
        HBox badge = new HBox(8, dot, label);
        badge.getStyleClass().addAll("customer-status-badge", active ? "customer-status-badge-active" : "customer-status-badge-inactive");
        dot.getStyleClass().addAll("customer-status-dot", active ? "customer-status-dot-active" : "customer-status-dot-inactive");
        label.getStyleClass().addAll("customer-status-text", active ? "customer-status-text-active" : "customer-status-text-inactive");
        badge.setAlignment(Pos.CENTER_LEFT);
        return badge;
    }

    private Node emptyState(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("customer-empty-state");
        StackPane wrap = new StackPane(label);
        wrap.getStyleClass().add("customer-empty-card");
        wrap.setPadding(new Insets(36, 0, 36, 0));
        wrap.setMinHeight(220);
        wrap.setPrefWidth(300);
        return wrap;
    }

    private Node searchGlyphWrap() {
        StackPane wrap = new StackPane(searchGlyph());
        wrap.getStyleClass().add("customer-search-icon-wrap");
        return wrap;
    }

    private Button buildToolbarButton(String text, Node icon, String styleClass) {
        Button button = new Button(text, icon);
        button.getStyleClass().addAll("button", styleClass, "customer-toolbar-button");
        return button;
    }

    private Button buildFooterButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "button-secondary", "customer-footer-button");
        button.setDisable(true);
        return button;
    }

    private void openDialog(Customer existing) {
        try {
            Dialog<Customer> dialog = new Dialog<>();
            dialog.setTitle(existing == null ? "Nuevo cliente" : "Editar cliente");
            FxSupport.applyDialogTheme(dialog.getDialogPane());

            ButtonType saveType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

            TextField nameField = new TextField();
            TextField phoneField = new TextField();
            TextField documentField = new TextField();
            TextField addressField = new TextField();
            CheckBox activeBox = new CheckBox("Activo");
            activeBox.setSelected(true);

            if (existing != null) {
                nameField.setText(existing.getName());
                phoneField.setText(existing.getPhone());
                documentField.setText(existing.getDocument());
                addressField.setText(existing.getAddress());
                activeBox.setSelected(existing.isActive());
            }

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.setPadding(new Insets(14));
            addRow(form, 0, "Nombre", nameField);
            addRow(form, 1, "Telefono", phoneField);
            addRow(form, 2, "Documento", documentField);
            addRow(form, 3, "Direccion", addressField);
            addRow(form, 4, "Estado", activeBox);
            dialog.getDialogPane().setContent(form);

            dialog.setResultConverter(button -> {
                if (button != saveType) {
                    return null;
                }
                Customer customer = existing == null ? new Customer() : existing;
                customer.setName(nameField.getText().trim());
                customer.setPhone(phoneField.getText().trim());
                customer.setDocument(documentField.getText().trim());
                customer.setAddress(addressField.getText().trim());
                customer.setActive(activeBox.isSelected());
                return customer;
            });

            Customer customer = dialog.showAndWait().orElse(null);
            if (customer == null) {
                return;
            }
            if (customer.getId() == null) {
                customerService.save(customer);
            } else {
                customerService.update(customer);
            }
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Clientes", exception.getMessage());
        }
    }

    private void addRow(GridPane grid, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private Customer findCustomerById(Long customerId) {
        if (customerId == null) {
            return null;
        }
        for (Customer customer : rows) {
            if (customer != null && customerId.equals(customer.getId())) {
                return customer;
            }
        }
        return null;
    }

    private boolean isSelected(Customer customer) {
        return selectedCustomer.get() != null
                && selectedCustomer.get().getId() != null
                && selectedCustomer.get().getId().equals(customer.getId());
    }

    private String fallback(String value, String emptyText) {
        return isBlank(value) ? emptyText : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String initials(String name) {
        String raw = fallback(name, "CL").trim();
        String[] chunks = raw.split("\\s+");
        if (chunks.length == 1) {
            return raw.substring(0, Math.min(2, raw.length())).toUpperCase(Locale.ROOT);
        }
        String first = chunks[0].substring(0, 1);
        String second = chunks[1].substring(0, 1);
        return (first + second).toUpperCase(Locale.ROOT);
    }

    private SVGPath searchGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M10.5 10.5 15 15M12 6.8A5.2 5.2 0 1 1 1.6 6.8A5.2 5.2 0 0 1 12 6.8Z");
        path.getStyleClass().add("customer-search-icon");
        return path;
    }

    private SVGPath editGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3 12.8V15h2.2l7.1-7.1-2.2-2.2L3 12.8Zm10.6-6.1a.8.8 0 0 0 0-1.1l-1.2-1.2a.8.8 0 0 0-1.1 0l-.9.9 2.2 2.2.9-.8Z");
        path.getStyleClass().add("customer-button-icon");
        return path;
    }

    private SVGPath plusGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 2v12M2 8h12");
        path.getStyleClass().addAll("customer-button-icon", "customer-button-icon-light");
        return path;
    }

    private SVGPath ellipsisGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 3.5A1.2 1.2 0 1 1 8 1.1A1.2 1.2 0 0 1 8 3.5Zm0 5.7A1.2 1.2 0 1 1 8 6.8A1.2 1.2 0 0 1 8 9.2Zm0 5.7A1.2 1.2 0 1 1 8 12.5A1.2 1.2 0 0 1 8 14.9Z");
        path.getStyleClass().add("customer-row-action-icon");
        return path;
    }

    private SVGPath phoneGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M6.2 2.8c.3-.3.7-.4 1.1-.2l1.6.8c.4.2.6.7.5 1.1l-.4 1.8c-.1.3 0 .6.2.8l1.7 1.7c.2.2.5.3.8.2l1.8-.4c.4-.1.8.1 1.1.5l.8 1.6c.2.4.1.8-.2 1.1l-1.2 1.2c-.5.5-1.3.7-2 .5c-2-.6-3.9-1.8-5.5-3.4C4.5 9.3 3.4 7.4 2.8 5.4c-.2-.7 0-1.5.5-2l1.2-1.2Z");
        path.getStyleClass().add("customer-info-icon");
        return path;
    }

    private SVGPath documentGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M5 2h6l3 3v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Zm5 1.5V6h2.5L10 3.5ZM5.8 9h6.4v1.2H5.8V9Zm0 2.6h6.4v1.2H5.8v-1.2Z");
        path.getStyleClass().add("customer-info-icon");
        return path;
    }

    private SVGPath locationGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 1.8a4.7 4.7 0 0 1 4.7 4.7c0 3.3-4.1 7.8-4.3 8a.5.5 0 0 1-.8 0c-.2-.2-4.3-4.7-4.3-8A4.7 4.7 0 0 1 8 1.8Zm0 6.3a1.7 1.7 0 1 0 0-3.4a1.7 1.7 0 0 0 0 3.4Z");
        path.getStyleClass().add("customer-info-icon");
        return path;
    }
}
