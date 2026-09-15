// ==============================
// FILE: /js/main.js
// ==============================

// Surface any otherwise-silent JS errors in the status bar, since a
// failure here would normally just be a blank/broken page with nothing
// in the UI to explain why.
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

import { $, setMsg } from './core.js';
import { loadClasses, loadRaces, loadBackgrounds, loadArmors, loadShields, loadAlignments, loadSubclasses, loadLanguages, loadSkills } from './api.js';
import { refreshPills } from './ui/pills.js';
import { wireSubclassUI, refreshSubclassUI } from './features/subclass.js';
import { initAbilityUI, refreshAbilityModeUI } from './features/abilities.js';
import { wireFormSubmit } from './form.js';

async function init() {
  try {
    await Promise.all([
      loadClasses(),
      loadRaces(),
      loadBackgrounds(),
      loadArmors(),
      loadShields(),
      loadAlignments(),
    ]);

    // Subclasses are tied to whichever class is selected, in either mode.
    await loadSubclasses($('#characterClass')?.value || null);
    refreshSubclassUI();

    await Promise.all([loadLanguages(), loadSkills()]);
    refreshPills();
    refreshSubclassUI(); // ensure correct visibility once data is in

    // Wiring
    wireSubclassUI();
    initAbilityUI();
    refreshAbilityModeUI();

    // Update pills whenever class/race/background changes
    $('#characterClass')?.addEventListener('change', refreshPills);
    $('#characterRace')?.addEventListener('change', refreshPills);
    $('#characterBackground')?.addEventListener('change', refreshPills);

    // RAW vs. Freedom toggle re-loads subclasses (level gate differs)
    // and refreshes both the pills and subclass visibility.
    document.querySelectorAll('input[name="mode"]').forEach((r) => {
      r.addEventListener('change', async () => {
        await loadSubclasses($('#characterClass')?.value || null);
        refreshPills();
        refreshSubclassUI();
      });
    });

    wireFormSubmit();
    setMsg('muted', 'Ready. Fill the form and click Generate.');
  } catch (e) {
    console.error(e);
    setMsg('err', 'Failed to load reference data: ' + (e.message || e));
  }
}

// kick off once DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
