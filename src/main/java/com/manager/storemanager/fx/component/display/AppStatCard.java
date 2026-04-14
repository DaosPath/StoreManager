package com.manager.storemanager.fx.component.display;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AppStatCard extends AppCard {

    private static final String DISPLAY_CSS = "/css/components-display.css";

    private final Label titleLabel = new Label();
    private final Label valueLabel = new Label();
    private final Label detailLabel = new Label();
    private final VBox copyBox = new VBox(4, titleLabel, valueLabel, detailLabel);

    public AppStatCard(String title, String value) {
        this(title, value, null);
    }

    public AppStatCard(String title, String value, String detail) {
        super(0.0);
        getStyleClass().add("app-stat-card");
        setAlignment(Pos.TOP_LEFT);
        titleLabel.getStyleClass().add("app-stat-card-title");
        valueLabel.getStyleClass().add("app-stat-card-value");
        detailLabel.getStyleClass().add("app-stat-card-detail");
        detailLabel.setWrapText(true);
        copyBox.getStyleClass().add("app-stat-card-copy");
        getChildren().setAll(copyBox);
        setTitle(title);
        setValue(value);
        setDetail(detail);
    }

    public final void setTitle(String title) {
        titleLabel.setText(title == null ? "" : title);
    }

    public final void setValue(String value) {
        valueLabel.setText(value == null ? "" : value);
    }

    public final void setDetail(String detail) {
        detailLabel.setText(detail == null ? "" : detail);
        detailLabel.setVisible(detail != null && !detail.isBlank());
        detailLabel.setManaged(detail != null && !detail.isBlank());
    }
}
