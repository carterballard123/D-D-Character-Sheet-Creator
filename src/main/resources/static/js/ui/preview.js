// ==============================
// FILE: /js/ui/preview.js
// ==============================
import { $ } from '../core.js';
import { cache } from '../state.js';

/**
 * Renders a lightweight summary of the currently selected class/race
 * into #creationPreview: hit die, primary ability, speed, and the
 * race's first trait. Pure read of already-cached data — no network
 * calls, safe to call as often as you like.
 */
export function renderPreview() {
  const box = $('#creationPreview');
  if (!box) return;

  const className = $('#characterClass')?.value;
  const raceName = $('#characterRace')?.value;

  const cls = className ? cache.classes.find((c) => c.name === className) : null;
  const race = raceName ? cache.races.find((r) => r.name === raceName) : null;

  if (!cls && !race) {
    box.innerHTML = '<div class="hint">Pick a class and race to see a preview.</div>';
    return;
  }

  const firstTrait = race?._raw?.traits?.[0]?.name;

  box.innerHTML = `
    <div class="preview-row"><span class="preview-label">Class</span><span>${cls ? cls.name : '—'}</span></div>
    <div class="preview-row"><span class="preview-label">Race</span><span>${race ? race.name : '—'}</span></div>
    <div class="preview-row"><span class="preview-label">Hit Die</span><span>${cls ? 'd' + cls.hitDie : '—'}</span></div>
    <div class="preview-row"><span class="preview-label">Primary Ability</span><span>${cls ? cls.primaryAbility : '—'}</span></div>
    <div class="preview-row"><span class="preview-label">Speed</span><span>${race ? race._raw.speed + ' ft.' : '—'}</span></div>
    <div class="preview-row"><span class="preview-label">Racial Trait</span><span>${firstTrait || '—'}</span></div>
  `;
}
