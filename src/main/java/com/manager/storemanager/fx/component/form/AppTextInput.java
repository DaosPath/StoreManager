package com.manager.storemanager.fx.component.form;

import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AppTextInput extends VBox {

    private static final String STYLESHEET = "/css/components-form.css";

    private final Label label = new Label();
    private final Label helper = new Label();
    private final TextField field = new TextField();
    private final HBox shell = new HBox();
    private final StringProperty labelText = new SimpleStringProperty("");
    private final StringProperty helperText = new SimpleStringProperty("");

    public AppTextInput() {
        this(null, null);
    }

    public AppTextInput(String labelText) {
        this(labelText, null);
    }

    public AppTextInput(String labelText, String promptText) {
        getStyleClass().add("app-form-field");
        getStylesheets().add(STYLESHEET);
        setSpacing(6);
        setFillWidth(true);
        setPadding(Insets.EMPTY);

        label.getStyleClass().add("app-form-label");
        helper.getStyleClass().add("app-form-helper");

        field.getStyleClass().add("app-form-control");
        field.setPromptText(promptText);
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);

        shell.getStyleClass().add("app-form-shell");
        shell.setAlignment(Pos.CENTER_LEFT);
        shell.getChildren().add(field);

        bindFocus(shell, field);

        this.labelText.addListener((obs, oldValue, newValue) -> updateLabel(newValue));
        this.helperText.addListener((obs, oldValue, newValue) -> updateHelper(newValue));
        updateLabel(labelText);
        updateHelper("");

        getChildren().addAll(label, shell, helper);
    }

    public TextField getControl() {
        return field;
    }

    public StringProperty textProperty() {
        return field.textProperty();
    }

    public String getText() {
        return field.getText();
    }

    public void setText(String value) {
        field.setText(value);
    }

    public void setPromptText(String value) {
        field.setPromptText(value);
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

    private void bindFocus(HBox shell, TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                if (!shell.getStyleClass().contains("is-focused")) {
                    shell.getStyleClass().add("is-focused");
                }
            } else {
                shell.getStyleClass().remove("is-focused");
            }
        });
    }
}
