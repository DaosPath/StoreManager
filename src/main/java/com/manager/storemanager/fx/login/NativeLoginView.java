package com.manager.storemanager.fx.login;

import java.util.function.BiConsumer;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public final class NativeLoginView extends BorderPane {

    private static final Preferences PREFS = Preferences.userNodeForPackage(NativeLoginView.class);
    private static final String KEY_REMEMBER = "login.remember";
    private static final String KEY_USERNAME = "login.username";
    private static final String KEY_PASSWORD = "login.password";

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final CheckBox rememberCheck = new CheckBox("Recordarme");
    private final Label feedbackLabel = new Label();
    private final Button submitButton = new Button("Entrar al sistema");
    private final Button adminButton = new Button("Usar");
    private final Button cashierButton = new Button("Usar");

    private final HBox usernameShell = inputShell(usernameField, userIcon());
    private final HBox passwordShell = inputShell(passwordField, lockIcon());
    private BiConsumer<String, String> onLoginRequested;

    public NativeLoginView() {
        getStyleClass().add("login-shell");
        setLeft(buildRail());
        setCenter(buildMain());
        wireActions();
    }

    public void setOnLoginRequested(BiConsumer<String, String> handler) {
        this.onLoginRequested = handler;
    }

    public void initialize() {
        restoreRememberedCredentials();
        clearFeedback();
        syncFilledState();
        Platform.runLater(usernameField::requestFocus);
    }

    public void setBusy(boolean busy) {
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        rememberCheck.setDisable(busy);
        adminButton.setDisable(busy);
        cashierButton.setDisable(busy);
        submitButton.setDisable(busy);
        submitButton.setText(busy ? "Validando acceso..." : "Entrar al sistema");
    }

    public void showFeedback(boolean success, String message) {
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
        feedbackLabel.setText(message == null || message.isBlank()
                ? (success ? "Acceso concedido." : "No se pudo iniciar sesion.")
                : message);
        feedbackLabel.getStyleClass().removeAll("login-feedback-success", "login-feedback-error");
        feedbackLabel.getStyleClass().add(success ? "login-feedback-success" : "login-feedback-error");
    }

    public void clearFeedback() {
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
        feedbackLabel.setText("");
        feedbackLabel.getStyleClass().removeAll("login-feedback-success", "login-feedback-error");
    }

    public void persistRememberedCredentials() {
        try {
            if (!rememberCheck.isSelected()) {
                PREFS.remove(KEY_REMEMBER);
                PREFS.remove(KEY_USERNAME);
                PREFS.remove(KEY_PASSWORD);
                return;
            }
            PREFS.putBoolean(KEY_REMEMBER, true);
            PREFS.put(KEY_USERNAME, usernameField.getText().trim());
            PREFS.put(KEY_PASSWORD, passwordField.getText());
        } catch (RuntimeException ignored) {
            // Preferences may be unavailable in some locked-down environments.
        }
    }

    private Node buildRail() {
        VBox rail = new VBox(24);
        rail.getStyleClass().add("login-rail");
        rail.setPrefWidth(392);
        rail.setPadding(new Insets(36, 30, 30, 30));

        HBox brand = new HBox(14, brandMark(), brandCopy());
        brand.getStyleClass().add("login-brand");

        VBox panel = new VBox(18,
                featureRow("Ventas", "Carrito, cobro y ticket de salida.", cartIcon()),
                featureRow("Inventario", "Entradas de mercaderia y alertas de stock.", boxIcon()),
                featureRow("Reportes", "Consulta diaria y seguimiento comercial.", reportIcon())
        );
        panel.getStyleClass().add("login-rail-panel");

        VBox credentials = new VBox(12,
                credentialsTitle(),
                credentialRow("Admin", "admin / admin123", "admin", "admin123", adminButton),
                credentialRow("Cajero", "cajero / cajero123", "cajero", "cajero123", cashierButton)
        );
        credentials.getStyleClass().add("login-credentials");
        VBox.setVgrow(credentials, Priority.ALWAYS);

        rail.getChildren().addAll(brand, panel, credentials);
        return rail;
    }

    private Node buildMain() {
        StackPane main = new StackPane();
        main.getStyleClass().add("login-main");

        VBox card = new VBox(22);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(500);

        VBox header = new VBox(8);
        Label title = new Label("Iniciar sesion");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Ingresa tus credenciales para continuar.");
        subtitle.getStyleClass().add("login-subtitle");
        header.getChildren().addAll(title, subtitle);

        VBox form = new VBox(18);
        form.getChildren().addAll(
                labeledField("Usuario", usernameShell),
                labeledField("Contrasena", passwordShell),
                rememberRow(),
                feedbackLabel,
                submitRow()
        );

        card.getChildren().addAll(header, form, roleNote());
        main.getChildren().add(card);
        return main;
    }

    private Node labeledField(String label, Node shell) {
        VBox field = new VBox(8);
        Label caption = new Label(label);
        caption.getStyleClass().add("login-field-label");
        field.getChildren().addAll(caption, shell);
        return field;
    }

    private HBox rememberRow() {
        HBox row = new HBox(10, rememberCheck);
        row.getStyleClass().add("login-remember-row");
        return row;
    }

    private HBox submitRow() {
        submitButton.getStyleClass().add("login-submit");
        submitButton.setGraphic(submitIcon());
        submitButton.setContentDisplay(ContentDisplay.RIGHT);
        submitButton.setGraphicTextGap(10);
        submitButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(submitButton, Priority.ALWAYS);

        HBox row = new HBox(submitButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node roleNote() {
        HBox note = new HBox(12);
        note.getStyleClass().add("login-role-note");

        StackPane iconBox = new StackPane(roleIcon());
        iconBox.getStyleClass().add("login-role-icon");
        iconBox.setMinSize(36, 36);
        iconBox.setPrefSize(36, 36);

        VBox copy = new VBox(4);
        Label heading = new Label("Acceso por rol");
        heading.getStyleClass().add("login-role-title");
        Label body = new Label("Admin gestiona catalogo y reportes. Cajero opera ventas e inventario.");
        body.getStyleClass().add("login-role-copy");
        body.setWrapText(true);
        copy.getChildren().addAll(heading, body);

        note.getChildren().addAll(iconBox, copy);
        return note;
    }

    private StackPane brandMark() {
        StackPane mark = new StackPane();
        mark.getStyleClass().add("login-brand-mark");
        Label letter = new Label("S");
        letter.getStyleClass().add("login-brand-letter");
        mark.getChildren().add(letter);
        mark.setMinSize(52, 52);
        mark.setPrefSize(52, 52);
        return mark;
    }

    private VBox brandCopy() {
        VBox copy = new VBox(4);
        Label title = new Label("StoreManager");
        title.getStyleClass().add("login-brand-title");
        Label subtitle = new Label("Acceso para caja y administracion");
        subtitle.getStyleClass().add("login-brand-subtitle");
        copy.getChildren().addAll(title, subtitle);
        return copy;
    }

    private Node credentialsTitle() {
        Label label = new Label("Credenciales de prueba");
        label.getStyleClass().add("login-credentials-title");
        return label;
    }

    private HBox credentialRow(String title, String subtitle, String username, String password, Button button) {
        VBox copy = new VBox(2);
        Label name = new Label(title);
        name.getStyleClass().add("login-credential-name");
        Label text = new Label(subtitle);
        text.getStyleClass().add("login-credential-copy");
        copy.getChildren().addAll(name, text);

        button.getStyleClass().add("login-credential-button");
        button.setOnAction(event -> fillCredentials(username, password));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, copy, spacer, button);
        row.getStyleClass().add("login-credential-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox inputShell(TextField input, SVGPath icon) {
        StackPane iconBox = new StackPane(icon);
        iconBox.getStyleClass().add("login-input-icon");

        HBox shell = new HBox(12, iconBox, input);
        shell.getStyleClass().add("login-input-shell");
        HBox.setHgrow(input, Priority.ALWAYS);
        return shell;
    }

    private HBox featureRow(String title, String subtitle, SVGPath icon) {
        StackPane iconBox = new StackPane(icon);
        iconBox.getStyleClass().add("login-feature-icon");

        VBox copy = new VBox(4);
        Label name = new Label(title);
        name.getStyleClass().add("login-feature-title");
        Label text = new Label(subtitle);
        text.getStyleClass().add("login-feature-copy");
        text.setWrapText(true);
        copy.getChildren().addAll(name, text);

        HBox row = new HBox(14, iconBox, copy);
        row.getStyleClass().add("login-feature");
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private void wireActions() {
        submitButton.setOnAction(event -> submit());
        usernameField.textProperty().addListener((obs, oldValue, newValue) -> syncFilledState());
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> syncFilledState());
        usernameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateFocusState(usernameShell, isFocused));
        passwordField.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateFocusState(passwordShell, isFocused));
        rememberCheck.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                PREFS.remove(KEY_REMEMBER);
                PREFS.remove(KEY_USERNAME);
                PREFS.remove(KEY_PASSWORD);
            }
        });
        usernameField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> submit());
    }

    private void submit() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (username.isBlank() || password.isBlank()) {
            showFeedback(false, "Completa usuario y contrasena.");
            return;
        }
        clearFeedback();
        setBusy(true);
        if (onLoginRequested != null) {
            onLoginRequested.accept(username, password);
        }
    }

    private void fillCredentials(String username, String password) {
        usernameField.setText(username);
        passwordField.setText(password);
        clearFeedback();
        syncFilledState();
        Platform.runLater(() -> {
            passwordField.requestFocus();
            passwordField.selectAll();
        });
    }

    private void restoreRememberedCredentials() {
        try {
            boolean remember = PREFS.getBoolean(KEY_REMEMBER, false);
            rememberCheck.setSelected(remember);
            if (remember) {
                usernameField.setText(PREFS.get(KEY_USERNAME, ""));
                passwordField.setText(PREFS.get(KEY_PASSWORD, ""));
            }
        } catch (RuntimeException ignored) {
            rememberCheck.setSelected(false);
        }
    }

    private void syncFilledState() {
        usernameShell.getStyleClass().remove("is-filled");
        passwordShell.getStyleClass().remove("is-filled");
        if (!usernameField.getText().isBlank()) {
            usernameShell.getStyleClass().add("is-filled");
        }
        if (!passwordField.getText().isBlank()) {
            passwordShell.getStyleClass().add("is-filled");
        }
    }

    private void updateFocusState(HBox shell, boolean focused) {
        shell.getStyleClass().remove("is-focused");
        if (focused) {
            shell.getStyleClass().add("is-focused");
        }
    }

    private SVGPath userIcon() {
        return icon("M12 11a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-4.4 0-8 2.4-8 5.5V20h16v-1.5C20 15.4 16.4 13 12 13Z");
    }

    private SVGPath lockIcon() {
        return icon("M7 10V7a5 5 0 0 1 10 0v3M6 10h12v8H6z");
    }

    private SVGPath cartIcon() {
        return icon("M3 4h2l2.4 11.4A2 2 0 0 0 9.4 17h8.7a2 2 0 0 0 2-1.6L22 9H6");
    }

    private SVGPath boxIcon() {
        return icon("m7.5 4.3 9 5.1m-13.5 0 8.5 4.9 8.5-4.9M12 22V12M3 8v8a2 2 0 0 0 1 1.7l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.7l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8Z");
    }

    private SVGPath reportIcon() {
        return icon("M6 3h9l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Zm9 0v5h5M9 11h6M9 15h6M9 19h6");
    }

    private SVGPath roleIcon() {
        return icon("M12 3l7 3v5c0 4.2-2.6 8-7 10-4.4-2-7-5.8-7-10V6l7-3Zm-2 9 1.8 1.8 3.7-4.1");
    }

    private SVGPath submitIcon() {
        return icon("M5 12h12m-5-5 5 5-5 5");
    }

    private SVGPath icon(String content) {
        SVGPath svg = new SVGPath();
        svg.setContent(content);
        svg.getStyleClass().add("login-icon");
        return svg;
    }
}
