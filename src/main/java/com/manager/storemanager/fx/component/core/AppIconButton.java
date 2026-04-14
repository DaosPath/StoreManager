package com.manager.storemanager.fx.component.core;

import com.manager.storemanager.fx.component.AppGlyphs;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;

public class AppIconButton extends Button {

    public AppIconButton() {
        this((Node) null);
    }

    public AppIconButton(Node graphic) {
        super();
        getStyleClass().add("app-icon-button");
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(graphic);
    }

    public AppIconButton(String svgPath) {
        this(svgPath == null || svgPath.isBlank() ? null : AppGlyphs.glyph(svgPath, "app-glyph"));
    }

    public void setGlyph(String svgPath) {
        setGraphic(svgPath == null || svgPath.isBlank() ? null : AppGlyphs.glyph(svgPath, "app-glyph"));
    }
}
