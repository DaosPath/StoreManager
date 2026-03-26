function renderReport(data) {
  document.getElementById("reportTitle").textContent = data.title;
  document.getElementById("reportSubtitle").textContent = data.subtitle;
  document.getElementById("reportBadge").textContent = data.badge;
  document.getElementById("reportNote").textContent = data.note;

  const table = document.getElementById("reportTable");
  const head = document.getElementById("tableHead");
  const body = document.getElementById("tableBody");
  const emptyState = document.getElementById("emptyState");

  head.innerHTML = "";
  body.innerHTML = "";

  const headRow = document.createElement("tr");
  data.columns.forEach((column) => {
    const th = document.createElement("th");
    th.textContent = column;
    headRow.appendChild(th);
  });
  head.appendChild(headRow);

  if (!data.rows || data.rows.length === 0) {
    table.hidden = true;
    emptyState.hidden = false;
    emptyState.textContent = data.emptyMessage;
    return;
  }

  data.rows.forEach((row) => {
    const tr = document.createElement("tr");
    row.forEach((cell) => {
      const td = document.createElement("td");
      td.textContent = cell;
      tr.appendChild(td);
    });
    body.appendChild(tr);
  });

  emptyState.hidden = true;
  table.hidden = false;
}
