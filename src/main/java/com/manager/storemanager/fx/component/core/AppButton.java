package com.manager.storemanager.fx.component.core;

import javafx.scene.control.Button;

public class AppButton extends Button {

    public enum Variant {
        PRIMARY,
        SECONDARY,
        DANGER,
        GHOST
    }

    public AppButton() {
        this("", Variant.PRIMARY);
    }

    public AppButton(String text) {
        this(text, Variant.PRIMARY);
    }

    public AppButton(String text, Variant variant) {
        super(text);
        getStyleClass().add("app-button");
        setVariant(variant);
    }

    public final void setVariant(Variant variant) {
        getStyleClass().removeAll(
                "app-button-primary",
                "app-button-secondary",
                "app-button-danger",
                "app-button-ghost"
        );
        String styleClass = switch (variant == null ? Variant.PRIMARY : variant) {
            case SECONDARY -> "app-button-secondary";
            case DANGER -> "app-button-danger";
            case GHOST -> "app-button-ghost";
            case PRIMARY -> "app-button-primary";
        };
        if (!getStyleClass().contains(styleClass)) {
            getStyleClass().add(styleClass);
        }
    }
}
