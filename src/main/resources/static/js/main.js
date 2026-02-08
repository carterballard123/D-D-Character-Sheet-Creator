// ==============================
// FILE: /js/main.js
// ==============================
// ---- BOOT DIAGNOSTICS (temporary) ----
console.log('[boot] main.js loaded; document.readyState =', document.readyState);

window.addEventListener('error', (e) => {
  console.error('[global error]', e.error || e.message || e);
  const m = document.getElementById('msg');
  if (m) m.textContent = 'JS error: ' + (e.error?.message || e.message || e);
});

window.addEventListener('unhandledrejection', (e) => {
  console.error('[unhandledrejection]', e.reason);
  const m = document.getElementById('msg');
  if (m) m.textContent = 'Promise error: ' + (e.reason?.message || e.reason);
});


import { $, isRAW } from './core.js';
import { loadClasses, loadRaces, loadBackgrounds, loadArmors, loadShields, loadAlignments, loadSubclasses, loadLanguages, loadSkills } from './api.js';
import { refreshPills } from './ui/pills.js';
import { wireSubclassUI, refreshSubclassUI } from './features/subclass.js';
import { initAbilityUI, refreshAbilityModeUI } from './features/abilities.js';
import { wireFormSubmit } from './form.js';
console.log('[boot] main.js loaded');

async function init() {
	console.log('[boot] init() starting');
try {
await Promise.all([loadClasses(), loadRaces(), loadBackgrounds(), loadArmors(), loadShields(), loadAlignments()]);
await loadSubclasses(isRAW() ? $('#characterClass')?.value || null : null);
refreshSubclassUI();


await Promise.all([loadLanguages(), loadSkills()]);
refreshPills();
refreshSubclassUI();


// wiring
wireSubclassUI();
initAbilityUI();
refreshAbilityModeUI();


// update pills when these change
$('#characterRace')?.addEventListener('change', refreshPills);
$('#characterBackground')?.addEventListener('change', refreshPills);
document.querySelectorAll('input[name="mode"]').forEach((r) => {
r.addEventListener('change', async () => {
await loadSubclasses(isRAW() ? $('#characterClass')?.value : null);
refreshPills();
refreshSubclassUI();
});
});


wireFormSubmit();
} catch (e) {
console.error(e);
const { setMsg } = await import('./core.js');
setMsg('err', 'Failed to load reference data: ' + (e.message || e));
}
}


// kick off once DOM is ready
if (document.readyState === 'loading') {
document.addEventListener('DOMContentLoaded', init);
} else {
init();
}