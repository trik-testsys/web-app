// Lightweight, dependency-free table sorter
(function () {
  // --- Utilities ---
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

  // --- Filtering ---
  function createFilterInput(placeholder) {
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'table-filter-input';
    input.placeholder = placeholder || 'Фильтр';
    input.setAttribute('aria-label', 'Фильтр');
    return input;
  }

  function buildFilterRow(table) {
    const thead = table.tHead;
    const tbody = table.tBodies[0];
    if (!thead || !tbody) return;

    const headerRow = thead.rows[0];
    if (!headerRow) return;

    // Avoid duplicating filter-row
    if (thead.querySelector('tr.table-filter-row')) return;

    const filterRow = document.createElement('tr');
    filterRow.className = 'table-filter-row';

    const filters = [];
    Array.from(headerRow.cells).forEach(function (th, colIndex) {
      const filterCell = document.createElement('th');
      const noFilter = th.hasAttribute('data-no-filter') || th.classList.contains('no-filter');
      if (!noFilter) {
        const input = createFilterInput('Фильтр…');
        filters.push({ colIndex: colIndex, input: input });
        filterCell.appendChild(input);
      } else {
        filterCell.appendChild(document.createTextNode(''));
      }
      filterRow.appendChild(filterCell);
    });

    thead.appendChild(filterRow);
    table.classList.add('has-filter');

    // Ensure sticky stacking: place filter row below header row
    function setFilterOffsets() {
      try {
        const headerHeight = headerRow.offsetHeight || 0;
        const cells = Array.from(filterRow.cells);
        cells.forEach(function (cell) {
          cell.style.top = headerHeight + 'px';
        });
      } catch (_) { /* no-op */ }
    }
    setFilterOffsets();
    window.addEventListener('resize', setFilterOffsets, { passive: true });
    table.__updateFilterOffsets = setFilterOffsets;

    function applyFilter() {
      const activeFilters = filters
        .map(function (f) { return { colIndex: f.colIndex, value: (f.input.value || '').trim().toLowerCase() }; })
        .filter(function (f) { return f.value.length > 0; });

      const rows = Array.from(tbody.rows);
      if (activeFilters.length === 0) {
        rows.forEach(function (row) { row.style.display = ''; });
        updateEmptyState(table, false);
        return;
      }

      let visibleCount = 0;
      rows.forEach(function (row) {
        const matches = activeFilters.every(function (f) {
          const cell = row.children[f.colIndex];
          if (!cell) return false;
          const text = (cell.textContent || '').toLowerCase();
          return text.indexOf(f.value) !== -1;
        });
        row.style.display = matches ? '' : 'none';
        if (matches) visibleCount += 1;
      });

      updateEmptyState(table, visibleCount === 0);
    }

    // Bind events
    filters.forEach(function (f) {
      f.input.addEventListener('input', applyFilter);
      f.input.addEventListener('change', applyFilter);
    });

    // Expose for testing if needed
    table.__applyFilter = applyFilter;
  }

  function updateEmptyState(table, isEmpty) {
    // Optionally toggle a class when no rows match
    if (isEmpty) table.classList.add('filter-empty');
    else table.classList.remove('filter-empty');
  }

  // --- Toggle visibility ---
  function insertFilterToggle(table) {
    // Do not add toggle if no filters for this table
    if (!table.tHead || !table.tHead.querySelector('tr.table-filter-row')) return;
    const container = document.createElement('div');
    container.className = 'table-tools';

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'btn table-filter-toggle';

    function setButtonText(hidden) {
      btn.textContent = hidden ? 'Показать фильтры' : 'Скрыть фильтры';
      btn.setAttribute('aria-pressed', String(!hidden));
    }

    function isHidden() { return table.classList.contains('filters-hidden'); }

    btn.addEventListener('click', function () {
      const hidden = !isHidden();
      table.classList.toggle('filters-hidden', hidden);
      setButtonText(hidden);
    });

    setButtonText(isHidden());
    container.appendChild(btn);

    // Insert before table
    const parent = table.parentNode;
    if (parent) parent.insertBefore(container, table);
  }

  function init() {
    // All tables
    const tables = Array.from(document.querySelectorAll('table'));
    tables.forEach(function (table) {
      makeTableSortable(table);
      // If table has explicit opt-out attribute, skip filters
      const noFilter = table.hasAttribute('data-no-filter') || table.classList.contains('no-filter');
      if (!noFilter) {
        buildFilterRow(table);

        // Visibility defaults: hidden by default unless explicitly made visible
        let defaultHidden = true;
        if (table.hasAttribute('data-filter-default-visible') || table.classList.contains('filters-visible')) {
          defaultHidden = false;
        }
        if (table.hasAttribute('data-filter-default-hidden') || table.classList.contains('filters-hidden')) {
          defaultHidden = true;
        }
        if (defaultHidden) {
          table.classList.add('filters-hidden');
        }

        insertFilterToggle(table);
      }

      // Add subtle shadow to sticky headers when body scrolled
      try {
        const tbody = table.tBodies && table.tBodies[0];
        if (tbody) {
          function updateShadow() {
            if (tbody.scrollTop > 0) table.classList.add('scrolled');
            else table.classList.remove('scrolled');
          }
          tbody.addEventListener('scroll', updateShadow, { passive: true });
          updateShadow();
        }
      } catch (_) { /* no-op */ }
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();