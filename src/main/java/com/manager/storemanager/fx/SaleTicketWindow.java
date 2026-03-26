package com.manager.storemanager.fx;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.model.SaleDetail;
import com.manager.storemanager.model.User;
import com.manager.storemanager.util.CurrencyUtils;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class SaleTicketWindow {

    private SaleTicketWindow() {
    }

    public static void show(Window owner, User currentUser, Sale sale) {
        Stage stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        }

        stage.setTitle("Ticket de venta");
        stage.setMinWidth(460);
        stage.setMinHeight(640);

        WebViewBridge bridge = new WebViewBridge("/web/ticket.html");
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setCenter(bridge.getView());

        Button printButton = new Button("Imprimir");
        printButton.getStyleClass().addAll("button", "button-secondary");
        printButton.setOnAction(event -> print(stage, bridge));

        Button closeButton = new Button("Cerrar");
        closeButton.getStyleClass().addAll("button", "button-primary");
        closeButton.setOnAction(event -> stage.close());

        HBox actions = new HBox(10, printButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(14, 18, 18, 18));
        root.setBottom(actions);

        Scene scene = new Scene(root, 460, 680);
        FxSupport.applyTheme(scene);
        stage.setScene(scene);

        bridge.execute("window.renderTicket(" + buildPayload(currentUser, sale) + ");");
        stage.showAndWait();
    }

    private static void print(Stage stage, WebViewBridge bridge) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob == null) {
            FxSupport.showError("Ticket", "No hay impresora disponible.");
            return;
        }
        if (!printerJob.showPrintDialog(stage)) {
            return;
        }
        try {
            bridge.getEngine().print(printerJob);
            printerJob.endJob();
        } catch (RuntimeException exception) {
            FxSupport.showError("Ticket", "No se pudo imprimir el ticket.");
        }
    }

    private static String buildPayload(User currentUser, Sale sale) {
        List<String[]> detailRows = new ArrayList<>();
        for (SaleDetail detail : sale.getDetails()) {
            detailRows.add(new String[]{
                detail.getProduct().getName(),
                String.valueOf(detail.getQuantity()),
                CurrencyUtils.format(detail.getUnitPrice()),
                CurrencyUtils.format(detail.getLineTotal())
            });
        }

        String dateText = sale.getSaleDate() == null
                ? ""
                : sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER);
        String customerName = sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName();
        String cashierName = currentUser == null ? "" : currentUser.getFullName();
        String observation = sale.getObservation() == null || sale.getObservation().isBlank()
                ? "Sin observaciones."
                : sale.getObservation();

        return "{"
                + "\"saleNumber\":" + WebViewBridge.jsString("#" + sale.getId()) + ","
                + "\"date\":" + WebViewBridge.jsString(dateText) + ","
                + "\"cashier\":" + WebViewBridge.jsString(cashierName) + ","
                + "\"customer\":" + WebViewBridge.jsString(customerName) + ","
                + "\"paymentMethod\":" + WebViewBridge.jsString(sale.getPaymentMethod()) + ","
                + "\"status\":" + WebViewBridge.jsString(sale.getStatus()) + ","
                + "\"observation\":" + WebViewBridge.jsString(observation) + ","
                + "\"subtotal\":" + WebViewBridge.jsString(CurrencyUtils.format(sale.getSubtotal())) + ","
                + "\"tax\":" + WebViewBridge.jsString(CurrencyUtils.format(sale.getTax())) + ","
                + "\"total\":" + WebViewBridge.jsString(CurrencyUtils.format(sale.getTotal())) + ","
                + "\"items\":" + WebViewBridge.jsMatrix(detailRows)
                + "}";
    }
}
