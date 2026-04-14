package com.manager.storemanager.fx.component.core;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.ToggleButton;

public class AppChip extends ToggleButton {

    private final ChangeListener<Boolean> selectionListener = (obs, oldValue, selected) -> syncSelectedState(selected);

    public AppChip() {
        this("");
    }

    public AppChip(String text) {
        super(text);
        getStyleClass().add("app-chip");
        selectedProperty().addListener(selectionListener);
        syncSelectedState(isSelected());
    }

    private void syncSelectedState(boolean selected) {
        getStyleClass().remove("app-chip-active");
        if (selected && !getStyleClass().contains("app-chip-active")) {
            getStyleClass().add("app-chip-active");
        }
    }
}
