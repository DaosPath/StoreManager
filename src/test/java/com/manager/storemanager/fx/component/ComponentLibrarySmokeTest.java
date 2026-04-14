package com.manager.storemanager.fx.component;

import com.manager.storemanager.fx.FxSupport;
import com.manager.storemanager.fx.component.core.AppAvatar;
import com.manager.storemanager.fx.component.core.AppBadge;
import com.manager.storemanager.fx.component.core.AppButton;
import com.manager.storemanager.fx.component.core.AppChip;
import com.manager.storemanager.fx.component.core.AppDivider;
import com.manager.storemanager.fx.component.core.AppIconButton;
import com.manager.storemanager.fx.component.core.AppSwitch;
import com.manager.storemanager.fx.component.display.AppCard;
import com.manager.storemanager.fx.component.display.AppEmptyState;
import com.manager.storemanager.fx.component.display.AppInfoBanner;
import com.manager.storemanager.fx.component.display.AppSectionCard;
import com.manager.storemanager.fx.component.display.AppSectionHeader;
import com.manager.storemanager.fx.component.display.AppStatCard;
import com.manager.storemanager.fx.component.display.AppToolbar;
import com.manager.storemanager.fx.component.form.AppComboBox;
import com.manager.storemanager.fx.component.form.AppDatePicker;
import com.manager.storemanager.fx.component.form.AppPasswordInput;
import com.manager.storemanager.fx.component.form.AppSearchField;
import com.manager.storemanager.fx.component.form.AppTextAreaInput;
import com.manager.storemanager.fx.component.form.AppTextInput;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class ComponentLibrarySmokeTest {

    private ComponentLibrarySmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        Platform.startup(startup::countDown);
        if (!startup.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX no inicio a tiempo.");
        }

        CountDownLatch renderLatch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                VBox root = new VBox(18);
                root.getStyleClass().add("app-root");

                AppButton primaryButton = new AppButton("Guardar");
                AppButton secondaryButton = new AppButton("Cancelar", AppButton.Variant.SECONDARY);
                AppButton dangerButton = new AppButton("Eliminar", AppButton.Variant.DANGER);
                AppIconButton iconButton = new AppIconButton("M12 5v14M5 12h14");
                AppBadge successBadge = new AppBadge("Activo", AppBadge.Variant.SUCCESS);
                AppChip chip = new AppChip("Catalogo");
                chip.setSelected(true);
                AppAvatar avatar = new AppAvatar("SM");
                AppSwitch appSwitch = new AppSwitch(true);
                AppDivider horizontalDivider = new AppDivider();
                AppDivider verticalDivider = new AppDivider(Orientation.VERTICAL);

                AppTextInput textInput = new AppTextInput("Nombre", "Ej. Arroz premium");
                textInput.setText("Arroz premium");
                AppPasswordInput passwordInput = new AppPasswordInput("Contrasena", "••••••••");
                passwordInput.setText("admin123");
                AppSearchField searchField = new AppSearchField("Busqueda", "Buscar cliente");
                searchField.setText("Ana");
                AppTextAreaInput textAreaInput = new AppTextAreaInput("Descripcion", "Detalle del movimiento");
                textAreaInput.setText("Carga inicial de mercaderia.");
                AppComboBox<String> comboBox = new AppComboBox<>("Categoria");
                comboBox.getItems().addAll("Abarrotes", "Limpieza", "Bebidas");
                comboBox.setValue("Abarrotes");
                AppDatePicker datePicker = new AppDatePicker("Vencimiento");
                datePicker.setValue(LocalDate.now().plusDays(30));

                AppCard card = new AppCard(new Label("Contenido base"));
                AppSectionHeader sectionHeader = new AppSectionHeader("Resumen", "Estado general de la interfaz");
                sectionHeader.setAction(new AppButton("Accion", AppButton.Variant.SECONDARY));
                AppSectionCard sectionCard = new AppSectionCard("Formulario", "Componentes de entrada");
                sectionCard.body().getChildren().addAll(textInput, comboBox, datePicker);
                AppInfoBanner banner = new AppInfoBanner(
                        "Sincronizacion",
                        "El kit inicial cargo correctamente."
                );
                banner.setTone(AppInfoBanner.Tone.INFO);
                AppEmptyState emptyState = new AppEmptyState(
                        "Sin elementos",
                        "Usa este estado cuando todavia no exista informacion para mostrar."
                );
                emptyState.setAction(new AppButton("Crear primero", AppButton.Variant.PRIMARY));
                AppStatCard statCard = new AppStatCard("Componentes", "20", "Kit inicial listo");
                AppToolbar toolbar = new AppToolbar();
                toolbar.setLeading(avatar, successBadge, chip);
                toolbar.setCenter(searchField);
                toolbar.setTrailing(iconButton, appSwitch);
                HBox.setHgrow(searchField, Priority.ALWAYS);

                HBox buttonRow = new HBox(10, primaryButton, secondaryButton, dangerButton, iconButton, successBadge, chip, avatar, appSwitch);
                HBox dividerRow = new HBox(12, new Label("Izquierda"), verticalDivider, new Label("Derecha"));
                VBox formColumn = new VBox(12, textInput, passwordInput, searchField, textAreaInput, comboBox, datePicker);
                VBox displayColumn = new VBox(12, card, banner, emptyState, statCard);

                root.getChildren().addAll(
                        toolbar,
                        sectionHeader,
                        buttonRow,
                        horizontalDivider,
                        dividerRow,
                        sectionCard,
                        formColumn,
                        displayColumn
                );

                ScrollPane scrollPane = new ScrollPane(root);
                scrollPane.setFitToWidth(true);
                FxSupport.enhanceScrollPane(scrollPane, 1.35);
                Scene scene = new Scene(scrollPane, 1280, 900);
                FxSupport.applyTheme(scene);

                assertStyled(primaryButton, "app-button");
                assertStyled(iconButton, "app-icon-button");
                assertStyled(successBadge, "app-badge");
                assertStyled(chip, "app-chip");
                assertStyled(avatar, "app-avatar");
                assertStyled(appSwitch, "app-switch");
                assertStyled(textInput, "app-form-field");
                assertStyled(comboBox, "app-form-field");
                assertStyled(card, "app-card");
                assertStyled(sectionCard, "app-section-card");
                assertStyled(sectionHeader, "app-section-header");
                assertStyled(toolbar, "app-toolbar");
                assertStyled(banner, "app-info-banner");
                assertStyled(emptyState, "app-empty-state");
                assertStyled(statCard, "app-stat-card");
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                renderLatch.countDown();
            }
        });

        if (!renderLatch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("La prueba de componentes no termino a tiempo.");
        }
        Platform.exit();

        if (failure.get() != null) {
            throw new RuntimeException("Fallo la prueba de humo del component kit.", failure.get());
        }

        System.out.println("ComponentLibrarySmokeTest OK");
    }

    private static void assertStyled(javafx.scene.Node node, String styleClass) {
        if (!node.getStyleClass().contains(styleClass)) {
            throw new IllegalStateException("Falta style class " + styleClass + " en " + node.getClass().getSimpleName());
        }
    }
}
