package com.manager.storemanager.fx.component.core;

import javafx.geometry.Orientation;
import javafx.scene.layout.Region;

public class AppDivider extends Region {

    private Orientation orientation = Orientation.HORIZONTAL;

    public AppDivider() {
        this(Orientation.HORIZONTAL);
    }

    public AppDivider(Orientation orientation) {
        getStyleClass().add("app-divider");
        setOrientation(orientation);
    }

    public final void setOrientation(Orientation orientation) {
        this.orientation = orientation == null ? Orientation.HORIZONTAL : orientation;
        if (this.orientation == Orientation.VERTICAL) {
            setMinSize(1, 24);
            setPrefSize(1, 24);
            setMaxSize(1, Double.MAX_VALUE);
        } else {
            setMinSize(24, 1);
            setPrefSize(24, 1);
            setMaxSize(Double.MAX_VALUE, 1);
        }
    }

    public Orientation getOrientation() {
        return orientation;
    }
}
