package com.manager.storemanager.fx;

import com.manager.storemanager.fx.ticket.NativeTicketView;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.model.User;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Node;
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
        stage.setMinWidth(520);
        stage.setMinHeight(720);

        NativeTicketView ticketView = new NativeTicketView();
        ticketView.setTicket(currentUser, sale);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setCenter(ticketView);

        Button printButton = new Button("Imprimir");
        printButton.getStyleClass().add("ticket-button-secondary");
        printButton.setOnAction(event -> print(stage, ticketView));

        Button closeButton = new Button("Cerrar");
        closeButton.getStyleClass().add("ticket-button-primary");
        closeButton.setOnAction(event -> stage.close());

        HBox actions = new HBox(10, printButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(14, 18, 18, 18));
        root.setBottom(actions);

        Scene scene = new Scene(root, 520, 760);
        scene.getStylesheets().setAll(css("/css/base.css"), css("/css/ticket-native.css"));
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static void print(Stage stage, NativeTicketView ticketView) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob == null) {
            FxSupport.showError("Ticket", "No hay impresora disponible.");
            return;
        }
        if (!printerJob.showPrintDialog(stage)) {
            return;
        }

        Node printable = ticketView.getPrintableNode();
        printable.applyCss();
        if (printable instanceof Parent parent) {
            parent.layout();
        }

        try {
            boolean printed = printerJob.printPage(printable);
            if (printed) {
                printerJob.endJob();
            } else {
                printerJob.cancelJob();
                FxSupport.showError("Ticket", "No se pudo imprimir el ticket.");
            }
        } catch (RuntimeException exception) {
            printerJob.cancelJob();
            FxSupport.showError("Ticket", "No se pudo imprimir el ticket.");
        }
    }

    private static String css(String path) {
        return Objects.requireNonNull(SaleTicketWindow.class.getResource(path)).toExternalForm();
    }
}
