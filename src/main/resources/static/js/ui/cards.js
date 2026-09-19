// ==============================
// FILE: /js/ui/cards.js
// ==============================

/**
 * Replaces a <select>'s visible UI with a clickable card grid, while
 * keeping the <select> itself as the real source of truth. The select
 * is visually hidden (not display:none — it stays usable via keyboard
 * or screen reader), and every card click just sets its value and
 * fires a real 'change' event, so any existing listeners on the select
 * keep working unmodified.
 *
 * @param {HTMLSelectElement} selectEl
 * @param {Array} items
 * @param {Object} opts
 * @param {(item) => string} opts.valueFor - the option value for an item
 * @param {(item) => string} opts.labelFor - the card's main title text
 * @param {(item) => string} [opts.subtitleFor] - optional secondary text
 */
export function renderCardPicker(selectEl, items, { valueFor, labelFor, subtitleFor } = {}) {
  if (!selectEl) return;

  selectEl.classList.add('sr-only');

  const grid = document.createElement('div');
  grid.className = 'card-picker';
  selectEl.insertAdjacentElement('afterend', grid);

  function render() {
    const current = selectEl.value;
    grid.innerHTML = items.map((item) => {
      const value = valueFor(item);
      const active = value === current;
      const subtitle = subtitleFor ? subtitleFor(item) : '';
      return `
        <button type="button" class="card-picker-item${active ? ' active' : ''}"
                data-value="${value}" aria-pressed="${active}">
          <div class="card-picker-title">${labelFor(item)}</div>
          ${subtitle ? `<div class="card-picker-subtitle">${subtitle}</div>` : ''}
        </button>
      `;
    }).join('');
  }

  // Single source of truth: whatever changed the select's value — a card
  // click below, or the native select itself — this is the one place
  // that reacts and redraws.
  selectEl.addEventListener('change', render);

  grid.addEventListener('click', (e) => {
    const btn = e.target.closest('.card-picker-item');
    if (!btn) return;
    selectEl.value = btn.dataset.value;
    selectEl.dispatchEvent(new Event('change', { bubbles: true }));
  });

  render(); // initial paint
}
