package com.manager.storemanager.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public final class WebViewBridge {

    private final WebView view = new WebView();
    private final WebEngine engine = view.getEngine();
    private final List<Runnable> pendingActions = new ArrayList<>();
    private boolean ready;

    public WebViewBridge(String resourcePath) {
        view.setContextMenuEnabled(false);
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                ready = true;
                flushPendingActions();
            }
        });
        engine.load(WebViewBridge.class.getResource(resourcePath).toExternalForm());
    }

    public WebView getView() {
        return view;
    }

    public WebEngine getEngine() {
        return engine;
    }

    public void whenReady(Runnable action) {
        if (action == null) {
            return;
        }
        if (ready) {
            action.run();
            return;
        }
        pendingActions.add(action);
    }

    public void execute(String script) {
        whenReady(() -> engine.executeScript(script));
    }

    public static String jsString(String value) {
        return "\"" + escape(value) + "\"";
    }

    public static String jsArray(List<String> values) {
        return values.stream()
                .map(WebViewBridge::jsString)
                .collect(Collectors.joining(",", "[", "]"));
    }

    public static String jsMatrix(List<String[]> rows) {
        return rows.stream()
                .map(WebViewBridge::jsRow)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private void flushPendingActions() {
        List<Runnable> actions = new ArrayList<>(pendingActions);
        pendingActions.clear();
        actions.forEach(Runnable::run);
    }

    private static String jsRow(String[] row) {
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < row.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(jsString(row[index]));
        }
        builder.append(']');
        return builder.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
