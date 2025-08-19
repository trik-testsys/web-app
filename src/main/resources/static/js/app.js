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
    var links = document.querySelectorAll('.app-sidebar .menu-item a');
    links.forEach(function (a) {
      var hrefPath;
      try {
        hrefPath = new URL(a.getAttribute('href'), window.location.origin).pathname;
      } catch (_) {
        hrefPath = a.getAttribute('href') || '';
      }
      if (!hrefPath) return;
      var isActive = (currentPath === hrefPath);
      if (isActive) {
        a.classList.add('active');
        var section = a.closest('.menu-section');
        if (section) section.classList.add('active');
      }
    });
  } catch (_) {
    // no-op
  }
}
