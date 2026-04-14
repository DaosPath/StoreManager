package com.manager.storemanager.fx.component;

import javafx.scene.shape.SVGPath;

public final class AppGlyphs {

    private AppGlyphs() {
    }

    public static SVGPath glyph(String content, String... styleClasses) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        if (styleClasses != null) {
            path.getStyleClass().addAll(styleClasses);
        }
        return path;
    }
}
