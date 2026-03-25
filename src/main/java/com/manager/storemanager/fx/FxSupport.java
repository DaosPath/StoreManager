package com.manager.storemanager.fx;

import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class FxSupport {

    private FxSupport() {
    }

    private static final String BASE_CSS = css("/css/base.css");
    private static final String LOGIN_CSS = css("/css/login.css");
    private static final String MAIN_CSS = css("/css/main.css");

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().setAll(BASE_CSS, MAIN_CSS);
    }

    public static void applyLoginTheme(Scene scene) {
        scene.getStylesheets().setAll(BASE_CSS, LOGIN_CSS);
    }

    private static String css(String path) {
        return FxSupport.class.getResource(path).toExternalForm();
    }

    public static VBox pageHeader(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("page-subtitle");

        VBox header = new VBox(4, titleLabel, subtitleLabel);
        header.getStyleClass().add("page-header");
        return header;
    }

    public static HBox toolbar(Node... nodes) {
        HBox toolbar = new HBox(10);
        toolbar.getStyleClass().add("toolbar");
        toolbar.getChildren().addAll(nodes);
        return toolbar;
    }

    public static Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().addAll(
                FxSupport.class.getResource("/css/base.css").toExternalForm(),
                FxSupport.class.getResource("/css/main.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("app-root");
        dialogPane.setPadding(new Insets(16));
        if (dialogPane.lookupButton(ButtonType.YES) != null) {
            dialogPane.lookupButton(ButtonType.YES).getStyleClass().add("button-primary");
        }
        if (dialogPane.lookupButton(ButtonType.NO) != null) {
            dialogPane.lookupButton(ButtonType.NO).getStyleClass().add("button-secondary");
        }
        if (dialogPane.lookupButton(ButtonType.OK) != null) {
            dialogPane.lookupButton(ButtonType.OK).getStyleClass().add("button-primary");
        }
    }
}
