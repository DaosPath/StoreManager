package com.manager.storemanager.fx;

import com.manager.storemanager.fx.login.NativeLoginView;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.AuthService;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StoreManagerApp extends Application {

    private final AuthService authService = new AuthService();
    private Stage primaryStage;
    private NativeLoginView loginView;
    private boolean authenticating;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("StoreManager");
        primaryStage.setScene(createLoginScene());
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(680);
        primaryStage.show();
        loginView.initialize();
    }

    private Scene createLoginScene() {
        loginView = new NativeLoginView();
        loginView.setOnLoginRequested(this::authenticate);

        Scene scene = new Scene(loginView, 1180, 720);
        scene.getStylesheets().setAll(
                css("/css/base.css"),
                css("/css/login-native.css")
        );
        return scene;
    }

    private void authenticate(String username, String password) {
        if (authenticating) {
            return;
        }

        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password;

        if (normalizedUsername.isBlank() || normalizedPassword.isBlank()) {
            notifyLoginResult(false, "Completa usuario y contrasena.");
            return;
        }

        authenticating = true;
        loginView.setBusy(true);
        loginView.clearFeedback();

        Task<User> task = new Task<>() {
            @Override
            protected User call() throws Exception {
                return authService.login(normalizedUsername, normalizedPassword.toCharArray());
            }
        };

        task.setOnSucceeded(event -> {
            authenticating = false;
            try {
                loginView.persistRememberedCredentials();
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
        loginView.setBusy(false);
        loginView.showFeedback(success, message);
    }

    private String normalizeLoginError(Throwable exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "No se pudo iniciar sesion.";
        }
        return exception.getMessage();
    }

    private void openMainScene(User user) {
        MainShell mainShell = new MainShell(user);
        Scene mainScene = new Scene(mainShell, 1380, 820);
        FxSupport.applyTheme(mainScene);
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
    }

    private static String css(String path) {
        return StoreManagerApp.class.getResource(path).toExternalForm();
    }
}
