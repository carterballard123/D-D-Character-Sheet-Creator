// ==============================
// FILE: /js/features/abilities.js
// ==============================
import { $, modeValue } from '../core.js';

// ------------- config/constants -------------
const ABILITY_IDS = [
  'characterStrength',
  'characterDexterity',
  'characterConstitution',
  'characterIntelligence',
  'characterWisdom',
  'characterCharisma',
];

const POINT_BUY_BUDGET = 27;
// PHB-style point-buy costs, min 8, max 15
const POINT_BUY_COST = { 8: 0, 9: 1, 10: 2, 11: 3, 12: 4, 13: 5, 14: 7, 15: 9 };

// ------------- helpers -------------
function abilityModeValue() {
  return document.querySelector('input[name="abilityMode"]:checked')?.value || 'free';
}

function clamp(n, lo, hi) {
  return Math.max(lo, Math.min(hi, n));
}

function getAbilities() {
  const vals = {};
  ABILITY_IDS.forEach((id) => {
    const v = parseInt($(`#${id}`)?.value || '0', 10) || 0;
    vals[id] = v;
  });
  return vals;
}

function setAbilities(map) {
  ABILITY_IDS.forEach((id) => {
    const el = $(`#${id}`);
    if (!el || map[id] == null) return;
    const next = String(map[id]);
    if (el.value === next) return; // no real change - skip the event
    el.value = next;
    // Setting .value in JS fires no event of its own. This is called
    // from button clicks (Roll/Apply, Point Buy's reset-to-8), not from
    // the input being edited directly, so without dispatching one
    // ourselves nothing downstream (e.g. the live PDF preview's
    // change-delegation in features/pdfPreview.js) would ever learn an
    // ability score changed.
    el.dispatchEvent(new Event('change', { bubbles: true }));
  });
}

// Total points spent by the current scores (relative to 8).
function pointBuySpent(vals) {
  let spent = 0;
  for (const id of ABILITY_IDS) {
    const s = clamp(vals[id], 8, 15);
    if (!(s in POINT_BUY_COST)) return Infinity;
    spent += POINT_BUY_COST[s];
  }
  return spent;
}

// ------------- Point Buy mode -------------
let lastValidAbilities = null; // remember last valid set to revert overspend

function updatePointBuyHint() {
  const hint = $('#abilityHint');
  const spent = pointBuySpent(getAbilities());
  if (spent === Infinity) {
    hint.textContent = 'Point buy: invalid scores.';
    return;
  }
  const left = POINT_BUY_BUDGET - spent;
  hint.textContent = `Point buy: ${spent}/${POINT_BUY_BUDGET} spent • ${Math.max(0, left)} left`;
  hint.className = 'hint' + (left < 0 ? ' err' : '');
}

function enterPointBuyMode() {
  // lock min/max for inputs (8..15) and initialize if needed
  ABILITY_IDS.forEach((id) => {
    const el = $(`#${id}`);
    if (!el) return;
    el.min = 8;
    el.max = 15;
    el.step = 1;
  });
  // If current values are not valid point-buy, reset all to 8
  const cur = getAbilities();
  const curSpent = pointBuySpent(cur);
  if (curSpent === Infinity || curSpent > POINT_BUY_BUDGET) {
    const all8 = {};
    ABILITY_IDS.forEach((id) => (all8[id] = 8));
    setAbilities(all8);
    lastValidAbilities = all8;
  } else {
    lastValidAbilities = cur;
  }
  updatePointBuyHint();
  $('#rollUI').innerHTML = '';
}

function onAbilityInputChange_PointBuy(e) {
  if (abilityModeValue() !== 'pointbuy') return;
  const id = e?.target?.id;
  if (!id || !ABILITY_IDS.includes(id)) return;

  // clamp to 8..15
  const el = $(`#${id}`);
  el.value = clamp(parseInt(el.value || '0', 10) || 0, 8, 15);

  const now = getAbilities();
  const spent = pointBuySpent(now);
  if (spent > POINT_BUY_BUDGET) {
    // revert just this field to last valid
    el.value = lastValidAbilities?.[id] ?? 8;
  } else {
    // accept new values
    lastValidAbilities = now;
  }
  updatePointBuyHint();
}

