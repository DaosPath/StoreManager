package com.manager.storemanager.fx.view;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.FxView;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.service.SupplierService;
import java.util.function.Predicate;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public class SupplierFxView implements FxView {

    private final SupplierService supplierService;
    private final VBox root = new VBox(18);
    private final ObservableList<Supplier> rows = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();
    private final Label resultsLabel = new Label("Mostrando 0 de 0 proveedores");
    private final Button filterButton = buildToolbarButton("Filtrar", filterGlyph(), "button-secondary");
    private final Button addButton = buildToolbarButton("Nuevo", plusGlyph(), "button-primary");
    private final Button previousButton = buildFooterButton("Anterior");
    private final Button nextButton = buildFooterButton("Siguiente");
    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(220));
    private final VBox cardsPane = new VBox(14);
    private final ScrollPane cardsScroll = new ScrollPane(cardsPane);
    private final HBox filterBar = new HBox(8);

    private String filterMode = "all";

    public SupplierFxView(SupplierService supplierService) {
        this.supplierService = supplierService;

        root.getStyleClass().addAll("module-root", "supplier-module");
        configureSearch();
        configureActions();
        configureCardsPane();
        configureFilterBar();
        root.getChildren().addAll(buildHeader(), buildPane());
    }

    @Override
    public String getName() {
        return "Proveedores";
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        try {
            rows.setAll(supplierService.findSuppliers(searchField.getText()).stream()
                    .filter(matchesFilter())
                    .toList());
            renderCards();
            resultsLabel.setText("Mostrando " + rows.size() + " de " + rows.size() + " proveedores");
            previousButton.setDisable(true);
            nextButton.setDisable(true);
        } catch (Exception exception) {
            FxSupport.showError("Proveedores", exception.getMessage());
        }
    }

    private void configureSearch() {
        searchField.setPromptText("Buscar por nombre, correo...");
        searchField.getStyleClass().add("supplier-search-field");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> searchDelay.playFromStart());
        searchField.setOnAction(event -> refresh());
        searchDelay.setOnFinished(event -> refresh());
    }

    private void configureActions() {
        addButton.setOnAction(event -> openDialog(null));
        filterButton.setOnAction(event -> {
            boolean visible = !filterBar.isVisible();
            filterBar.setVisible(visible);
            filterBar.setManaged(visible);
            filterButton.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("active"), visible);
        });
    }

    private void configureCardsPane() {
        cardsPane.getStyleClass().add("supplier-cards");
        cardsScroll.getStyleClass().add("supplier-card-scroll");
        cardsScroll.setFitToWidth(true);
        cardsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        cardsScroll.setContent(cardsPane);
        VBox.setVgrow(cardsScroll, Priority.ALWAYS);
    }

    private void configureFilterBar() {
        filterBar.getStyleClass().add("supplier-filter-bar");
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setVisible(false);
        filterBar.setManaged(false);
        filterBar.getChildren().addAll(
                buildFilterChip("Todos", "all"),
                buildFilterChip("Activos", "active"),
                buildFilterChip("Inactivos", "inactive")
        );
        updateFilterChips();
    }

    private Node buildHeader() {
        HBox header = new HBox(14);
        header.getStyleClass().add("supplier-header");

        HBox searchShell = new HBox(10, searchGlyphWrap(), searchField);
        searchShell.getStyleClass().add("supplier-search-shell");
        HBox.setHgrow(searchShell, Priority.ALWAYS);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        HBox actions = new HBox(10, searchShell, filterButton, addButton);
        actions.getStyleClass().add("supplier-toolbar-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(640);
        HBox.setHgrow(actions, Priority.ALWAYS);

        header.getChildren().addAll(
                FxSupport.pageHeader("Proveedores", "Gestion de proveedores y contactos de compra."),
                FxSupport.spacer(),
                actions
        );
        return header;
    }

    private Node buildPane() {
        VBox shell = new VBox(0);
        shell.getStyleClass().addAll("surface-pane", "supplier-board-shell");
        VBox.setVgrow(shell, Priority.ALWAYS);
        shell.getChildren().addAll(filterBar, cardsScroll, buildFooter());
        return shell;
    }

    private Node buildFooter() {
        HBox footer = new HBox(12, resultsLabel, FxSupport.spacer(), previousButton, nextButton);
        footer.getStyleClass().add("supplier-footer");
        footer.setAlignment(Pos.CENTER_LEFT);
        resultsLabel.getStyleClass().add("supplier-results");
        return footer;
    }

    private void renderCards() {
        cardsPane.getChildren().clear();
        if (rows.isEmpty()) {
            cardsPane.getChildren().add(emptyState("No hay proveedores para mostrar"));
            return;
        }
        for (Supplier supplier : rows) {
            cardsPane.getChildren().add(supplierCard(supplier));
        }
    }

    private Node supplierCard(Supplier supplier) {
        HBox card = new HBox(18);
        card.getStyleClass().add("supplier-card");
        card.setAlignment(Pos.CENTER_LEFT);

        SVGPath avatarIcon = buildingGlyph(supplier);
        StackPane avatar = new StackPane(avatarIcon);
        avatar.getStyleClass().addAll(
                "supplier-avatar",
                supplier.isActive() ? "supplier-avatar-active" : "supplier-avatar-inactive",
                supplierToneClass(supplier)
        );

        VBox identity = new VBox(8);
        identity.getStyleClass().add("supplier-identity");
        Label name = new Label(fallback(supplier.getName(), "-"));
        name.getStyleClass().add("supplier-card-name");
        identity.getChildren().addAll(name, statusBadge(supplier.isActive()));

        VBox contactColumn = new VBox(12,
                infoItem(mailGlyph(), fallback(supplier.getEmail(), "No registrado"), isBlank(supplier.getEmail())),
                infoItem(phoneGlyph(), fallback(supplier.getPhone(), "No registrado"), isBlank(supplier.getPhone()))
        );
        contactColumn.getStyleClass().add("supplier-card-column");

        VBox addressColumn = new VBox(infoItem(locationGlyph(), fallback(supplier.getAddress(), "-"), false));
        addressColumn.getStyleClass().addAll("supplier-card-column", "supplier-address-column");
        addressColumn.setAlignment(Pos.CENTER_LEFT);

        HBox info = new HBox(26, contactColumn, addressColumn);
        info.getStyleClass().add("supplier-card-info");
        HBox.setHgrow(info, Priority.ALWAYS);

        Button editButton = new Button();
        editButton.getStyleClass().addAll("button", "supplier-card-action");
        editButton.setGraphic(editGlyph());
        editButton.setOnAction(event -> {
            openDialog(supplier);
        });

        Button deactivateButton = new Button();
        deactivateButton.getStyleClass().addAll("button", "supplier-card-action");
        deactivateButton.setGraphic(trashGlyph());
        deactivateButton.setOnAction(event -> deactivateSupplier(supplier));

        HBox actions = new HBox(8, editButton, deactivateButton);
        actions.getStyleClass().add("supplier-card-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(avatar, identity, info, actions);
        HBox.setHgrow(identity, Priority.NEVER);
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                openDialog(supplier);
            }
        });
        return card;
    }

    private Node infoItem(Node icon, String value, boolean muted) {
        HBox row = new HBox(10);
        row.getStyleClass().add("supplier-info-item");
        row.setAlignment(Pos.CENTER_LEFT);
        StackPane iconWrap = new StackPane(icon);
        iconWrap.getStyleClass().add("supplier-info-icon-wrap");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("supplier-info-text");
        if (muted) {
            valueLabel.getStyleClass().add("supplier-info-text-muted");
        }
        row.getChildren().addAll(iconWrap, valueLabel);
        return row;
    }

    private Node statusBadge(boolean active) {
        Region dot = new Region();
        Label label = new Label(active ? "Activo" : "Inactivo");
        HBox badge = new HBox(8, dot, label);
        badge.getStyleClass().addAll("supplier-status-badge", active ? "supplier-status-badge-active" : "supplier-status-badge-inactive");
        dot.getStyleClass().addAll("supplier-status-dot", active ? "supplier-status-dot-active" : "supplier-status-dot-inactive");
        label.getStyleClass().addAll("supplier-status-text", active ? "supplier-status-text-active" : "supplier-status-text-inactive");
        badge.setAlignment(Pos.CENTER_LEFT);
        return badge;
    }

    private Node emptyState(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("supplier-empty-state");
        StackPane wrap = new StackPane(label);
        wrap.getStyleClass().add("supplier-empty-card");
        wrap.setPadding(new Insets(40, 0, 40, 0));
        wrap.setMinHeight(220);
        return wrap;
    }

    private Button buildFilterChip(String text, String mode) {
        Button chip = new Button(text);
        chip.getStyleClass().addAll("button", "button-secondary", "supplier-filter-chip");
        chip.setOnAction(event -> {
            filterMode = mode;
            updateFilterChips();
            refresh();
        });
        chip.setUserData(mode);
        return chip;
    }

    private void updateFilterChips() {
        for (Node node : filterBar.getChildren()) {
            if (!(node instanceof Button button)) {
                continue;
            }
            button.getStyleClass().remove("supplier-filter-chip-active");
            if (filterMode.equals(button.getUserData())) {
                button.getStyleClass().add("supplier-filter-chip-active");
            }
        }
    }

    private Node searchGlyphWrap() {
        StackPane wrap = new StackPane(searchGlyph());
        wrap.getStyleClass().add("supplier-search-icon-wrap");
        return wrap;
    }

    private Button buildToolbarButton(String text, Node icon, String styleClass) {
        Button button = new Button(text, icon);
        button.getStyleClass().addAll("button", styleClass, "supplier-toolbar-button");
        return button;
    }

    private Button buildFooterButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", "button-secondary", "supplier-footer-button");
        button.setDisable(true);
        return button;
    }

    private Predicate<Supplier> matchesFilter() {
        return switch (filterMode) {
            case "active" -> Supplier::isActive;
            case "inactive" -> supplier -> !supplier.isActive();
            default -> supplier -> true;
        };
    }

    private void openDialog(Supplier existing) {
        try {
            TextField nameField = new TextField();
            TextField phoneField = new TextField();
            TextField emailField = new TextField();
            TextField addressField = new TextField();
            BooleanProperty activeState = new SimpleBooleanProperty(true);

            if (existing != null) {
                nameField.setText(existing.getName());
                phoneField.setText(existing.getPhone());
                emailField.setText(existing.getEmail());
                addressField.setText(existing.getAddress());
                activeState.set(existing.isActive());
            }

            Stage modal = buildSupplierStage();
            Supplier[] resultHolder = new Supplier[1];

            nameField.getStyleClass().add("supplier-dialog-input");
            phoneField.getStyleClass().add("supplier-dialog-input");
            emailField.getStyleClass().add("supplier-dialog-input");
            addressField.getStyleClass().add("supplier-dialog-input");

            VBox nameBox = formField("Nombre", nameField);
            VBox phoneBox = formField("Telefono", phoneField);
            VBox emailBox = formField("Correo", emailField);
            VBox addressBox = formField("Direccion", addressField);

            GridPane form = new GridPane();
            form.getStyleClass().add("supplier-dialog-form");
            form.setHgap(18);
            form.setVgap(18);

            ColumnConstraints left = new ColumnConstraints();
            left.setPercentWidth(50);
            ColumnConstraints right = new ColumnConstraints();
            right.setPercentWidth(50);
            form.getColumnConstraints().addAll(left, right);

            form.add(nameBox, 0, 0, 2, 1);
            form.add(phoneBox, 0, 1);
            form.add(emailBox, 1, 1);
            form.add(addressBox, 0, 2, 2, 1);

            Button closeButton = new Button();
            closeButton.getStyleClass().add("supplier-dialog-close");
            closeButton.setGraphic(closeGlyph());
            closeButton.setOnAction(event -> modal.close());

            StackPane iconBubble = new StackPane(editDialogGlyph());
            iconBubble.getStyleClass().add("supplier-dialog-title-icon-wrap");

            Label titleLabel = new Label(existing == null ? "Nuevo proveedor" : "Editar proveedor");
            titleLabel.getStyleClass().add("supplier-dialog-title");

            HBox titleRow = new HBox(12, iconBubble, titleLabel, FxSupport.spacer(), closeButton);
            titleRow.getStyleClass().add("supplier-dialog-head");
            titleRow.setAlignment(Pos.CENTER_LEFT);
            installWindowDrag(modal, titleRow);

            Region divider = new Region();
            divider.getStyleClass().add("supplier-dialog-divider");

            Label stateLabel = new Label("Estado");
            stateLabel.getStyleClass().add("supplier-dialog-state-title");
            Label stateHelp = new Label("Activar o desactivar este proveedor.");
            stateHelp.getStyleClass().add("supplier-dialog-state-help");
            VBox stateText = new VBox(4, stateLabel, stateHelp);

            Node stateSwitch = stateSwitchNode(activeState);
            HBox stateRow = new HBox(12, stateText, FxSupport.spacer(), stateSwitch);
            stateRow.getStyleClass().add("supplier-dialog-state-row");
            stateRow.setAlignment(Pos.CENTER_LEFT);

            Button cancelButton = new Button("Cancelar");
            cancelButton.getStyleClass().addAll("button", "button-secondary", "supplier-dialog-cancel");
            cancelButton.setCancelButton(true);
            cancelButton.setOnAction(event -> modal.close());

            Button saveButton = new Button("Guardar");
            saveButton.getStyleClass().addAll("button", "button-primary", "supplier-dialog-save");
            saveButton.setDefaultButton(true);
            saveButton.setOnAction(event -> {
                try {
                    Supplier supplier = existing == null ? new Supplier() : existing;
                    supplier.setName(nameField.getText().trim());
                    supplier.setPhone(phoneField.getText().trim());
                    supplier.setEmail(emailField.getText().trim());
                    supplier.setAddress(addressField.getText().trim());
                    supplier.setActive(activeState.get());
                    if (existing == null) {
                        supplierService.save(supplier);
                    } else {
                        supplierService.update(supplier);
                    }
                    resultHolder[0] = supplier;
                    modal.close();
                } catch (IllegalArgumentException exception) {
                    FxSupport.showError("Proveedores", exception.getMessage());
                } catch (Exception exception) {
                    FxSupport.showError("Proveedores", exception.getMessage());
                }
            });

            HBox actions = new HBox(12, cancelButton, saveButton);
            actions.getStyleClass().add("supplier-dialog-actions");
            actions.setAlignment(Pos.CENTER_RIGHT);

            VBox card = new VBox(22, titleRow, divider, form, stateRow, dividerCopy(), actions);
            card.getStyleClass().add("supplier-dialog-card");
            card.setMaxWidth(540);
            card.setPrefWidth(540);

            StackPane overlay = new StackPane(card);
            overlay.getStyleClass().add("supplier-dialog-overlay");
            overlay.setPadding(new Insets(20));
            overlay.setOnMouseClicked(event -> {
                if (event.getTarget() == overlay) {
                    modal.close();
                }
            });

            StackPane shell = new StackPane(overlay);
            shell.getStyleClass().add("supplier-dialog-shell");

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

            Supplier supplier = resultHolder[0];
            if (supplier == null) {
                return;
            }
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Proveedores", exception.getMessage());
        }
    }

    private void deactivateSupplier(Supplier supplier) {
        if (supplier == null || supplier.getId() == null) {
            return;
        }
        boolean confirmed = showDeactivateDialog(supplier);
        if (!confirmed) {
            return;
        }
        try {
            supplierService.deactivate(supplier.getId());
            refresh();
        } catch (Exception exception) {
            FxSupport.showError("Proveedores", exception.getMessage());
        }
    }

    private boolean showDeactivateDialog(Supplier supplier) {
        Stage modal = buildSupplierStage();
        boolean[] confirmed = new boolean[1];

        StackPane iconWrap = new StackPane(alertGlyph());
        iconWrap.getStyleClass().add("supplier-confirm-icon-wrap");

        Label title = new Label("Desactivar proveedor");
        title.getStyleClass().add("supplier-confirm-title");

        Text leading = new Text("¿Estas seguro de que deseas desactivar\nal proveedor ");
        leading.getStyleClass().add("supplier-confirm-text");
        Text supplierName = new Text(fallback(supplier.getName(), "sin nombre"));
        supplierName.getStyleClass().add("supplier-confirm-text-strong");
        Text closing = new Text("?");
        closing.getStyleClass().add("supplier-confirm-text");
        TextFlow message = new TextFlow(leading, supplierName, closing);
        message.getStyleClass().add("supplier-confirm-message");

        Button cancelButton = new Button("No, cancelar");
        cancelButton.getStyleClass().addAll("button", "button-secondary", "supplier-confirm-cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> modal.close());

        Button confirmButton = new Button("Si, desactivar");
        confirmButton.getStyleClass().addAll("button", "supplier-confirm-danger");
        confirmButton.setDefaultButton(true);
        confirmButton.setOnAction(event -> {
            confirmed[0] = true;
            modal.close();
        });

        HBox actions = new HBox(10, cancelButton, confirmButton);
        actions.getStyleClass().add("supplier-confirm-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox head = new HBox(14, iconWrap, title);
        head.getStyleClass().add("supplier-confirm-head");
        head.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(18, head, message, actions);
        card.getStyleClass().add("supplier-confirm-card");
        card.setMaxWidth(420);
        card.setPrefWidth(420);

        StackPane overlay = new StackPane(card);
        overlay.getStyleClass().add("supplier-confirm-overlay");
        overlay.setPadding(new Insets(18));
        overlay.setOnMouseClicked(event -> {
            if (event.getTarget() == overlay) {
                modal.close();
            }
        });

        Scene scene = new Scene(new StackPane(overlay));
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
        return confirmed[0];
    }

    private VBox formField(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("supplier-dialog-label");
        field.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(8, label, field);
        box.getStyleClass().add("supplier-dialog-field");
        VBox.setVgrow(field, Priority.NEVER);
        return box;
    }

    private String fallback(String value, String emptyText) {
        return isBlank(value) ? emptyText : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private SVGPath searchGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M10.5 10.5 15 15M12 6.8A5.2 5.2 0 1 1 1.6 6.8A5.2 5.2 0 0 1 12 6.8Z");
        path.getStyleClass().add("supplier-search-icon");
        return path;
    }

    private SVGPath filterGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3 4h10l-4 4.7v3.4l-2 1V8.7L3 4Z");
        path.getStyleClass().add("supplier-toolbar-icon");
        return path;
    }

    private SVGPath plusGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 2v12M2 8h12");
        path.getStyleClass().addAll("supplier-toolbar-icon", "supplier-toolbar-icon-light");
        return path;
    }

    private SVGPath editGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3 12.8V15h2.2l7.1-7.1-2.2-2.2L3 12.8Zm10.6-6.1a.8.8 0 0 0 0-1.1l-1.2-1.2a.8.8 0 0 0-1.1 0l-.9.9 2.2 2.2.9-.8Z");
        path.getStyleClass().add("supplier-card-action-icon");
        return path;
    }

    private SVGPath trashGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M5 4h6l.6 1.2H14v1.2H2V5.2h2.4L5 4Zm-.9 3.1h1.3v6H4.1v-6Zm3.2 0h1.3v6H7.3v-6Zm3.2 0h1.3v6h-1.3v-6Z");
        path.getStyleClass().add("supplier-card-action-icon");
        return path;
    }

    private SVGPath buildingGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3 14V4.8h4.4V2h5.6v12H3Zm2-2h1.7v-1.7H5V12Zm0-3.4h1.7V6.9H5v1.7Zm3.4 3.4h1.7v-1.7H8.4V12Zm0-3.4h1.7V6.9H8.4v1.7Zm3.4 3.4h1.2V10h-1.2v2Z");
        path.getStyleClass().add("supplier-avatar-icon");
        return path;
    }

    private SVGPath buildingGlyph(Supplier supplier) {
        SVGPath path = new SVGPath();
        path.setContent("M3 14V4.8h4.4V2h5.6v12H3Zm2-2h1.7v-1.7H5V12Zm0-3.4h1.7V6.9H5v1.7Zm3.4 3.4h1.7v-1.7H8.4V12Zm0-3.4h1.7V6.9H8.4v1.7Zm3.4 3.4h1.2V10h-1.2v2Z");
        path.getStyleClass().addAll("supplier-avatar-icon", supplierToneIconClass(supplier));
        return path;
    }

    private SVGPath mailGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M2.5 4h11a1 1 0 0 1 1 1v6a1 1 0 0 1-1 1h-11a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1Zm.8 1.4 4.7 3.6 4.7-3.6H3.3Zm10.2 5.2V6.7l-5.2 4-5.2-4v3.9h10.4Z");
        path.getStyleClass().add("supplier-info-icon");
        return path;
    }

    private SVGPath phoneGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M6.2 2.8c.3-.3.7-.4 1.1-.2l1.6.8c.4.2.6.7.5 1.1l-.4 1.8c-.1.3 0 .6.2.8l1.7 1.7c.2.2.5.3.8.2l1.8-.4c.4-.1.8.1 1.1.5l.8 1.6c.2.4.1.8-.2 1.1l-1.2 1.2c-.5.5-1.3.7-2 .5c-2-.6-3.9-1.8-5.5-3.4C4.5 9.3 3.4 7.4 2.8 5.4c-.2-.7 0-1.5.5-2l1.2-1.2Z");
        path.getStyleClass().add("supplier-info-icon");
        return path;
    }

    private SVGPath locationGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 1.8a4.7 4.7 0 0 1 4.7 4.7c0 3.3-4.1 7.8-4.3 8a.5.5 0 0 1-.8 0c-.2-.2-4.3-4.7-4.3-8A4.7 4.7 0 0 1 8 1.8Zm0 6.3a1.7 1.7 0 1 0 0-3.4a1.7 1.7 0 0 0 0 3.4Z");
        path.getStyleClass().add("supplier-info-icon");
        return path;
    }

    private SVGPath closeGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3 3 13 13M13 3 3 13");
        path.getStyleClass().add("supplier-dialog-close-icon");
        return path;
    }

    private SVGPath editDialogGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M3 12.8V15h2.2l7.1-7.1-2.2-2.2L3 12.8Zm10.6-6.1a.8.8 0 0 0 0-1.1l-1.2-1.2a.8.8 0 0 0-1.1 0l-.9.9 2.2 2.2.9-.8Z");
        path.getStyleClass().add("supplier-dialog-title-icon");
        return path;
    }

    private SVGPath alertGlyph() {
        SVGPath path = new SVGPath();
        path.setContent("M8 1.5a6.5 6.5 0 1 1 0 13a6.5 6.5 0 0 1 0-13Zm0 3.1a.9.9 0 0 0-.9.9v3.5a.9.9 0 1 0 1.8 0V5.5a.9.9 0 0 0-.9-.9Zm0 6.2a1 1 0 1 0 0 2a1 1 0 0 0 0-2Z");
        path.getStyleClass().add("supplier-confirm-icon");
        return path;
    }

    private Node stateSwitchNode(BooleanProperty activeState) {
        Region track = new Region();
        track.getStyleClass().add("supplier-dialog-switch-track");
        Region thumb = new Region();
        thumb.getStyleClass().add("supplier-dialog-switch-thumb");
        StackPane toggle = new StackPane(track, thumb);
        toggle.getStyleClass().add("supplier-dialog-switch");

        Label label = new Label();
        label.getStyleClass().add("supplier-dialog-switch-label");

        Runnable syncState = () -> {
            boolean active = activeState.get();
            track.getStyleClass().remove("supplier-dialog-switch-track-active");
            label.getStyleClass().remove("supplier-dialog-switch-label-active");
            thumb.setTranslateX(active ? 10 : -10);
            label.setText(active ? "Activo" : "Inactivo");
            if (active) {
                track.getStyleClass().add("supplier-dialog-switch-track-active");
                label.getStyleClass().add("supplier-dialog-switch-label-active");
            }
        };
        syncState.run();
        activeState.addListener((obs, oldValue, newValue) -> syncState.run());

        Runnable toggleState = () -> activeState.set(!activeState.get());
        toggle.setOnMouseClicked(event -> toggleState.run());
        label.setOnMouseClicked(event -> toggleState.run());

        HBox box = new HBox(10, toggle, label);
        box.getStyleClass().add("supplier-dialog-switch-wrap");
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Region dividerCopy() {
        Region divider = new Region();
        divider.getStyleClass().add("supplier-dialog-divider");
        return divider;
    }

    private Stage buildSupplierStage() {
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

    private String supplierToneClass(Supplier supplier) {
        return toneIndex(supplier) == 0 ? "supplier-avatar-tone-blue" : "supplier-avatar-tone-amber";
    }

    private String supplierToneIconClass(Supplier supplier) {
        return toneIndex(supplier) == 0 ? "supplier-avatar-icon-blue" : "supplier-avatar-icon-amber";
    }

    private int toneIndex(Supplier supplier) {
        long seed = supplier.getId() == null ? Math.abs(fallback(supplier.getName(), "").hashCode()) : supplier.getId();
        return (int) (Math.abs(seed) % 2);
    }
}
