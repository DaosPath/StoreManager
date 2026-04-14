package com.manager.storemanager.fx.component.form;

import java.time.LocalDate;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

public class AppDatePicker extends javafx.scene.layout.VBox {

    private static final String STYLESHEET = "/css/components-form.css";

    private final Label label = new Label();
    private final Label helper = new Label();
    private final DatePicker datePicker = new DatePicker();
    private final StringProperty labelText = new SimpleStringProperty("");
    private final StringProperty helperText = new SimpleStringProperty("");

    public AppDatePicker() {
        this(null, null);
    }

    public AppDatePicker(String labelText) {
        this(labelText, null);
    }

    public AppDatePicker(String labelText, String promptText) {
        getStyleClass().add("app-form-field");
        getStylesheets().add(STYLESHEET);
        setSpacing(6);
        setFillWidth(true);
        setPadding(Insets.EMPTY);

        label.getStyleClass().add("app-form-label");
        helper.getStyleClass().add("app-form-helper");

        datePicker.getStyleClass().add("app-form-date-picker");
        datePicker.setEditable(false);
        datePicker.setPromptText(promptText);
        datePicker.setMaxWidth(Double.MAX_VALUE);

        this.labelText.addListener((obs, oldValue, newValue) -> updateLabel(newValue));
        this.helperText.addListener((obs, oldValue, newValue) -> updateHelper(newValue));
        updateLabel(labelText);
        updateHelper("");

        getChildren().addAll(label, datePicker, helper);
    }

    public DatePicker getControl() {
        return datePicker;
    }

    public ObjectProperty<LocalDate> valueProperty() {
        return datePicker.valueProperty();
    }

    public LocalDate getValue() {
        return datePicker.getValue();
    }

    public void setValue(LocalDate value) {
        datePicker.setValue(value);
    }

    public void setPromptText(String value) {
        datePicker.setPromptText(value);
    }

    public void setConverter(StringConverter<LocalDate> converter) {
        datePicker.setConverter(converter);
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
}
