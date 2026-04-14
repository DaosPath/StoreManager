package com.manager.storemanager.fx.component.core;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

public class AppSwitch extends ToggleButton {

    private final StackPane track = new StackPane();
    private final StackPane thumb = new StackPane();
    private final StackPane graphic = new StackPane(track, thumb);
    private final ChangeListener<Boolean> selectionListener = (obs, oldValue, selected) -> syncState(selected);

    public AppSwitch() {
        this(false);
    }

    public AppSwitch(boolean selected) {
        getStyleClass().add("app-switch");
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setAlignment(Pos.CENTER_LEFT);
        graphic.getStyleClass().add("app-switch-graphic");
        track.getStyleClass().add("app-switch-track");
        thumb.getStyleClass().add("app-switch-thumb");
        setGraphic(graphic);
        setSelected(selected);
        selectedProperty().addListener(selectionListener);
        syncState(isSelected());
    }

    private void syncState(boolean selected) {
        graphic.getStyleClass().remove("app-switch-active");
        if (selected && !graphic.getStyleClass().contains("app-switch-active")) {
            graphic.getStyleClass().add("app-switch-active");
        }
        track.setMinSize(38, 22);
        track.setPrefSize(38, 22);
        track.setMaxSize(38, 22);
        thumb.setMinSize(16, 16);
        thumb.setPrefSize(16, 16);
        thumb.setMaxSize(16, 16);
        StackPane.setAlignment(thumb, selected ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        StackPane.setMargin(thumb, selected ? new javafx.geometry.Insets(0, 2, 0, 0) : new javafx.geometry.Insets(0, 0, 0, 2));
    }

}
