package com.manager.storemanager.fx.component.display;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AppSectionHeader extends HBox {

    private static final String DISPLAY_CSS = "/css/components-display.css";

    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final VBox textBox = new VBox(4, titleLabel, subtitleLabel);
    private final Region spacer = new Region();
    private final HBox actionBox = new HBox();

    public AppSectionHeader() {
        this(null, null);
    }

    public AppSectionHeader(String title, String subtitle) {
        getStyleClass().add("app-section-header");
        textBox.getStyleClass().add("app-section-header-copy");
        titleLabel.getStyleClass().add("app-section-header-title");
        subtitleLabel.getStyleClass().add("app-section-header-subtitle");
        actionBox.getStyleClass().add("app-section-header-action");
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(textBox, spacer, actionBox);
        setTitle(title);
        setSubtitle(subtitle);
        installDisplayStylesheet();
    }

    public final void setTitle(String title) {
        titleLabel.setText(title == null ? "" : title);
        titleLabel.setVisible(title != null && !title.isBlank());
        titleLabel.setManaged(title != null && !title.isBlank());
    }

    public final void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle == null ? "" : subtitle);
        subtitleLabel.setVisible(subtitle != null && !subtitle.isBlank());
        subtitleLabel.setManaged(subtitle != null && !subtitle.isBlank());
    }

    public final void setAction(Node action) {
        actionBox.getChildren().setAll(action == null ? new Node[0] : new Node[]{action});
        actionBox.setVisible(action != null);
        actionBox.setManaged(action != null);
    }

    public final VBox textBox() {
        return textBox;
    }

    public final HBox actionBox() {
        return actionBox;
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
        return Objects.requireNonNull(AppSectionHeader.class.getResource(DISPLAY_CSS)).toExternalForm();
    }
}
