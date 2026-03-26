package com.manager.storemanager;

import com.manager.storemanager.fx.StoreManagerApp;
import javafx.application.Application;

public class StoreManager {

    public static void main(String[] args) {
        configureGraphicsPipeline();
        Application.launch(StoreManagerApp.class, args);
    }

    private static void configureGraphicsPipeline() {
        // WebView on Windows is currently unstable on the D3D Prism path in this app.
        // Force the software pipeline before JavaFX boots to avoid RTTexture/D3D crashes.
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.d3d", "false");
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.noddraw", "true");
    }
}
