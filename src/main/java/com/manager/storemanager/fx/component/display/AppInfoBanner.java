package com.manager.storemanager.fx.component.display;

import com.manager.storemanager.fx.component.AppGlyphs;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class AppInfoBanner extends HBox {

    private static final String DISPLAY_CSS = "/css/components-display.css";
    private static final String INFO_ICON = "M12 22a10 10 0 1 0 0-20a10 10 0 0 0 0 20Zm0-14v2m0 4v4";
    private static final String SUCCESS_ICON = "M20 6 9 17l-5-5";
    private static final String WARNING_ICON = "M12 9v4m0 4h.01";
    private static final String DANGER_ICON = "M12 8v4m0 4h.01";

    public enum Tone {
        INFO,
        SUCCESS,
        WARNING,
        DANGER
    }

    private final StackPane iconBox = new StackPane();
    private final Label titleLabel = new Label();
    private final Label messageLabel = new Label();
    private final VBox copyBox = new VBox(4, titleLabel, messageLabel);
    private Tone tone = Tone.INFO;
    private boolean customIcon;

    public AppInfoBanner(String message) {
        this(null, null, message);
    }

    public AppInfoBanner(String title, String message) {
        this(null, title, message);
    }

    public AppInfoBanner(Node icon, String title, String message) {
        getStyleClass().add("app-info-banner");
        setAlignment(Pos.CENTER_LEFT);
        iconBox.getStyleClass().add("app-info-banner-icon");
        copyBox.getStyleClass().add("app-info-banner-copy");
        titleLabel.getStyleClass().add("app-info-banner-title");
        messageLabel.getStyleClass().add("app-info-banner-message");
        titleLabel.setWrapText(true);
        messageLabel.setWrapText(true);
        messageLabel.setText(message == null ? "" : message);
        setTitle(title);
        setIcon(icon);
        getChildren().addAll(iconBox, copyBox);
        setTone(Tone.INFO);
        installDisplayStylesheet();
    }

    public final void setTone(Tone tone) {
        this.tone = tone == null ? Tone.INFO : tone;
        getStyleClass().removeAll("app-info-banner-info", "app-info-banner-success", "app-info-banner-warning", "app-info-banner-danger");
        switch (this.tone) {
            case SUCCESS -> getStyleClass().add("app-info-banner-success");
            case WARNING -> getStyleClass().add("app-info-banner-warning");
            case DANGER -> getStyleClass().add("app-info-banner-danger");
            case INFO -> getStyleClass().add("app-info-banner-info");
        }
        if (!customIcon) {
            refreshDefaultIcon();
        }
    }

    public final void setTitle(String title) {
        titleLabel.setText(title == null ? "" : title);
        titleLabel.setVisible(title != null && !title.isBlank());
        titleLabel.setManaged(title != null && !title.isBlank());
    }

    public final void setMessage(String message) {
        messageLabel.setText(message == null ? "" : message);
    }

    public final void setIcon(Node icon) {
        customIcon = icon != null;
        Node graphic = icon == null ? defaultIcon() : icon;
        iconBox.getChildren().setAll(graphic);
    }

    public final Tone getTone() {
        return tone;
    }

    private void refreshDefaultIcon() {
        iconBox.getChildren().setAll(defaultIcon());
    }

    private Node defaultIcon() {
        SVGPath path = AppGlyphs.glyph(switch (tone) {
            case SUCCESS -> SUCCESS_ICON;
            case WARNING -> WARNING_ICON;
            case DANGER -> DANGER_ICON;
            case INFO -> INFO_ICON;
        }, "app-banner-glyph");
        return path;
    }

    protected final void installDisplayStylesheet() {
        sceneProperty().addListener((observable, oldScene, newScene) -> applyStylesheet(newScene));
        applyStylesheet(getScene());
    }

    private void applyStylesheet(Scene scene) {
        if (scene == null) {
            return;
        }
        String css = css();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    private static String css() {
        return Objects.requireNonNull(AppInfoBanner.class.getResource(DISPLAY_CSS)).toExternalForm();
    }
}
