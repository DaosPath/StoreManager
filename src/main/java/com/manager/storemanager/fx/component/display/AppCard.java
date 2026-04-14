package com.manager.storemanager.fx.component.display;

import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

public class AppCard extends VBox {

    private static final String DISPLAY_CSS = "/css/components-display.css";

    public AppCard() {
        this(0.0);
    }

    public AppCard(Node... children) {
        this(0.0, children);
    }

    public AppCard(double spacing, Node... children) {
        super(spacing);
        getStyleClass().add("app-card");
        setFillWidth(true);
        if (children != null && children.length > 0) {
            getChildren().addAll(children);
        }
        installDisplayStylesheet();
    }

    public void setContent(Node... children) {
        getChildren().setAll(children);
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
        return Objects.requireNonNull(AppCard.class.getResource(DISPLAY_CSS)).toExternalForm();
    }
}