// ------------- Roll mode -------------
let rolledScores = []; // current pool shown in UI

function roll1() {
  // roll 4d6 drop lowest
  const rolls = [1, 2, 3, 4].map(() => 1 + Math.floor(Math.random() * 6)).sort((a, b) => a - b);
  return rolls[1] + rolls[2] + rolls[3];
}

function rollSix() {
  rolledScores = Array.from({ length: 6 }, roll1);
  renderRollUI();
}

function applyRolledInOrder() {
  // Fill STR DEX CON INT WIS CHA in that order
  const map = {};
  ABILITY_IDS.forEach((id, idx) => (map[id] = rolledScores[idx] ?? 8));
  setAbilities(map);
}

function clearRolls() {
  rolledScores = [];
  renderRollUI();
}

function renderRollUI() {
  const box = $('#rollUI');
  if (!box) return;
  if (abilityModeValue() !== 'roll') {
    box.innerHTML = '';
    return;
  }
  if (!rolledScores.length) {
    box.innerHTML = `
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
        <button type="button" id="btnRoll6">Roll 6</button>
        <span class="hint">Rolls: none yet.</span>
      </div>
    `;
  } else {
    const pills = rolledScores.map((n) => `<span class="pill" style="display:inline-flex;">${n}</span>`).join(' ');
    box.innerHTML = `
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;margin-bottom:6px">
        <button type="button" id="btnRoll6">Re-roll</button>
        <button type="button" id="btnApplyRolls">Apply in order</button>
        <button type="button" id="btnClearRolls">Clear</button>
        <span class="hint">Rolled:</span> ${pills}
      </div>
    `;
  }
  // wire buttons
  $('#btnRoll6')?.addEventListener('click', rollSix);
  $('#btnApplyRolls')?.addEventListener('click', applyRolledInOrder);
  $('#btnClearRolls')?.addEventListener('click', clearRolls);
}

function enterRollMode() {
  // Make fields read-only-ish (still editable, but the roll UI is the primary flow)
  ABILITY_IDS.forEach((id) => {
    const el = $(`#${id}`);
    if (!el) return;
    el.min = 3;
    el.max = 18;
    el.step = 1;
  });
  $('#abilityHint').textContent = 'Roll 4d6 (drop lowest) to generate six scores.';
  renderRollUI();
}

// ------------- Free mode -------------
function enterFreeMode() {
  ABILITY_IDS.forEach((id) => {
    const el = $(`#${id}`);
    if (!el) return;
    el.min = 1;
    el.max = 30;
    el.step = 1;
  });
  $('#abilityHint').textContent = 'Free mode: set any values (1–30).';
  $('#rollUI').innerHTML = '';
}

// ------------- Mode switcher + wiring -------------
export function refreshAbilityModeUI() {
  const mode = abilityModeValue();
  if (mode === 'pointbuy') enterPointBuyMode();
  else if (mode === 'roll') enterRollMode();
  else enterFreeMode();

  renderRollUI();
}

/** Wires ability-score inputs and the ability-mode radio group. Call once at boot. */
export function initAbilityUI() {
  // update point-buy constraints when user types
  ABILITY_IDS.forEach((id) => {
    $(`#${id}`)?.addEventListener('input', onAbilityInputChange_PointBuy);
    $(`#${id}`)?.addEventListener('change', onAbilityInputChange_PointBuy);
  });
  // mode radio
  document.querySelectorAll('input[name="abilityMode"]').forEach((r) => {
    r.addEventListener('change', () => {
      refreshAbilityModeUI();
    });
  });
}
