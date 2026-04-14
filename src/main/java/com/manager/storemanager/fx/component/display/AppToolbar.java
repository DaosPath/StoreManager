package com.manager.storemanager.fx.component.display;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class AppToolbar extends HBox {

    private static final String DISPLAY_CSS = "/css/components-display.css";

    private final HBox leading = new HBox(8);
    private final HBox center = new HBox(8);
    private final HBox trailing = new HBox(8);
    private final Region leadingSpacer = new Region();
    private final Region centerSpacer = new Region();

    public AppToolbar() {
        getStyleClass().add("app-toolbar");
        leading.getStyleClass().add("app-toolbar-leading");
        center.getStyleClass().add("app-toolbar-center");
        trailing.getStyleClass().add("app-toolbar-trailing");
        setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leadingSpacer, Priority.ALWAYS);
        HBox.setHgrow(centerSpacer, Priority.ALWAYS);
        getChildren().addAll(leading, leadingSpacer, center, centerSpacer, trailing);
        installDisplayStylesheet();
    }

    public AppToolbar(Node... trailingNodes) {
        this();
        setTrailing(trailingNodes);
    }

    public HBox leading() {
        return leading;
    }

    public HBox center() {
        return center;
    }

    public HBox trailing() {
        return trailing;
    }

    public final void setLeading(Node... nodes) {
        leading.getChildren().setAll(nodes == null ? new Node[0] : nodes);
    }

    public final void setCenter(Node... nodes) {
        center.getChildren().setAll(nodes == null ? new Node[0] : nodes);
    }

    public final void setTrailing(Node... nodes) {
        trailing.getChildren().setAll(nodes == null ? new Node[0] : nodes);
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
        return Objects.requireNonNull(AppToolbar.class.getResource(DISPLAY_CSS)).toExternalForm();
    }
}
