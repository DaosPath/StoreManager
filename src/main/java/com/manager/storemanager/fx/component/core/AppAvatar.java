package com.manager.storemanager.fx.component.core;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AppAvatar extends StackPane {

    private final Label label = new Label();
    private double size = 36;

    public AppAvatar() {
        this("");
    }

    public AppAvatar(String initials) {
        this(initials, 36);
    }

    public AppAvatar(String initials, double size) {
        getStyleClass().add("app-avatar");
        label.getStyleClass().add("app-avatar-label");
        label.setAlignment(Pos.CENTER);
        setSize(size);
        setText(initials);
        getChildren().setAll(label);
        setAlignment(Pos.CENTER);
    }

    public AppAvatar(Node graphic, double size) {
        getStyleClass().add("app-avatar");
        setSize(size);
        if (graphic == null) {
            getChildren().setAll(label);
        } else {
            getChildren().setAll(graphic);
        }
        setAlignment(Pos.CENTER);
    }

    public void setText(String text) {
        if (!getChildren().contains(label)) {
            getChildren().setAll(label);
        }
        label.setText(text == null ? "" : text.trim());
    }

    public void setGraphic(Node graphic) {
        if (graphic == null) {
            getChildren().setAll(label);
            return;
        }
        getChildren().setAll(graphic);
    }

    public final void setSize(double size) {
        this.size = Math.max(24, size);
        setMinSize(this.size, this.size);
        setPrefSize(this.size, this.size);
        setMaxSize(this.size, this.size);
    }

    public double getSize() {
        return size;
    }
}
