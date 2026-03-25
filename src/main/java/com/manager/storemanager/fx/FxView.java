package com.manager.storemanager.fx;

import javafx.scene.Node;

public interface FxView {

    String getName();

    Node getContent();

    void refresh();
}
