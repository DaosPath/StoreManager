package com.manager.storemanager;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.Category;
import com.manager.storemanager.model.Customer;
import com.manager.storemanager.model.DashboardSummary;
import com.manager.storemanager.model.DailySalesTotal;
import com.manager.storemanager.model.Product;
import com.manager.storemanager.model.Sale;
import com.manager.storemanager.model.SaleDetail;
import com.manager.storemanager.model.StockEntryItem;
import com.manager.storemanager.model.StockEntryRequest;
import com.manager.storemanager.model.StockMovement;
import com.manager.storemanager.model.Supplier;
import com.manager.storemanager.model.User;
import com.manager.storemanager.service.AuthService;
import com.manager.storemanager.service.CustomerService;
import com.manager.storemanager.service.ProductService;
import com.manager.storemanager.service.ReportService;
import com.manager.storemanager.service.SaleService;
import com.manager.storemanager.service.StockService;
import com.manager.storemanager.service.SupplierService;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public final class StoreManagerIntegrationSmokeTest {

    private StoreManagerIntegrationSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        log("Iniciando pruebas de integracion");

        AuthService authService = new AuthService();
        CustomerService customerService = new CustomerService();
        SupplierService supplierService = new SupplierService();
        ProductService productService = new ProductService();
        StockService stockService = new StockService();
        SaleService saleService = new SaleService();
        ReportService reportService = new ReportService();

        User admin = authService.login("admin", "admin123".toCharArray());
        User cashier = authService.login("cajero", "cajero123".toCharArray());
        assertTrue(admin.getId() != null, "Login admin debe devolver usuario");
        assertTrue("admin".equalsIgnoreCase(admin.getRole().getName()), "Admin debe tener rol admin");
        assertTrue(cashier.getId() != null, "Login cajero debe devolver usuario");

        assertTrue(queryInt("SELECT COUNT(*) FROM roles") == 2, "Debe haber 2 roles base");
        assertTrue(queryInt("SELECT COUNT(*) FROM productos") >= 3, "Debe haber productos semilla");
        assertTrue(queryInt("SELECT COUNT(*) FROM movimientos_inventario") >= 3, "Debe haber movimientos iniciales");

        Supplier supplier = new Supplier();
        supplier.setName("Proveedor QA");
        supplier.setPhone("3009990001");
        supplier.setEmail("qa@proveedor.test");
        supplier.setAddress("Zona industrial QA");
        supplier.setActive(true);
        supplierService.save(supplier);
        assertTrue(supplier.getId() != null, "Proveedor QA debe guardarse");

        supplier.setEmail("compras.qa@proveedor.test");
        supplier.setAddress("Zona industrial QA bodega 2");
        supplierService.update(supplier);
        Supplier savedSupplier = findSupplierByName(supplierService.findSuppliers("Proveedor QA"), "Proveedor QA");
        assertTrue(savedSupplier != null, "Proveedor QA debe encontrarse tras actualizar");
        assertTrue("compras.qa@proveedor.test".equals(savedSupplier.getEmail()), "Proveedor QA debe reflejar correo actualizado");

        Customer customer = new Customer();
        customer.setName("Cliente QA");
        customer.setPhone("3012345678");
        customer.setDocument("QA-CC-1");
        customer.setAddress("Barrio QA");
        customer.setActive(true);
        customerService.save(customer);
        assertTrue(customer.getId() != null, "Cliente QA debe guardarse");

        customer.setPhone("3010001111");
        customer.setAddress("Barrio QA Norte");
        customerService.update(customer);
        Customer savedCustomer = findCustomerByName(customerService.findCustomers("Cliente QA"), "Cliente QA");
        assertTrue(savedCustomer != null, "Cliente QA debe encontrarse tras actualizar");
        assertTrue("3010001111".equals(savedCustomer.getPhone()), "Cliente QA debe reflejar telefono actualizado");

        Category category = productService.findCategories().get(0);
        Product product = new Product();
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setCode("QA-PRD-001");
        product.setName("Producto QA");
        product.setDescription("Producto creado para pruebas de integracion");
        product.setPurchasePrice(new BigDecimal("5000.00"));
        product.setSalePrice(new BigDecimal("6900.00"));
        product.setStock(6);
        product.setMinimumStock(3);
        product.setStatus("ACTIVO");
        productService.save(product);
        assertTrue(product.getId() != null, "Producto QA debe guardarse");

        product.setSalePrice(new BigDecimal("7200.00"));
        product.setDescription("Producto QA actualizado");
        product.setStock(6);
        product.setMinimumStock(3);
        productService.update(product);
        Product productFromSearch = findProductByCode(productService.findProducts("QA-PRD-001"), "QA-PRD-001");
        assertTrue(productFromSearch != null, "Producto QA debe encontrarse");
        assertBigDecimal(productFromSearch.getSalePrice(), new BigDecimal("7200.00"), "Producto QA debe reflejar precio de venta actualizado");

        productService.updateStatus(product.getId(), "INACTIVO");
        assertTrue(productService.findProductsForSale("QA-PRD-001").isEmpty(), "Producto inactivo no debe aparecer para venta");
        productService.updateStatus(product.getId(), "ACTIVO");
        product.setStatus("ACTIVO");

        StockEntryRequest purchaseEntry = new StockEntryRequest();
        purchaseEntry.setUser(admin);
        purchaseEntry.setEntryMode("COMPRA");
        purchaseEntry.setSupplier(supplier);
        purchaseEntry.setDocumentType("FACTURA");
        purchaseEntry.setSeries("QA");
        purchaseEntry.setCorrelative("1001");
        purchaseEntry.setDestination("Almacen principal");
        purchaseEntry.setNote("Ingreso de prueba QA");
        StockEntryItem purchaseItem = new StockEntryItem();
        purchaseItem.setProduct(product);
        purchaseItem.setQuantity(3);
        purchaseItem.setUnitCost(new BigDecimal("5100.00"));
        purchaseItem.setLot("L-QA-001");
        purchaseItem.setExpirationDate(LocalDate.now().plusMonths(8));
        purchaseEntry.getItems().add(purchaseItem);
        stockService.registerStockEntry(purchaseEntry);

        Product afterPurchase = findProductByCode(productService.findProducts("QA-PRD-001"), "QA-PRD-001");
        assertTrue(afterPurchase != null, "Producto QA debe existir despues de compra");
        assertTrue(afterPurchase.getStock() == 9, "La compra debe aumentar el stock a 9");
        assertBigDecimal(afterPurchase.getPurchasePrice(), new BigDecimal("5100.00"), "La compra debe actualizar el precio de compra");
        assertTrue(queryInt("SELECT COUNT(*) FROM documentos_stock WHERE tipo_registro = 'COMPRA'") == 1, "Debe existir un documento de compra");
        assertTrue(queryInt("SELECT COUNT(*) FROM movimientos_inventario WHERE referencia_tipo = 'DOCUMENTO_STOCK' AND lote = 'L-QA-001'") == 1,
                "Debe existir un movimiento de entrada con lote");

        StockEntryRequest manualEntry = new StockEntryRequest();
        manualEntry.setUser(admin);
        manualEntry.setEntryMode("AJUSTE");
        manualEntry.setDestination("Almacen principal");
        manualEntry.setAdjustmentType("Conteo");
        manualEntry.setGeneralReason("Ajuste QA");
        manualEntry.setNote("Conteo de validacion");
        StockEntryItem manualItem = new StockEntryItem();
        manualItem.setProduct(product);
        manualItem.setQuantity(2);
        manualItem.setUnitCost(BigDecimal.ZERO);
        manualEntry.getItems().add(manualItem);
        stockService.registerStockEntry(manualEntry);

        Product afterManual = findProductByCode(productService.findProducts("QA-PRD-001"), "QA-PRD-001");
        assertTrue(afterManual.getStock() == 11, "El ajuste manual debe aumentar el stock a 11");
        assertTrue(queryInt("SELECT COUNT(*) FROM documentos_stock WHERE tipo_registro = 'AJUSTE'") == 2,
                "Deben existir documentos de ajuste contando la carga inicial y el ajuste QA");

        Product soda = findProductByCode(productService.findProducts("PRD-002"), "PRD-002");
        assertTrue(soda != null, "Debe existir producto base PRD-002");

        SaleDetail detailOne = new SaleDetail();
        detailOne.setProduct(product);
        detailOne.setQuantity(4);
        detailOne.setUnitPrice(product.getSalePrice());

        SaleDetail detailTwo = new SaleDetail();
        detailTwo.setProduct(soda);
        detailTwo.setQuantity(1);
        detailTwo.setUnitPrice(soda.getSalePrice());

        Sale preparedSale = saleService.prepareSale(admin, customer, List.of(detailOne, detailTwo), "Efectivo", "Venta QA");
        Long saleId = saleService.createSale(preparedSale);
        assertTrue(saleId != null, "La venta debe persistirse");
        assertTrue(queryInt("SELECT COUNT(*) FROM detalle_ventas WHERE venta_id = " + saleId) == 2, "La venta debe guardar 2 lineas");
        assertTrue(queryInt("SELECT COUNT(*) FROM movimientos_inventario WHERE referencia_tipo = 'VENTA' AND referencia_id = " + saleId) == 2,
                "La venta debe generar 2 movimientos de salida");

        Product afterSale = findProductByCode(productService.findProducts("QA-PRD-001"), "QA-PRD-001");
        Product sodaAfterSale = findProductByCode(productService.findProducts("PRD-002"), "PRD-002");
        assertTrue(afterSale.getStock() == 7, "La venta debe bajar el stock del producto QA a 7");
        assertTrue(sodaAfterSale.getStock() == 23, "La venta debe bajar el stock de PRD-002 a 23");

        product.setStock(7);
        product.setMinimumStock(8);
        product.setPurchasePrice(afterSale.getPurchasePrice());
        product.setSalePrice(afterSale.getSalePrice());
        productService.update(product);

        List<Product> lowStock = productService.findLowStockProducts();
        assertTrue(lowStock.stream().anyMatch(item -> "QA-PRD-001".equals(item.getCode())), "Producto QA debe aparecer en stock bajo");

        List<Sale> todaySales = saleService.findTodaySales();
        assertTrue(todaySales.stream().anyMatch(sale -> saleId.equals(sale.getId())), "La venta QA debe aparecer en ventas del dia");

        List<DailySalesTotal> dailyTotals = reportService.findDailyTotals(LocalDate.now().minusDays(1), LocalDate.now());
        assertTrue(!dailyTotals.isEmpty(), "Los totales diarios no deben venir vacios");

        DashboardSummary summary = reportService.loadDashboardSummary();
        assertTrue(summary.getProductCount() >= 4, "El dashboard debe contar al menos 4 productos activos");
        assertTrue(summary.getSupplierCount() >= 3, "El dashboard debe contar al menos 3 proveedores activos antes de desactivar QA");
        assertTrue(summary.getCustomerCount() >= 3, "El dashboard debe contar al menos 3 clientes activos antes de desactivar QA");

        customer.setActive(false);
        customerService.update(customer);
        assertTrue(customerService.findCustomers("Cliente QA").isEmpty(), "Cliente QA no debe aparecer activo tras desactivarlo");

        supplierService.deactivate(supplier.getId());
        assertTrue(supplierService.findSuppliers("Proveedor QA").isEmpty(), "Proveedor QA no debe aparecer activo tras desactivarlo");

        List<StockMovement> movements = stockService.findMovements();
        assertTrue(movements.stream().anyMatch(movement -> "DOCUMENTO_STOCK".equals(movement.getReferenceType())), "Debe haber movimientos de documento de stock");
        assertTrue(movements.stream().anyMatch(movement -> "VENTA".equals(movement.getReferenceType())), "Debe haber movimientos de venta");

        assertTrue(queryInt("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'documentos_stock'") > 0,
                "La tabla documentos_stock debe existir");
        assertTrue(queryInt("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'movimientos_inventario' AND COLUMN_NAME = 'referencia_tipo'") == 1,
                "movimientos_inventario debe tener referencia_tipo");

        log("Todas las pruebas pasaron");
    }

    private static Product findProductByCode(List<Product> products, String code) {
        return products.stream().filter(product -> code.equals(product.getCode())).findFirst().orElse(null);
    }

    private static Customer findCustomerByName(List<Customer> customers, String name) {
        return customers.stream().filter(customer -> name.equals(customer.getName())).findFirst().orElse(null);
    }

    private static Supplier findSupplierByName(List<Supplier> suppliers, String name) {
        return suppliers.stream().filter(supplier -> name.equals(supplier.getName())).findFirst().orElse(null);
    }

    private static int queryInt(String sql) throws Exception {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void assertBigDecimal(BigDecimal actual, BigDecimal expected, String message) {
        if (actual == null || actual.compareTo(expected) != 0) {
            throw new IllegalStateException(message + " (esperado=" + expected + ", actual=" + actual + ")");
        }
    }

    private static void log(String message) {
        System.out.println("[TEST] " + message);
    }
}
