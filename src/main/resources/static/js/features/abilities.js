// ==============================
// FILE: /js/features/abilities.js (optional – keep or remove)
// ==============================
import { $, modeValue } from '../core.js';


const ABILITY_IDS = [
'characterStrength','characterDexterity','characterConstitution','characterIntelligence','characterWisdom','characterCharisma'
];


const POINT_BUY_BUDGET = 27;
const POINT_BUY_COST = { 8:0, 9:1, 10:2, 11:3, 12:4, 13:5, 14:7, 15:9 };


function abilityModeValue(){ return document.querySelector('input[name="abilityMode"]:checked')?.value || 'free'; }
function clamp(n, lo, hi){ return Math.max(lo, Math.min(hi, n)); }
function getAbilities(){ const vals = {}; ABILITY_IDS.forEach(id => { const v = parseInt($(id)?.value || '0', 10) || 0; vals[id] = v; }); return vals; }
function setAbilities(map){ ABILITY_IDS.forEach(id => { if ($(id) && map[id] != null) $(id).value = map[id]; }); }
function pointBuySpent(vals){ let spent = 0; for (const id of ABILITY_IDS){ const s = clamp(vals[id], 8, 15); if (!(s in POINT_BUY_COST)) return Infinity; spent += POINT_BUY_COST[s]; } return spent; }


let lastValidAbilities = null; let rolledScores = [];


function updatePointBuyHint(){ const hint = $('#abilityHint'); const spent = pointBuySpent(getAbilities()); if (spent === Infinity) { hint.textContent = 'Point buy: invalid scores.'; return; } const left = POINT_BUY_BUDGET - spent; hint.textContent = `Point buy: ${spent}/${POINT_BUY_BUDGET} spent • ${Math.max(0,left)} left`; hint.className = 'hint' + (left < 0 ? ' err' : ''); }


function enterPointBuyMode(){ ABILITY_IDS.forEach(id => { const el = $(id); if (!el) return; el.min = 8; el.max = 15; el.step = 1; }); const cur = getAbilities(); const curSpent = pointBuySpent(cur); if (curSpent === Infinity || curSpent > POINT_BUY_BUDGET){ const all8 = {}; ABILITY_IDS.forEach(id => all8[id] = 8); setAbilities(all8); lastValidAbilities = all8; } else lastValidAbilities = cur; updatePointBuyHint(); $('#rollUI').innerHTML = ''; }


function onAbilityInputChange_PointBuy(e){ if (abilityModeValue() !== 'pointbuy') return; const id = e?.target?.id; if (!id || !ABILITY_IDS.includes(id)) return; const el = $(id); el.value = clamp(parseInt(el.value || '0',10) || 0, 8, 15); const now = getAbilities(); const spent = pointBuySpent(now); if (spent > POINT_BUY_BUDGET){ el.value = lastValidAbilities?.[id] ?? 8; } else { lastValidAbilities = now; } updatePointBuyHint(); }


function roll1(){ const rolls = [1,2,3,4].map(() => 1 + Math.floor(Math.random()*6)).sort((a,b)=>a-b); return rolls[1] + rolls[2] + rolls[3]; }
function rollSix(){ rolledScores = Array.from({length:6}, roll1); renderRollUI(); }
function applyRolledInOrder(){ const map = {}; ABILITY_IDS.forEach((id, idx) => map[id] = rolledScores[idx] ?? 8); setAbilities(map); }
function clearRolls(){ rolledScores = []; renderRollUI(); }


function renderRollUI(){ const box = $('#rollUI'); if (!box) return; if (abilityModeValue() !== 'roll'){ box.innerHTML = ''; return; } if (!rolledScores.length){ box.innerHTML = `
<div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
<button type="button" id="btnRoll6">Roll 6</button>
<span class="hint">Rolls: none yet.</span>
</div>`; } else { const pills = rolledScores.map(n => `<span class="pill" style="display:inline-flex;">${n}</span>`).join(' '); box.innerHTML = `
<div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;margin-bottom:6px">
<button type="button" id="btnRoll6">Re-roll</button>
<button type="button" id="btnApplyRolls">Apply in order</button>
<button type="button" id="btnClearRolls">Clear</button>
<span class="hint">Rolled:</span> ${pills}
</div>`; }
$('#btnRoll6')?.addEventListener('click', rollSix);
$('#btnApplyRolls')?.addEventListener('click', applyRolledInOrder);
$('#btnClearRolls')?.addEventListener('click', clearRolls);
}


function enterRollMode(){ ABILITY_IDS.forEach(id => { const el = $(id); if (!el) return; el.min = 3; el.max = 18; el.step = 1; }); $('#abilityHint').textContent = 'Roll 4d6 (drop lowest) to generate six scores.'; renderRollUI(); }
function enterFreeMode(){ ABILITY_IDS.forEach(id => { const el = $(id); if (!el) return; el.min = 1; el.max = 30; el.step = 1; }); $('#abilityHint').textContent = 'Free mode: set any values (1–30).'; $('#rollUI').innerHTML = ''; }


export function refreshAbilityModeUI(){ const mode = abilityModeValue(); if (mode === 'pointbuy') enterPointBuyMode(); else if (mode === 'roll') enterRollMode(); else enterFreeMode(); renderRollUI(); }


export function initAbilityUI(){
// wire changes
ABILITY_IDS.forEach(id => {
$(id)?.addEventListener('input', onAbilityInputChange_PointBuy);
$(id)?.addEventListener('change', onAbilityInputChange_PointBuy);
});
document.querySelectorAll('input[name="abilityMode"]').forEach(r => {
r.addEventListener('change', () => { refreshAbilityModeUI(); });
});
}