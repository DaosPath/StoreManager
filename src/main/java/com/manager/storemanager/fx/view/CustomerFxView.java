package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Customer;
import com.manager.storemanager.service.CustomerService;
import java.util.Locale;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
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
        FxSupport.enhanceScrollPane(cardsScroll, 2.1);
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
            TextField nameField = new TextField();
            TextField phoneField = new TextField();
            TextField documentField = new TextField();
            TextField addressField = new TextField();
            BooleanProperty activeState = new SimpleBooleanProperty(true);

            if (existing != null) {
                nameField.setText(existing.getName());
                phoneField.setText(existing.getPhone());
                documentField.setText(existing.getDocument());
                addressField.setText(existing.getAddress());
                activeState.set(existing.isActive());
            }

            nameField.getStyleClass().add("customer-dialog-input");
            phoneField.getStyleClass().add("customer-dialog-input");
            documentField.getStyleClass().add("customer-dialog-input");
            addressField.getStyleClass().add("customer-dialog-input");

            nameField.setPromptText("Nombre completo");
            phoneField.setPromptText("Telefono");
            documentField.setPromptText("Documento");
            addressField.setPromptText("Direccion");

            if (existing != null) {
                documentField.setEditable(false);
                documentField.setFocusTraversable(false);
            }

            VBox nameBox = dialogField("Nombre completo *", dialogInputShell(null, nameField), null);
            VBox phoneBox = dialogField("Telefono", dialogInputShell(phoneGlyph(), phoneField), null);
            VBox documentBox = dialogField(
                    "Documento (DNI/NIT)",
                    dialogInputShell(documentGlyph(), documentField),
                    existing == null ? null : "El documento principal no se puede cambiar."
            );
            VBox addressBox = dialogField("Direccion de facturacion", dialogInputShell(locationGlyph(), addressField), null);

            GridPane form = new GridPane();
            form.getStyleClass().add("customer-dialog-form");
            form.setHgap(16);
            form.setVgap(16);

            ColumnConstraints left = new ColumnConstraints();
            left.setPercentWidth(50);
            left.setHgrow(Priority.ALWAYS);
            ColumnConstraints right = new ColumnConstraints();
            right.setPercentWidth(50);
            right.setHgrow(Priority.ALWAYS);
            form.getColumnConstraints().addAll(left, right);

            form.add(nameBox, 0, 0, 2, 1);
            form.add(phoneBox, 0, 1);
            form.add(documentBox, 1, 1);
            form.add(addressBox, 0, 2, 2, 1);

            Stage modal = buildCustomerStage();
            Customer[] resultHolder = new Customer[1];

            StackPane avatar = new StackPane();
            avatar.getStyleClass().add("customer-dialog-avatar");
            Label initialsLabel = new Label(initials(existing == null ? nameField.getText() : existing.getName()));
            initialsLabel.getStyleClass().add("customer-dialog-avatar-text");
            avatar.getChildren().add(initialsLabel);

            nameField.textProperty().addListener((obs, oldValue, newValue) -> initialsLabel.setText(initials(newValue)));

            Label headName = new Label(existing == null ? "Nuevo cliente" : fallback(existing.getName(), "Cliente"));
            headName.getStyleClass().add("customer-dialog-head-name");
            nameField.textProperty().addListener((obs, oldValue, newValue) -> {
                String fallbackName = existing == null ? "Nuevo cliente" : "Cliente";
                String value = isBlank(newValue) ? fallbackName : newValue.trim();
                headName.setText(value);
            });

            Label headDocument = new Label(fallback(existing == null ? documentField.getText() : existing.getDocument(), "Documento pendiente"));
            headDocument.getStyleClass().add("customer-dialog-head-document");
            documentField.textProperty().addListener((obs, oldValue, newValue) -> {
                String value = fallback(newValue, "Documento pendiente");
                headDocument.setText(value);
            });

            HBox headDocRow = new HBox(6, headerDocumentGlyph(), headDocument);
            headDocRow.getStyleClass().add("customer-dialog-head-doc-row");
            headDocRow.setAlignment(Pos.CENTER_LEFT);

            VBox identity = new VBox(4, headName, headDocRow);
            identity.getStyleClass().add("customer-dialog-identity");
            HBox.setHgrow(identity, Priority.ALWAYS);

            Button closeButton = new Button();
            closeButton.getStyleClass().add("customer-dialog-close");
            closeButton.setGraphic(closeGlyph());
            closeButton.setOnAction(event -> modal.close());

            HBox head = new HBox(14, avatar, identity, FxSupport.spacer(), closeButton);
            head.getStyleClass().add("customer-dialog-head");
            head.setAlignment(Pos.CENTER_LEFT);
            installWindowDrag(modal, head);

            Region dividerTop = dialogDivider();

            Label sectionLabel = new Label("CONFIGURACION DE CUENTA");
            sectionLabel.getStyleClass().add("customer-dialog-section-title");

            Label stateTitle = new Label("Estado activo");
            stateTitle.getStyleClass().add("customer-dialog-state-title");
            Label stateHelp = new Label("Permite a este cliente realizar nuevas compras y generar facturas.");
            stateHelp.getStyleClass().add("customer-dialog-state-help");
            VBox stateText = new VBox(4, stateTitle, stateHelp);

            HBox stateRow = new HBox(12, stateText, FxSupport.spacer(), stateSwitchNode(activeState));
            stateRow.getStyleClass().add("customer-dialog-state-card");
            stateRow.setAlignment(Pos.CENTER_LEFT);

            Button cancelButton = new Button("Cancelar");
            cancelButton.getStyleClass().addAll("button", "button-secondary", "customer-dialog-cancel");
            cancelButton.setCancelButton(true);
            cancelButton.setOnAction(event -> modal.close());

            Button saveButton = new Button(existing == null ? "Guardar cliente" : "Guardar cambios");
            saveButton.getStyleClass().addAll("button", "button-primary", "customer-dialog-save");
            saveButton.setDefaultButton(true);
            saveButton.setGraphic(saveGlyph());
            saveButton.setOnAction(event -> {
                try {
                    Customer customer = existing == null ? new Customer() : existing;
                    customer.setName(nameField.getText().trim());
                    customer.setPhone(phoneField.getText().trim());
                    customer.setDocument(documentField.getText().trim());
                    customer.setAddress(addressField.getText().trim());
                    customer.setActive(activeState.get());
                    if (existing == null) {
                        customerService.save(customer);
                    } else {
                        customerService.update(customer);
                    }
                    resultHolder[0] = customer;
                    modal.close();
                } catch (Exception exception) {
                    FxSupport.showError("Clientes", exception.getMessage());
                }
            });

            HBox actions = new HBox(12, cancelButton, saveButton);
            actions.getStyleClass().add("customer-dialog-actions");
            actions.setAlignment(Pos.CENTER_RIGHT);

            VBox body = new VBox(18, form, dialogDivider(), sectionLabel, stateRow);
            body.getStyleClass().add("customer-dialog-body");

            VBox card = new VBox(0, head, dividerTop, body, dialogDivider(), actions);
            card.getStyleClass().add("customer-dialog-card");
            card.setMaxWidth(540);
            card.setPrefWidth(540);

            StackPane overlay = new StackPane(card);
            overlay.getStyleClass().add("customer-dialog-overlay");
            overlay.setPadding(new Insets(18));
            overlay.setOnMouseClicked(event -> {
                if (event.getTarget() == overlay) {
                    modal.close();
                }
            });

            StackPane shell = new StackPane(overlay);
            shell.getStyleClass().add("customer-dialog-shell");

            Scene scene = new Scene(shell);
            scene.setFill(Color.TRANSPARENT);
            FxSupport.applyTheme(scene);
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    modal.close();
                    event.consume();
                }
            });

            modal.setScene(scene);
            modal.showAndWait();

            Customer customer = resultHolder[0];
            if (customer == null) {
                return;
            }
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Clientes", exception.getMessage());
        }
    }

    private VBox dialogField(String labelText, Node field, String noteText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("customer-dialog-label");
        VBox box = new VBox(8, label, field);
        box.getStyleClass().add("customer-dialog-field");
        if (noteText != null) {
            HBox note = new HBox(6, noteGlyph(), noteLabel(noteText));
            note.getStyleClass().add("customer-dialog-note-row");
            note.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(note);
        }
        return box;
    }

    private Node dialogInputShell(Node icon, TextField field) {
        if (icon == null) {
            return field;
        }
        StackPane iconWrap = new StackPane(icon);
        iconWrap.getStyleClass().add("customer-dialog-input-icon-wrap");
        HBox shell = new HBox(10, iconWrap, field);
        shell.getStyleClass().add("customer-dialog-input-shell");
        shell.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return shell;
    }

    private Label noteLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("customer-dialog-note");
        return label;
    }

    private Node stateSwitchNode(BooleanProperty activeState) {
        Region track = new Region();
        track.getStyleClass().add("customer-dialog-switch-track");
        Region thumb = new Region();
        thumb.getStyleClass().add("customer-dialog-switch-thumb");
        StackPane toggle = new StackPane(track, thumb);
        toggle.getStyleClass().add("customer-dialog-switch");

        Label label = new Label();
        label.getStyleClass().add("customer-dialog-switch-label");

        Runnable syncState = () -> {
            boolean active = activeState.get();
            track.getStyleClass().remove("customer-dialog-switch-track-active");
            label.getStyleClass().remove("customer-dialog-switch-label-active");
            thumb.setTranslateX(active ? 10 : -10);
            label.setText(active ? "Activo" : "Inactivo");
            if (active) {
                track.getStyleClass().add("customer-dialog-switch-track-active");
                label.getStyleClass().add("customer-dialog-switch-label-active");
            }
        };
        syncState.run();
        activeState.addListener((obs, oldValue, newValue) -> syncState.run());

        Runnable toggleState = () -> activeState.set(!activeState.get());
        toggle.setOnMouseClicked(event -> toggleState.run());
        label.setOnMouseClicked(event -> toggleState.run());

        HBox box = new HBox(10, toggle, label);
        box.getStyleClass().add("customer-dialog-switch-wrap");
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Region dialogDivider() {
        Region divider = new Region();
        divider.getStyleClass().add("customer-dialog-divider");
        return divider;
    }

    private Stage buildCustomerStage() {
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

    private SVGPath closeGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 4 12 12M12 4 4 12");
        path.getStyleClass().add("customer-dialog-close-icon");
        return path;
    }

    private SVGPath saveGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M4 8.5 7 11.5 14 4.5");
        path.getStyleClass().add("customer-dialog-save-icon");
        return path;
    }

    private SVGPath noteGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 1.8a6.2 6.2 0 1 1 0 12.4A6.2 6.2 0 0 1 8 1.8zm0 3.1a.9.9 0 1 0 0 1.8a.9.9 0 0 0 0-1.8zm-.8 3.3h1.6v3.6H7.2V8.2z");
        path.getStyleClass().add("customer-dialog-note-icon");
        return path;
    }

    private SVGPath headerDocumentGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M5 2h6l3 3v8.5a1.5 1.5 0 0 1-1.5 1.5h-7A1.5 1.5 0 0 1 4 13.5v-10A1.5 1.5 0 0 1 5.5 2Zm5 .9V5.4h2.5L10 2.9ZM6.2 8.7h5.6v1H6.2v-1Z");
        path.getStyleClass().add("customer-dialog-head-doc-icon");
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
