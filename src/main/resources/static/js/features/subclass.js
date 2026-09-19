// ==============================
// FILE: /js/features/subclass.js
// ==============================
import { $, isRAW } from '../core.js';
import { loadSubclasses } from '../api.js';

/**
 * Clears a select's value, but only dispatches a 'change' event if it
 * actually had a value to clear. Setting .value in JS fires no event of
 * its own, and this runs on every keystroke of the level field (via
 * refreshSubclassUI below) - dispatching unconditionally would fire a
 * 'change' (and, downstream, a PDF preview refresh) on every keystroke
 * even when the select was already empty, defeating the point of using
 * 'change' instead of 'input' in the first place.
 */
function clearSelectValue(select) {
  if (!select || select.value === '') return;
  select.value = '';
  select.dispatchEvent(new Event('change', { bubbles: true }));
}

/**
 * Shows/hides and enables/disables the subclass group based on whether a
 * class is selected and, in RAW mode, whether the character is level 3+.
 * (In Freedom mode there's no level gate.)
 */
export function refreshSubclassUI() {
  const classEl = $('#characterClass');
  const levelEl = $('#characterLevel');
  const subclassGroup = $('#subclassGroup');
  const subclassSelect = $('#subclassName');
  const help = $('#subclassHelp');

  const hasClass = !!classEl?.value;
  const level = parseInt(levelEl?.value || '1', 10) || 1;

  // No class chosen → hide entirely
  if (!hasClass) {
    if (subclassGroup) subclassGroup.hidden = true;
    if (subclassSelect) { subclassSelect.disabled = true; clearSelectValue(subclassSelect); }
    if (help) help.textContent = 'Pick a class to see subclasses.';
    return;
  }

  // Gate by level (only enforced in RAW mode)
  if (isRAW() && level < 3) {
    if (subclassGroup) subclassGroup.hidden = true;
    if (subclassSelect) { subclassSelect.disabled = true; clearSelectValue(subclassSelect); }
    if (help) help.textContent = 'Subclass becomes available at level 3.';
    return;
  }

  // Class picked and (RAW: level ≥3, or Freedom: any level) → show + enable
  if (subclassGroup) subclassGroup.hidden = false;
  if (subclassSelect) subclassSelect.disabled = false;
  if (help) help.textContent = 'Choose a subclass.';
}

/** Wires the class/level inputs to keep subclass data and visibility in sync. Call once at boot. */
export function wireSubclassUI() {
  const classEl = $('#characterClass');
  const levelEl = $('#characterLevel');

  levelEl?.addEventListener('input', refreshSubclassUI);
  levelEl?.addEventListener('change', refreshSubclassUI);

  classEl?.addEventListener('change', async (e) => {
    // Subclasses are tied to the selected class in both modes — RAW's
    // level-3 gate is enforced separately by refreshSubclassUI().
    await loadSubclasses(e.target.value || null);
    refreshSubclassUI();
  });
}
