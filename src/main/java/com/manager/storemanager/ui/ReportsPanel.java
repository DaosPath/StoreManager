package com.manager.storemanager.ui;

import com.manager.storemanager.config.AppConfig;
import com.manager.storemanager.model.DailySalesTotal;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.service.SaleService;
import com.manager.storemanager.util.CurrencyUtils;
import com.manager.storemanager.util.MessageUtils;
import com.manager.storemanager.util.TableUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class ReportsPanel extends JPanel implements RefreshableView {

    private final ProductService productService;
    private final SaleService saleService;
    private final ReportService reportService;
    private JTable table;
    private JTextField fromField;
    private JTextField toField;

    public ReportsPanel(ProductService productService, SaleService saleService, ReportService reportService) {
        this.productService = productService;
        this.saleService = saleService;
        this.reportService = reportService;
        setLayout(new BorderLayout(16, 16));
        setBackground(UIConstants.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UIConstants.titleLabel("Reportes"), BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setOpaque(false);
        fromField = new JTextField(LocalDate.now().minusDays(7).format(AppConfig.DATE_FORMATTER), 10);
        toField = new JTextField(LocalDate.now().format(AppConfig.DATE_FORMATTER), 10);
        JButton todayButton = new JButton("Ventas del día");
        JButton rangeButton = new JButton("Ventas por rango");
        JButton lowStockButton = new JButton("Bajo stock");
        JButton productButton = new JButton("Productos");
        JButton totalsButton = new JButton("Total por día");

        UIConstants.styleSecondaryButton(todayButton);
        UIConstants.styleSecondaryButton(rangeButton);
        UIConstants.styleSecondaryButton(lowStockButton);
        UIConstants.styleSecondaryButton(productButton);
        UIConstants.stylePrimaryButton(totalsButton);

        todayButton.addActionListener(event -> showTodaySales());
        rangeButton.addActionListener(event -> showSalesByRange());
        lowStockButton.addActionListener(event -> showLowStock());
        productButton.addActionListener(event -> showProducts());
        totalsButton.addActionListener(event -> showDailyTotals());

        controls.add(fromField);
        controls.add(toField);
        controls.add(todayButton);
        controls.add(rangeButton);
        controls.add(lowStockButton);
        controls.add(productButton);
        controls.add(totalsButton);
        top.add(controls, BorderLayout.EAST);

        table = new JTable();
        TableUtils.configureTable(table);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void reloadData() {
        showTodaySales();
    }

    private void showTodaySales() {
        try {
            List<Sale> sales = saleService.findTodaySales();
            DefaultTableModel model = TableUtils.nonEditableModel("ID", "Fecha", "Cajero", "Cliente", "Pago", "Total");
            for (Sale sale : sales) {
                model.addRow(new Object[]{
                    sale.getId(),
                    sale.getSaleDate() == null ? "" : sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER),
                    sale.getUser().getFullName(),
                    sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName(),
                    sale.getPaymentMethod(),
                    CurrencyUtils.format(sale.getTotal())
                });
            }
            table.setModel(model);
            TableUtils.configureTable(table);
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible generar el reporte.\n" + exception.getMessage());
        }
    }

    private void showSalesByRange() {
        try {
            LocalDate from = LocalDate.parse(fromField.getText().trim(), AppConfig.DATE_FORMATTER);
            LocalDate to = LocalDate.parse(toField.getText().trim(), AppConfig.DATE_FORMATTER);
            List<Sale> sales = saleService.findSalesByDateRange(from, to);
            DefaultTableModel model = TableUtils.nonEditableModel("ID", "Fecha", "Cajero", "Cliente", "Pago", "Subtotal", "Impuesto", "Total");
            for (Sale sale : sales) {
                model.addRow(new Object[]{
                    sale.getId(),
                    sale.getSaleDate() == null ? "" : sale.getSaleDate().format(AppConfig.DATE_TIME_FORMATTER),
                    sale.getUser().getFullName(),
                    sale.getCustomer() == null ? "Mostrador" : sale.getCustomer().getName(),
                    sale.getPaymentMethod(),
                    CurrencyUtils.format(sale.getSubtotal()),
                    CurrencyUtils.format(sale.getTax()),
                    CurrencyUtils.format(sale.getTotal())
                });
            }
            table.setModel(model);
            TableUtils.configureTable(table);
        } catch (Exception exception) {
            MessageUtils.showError(this, "Revise las fechas o el acceso a datos.\n" + exception.getMessage());
        }
    }

    private void showLowStock() {
        try {
            List<Product> products = productService.findLowStockProducts();
            DefaultTableModel model = TableUtils.nonEditableModel("Código", "Producto", "Categoría", "Stock", "Mínimo", "Estado");
            for (Product product : products) {
                model.addRow(new Object[]{
                    product.getCode(),
                    product.getName(),
                    product.getCategory().getName(),
                    product.getStock(),
                    product.getMinimumStock(),
                    product.getStatus()
                });
            }
            table.setModel(model);
            TableUtils.configureTable(table);
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible generar el reporte.\n" + exception.getMessage());
        }
    }

    private void showProducts() {
        try {
            List<Product> products = productService.findProducts("");
            DefaultTableModel model = TableUtils.nonEditableModel("Código", "Producto", "Categoría", "Venta", "Stock", "Estado");
            for (Product product : products) {
                model.addRow(new Object[]{
                    product.getCode(),
                    product.getName(),
                    product.getCategory().getName(),
                    CurrencyUtils.format(product.getSalePrice()),
                    product.getStock(),
                    product.getStatus()
                });
            }
            table.setModel(model);
            TableUtils.configureTable(table);
        } catch (SQLException exception) {
            MessageUtils.showError(this, "No fue posible generar el reporte.\n" + exception.getMessage());
        }
    }

    private void showDailyTotals() {
        try {
            LocalDate from = LocalDate.parse(fromField.getText().trim(), AppConfig.DATE_FORMATTER);
            LocalDate to = LocalDate.parse(toField.getText().trim(), AppConfig.DATE_FORMATTER);
            List<DailySalesTotal> totals = reportService.findDailyTotals(from, to);
            DefaultTableModel model = TableUtils.nonEditableModel("Fecha", "Total vendido");
            for (DailySalesTotal row : totals) {
                model.addRow(new Object[]{
                    row.getDate().format(AppConfig.DATE_FORMATTER),
                    CurrencyUtils.format(row.getTotal())
                });
            }
            table.setModel(model);
            TableUtils.configureTable(table);
        } catch (Exception exception) {
            MessageUtils.showError(this, "Revise las fechas o el acceso a datos.\n" + exception.getMessage());
        }
    }
}
