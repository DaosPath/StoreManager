package com.manager.storemanager.fx;

import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Orientation;

public final class FxSupport {

    private static final String ENHANCED_SCROLL_KEY = "fxsupport.enhancedScroll";
    private static final String ENHANCED_LIST_KEY = "fxsupport.enhancedList";
    private static final String ENHANCED_TABLE_KEY = "fxsupport.enhancedTable";

    private FxSupport() {
    }

    private static final String BASE_CSS = css("/css/base.css");
    private static final String LOGIN_CSS = css("/css/login.css");
    private static final String MAIN_CSS = css("/css/main.css");

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().setAll(BASE_CSS, MAIN_CSS);
    }

    public static void applyLoginTheme(Scene scene) {
        scene.getStylesheets().setAll(BASE_CSS, LOGIN_CSS);
    }

    public static void enhanceScrollPane(ScrollPane scrollPane, double multiplier) {
        if (scrollPane.getProperties().putIfAbsent(ENHANCED_SCROLL_KEY, Boolean.TRUE) != null) {
            return;
        }
        if (!scrollPane.getStyleClass().contains("app-scroll-pane")) {
            scrollPane.getStyleClass().add("app-scroll-pane");
        }
        scrollPane.setPannable(true);
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (Math.abs(event.getDeltaY()) < 0.01) {
                return;
            }
            Node content = scrollPane.getContent();
            if (content == null) {
                return;
            }
            double boostedDelta = event.getDeltaY() * multiplier;
            double contentHeight = content.getBoundsInLocal().getHeight();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            double scrollableHeight = contentHeight - viewportHeight;
            if (scrollableHeight > 0) {
                double newValue = clamp(scrollPane.getVvalue() - (boostedDelta / scrollableHeight), 0, 1);
                scrollPane.setVvalue(newValue);
                event.consume();
                return;
            }

            double contentWidth = content.getBoundsInLocal().getWidth();
            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double scrollableWidth = contentWidth - viewportWidth;
            if (scrollableWidth > 0) {
                double newValue = clamp(scrollPane.getHvalue() - (boostedDelta / scrollableWidth), 0, 1);
                scrollPane.setHvalue(newValue);
                event.consume();
            }
        });
    }

    public static void enhanceListView(ListView<?> listView, double multiplier) {
        if (listView.getProperties().putIfAbsent(ENHANCED_LIST_KEY, Boolean.TRUE) != null) {
            return;
        }
        if (!listView.getStyleClass().contains("app-list-scroll")) {
            listView.getStyleClass().add("app-list-scroll");
        }
        installVirtualScrollBoost(listView, multiplier);
    }

    public static void enhanceTableView(TableView<?> tableView, double multiplier) {
        if (tableView.getProperties().putIfAbsent(ENHANCED_TABLE_KEY, Boolean.TRUE) != null) {
            return;
        }
        if (!tableView.getStyleClass().contains("app-table-scroll")) {
            tableView.getStyleClass().add("app-table-scroll");
        }
        installVirtualScrollBoost(tableView, multiplier);
    }

    private static String css(String path) {
        return FxSupport.class.getResource(path).toExternalForm();
    }

    public static VBox pageHeader(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("page-subtitle");

        VBox header = new VBox(4, titleLabel, subtitleLabel);
        header.getStyleClass().add("page-header");
        return header;
    }

    public static HBox toolbar(Node... nodes) {
        HBox toolbar = new HBox(10);
        toolbar.getStyleClass().add("toolbar");
        toolbar.getChildren().addAll(nodes);
        return toolbar;
    }

    public static Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        applyDialogTheme(dialogPane);
        if (dialogPane.lookupButton(ButtonType.YES) != null) {
            dialogPane.lookupButton(ButtonType.YES).getStyleClass().add("button-primary");
        }
        if (dialogPane.lookupButton(ButtonType.NO) != null) {
            dialogPane.lookupButton(ButtonType.NO).getStyleClass().add("button-secondary");
        }
        if (dialogPane.lookupButton(ButtonType.OK) != null) {
            dialogPane.lookupButton(ButtonType.OK).getStyleClass().add("button-primary");
        }
    }

    public static void applyDialogTheme(DialogPane dialogPane) {
        dialogPane.getStylesheets().setAll(BASE_CSS, MAIN_CSS);
        if (!dialogPane.getStyleClass().contains("app-root")) {
            dialogPane.getStyleClass().add("app-root");
        }
        dialogPane.setPadding(new Insets(16));
    }

    private static void installVirtualScrollBoost(Parent parent, double multiplier) {
        parent.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (Math.abs(event.getDeltaY()) < 0.01) {
                return;
            }
            ScrollBar verticalBar = findScrollBar(parent, Orientation.VERTICAL);
            if (verticalBar != null && verticalBar.isVisible() && verticalBar.getVisibleAmount() < (verticalBar.getMax() - verticalBar.getMin())) {
                double delta = event.getDeltaY() * 0.00125 * multiplier;
                double newValue = clamp(verticalBar.getValue() - delta, verticalBar.getMin(), verticalBar.getMax());
                verticalBar.setValue(newValue);
                event.consume();
                return;
            }

            ScrollBar horizontalBar = findScrollBar(parent, Orientation.HORIZONTAL);
            if (horizontalBar != null && horizontalBar.isVisible()) {
                double delta = event.getDeltaY() * 0.00115 * multiplier;
                double newValue = clamp(horizontalBar.getValue() - delta, horizontalBar.getMin(), horizontalBar.getMax());
                horizontalBar.setValue(newValue);
                event.consume();
            }
        });
    }

    private static ScrollBar findScrollBar(Parent parent, Orientation orientation) {
        for (Node node : parent.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == orientation) {
                return scrollBar;
            }
        }
        return null;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
