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


