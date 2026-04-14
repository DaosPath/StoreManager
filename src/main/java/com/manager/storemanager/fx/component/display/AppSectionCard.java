package com.manager.storemanager.fx.component.display;

import javafx.scene.Node;

public class AppSectionCard extends AppCard {

    private final AppSectionHeader header = new AppSectionHeader();

    public AppSectionCard() {
        this(null, null);
    }

    public AppSectionCard(String title, String subtitle) {
        this(title, subtitle, (Node[]) null);
    }

    public AppSectionCard(String title, String subtitle, Node... content) {
        super(0.0);
        getStyleClass().add("app-section-card");
        header.getStyleClass().add("app-section-card-header");
        getChildren().setAll(header);
        if (content != null && content.length > 0) {
            getChildren().add(createBody(content));
        }
        setTitle(title);
        setSubtitle(subtitle);
    }

    public final void setTitle(String title) {
        header.setTitle(title);
    }

    public final void setSubtitle(String subtitle) {
        header.setSubtitle(subtitle);
    }

    public final void setAction(Node action) {
        header.setAction(action);
    }

    public final AppSectionHeader header() {
        return header;
    }

    public final void setContent(Node... content) {
        if (getChildren().size() == 1) {
            getChildren().add(createBody(content));
            return;
        }
        if (getChildren().size() > 1 && getChildren().get(1) instanceof javafx.scene.layout.VBox body) {
            body.getChildren().setAll(content == null ? new Node[0] : content);
        }
    }

    public final javafx.scene.layout.VBox body() {
        if (getChildren().size() > 1 && getChildren().get(1) instanceof javafx.scene.layout.VBox body) {
            return body;
        }
        javafx.scene.layout.VBox body = createBody();
        getChildren().add(body);
        return body;
    }

    private javafx.scene.layout.VBox createBody(Node... content) {
        javafx.scene.layout.VBox body = new javafx.scene.layout.VBox(12);
        body.getStyleClass().add("app-section-card-body");
        if (content != null && content.length > 0) {
            body.getChildren().addAll(content);
        }
        return body;
    }
}
