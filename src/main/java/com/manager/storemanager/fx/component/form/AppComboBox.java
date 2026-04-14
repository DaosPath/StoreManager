package com.manager.storemanager.fx.component.form;

import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class AppComboBox<T> extends VBox {

    private static final String STYLESHEET = "/css/components-form.css";

    private final Label label = new Label();
    private final Label helper = new Label();
    private final ComboBox<T> comboBox = new ComboBox<>();
    private final StringProperty labelText = new SimpleStringProperty("");
    private final StringProperty helperText = new SimpleStringProperty("");

    public AppComboBox() {
        this(null, null);
    }

    public AppComboBox(String labelText) {
        this(labelText, null);
    }

    public AppComboBox(String labelText, String promptText) {
        getStyleClass().add("app-form-field");
        getStylesheets().add(STYLESHEET);
        setSpacing(6);
        setFillWidth(true);
        setPadding(Insets.EMPTY);

        label.getStyleClass().add("app-form-label");
        helper.getStyleClass().add("app-form-helper");

        comboBox.getStyleClass().add("app-form-combo-box");
        comboBox.setPromptText(promptText);
        comboBox.setMaxWidth(Double.MAX_VALUE);

        this.labelText.addListener((obs, oldValue, newValue) -> updateLabel(newValue));
        this.helperText.addListener((obs, oldValue, newValue) -> updateHelper(newValue));
        updateLabel(labelText);
        updateHelper("");

        getChildren().addAll(label, comboBox, helper);
    }

    public ComboBox<T> getControl() {
        return comboBox;
    }

    public ObservableList<T> getItems() {
        return comboBox.getItems();
    }

    public ObjectProperty<T> valueProperty() {
        return comboBox.valueProperty();
    }

    public T getValue() {
        return comboBox.getValue();
    }

    public void setValue(T value) {
        comboBox.setValue(value);
    }

    public void setPromptText(String value) {
        comboBox.setPromptText(value);
    }

    public void setEditable(boolean editable) {
        comboBox.setEditable(editable);
    }

    public void setConverter(StringConverter<T> converter) {
        comboBox.setConverter(converter);
    }

    public void setCellFactory(javafx.util.Callback<javafx.scene.control.ListView<T>, ListCell<T>> cellFactory) {
        comboBox.setCellFactory(cellFactory);
    }

    public void setButtonCell(ListCell<T> buttonCell) {
        comboBox.setButtonCell(buttonCell);
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
