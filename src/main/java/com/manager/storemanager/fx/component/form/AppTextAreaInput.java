package com.manager.storemanager.fx.component.form;

import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class AppTextAreaInput extends VBox {

    private static final String STYLESHEET = "/css/components-form.css";

    private final Label label = new Label();
    private final Label helper = new Label();
    private final TextArea area = new TextArea();
    private final StringProperty labelText = new SimpleStringProperty("");
    private final StringProperty helperText = new SimpleStringProperty("");

    public AppTextAreaInput() {
        this(null, null);
    }

    public AppTextAreaInput(String labelText) {
        this(labelText, null);
    }

    public AppTextAreaInput(String labelText, String promptText) {
        getStyleClass().add("app-form-field");
        getStylesheets().add(STYLESHEET);
        setSpacing(6);
        setFillWidth(true);
        setPadding(Insets.EMPTY);

        label.getStyleClass().add("app-form-label");
        helper.getStyleClass().add("app-form-helper");

        area.getStyleClass().add("app-form-text-area");
        area.setPromptText(promptText);
        area.setWrapText(true);
        area.setPrefRowCount(4);
        area.setMaxWidth(Double.MAX_VALUE);

        bindFocus(area);

        this.labelText.addListener((obs, oldValue, newValue) -> updateLabel(newValue));
        this.helperText.addListener((obs, oldValue, newValue) -> updateHelper(newValue));
        updateLabel(labelText);
        updateHelper("");

        getChildren().addAll(label, area, helper);
    }

    public TextArea getControl() {
        return area;
    }

    public StringProperty textProperty() {
        return area.textProperty();
    }

    public String getText() {
        return area.getText();
    }

    public void setText(String value) {
        area.setText(value);
    }

    public void setPromptText(String value) {
        area.setPromptText(value);
    }

    public void setRowCount(int rows) {
        area.setPrefRowCount(rows);
    }

    public void setLabelText(String value) {
        labelText.set(value);
    }

    public StringProperty labelTextProperty() {
        return labelText;
    }

    public void setHelperText(String value) {
        helperText.set(value);
    }

    public StringProperty helperTextProperty() {
        return helperText;
    }

    private void updateLabel(String value) {
        String text = Objects.toString(value, "");
        label.setText(text);
        label.setVisible(!text.isBlank());
        label.setManaged(!text.isBlank());
    }

    private void updateHelper(String value) {
        String text = Objects.toString(value, "");
        helper.setText(text);
        helper.setVisible(!text.isBlank());
        helper.setManaged(!text.isBlank());
    }

    private void bindFocus(TextArea area) {
        area.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                if (!area.getStyleClass().contains("is-focused")) {
                    area.getStyleClass().add("is-focused");
                }
            } else {
                area.getStyleClass().remove("is-focused");
            }
        });
    }
}
