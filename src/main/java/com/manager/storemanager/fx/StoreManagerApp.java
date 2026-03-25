package com.manager.storemanager.fx;

import com.manager.storemanager.model.User;
import com.manager.storemanager.service.AuthService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

public class StoreManagerApp extends Application {

    private final AuthService authService = new AuthService();
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("StoreManager");
        primaryStage.setScene(createLoginScene());
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(620);
        primaryStage.show();
    }

    private Scene createLoginScene() {
        HBox shell = new HBox();
        shell.getStyleClass().add("login-shell");

        // ═══════════════════════════════════════════════════════════════════
        //  LEFT — Branding panel with geometric planes
        // ═══════════════════════════════════════════════════════════════════
        StackPane brandLayer = new StackPane();
        brandLayer.getStyleClass().add("login-brand-panel");
        brandLayer.setPrefWidth(460);
        brandLayer.setMinWidth(460);

        // Geometric angular planes (triangles overlaid)
        Polygon plane1 = new Polygon(0, 0, 460, 0, 460, 350, 0, 220);
        plane1.setFill(Color.rgb(30, 70, 150, 0.18));

        Polygon plane2 = new Polygon(0, 180, 460, 100, 460, 500, 0, 660);
        plane2.setFill(Color.rgb(20, 50, 130, 0.12));

        Polygon plane3 = new Polygon(200, 0, 460, 0, 460, 280);
        plane3.setFill(Color.rgb(50, 100, 200, 0.10));

        // Brand content
        VBox brandContent = new VBox(12);
        brandContent.setAlignment(Pos.CENTER);
        brandContent.setPadding(new Insets(0, 40, 0, 40));
        brandContent.setMaxWidth(400);

        // Logo row
        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER);

        StackPane logoBadge = new StackPane();
        logoBadge.getStyleClass().add("logo-badge");
        logoBadge.setPrefSize(48, 48);
        logoBadge.setMaxSize(48, 48);
        Label logoLetter = new Label("S");
        logoLetter.getStyleClass().add("logo-letter");
        logoBadge.getChildren().add(logoLetter);

        Label appName = new Label("StoreManager");
        appName.getStyleClass().add("brand-title");

        logoRow.getChildren().addAll(logoBadge, appName);

        // Tagline
        Label tagline = new Label("Tu gesti\u00f3n integral en un solo lugar");
        tagline.getStyleClass().add("brand-tagline");

        // Feature icons row — using Unicode symbols
        HBox featuresRow = new HBox(44);
        featuresRow.setAlignment(Pos.CENTER);
        featuresRow.setPadding(new Insets(30, 0, 0, 0));

        featuresRow.getChildren().addAll(
                createFeatureItem("Ventas", "\uD83D\uDED2"),       // 🛒
                createFeatureItem("Inventario", "\uD83D\uDCE6"),   // 📦
                createFeatureItem("Reportes", "\uD83D\uDCCA")     // 📊
        );

        brandContent.getChildren().addAll(logoRow, tagline, featuresRow);
        brandLayer.getChildren().addAll(plane1, plane2, plane3, brandContent);

        // ═══════════════════════════════════════════════════════════════════
        //  RIGHT — Form side
        // ═══════════════════════════════════════════════════════════════════
        VBox rightSide = new VBox();
        rightSide.getStyleClass().add("login-right");
        HBox.setHgrow(rightSide, Priority.ALWAYS);

        // Form card centered
        StackPane formArea = new StackPane();
        formArea.setAlignment(Pos.CENTER);
        VBox.setVgrow(formArea, Priority.ALWAYS);

        VBox formCard = new VBox(8);
        formCard.getStyleClass().addAll("card", "login-card");
        formCard.setMaxWidth(420);
        formCard.setPadding(new Insets(44, 44, 38, 44));
        formCard.setAlignment(Pos.CENTER_LEFT);

        // Title
        Label loginTitle = new Label("Bienvenido");
        loginTitle.getStyleClass().add("login-title");
        loginTitle.setAlignment(Pos.CENTER);
        loginTitle.setMaxWidth(Double.MAX_VALUE);

        Label loginSubtitle = new Label("Ingresa tus credenciales para continuar");
        loginSubtitle.getStyleClass().add("login-subtitle");
        loginSubtitle.setAlignment(Pos.CENTER);
        loginSubtitle.setMaxWidth(Double.MAX_VALUE);

        // Username field with icon
        Label userLabel = new Label("Usuario");
        userLabel.getStyleClass().add("field-label");
        VBox.setMargin(userLabel, new Insets(14, 0, 0, 0));

        HBox userFieldBox = createIconField("\uD83D\uDC64", false); // 👤
        TextField usernameField = (TextField) userFieldBox.getChildren().get(1);
        usernameField.setPromptText("Ingresa tu usuario");

        // Password field with icon
        Label passLabel = new Label("Contrase\u00f1a");
        passLabel.getStyleClass().add("field-label");
        VBox.setMargin(passLabel, new Insets(6, 0, 0, 0));

        HBox passFieldBox = createIconField("\uD83D\uDD12", true); // 🔒
        PasswordField passwordField = (PasswordField) passFieldBox.getChildren().get(1);
        passwordField.setPromptText("Ingresa tu contrase\u00f1a");

        // Login button
        Button loginButton = new Button("Iniciar Sesi\u00f3n");
        loginButton.getStyleClass().addAll("button", "login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(loginButton, new Insets(12, 0, 0, 0));

        Runnable loginAction = () -> {
            try {
                User user = authService.login(usernameField.getText(), passwordField.getText().toCharArray());
                openMainScene(user);
            } catch (Exception exception) {
                FxSupport.showError("Inicio de sesion", exception.getMessage());
            }
        };

        loginButton.setOnAction(event -> loginAction.run());
        passwordField.setOnAction(event -> loginAction.run());

        formCard.getChildren().addAll(
                loginTitle, loginSubtitle,
                userLabel, userFieldBox,
                passLabel, passFieldBox,
                loginButton
        );

        formArea.getChildren().add(formCard);

        // Credentials footer
        VBox credFooter = new VBox(10);
        credFooter.setAlignment(Pos.CENTER);
        credFooter.getStyleClass().add("cred-footer");
        credFooter.setPadding(new Insets(16, 40, 28, 40));

        Region separator = new Region();
        separator.getStyleClass().add("cred-separator");
        separator.setMaxWidth(420);
        separator.setPrefHeight(1);

        Label credTitle = new Label("Credenciales de prueba");
        credTitle.getStyleClass().add("cred-title");

        HBox credRow = new HBox(50);
        credRow.setAlignment(Pos.CENTER);
        credRow.getChildren().addAll(
                createCredBlock("Admin", "admin / admin123"),
                createCredBlock("Cajero", "cajero / cajero123")
        );

        credFooter.getChildren().addAll(separator, credTitle, credRow);

        rightSide.getChildren().addAll(formArea, credFooter);

        shell.getChildren().addAll(brandLayer, rightSide);

        Scene scene = new Scene(shell, 1100, 660);
        FxSupport.applyLoginTheme(scene);
        return scene;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private HBox createIconField(String iconChar, boolean isPassword) {
        HBox box = new HBox();
        box.getStyleClass().add("icon-field-box");
        box.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(iconChar);
        icon.getStyleClass().add("field-icon");
        icon.setMinWidth(40);
        icon.setAlignment(Pos.CENTER);

        Region field;
        if (isPassword) {
            field = new PasswordField();
        } else {
            field = new TextField();
        }
        field.getStyleClass().add("login-inner-field");
        HBox.setHgrow(field, Priority.ALWAYS);

        box.getChildren().addAll(icon, field);
        return box;
    }

    private VBox createFeatureItem(String label, String iconChar) {
        VBox item = new VBox(8);
        item.setAlignment(Pos.CENTER);

        StackPane icon = new StackPane();
        icon.getStyleClass().add("feature-icon");
        icon.setPrefSize(56, 56);
        icon.setMaxSize(56, 56);

        Label iconLabel = new Label(iconChar);
        iconLabel.getStyleClass().add("feature-icon-text");
        icon.getChildren().add(iconLabel);

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("feature-label");

        item.getChildren().addAll(icon, textLabel);
        return item;
    }

    private VBox createCredBlock(String role, String creds) {
        VBox block = new VBox(3);
        block.setAlignment(Pos.CENTER);

        Label roleLabel = new Label(role);
        roleLabel.getStyleClass().add("cred-role");

        Label credsLabel = new Label(creds);
        credsLabel.getStyleClass().add("cred-detail");

        block.getChildren().addAll(roleLabel, credsLabel);
        return block;
    }

    private void openMainScene(User user) {
        MainShell mainShell = new MainShell(user);
        Scene mainScene = new Scene(mainShell, 1380, 820);
        FxSupport.applyTheme(mainScene);
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
    }
}
