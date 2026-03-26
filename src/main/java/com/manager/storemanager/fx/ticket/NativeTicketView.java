package com.manager.storemanager.fx.ticket;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.model.SaleDetail;
import com.manager.storemanager.model.User;
import com.manager.storemanager.util.CurrencyUtils;
import java.util.Objects;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class NativeTicketView extends BorderPane {

    private final StackPane printableArea = new StackPane();
    private final VBox ticketCard = new VBox(14);
    private final Label saleNumberValue = valueLabel("ticket-value-emphasis");
    private final Label saleDateValue = valueLabel(null);
    private final Label cashierValue = valueLabel(null);
    private final Label customerValue = valueLabel(null);
    private final Label paymentValue = valueLabel(null);
    private final Label statusValue = valueLabel(null);
    private final Label subtotalValue = valueLabel("ticket-money");
    private final Label taxValue = valueLabel("ticket-money");
    private final Label totalValue = valueLabel("ticket-money ticket-money-total");
    private final Label observationValue = new Label();
    private final VBox itemRows = new VBox(0);
    private final Label statusChip = new Label("Pendiente");

    public NativeTicketView() {
        getStyleClass().add("ticket-shell");
        setPadding(new Insets(18));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("ticket-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setContent(printableArea);
        setCenter(scrollPane);

        printableArea.getStyleClass().add("ticket-printable");
        printableArea.setAlignment(Pos.TOP_CENTER);
        printableArea.setPadding(new Insets(12));
        printableArea.getChildren().add(ticketCard);

        ticketCard.getStyleClass().add("ticket-card");
        ticketCard.setMaxWidth(420);
        ticketCard.getChildren().addAll(
                buildHeader(),
                buildInfoGrid(),
                buildItemsSection(),
                buildTotalsSection(),
                buildObservationSection()
        );
    }

    public void setTicket(User currentUser, Sale sale) {
        Objects.requireNonNull(sale, "sale");

        saleNumberValue.setText("#" + sale.getId());
        saleDateValue.setText(sale.getSaleDate() == null ? "-" : sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER));
        cashierValue.setText(currentUser == null ? "" : currentUser.getFullName());
        customerValue.setText(sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName());
        paymentValue.setText(valueOrDash(sale.getPaymentMethod()));
        statusValue.setText(valueOrDash(sale.getStatus()));
        subtotalValue.setText(CurrencyUtils.format(sale.getSubtotal()));
        taxValue.setText(CurrencyUtils.format(sale.getTax()));
        totalValue.setText(CurrencyUtils.format(sale.getTotal()));
        observationValue.setText(normalizeObservation(sale.getObservation()));

        statusChip.setText(valueOrDash(sale.getStatus()));
        statusChip.getStyleClass().removeAll("ticket-status-completed", "ticket-status-other");
        statusChip.getStyleClass().add("COMPLETADA".equalsIgnoreCase(sale.getStatus())
                ? "ticket-status-completed"
                : "ticket-status-other");

        itemRows.getChildren().clear();
        if (sale.getDetails() != null) {
            for (SaleDetail detail : sale.getDetails()) {
                itemRows.getChildren().add(buildItemRow(detail));
            }
        }

        if (itemRows.getChildren().isEmpty()) {
            Label empty = new Label("Sin productos en la venta.");
            empty.getStyleClass().add("ticket-empty-line");
            itemRows.getChildren().add(empty);
        }
    }

    public Node getPrintableNode() {
        return printableArea;
    }

    private Node buildHeader() {
        VBox heading = new VBox(4);
        Label brand = new Label("StoreManager");
        brand.getStyleClass().add("ticket-brand");
        Label title = new Label("Comprobante de venta");
        title.getStyleClass().add("ticket-title");
        heading.getChildren().addAll(brand, title);

        HBox row = new HBox(12, heading, spacer(), statusChip);
        row.getStyleClass().add("ticket-header-row");
        row.setAlignment(Pos.TOP_LEFT);

        HBox meta = new HBox(10, metaBox("Venta", saleNumberValue), metaBox("Fecha", saleDateValue));
        meta.getStyleClass().add("ticket-meta-row");

        VBox header = new VBox(10, row, meta);
        header.getStyleClass().add("ticket-header");
        return header;
    }

    private Node buildInfoGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("ticket-info-grid");
        grid.setHgap(10);
        grid.setVgap(10);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        left.setHgrow(Priority.ALWAYS);

        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        right.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(left, right);

        grid.add(infoBlock("Cajero", cashierValue), 0, 0);
        grid.add(infoBlock("Cliente", customerValue), 1, 0);
        grid.add(infoBlock("Pago", paymentValue), 0, 1);
        grid.add(infoBlock("Estado", statusValue), 1, 1);
        return grid;
    }

    private Node buildItemsSection() {
        VBox section = new VBox(0);
        section.getStyleClass().add("ticket-section");

        Label heading = new Label("Detalle de productos");
        heading.getStyleClass().add("ticket-section-title");

        GridPane header = createItemGrid();
        header.getStyleClass().add("ticket-table-header");
        header.add(tableHeader("Producto"), 0, 0);
        header.add(tableHeader("Cant."), 1, 0);
        header.add(tableHeader("Unit."), 2, 0);
        header.add(tableHeader("Total"), 3, 0);

        itemRows.getStyleClass().add("ticket-item-rows");

        section.getChildren().addAll(heading, header, itemRows);
        return section;
    }

    private Node buildTotalsSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("ticket-section");

        HBox subtotalRow = amountRow("Subtotal", subtotalValue);
        HBox taxRow = amountRow("Impuesto", taxValue);
        HBox totalRow = amountRow("Total", totalValue);
        totalRow.getStyleClass().add("ticket-total-row");

        section.getChildren().addAll(subtotalRow, taxRow, totalRow);
        return section;
    }

    private Node buildObservationSection() {
        VBox section = new VBox(8);
        section.getStyleClass().add("ticket-section");

        Label heading = new Label("Observacion");
        heading.getStyleClass().add("ticket-section-title");

        observationValue.getStyleClass().add("ticket-observation");
        observationValue.setWrapText(true);

        section.getChildren().addAll(heading, observationValue);
        return section;
    }

    private Node buildItemRow(SaleDetail detail) {
        GridPane row = createItemGrid();
        row.getStyleClass().add("ticket-item-row");

        String productName = detail.getProduct() == null ? "-" : valueOrDash(detail.getProduct().getName());
        Label product = new Label(productName);
        product.getStyleClass().add("ticket-item-product");
        product.setWrapText(true);

        Label quantity = tableCell(String.valueOf(detail.getQuantity()), "ticket-cell-strong");
        Label unit = tableCell(CurrencyUtils.format(detail.getUnitPrice()), "ticket-cell-money");
        Label total = tableCell(CurrencyUtils.format(detail.getLineTotal()), "ticket-cell-money ticket-cell-strong");

        row.add(product, 0, 0);
        row.add(quantity, 1, 0);
        row.add(unit, 2, 0);
        row.add(total, 3, 0);
        return row;
    }

    private GridPane createItemGrid() {
        GridPane grid = new GridPane();
        grid.getColumnConstraints().addAll(
                growColumn(),
                fixedColumn(60),
                fixedColumn(84),
                fixedColumn(92)
        );
        grid.setHgap(10);
        return grid;
    }

    private ColumnConstraints growColumn() {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setHgrow(Priority.ALWAYS);
        return constraints;
    }

    private ColumnConstraints fixedColumn(double width) {
        ColumnConstraints constraints = new ColumnConstraints(width, width, width);
        constraints.setHalignment(HPos.RIGHT);
        return constraints;
    }

    private Node tableHeader(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("ticket-table-header-label");
        return label;
    }

    private Label tableCell(String text, String styleClass) {
        Label label = new Label(valueOrDash(text));
        label.getStyleClass().add("ticket-table-cell");
        if (styleClass != null && !styleClass.isBlank()) {
            for (String style : styleClass.split("\\s+")) {
                if (!style.isBlank()) {
                    label.getStyleClass().add(style);
                }
            }
        }
        return label;
    }

    private Node infoBlock(String label, Label value) {
        VBox block = new VBox(4);
        block.getStyleClass().add("ticket-info-block");
        Label caption = new Label(label);
        caption.getStyleClass().add("ticket-info-label");
        value.getStyleClass().add("ticket-info-value");
        block.getChildren().addAll(caption, value);
        return block;
    }

    private Node metaBox(String label, Label value) {
        VBox block = new VBox(4);
        block.getStyleClass().add("ticket-meta-block");
        Label caption = new Label(label);
        caption.getStyleClass().add("ticket-meta-label");
        value.getStyleClass().add("ticket-meta-value");
        block.getChildren().addAll(caption, value);
        return block;
    }

    private HBox amountRow(String label, Label value) {
        HBox row = new HBox(8);
        row.getStyleClass().add("ticket-amount-row");
        Label caption = new Label(label);
        caption.getStyleClass().add("ticket-amount-label");
        row.getChildren().addAll(caption, spacer(), value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private Label valueLabel(String styleClass) {
        Label label = new Label("-");
        label.getStyleClass().add("ticket-value");
        if (styleClass != null && !styleClass.isBlank()) {
            for (String style : styleClass.split("\\s+")) {
                if (!style.isBlank()) {
                    label.getStyleClass().add(style);
                }
            }
        }
        return label;
    }

    private String normalizeObservation(String observation) {
        if (observation == null || observation.isBlank()) {
            return "Sin observaciones.";
        }
        return observation.trim();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
