package com.manager.storemanager.fx.component.core;

import javafx.scene.control.Label;

public class AppBadge extends Label {

    public enum Variant {
        NEUTRAL,
        SUCCESS,
        WARNING,
        DANGER
    }

    public AppBadge() {
        this("", Variant.NEUTRAL);
    }

    public AppBadge(String text) {
        this(text, Variant.NEUTRAL);
    }

    public AppBadge(String text, Variant variant) {
        super(text);
        getStyleClass().add("app-badge");
        setVariant(variant);
    }

    public final void setVariant(Variant variant) {
        getStyleClass().removeAll(
                "app-badge-neutral",
                "app-badge-success",
                "app-badge-warning",
                "app-badge-danger"
        );
        String styleClass = switch (variant == null ? Variant.NEUTRAL : variant) {
            case SUCCESS -> "app-badge-success";
            case WARNING -> "app-badge-warning";
            case DANGER -> "app-badge-danger";
            case NEUTRAL -> "app-badge-neutral";
        };
        if (!getStyleClass().contains(styleClass)) {
            getStyleClass().add(styleClass);
        }
    }
}
