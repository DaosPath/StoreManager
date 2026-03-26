const presetButtons = Array.from(document.querySelectorAll(".report-preset"));
const presetWrap = document.getElementById("reportPresets");

for (const button of presetButtons) {
  button.addEventListener("click", () => {
    const preset = button.dataset.preset || "";
    if (window.reportActions && typeof window.reportActions.applyPreset === "function") {
      window.reportActions.applyPreset(preset);
    }
  });
}

function renderReport(data) {
  document.getElementById("reportChip").textContent = data.chip || "Reporte";
  document.getElementById("reportTitle").textContent = data.title || "Reporte";
  document.getElementById("reportSubtitle").textContent = data.subtitle || "";
  document.getElementById("reportCount").textContent = data.summaryCount || "0";
  document.getElementById("reportSummaryText").textContent = data.summaryText || "registros encontrados.";
  document.getElementById("reportNote").textContent = data.note || "";
  presetWrap.hidden = !data.showPresets;
  const activePreset = data.activePreset || "";
  for (const button of presetButtons) {
    button.classList.toggle("report-preset-active", button.dataset.preset === activePreset);
  }

  const tableWrap = document.getElementById("tableWrap");
  const table = document.getElementById("reportTable");
  const head = document.getElementById("tableHead");
  const body = document.getElementById("tableBody");
  const emptyState = document.getElementById("emptyState");
  const emptyTitle = document.getElementById("emptyTitle");
  const emptyMessage = document.getElementById("emptyMessage");
  const emptyAction = document.getElementById("emptyAction");

  head.innerHTML = "";
  body.innerHTML = "";

  const headerRow = document.createElement("tr");
  (data.columns || []).forEach((column) => {
    const th = document.createElement("th");
    th.textContent = column;
    headerRow.appendChild(th);
  });
  head.appendChild(headerRow);

  const rows = data.rows || [];
  if (rows.length === 0) {
    table.hidden = true;
    tableWrap.hidden = true;
    emptyState.hidden = false;
    emptyTitle.textContent = data.emptyTitle || "No hay datos para mostrar";
    emptyMessage.textContent = data.emptyMessage || "No se encontraron registros para la vista seleccionada.";
    if (data.emptyActionLabel && data.emptyActionLabel.trim()) {
      emptyAction.hidden = false;
      emptyAction.textContent = data.emptyActionLabel;
    } else {
      emptyAction.hidden = true;
    }
    return;
  }

  rows.forEach((row) => {
    const tr = document.createElement("tr");
    row.forEach((cell) => {
      const td = document.createElement("td");
      td.textContent = cell;
      tr.appendChild(td);
    });
    body.appendChild(tr);
  });

  emptyState.hidden = true;
  tableWrap.hidden = false;
  table.hidden = false;
}
