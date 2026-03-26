function renderTicket(data) {
  document.getElementById("saleNumber").textContent = data.saleNumber;
  document.getElementById("saleDate").textContent = data.date;
  document.getElementById("cashierName").textContent = data.cashier;
  document.getElementById("customerName").textContent = data.customer;
  document.getElementById("paymentMethod").textContent = data.paymentMethod;
  document.getElementById("saleStatus").textContent = data.status;
  document.getElementById("subtotalValue").textContent = data.subtotal;
  document.getElementById("taxValue").textContent = data.tax;
  document.getElementById("totalValue").textContent = data.total;
  document.getElementById("observationText").textContent = data.observation;

  const body = document.getElementById("ticketBody");
  body.innerHTML = "";

  data.items.forEach((item) => {
    const row = document.createElement("tr");
    item.forEach((cell) => {
      const td = document.createElement("td");
      td.textContent = cell;
      row.appendChild(td);
    });
    body.appendChild(row);
  });
}
