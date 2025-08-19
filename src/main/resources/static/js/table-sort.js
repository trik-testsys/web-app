// Lightweight, dependency-free table sorter
(function () {
  function isNumeric(value) {
    if (value === null || value === undefined) return false;
    const n = parseFloat(value.replace?.(/\s+/g, '') ?? value);
    return !Number.isNaN(n) && Number.isFinite(n);
  }

  function getCellText(row, index) {
    const cell = row.children[index];
    if (!cell) return '';
    return (cell.textContent || '').trim();
  }

  function compareFactory(index, asc) {
    return function (rowA, rowB) {
      const a = getCellText(rowA, index);
      const b = getCellText(rowB, index);

      const aNum = isNumeric(a) ? parseFloat(a.replace(/\s+/g, '')) : null;
      const bNum = isNumeric(b) ? parseFloat(b.replace(/\s+/g, '')) : null;

      let result;
      if (aNum !== null && bNum !== null) {
        result = aNum - bNum;
      } else {
        result = a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' });
      }
      return asc ? result : -result;
    };
  }

  function clearSortIndicators(thElements) {
    thElements.forEach(function (th) {
      th.classList.remove('sort-asc');
      th.classList.remove('sort-desc');
    });
  }

  function makeTableSortable(table) {
    const thead = table.tHead;
    const tbody = table.tBodies[0] || table.createTBody();
    if (!thead || !tbody) return;

    const headerRow = thead.rows[0];
    if (!headerRow) return;

    const ths = Array.from(headerRow.cells);
    ths.forEach(function (th, index) {
      th.classList.add('sortable');
      th.addEventListener('click', function () {
        const asc = !(th.dataset.sortAsc === 'true');
        const rows = Array.from(tbody.rows);

        rows.sort(compareFactory(index, asc));

        // Re-attach sorted rows
        const frag = document.createDocumentFragment();
        rows.forEach(function (r) { frag.appendChild(r); });
        tbody.appendChild(frag);

        // Update indicators
        clearSortIndicators(ths);
        th.dataset.sortAsc = String(asc);
        th.classList.add(asc ? 'sort-asc' : 'sort-desc');
      });
    });
  }

  function init() {
    // All tables, but prefer those explicitly marked with class "table"
    const tables = Array.from(document.querySelectorAll('table'));
    tables.forEach(makeTableSortable);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();