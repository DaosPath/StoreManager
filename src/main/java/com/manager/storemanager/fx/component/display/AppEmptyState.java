package com.manager.storemanager.fx.component.display;

import com.manager.storemanager.fx.component.AppGlyphs;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class AppEmptyState extends AppCard {

    private static final String EMPTY_ICON = "M4 7h16M4 12h16M4 17h10";

    private final StackPane iconBox = new StackPane();
    private final Label titleLabel = new Label();
    private final Label messageLabel = new Label();
    private final HBox actionRow = new HBox();

    public AppEmptyState(String title, String message) {
        this(title, message, null);
    }

    public AppEmptyState(String title, String message, Node action) {
        super(14);
        getStyleClass().add("app-empty-state");
        setAlignment(Pos.CENTER);
        iconBox.getStyleClass().add("app-empty-state-icon");
        titleLabel.getStyleClass().add("app-empty-state-title");
        messageLabel.getStyleClass().add("app-empty-state-message");
        messageLabel.setWrapText(true);
        titleLabel.setText(title == null ? "" : title);
        messageLabel.setText(message == null ? "" : message);
        actionRow.getStyleClass().add("app-empty-state-action");
        actionRow.setAlignment(Pos.CENTER);
        if (action != null) {
            actionRow.getChildren().add(action);
        }
        getChildren().setAll(iconBox, titleLabel, messageLabel, actionRow);
        setIcon(null);
    }

    public final void setIcon(Node icon) {
        iconBox.getChildren().setAll(icon == null ? defaultIcon() : icon);
    }

    public final void setAction(Node action) {
        actionRow.getChildren().setAll(action == null ? new Node[0] : new Node[]{action});
        actionRow.setVisible(action != null);
        actionRow.setManaged(action != null);
    }

    public final void setTitle(String title) {
        titleLabel.setText(title == null ? "" : title);
    }

    public final void setMessage(String message) {
        messageLabel.setText(message == null ? "" : message);
    }

    private Node defaultIcon() {
        SVGPath path = AppGlyphs.glyph(EMPTY_ICON, "app-empty-state-glyph");
        return path;
    }
}
