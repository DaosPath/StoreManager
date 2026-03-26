package com.manager.storemanager.fx;

import com.manager.storemanager.model.User;
import com.manager.storemanager.service.AuthService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class StoreManagerApp extends Application {

    private static final String LOGIN_PROMPT = "__storemanager_login__";

    private final AuthService authService = new AuthService();
    private Stage primaryStage;
    private WebViewBridge loginView;
    private boolean authenticating;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("StoreManager");
        primaryStage.setScene(createLoginScene());
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(680);
        primaryStage.show();
    }

    private Scene createLoginScene() {
        loginView = new WebViewBridge("/web/login.html");
        loginView.getEngine().setPromptHandler(prompt -> {
            if (LOGIN_PROMPT.equals(prompt.getMessage())) {
                handleLoginPrompt(prompt.getDefaultValue());
            }
            return "";
        });
        loginView.whenReady(() -> loginView.execute("window.initializeLogin();"));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("login-scene");
        root.setCenter(loginView.getView());

        Scene scene = new Scene(root, 1180, 720);
        FxSupport.applyLoginTheme(scene);
        return scene;
    }

    private void handleLoginPrompt(String payload) {
        if (payload == null || payload.isBlank()) {
            notifyLoginResult(false, "No se pudo iniciar sesión.");
            return;
        }

        String[] parts = payload.split("\\|", 2);
        if (parts.length != 2) {
            notifyLoginResult(false, "No se pudo iniciar sesión.");
            return;
        }

        String username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
        String password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        authenticate(username, password);
    }

    private void authenticate(String username, String password) {
        if (authenticating) {
            return;
        }

        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password;

        if (normalizedUsername.isBlank() || normalizedPassword.isBlank()) {
            notifyLoginResult(false, "Completa usuario y contraseña.");
            return;
        }

        authenticating = true;
        Task<User> task = new Task<>() {
            @Override
            protected User call() throws Exception {
                return authService.login(normalizedUsername, normalizedPassword.toCharArray());
            }
        };

        task.setOnSucceeded(event -> {
            authenticating = false;
            try {
                openMainScene(task.getValue());
            } catch (RuntimeException exception) {
                exception.printStackTrace();
                notifyLoginResult(false, "No se pudo abrir el panel principal.");
            }
        });

        task.setOnFailed(event -> {
            authenticating = false;
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
            notifyLoginResult(false, normalizeLoginError(task.getException()));
        });

        Thread worker = new Thread(task, "storemanager-login");
        worker.setDaemon(true);
        worker.start();
    }

    private void notifyLoginResult(boolean success, String message) {
        if (loginView == null) {
            return;
        }
        loginView.execute("window.finishLogin(" + success + ", " + WebViewBridge.jsString(message) + ");");
    }

    private String normalizeLoginError(Throwable exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "No se pudo iniciar sesión.";
        }
        return exception.getMessage()
                .replace("contraseÃƒÂ±a", "contraseña")
                .replace("contraseÃ±a", "contraseña")
                .replace("invÃƒÂ¡lidos", "inválidos")
                .replace("invÃ¡lidos", "inválidos")
                .replace("sesiÃƒÂ³n", "sesión")
                .replace("sesiÃ³n", "sesión");
    }

    private void openMainScene(User user) {
        MainShell mainShell = new MainShell(user);
        Scene mainScene = new Scene(mainShell, 1380, 820);
        FxSupport.applyTheme(mainScene);
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
    }
}
