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

  return { toggleSidebar, toggleEdit, mount };
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
