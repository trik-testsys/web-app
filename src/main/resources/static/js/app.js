window.App = (function () {
  const sidebar = () => document.getElementById('app-sidebar');
  function toggleSidebar() {
    const el = sidebar();
    if (!el) return;
    el.classList.toggle('open');
  }

  function closeSidebarOnNavigate() {
    document.addEventListener('click', (e) => {
      const el = sidebar();
      if (!el || !el.classList.contains('open')) return;
      const target = e.target;
      if (target.closest && target.closest('#app-sidebar')) return;
      if (target.tagName === 'A') el.classList.remove('open');
    });
  }

  function mount() {
    closeSidebarOnNavigate();
    markActiveNavigation();
    initReadonlyForms();
  }

  function toggleEdit(field) {
    const view = document.getElementById(`${field}-view`);
    const form = document.getElementById(`${field}-form`);
    if (!view || !form) return;
    const isHidden = getComputedStyle(form).display === 'none';
    if (isHidden) {
      form.style.display = 'flex';
      view.style.display = 'none';
      const input = form.querySelector('input, textarea');
      if (input) input.focus();
    } else {
      form.style.display = 'none';
      view.style.display = '';
    }
  }

  function setFormEnabled(formId, enabled) {
    const form = document.getElementById(formId);
    if (!form) return;
    const inputs = form.querySelectorAll('input, textarea, select');
    inputs.forEach(el => { el.disabled = !enabled; });
    const editBtn = form.querySelector('.edit-button');
    const saveBtn = form.querySelector('.save-button');
    const cancelBtn = form.querySelector('.cancel-button');
    if (editBtn) editBtn.style.display = enabled ? 'none' : 'inline-block';
    if (saveBtn) saveBtn.style.display = enabled ? 'inline-block' : 'none';
    if (cancelBtn) cancelBtn.style.display = enabled ? 'inline-block' : 'none';
  }

  function enableForm(formId) { setFormEnabled(formId, true); }

  function disableForm(formId) {
    const form = document.getElementById(formId);
    if (form) { try { form.reset(); } catch (_) {} }
    setFormEnabled(formId, false);
    setDisabledVisualState(form);
  }

  function setDisabledVisualState(form) {
    if (!form) return;
    const fields = form.querySelectorAll('input, textarea');
    fields.forEach((el) => {
      el.classList.remove('has-value', 'is-empty');
      if (el.disabled) {
        const hasValue = (el.value || '').trim().length > 0;
        el.classList.add(hasValue ? 'has-value' : 'is-empty');
      }
    });
  }

  function initReadonlyForms() {
    const forms = document.querySelectorAll('form[data-readonly-toggle]');
    forms.forEach((form) => setDisabledVisualState(form));
  }

  return { toggleSidebar, toggleEdit, enableForm, disableForm, mount, initReadonlyForms };
})();

document.addEventListener('DOMContentLoaded', () => {
  window.App.mount();
});



function markActiveNavigation() {
  try {
    var currentPath = window.location.pathname;
    var links = Array.from(document.querySelectorAll('.app-sidebar .menu-item a'));
    if (!links.length) return;

    var bestLink = null;
    var bestLen = -1;

    links.forEach(function (a) {
      var hrefPath;
      try {
        hrefPath = new URL(a.getAttribute('href'), window.location.origin).pathname;
      } catch (_) {
        hrefPath = a.getAttribute('href') || '';
      }
      if (!hrefPath) return;
      var matches = (currentPath === hrefPath) || (hrefPath !== '/' && currentPath.indexOf(hrefPath + '/') === 0);
      if (matches && hrefPath.length > bestLen) {
        bestLink = a;
        bestLen = hrefPath.length;
      }
    });

    // Reset existing states to avoid multiple actives
    links.forEach(function (a) { a.classList.remove('active'); });
    Array.from(document.querySelectorAll('.app-sidebar .menu-section')).forEach(function (s) { s.classList.remove('active'); });

    if (bestLink) {
      bestLink.classList.add('active');
      var section = bestLink.closest('.menu-section');
      if (section) section.classList.add('active');
    }
  } catch (_) {
    // no-op
  }
}
