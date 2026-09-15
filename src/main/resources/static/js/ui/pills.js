// ==============================
// FILE: /js/ui/pills.js
// ==============================
import { $, modeValue } from '../core.js';
import { cache } from '../state.js';
import { computeRAW } from '../rules.js';

/** Returns the values of every checked checkbox/radio with the given name. */
export function getChecked(name) {
  return Array.from(
    document.querySelectorAll(`input[name="${name}"]:checked`)
  ).map((i) => i.value);
}

/**
 * Renders a checkbox "pill" group into a container: some items are granted
 * (locked, always checked), the rest are choosable up to chooseCount, with
 * a live counter and auto-disable once the limit is reached.
 *
 * @param {string} containerId - CSS selector for the container element
 * @param {{titleGrant: string, titleChoose: string}} titles - section headings
 * @param {string} name - the checkbox `name` attribute shared by this group
 * @param {Iterable} allItems - every item that could appear (granted + choosable)
 * @param {Set} grantSet - items granted automatically (checked, disabled)
 * @param {Set|null} poolSet - items eligible to be chosen (null = any non-granted item)
 * @param {number} chooseCount - how many choosable items may be picked
 */
function renderChoicePills(
  containerId,
  { titleGrant, titleChoose },
  name,
  allItems,
  grantSet,
  poolSet,
  chooseCount
) {
  const box = $(containerId);
  const prevChecked = new Set(getChecked(name));
  const all = Array.from(allItems).map(String).sort((a, b) => a.localeCompare(b));

  const granted = all.filter((v) => grantSet.has(v));
  const choosable = all.filter(
    (v) => !grantSet.has(v) && (!poolSet || poolSet.has(v))
  );

  const alreadyChosen = [...prevChecked].filter((v) => choosable.includes(v));
  const chosenCount = Math.min(alreadyChosen.length, chooseCount);
  const maxed = chooseCount > 0 && chosenCount >= chooseCount;

  const h = [];
  if (granted.length) {
    h.push(`<div class="hint" style="margin:6px 0">${titleGrant} (${granted.length})</div>`);
    h.push(
      granted
        .map(
          (v) => `
        <label class="pill disabled" title="Granted by Background">
          <input type="checkbox" name="${name}" value="${v}" checked disabled> ${v}
        </label>`
        )
        .join('')
    );
  }
  if (chooseCount > 0) {
    const left = chooseCount - chosenCount;
    h.push(
      `<div class="hint" style="margin:10px 0 6px">${titleChoose} — pick <b>${chooseCount}</b> (left: <b id="${name}-left">${left}</b>)</div>`
    );
    h.push(
      choosable
        .map((v) => {
          const checked = prevChecked.has(v);
          const dis = !checked && maxed ? 'disabled' : '';
          return `
          <label class="pill ${dis ? 'disabled' : ''}">
            <input type="checkbox" name="${name}" value="${v}" ${checked ? 'checked' : ''} ${dis}>
            ${v}
          </label>`;
        })
        .join('')
    );
  }
  if (chooseCount === 0 && choosable.length) {
    h.push(`<div class="hint" style="margin:10px 0 6px">Optional</div>`);
    h.push(
      choosable
        .map(
          (v) => `
        <label class="pill">
          <input type="checkbox" name="${name}" value="${v}" ${prevChecked.has(v) ? 'checked' : ''}>
          ${v}
        </label>`
        )
        .join('')
    );
  }
  box.innerHTML = h.join('\n');

  // Live limit enforcement: re-bound on every render so the disabled
  // state always reflects the current chooseCount/choosable set.
  box.onchange = (e) => {
    if (e.target?.name !== name) return;
    if (chooseCount <= 0) return;
    const picks = getChecked(name).filter((v) => choosable.includes(v)).length;
    const left = Math.max(0, chooseCount - picks);
    const counter = document.getElementById(`${name}-left`);
    if (counter) counter.textContent = String(left);
    const disable = picks >= chooseCount;
    box.querySelectorAll(`input[name="${name}"]`).forEach((inp) => {
      if (inp.checked) return;
      if (choosable.includes(inp.value)) {
        inp.disabled = disable;
        inp.closest('.pill')?.classList.toggle('disabled', disable);
      }
    });
  };
}

/**
 * Re-renders the languages and skills pill groups based on the current
 * mode (RAW vs. Freedom) and the currently selected class/race/background.
 */
export function refreshPills() {
  const mode = modeValue();

  if (mode === 'freedom') {
    // Freedom mode: no grants, no caps — every language/skill is an
    // unrestricted optional pick.
    const allLangs = new Set((cache.languages || []).map((x) => (typeof x === 'string' ? x : x.name || x.languageName)));
    const allSkills = new Set((cache.skills || []).map(String));
    renderChoicePills('#languagesBox', { titleGrant: 'Granted', titleChoose: 'Languages' }, 'languages', allLangs, new Set(), null, 0);
    renderChoicePills('#skillsBox', { titleGrant: 'Granted', titleChoose: 'Skills' }, 'skills', allSkills, new Set(), null, 0);
    return;
  }

  // RAW mode: pills are computed from the selected class/background rules.
  const rules = computeRAW({
    class: $('#characterClass')?.value || null,
    background: $('#characterBackground')?.value || null,
    race: $('#characterRace')?.value || null,
  });

  const langAll = new Set([...rules.languages.grant, ...rules.languages.from]);
  renderChoicePills('#languagesBox', { titleGrant: 'Granted by Background', titleChoose: 'Languages (RAW)' }, 'languages', langAll, rules.languages.grant, rules.languages.from, rules.languages.choose);

  const allSkillNames = new Set((cache.skills || []).map(String));
  const skillPool = new Set([...rules.skills.from].filter((s) => allSkillNames.has(s)));
  const skillAll = new Set([...rules.skills.grant, ...skillPool]);
  renderChoicePills('#skillsBox', { titleGrant: 'Granted by Background', titleChoose: 'Class Skill Choices (RAW)' }, 'skills', skillAll, rules.skills.grant, skillPool, rules.skills.choose);
}
