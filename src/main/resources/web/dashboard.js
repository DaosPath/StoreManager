function renderDashboard(data) {
  document.getElementById("products").textContent = data.products;
  document.getElementById("customers").textContent = data.customers;
  document.getElementById("suppliers").textContent = data.suppliers;
  document.getElementById("lowStock").textContent = data.lowStock;
  document.getElementById("sales").textContent = data.sales;

  document.getElementById("factsCustomers").textContent = data.customers;
  document.getElementById("factsProducts").textContent = data.products;
  document.getElementById("factsSuppliers").textContent = data.suppliers;
  document.getElementById("factsSuppliersSecondary").textContent = data.suppliers;

  const overviewTotal = data.products + data.customers + data.suppliers + data.lowStock;
  document.getElementById("overviewTotal").textContent = overviewTotal;

  document.getElementById("summaryProducts").textContent = `Catalogo (${data.products} productos)`;
  document.getElementById("summaryCustomers").textContent = `Base de clientes (${data.customers} registros)`;
  document.getElementById("summarySuppliers").textContent = `Abastecimiento (${data.suppliers} proveedores)`;

  const hasSales = data.sales !== "$0,00" && data.sales !== "$0.00";
  document.getElementById("salesMessage").textContent = hasSales
    ? "Hay ventas registradas en la jornada actual."
    : "Todavia no hay ventas registradas hoy.";

  const stockFlag = document.getElementById("stockFlag");
  const stockMessage = document.getElementById("stockMessage");
  if (data.lowStock > 0) {
    stockFlag.textContent = "Atencion";
    stockFlag.className = "tag tag-warning";
    stockMessage.textContent = `${data.lowStock} productos requieren reposicion.`;
  } else {
    stockFlag.textContent = "Normal";
    stockFlag.className = "tag tag-warning";
    stockMessage.textContent = "No hay productos con reposicion urgente.";
  }

  document.getElementById("checkSales").textContent = hasSales
    ? "Revisar tickets emitidos y validar el cierre de caja."
    : "Verificar apertura de caja y registro de ventas del turno.";

  document.getElementById("checkStock").textContent = data.lowStock > 0
    ? `Priorizar la compra de ${data.lowStock} productos con stock bajo.`
    : "El nivel de stock se mantiene estable por ahora.";

  document.getElementById("checkCatalog").textContent = data.products === 0
    ? "Cargar productos antes de habilitar la operacion comercial."
    : "Mantener catalogo, clientes y proveedores consistentes.";
}
