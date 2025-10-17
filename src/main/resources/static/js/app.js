/* ======================================================
   app.js — D&D Character → PDF (single-file frontend logic)
   ====================================================== */
'use strict';

/* =========================
   CORE / CONFIG / SMALL HELPERS
   ========================= */
const $ = (id) => document.getElementById(id);

async function fetchJson(url) {
  const r = await fetch(url);
  if (!r.ok) throw new Error(url + ' → HTTP ' + r.status);
  return r.json();
}

function prettyEnum(tok) {
  return String(tok || '')
    .toLowerCase()
    .split('_')
    .map((s) => s.charAt(0).toUpperCase() + s.slice(1))
    .join(' ');
}

const toArray = (v) => (Array.isArray(v) ? v : v != null ? [v] : []);
const modeValue = () =>
  document.querySelector('input[name="mode"]:checked')?.value || 'raw';
const isRAW = () => modeValue() === 'raw';

// Global state cache
const cache = {
  classes: [],
  subclasses: [],
  races: [],
  backgrounds: [],
  armors: [],
  shields: [],
  languages: [],
  skills: [],
};

// RAW bonuses from class features (expand as needed)
const SKILL_FEATURE_BONUSES = {
  barbarian: [{ nameRegex: /Primal Knowledge/i, choose: 1, from: 'classList' }],
};

/* =========================
   NORMALIZERS / FINDERS / SELECT HELPERS
   ========================= */
function normalizeClass(c) {
  return {
    id: c.classID || c.name,
    name: c.name || c.classID,
    profSkills: {
      choose: c?.proficiencies?.skills?.choose ?? 0,
      from: toArray(c?.proficiencies?.skills?.from).map(String),
    },
    featuresByLevel: c?.featuresByLevel || {},
  };
}

function normalizeBackground(b) {
  // Optional languages shape on background:
  // { grant:["Common"], choose:1, from:"ANY" } or from:["Dwarvish","Elvish"]
  const lang = b.languages || {};
  return {
    name: b.name,
    fixedSkills: toArray(b.skillProficiencies).map(String),
    languages: {
      grant: toArray(lang.grant).map(String),
      choose: Number(lang.choose || 0),
      from: lang.from || 'ANY',
    },
    _raw: b,
  };
}

function normalizeRace(r) {
  return { name: r.name, _raw: r };
}

const findByName = (list, name) => {
  const key = String(name || '').toLowerCase();
  return list.find((it) =>
    [it?.name, it?.className, it?.classID, it?.subclassName].some(
      (n) => String(n || '').toLowerCase() === key
    )
  );
};

function guessName(obj) {
  if (!obj || typeof obj !== 'object') return String(obj ?? '');
  const order = [
    'name',
    'armorName',
    'raceName',
    'className',
    'backgroundName',
    'shieldName',
    'subclassName',
    'displayName',
    'title',
  ];
  for (const k of order) {
    if (typeof obj[k] === 'string' && obj[k]) return obj[k];
  }
  for (const k of Object.keys(obj)) {
    const v = obj[k];
    if (typeof v === 'string' && /name$/i.test(k)) return v;
  }
  for (const k of Object.keys(obj)) {
    const v = obj[k];
    if (typeof v === 'string') return v;
  }
  return '';
}

function populateSelect(sel, items, { placeholder, sort = true } = {}) {
  const arr = Array.from(items || []);
  const labels = arr
    .map((x) => {
      const label = typeof x === 'string' ? x : guessName(x);
      return { label, value: label };
    })
    .filter((x) => x.label && x.value);
  if (sort) labels.sort((a, b) => a.label.localeCompare(b.label));
  const opts = [];
  if (placeholder)
    opts.push(
      `<option value="" selected disabled hidden>${placeholder}</option>`
    );
  for (const it of labels) {
    opts.push(`<option value="${it.value}">${it.label}</option>`);
  }
  sel.innerHTML = opts.join('');
}

/* =========================
   LOADERS (REMOTE DATA)
   ========================= */
async function loadClasses() {
  const data = await fetchJson('/api/reference/classes');
  cache.classes = (data || []).map(normalizeClass);
  populateSelect($('characterClass'), cache.classes, {
    placeholder: 'Select a class',
  });
}

async function loadSubclasses(forClass) {
  try {
    if (!cache.subclasses.length)
      cache.subclasses = await fetchJson('/api/reference/subclasses');
    const filtered = forClass
      ? cache.subclasses.filter((sc) => {
          const parent =
            sc.className ?? sc.parentClass ?? sc.forClass ?? sc.baseClass;
          return (
            String(parent || '').toLowerCase() ===
            String(forClass || '').toLowerCase()
          );
        })
      : []; // empty if no class selected
    populateSelect($('subclassName'), filtered, { placeholder: '(optional)' });
  } catch (e) {
    $('subclassName').innerHTML = '<option value="">(no subclasses)</option>';
  }
}

async function loadRaces() {
  const data = await fetchJson('/api/reference/races');
  cache.races = (data || []).map(normalizeRace);
  populateSelect($('characterRace'), cache.races, {
    placeholder: 'Select a race',
  });
}

async function loadBackgrounds() {
  const data = await fetchJson('/api/reference/backgrounds');
  cache.backgrounds = (data || []).map(normalizeBackground);
  populateSelect($('characterBackground'), cache.backgrounds, {
    placeholder: 'Select a background',
  });
}

async function loadArmors() {
  const data = await fetchJson('/api/reference/armor');
  cache.armors = data || [];
  populateSelect($('armorName'), cache.armors, { placeholder: 'Choose armor' });
  $('armorStatus').textContent = `Loaded ${cache.armors.length} armors`;
}

async function loadShields() {
  const data = await fetchJson('/api/reference/shields');
  cache.shields = data || [];
  populateSelect($('shieldName'), cache.shields, {
    placeholder: 'Choose shield',
  });
}

async function loadLanguages() {
  try {
    const data = await fetchJson('/api/reference/languages');
    cache.languages = (data || [])
      .map((x) => (typeof x === 'string' ? x : x.name || x.languageName))
      .filter(Boolean);
  } catch {
    cache.languages = [];
  }
  refreshPills(); // render after load
}

async function loadSkills() {
  try {
    const data = await fetchJson('/api/reference/skills');
    cache.skills = (data || []).map(String);
  } catch {
    cache.skills = [];
  }
  refreshPills(); // render after load
}

async function loadAlignments() {
  try {
    const data = await fetchJson('/api/enums/alignments');
    const items = (data || []).map((x) => ({
      label: prettyEnum(x),
      value: String(x),
    }));
    const sel = $('characterAlignment');
    sel.innerHTML = ['<option value="" selected disabled hidden>Select alignment</option>']
      .concat(items.map((it) => `<option value="${it.value}">${it.label}</option>`))
      .join('');
  } catch (e) {
    const tokens = [
      'Lawful Good',
      'Neutral Good',
      'Chaotic Good',
      'Lawful Neutral',
      'Neutral',
      'Chaotic Neutral',
      'Lawful Evil',
      'Neutral Evil',
      'Chaotic Evil',
    ];
    const items = tokens.map((x) => ({ label: prettyEnum(x), value: x }));
    $('characterAlignment').innerHTML = ['<option value="" selected disabled hidden>Select alignment</option>']
      .concat(items.map((it) => `<option value="${it.value}">${it.label}</option>`))
      .join('');
  }
}

/* =========================
   RAW RULES / RENDERERS / UI HELPERS
   ========================= */
function computeRAW(ctx) {
  const cls =
    findByName(cache.classes, ctx.class) || {
      profSkills: { choose: 0, from: [] },
      featuresByLevel: {},
    };
  const bg =
    findByName(cache.backgrounds, ctx.background) || {
      fixedSkills: [],
      languages: { grant: [], choose: 0, from: 'ANY' },
    };
  const level = Number($('characterLevel').value || 1);

  // class picks
  let classChoose = Number(cls.profSkills.choose || 0);
  const pool = new Set(cls.profSkills.from.map(String));

  // feature-based picks up to current level
  const featureDefs = SKILL_FEATURE_BONUSES[(cls.id || '').toLowerCase()] || [];
  for (let L = 1; L <= level; L++) {
    const feats = toArray(cls.featuresByLevel?.[String(L)]);
    for (const f of feats) {
      const nm = f?.name || '';
      for (const def of featureDefs) {
        if (def.nameRegex.test(nm)) classChoose += def.choose || 0;
      }
    }
  }

  // background grants
  const grantedSkills = new Set(bg.fixedSkills.map(String));

  // languages
  const allLangNames = new Set(
    (cache.languages || []).map((x) =>
      typeof x === 'string' ? x : x.name || x.languageName || String(x)
    )
  );
  const langGrant = new Set((bg.languages?.grant || []).map(String));
  const langFrom =
    bg.languages?.from === 'ANY'
      ? new Set(allLangNames)
      : new Set(toArray(bg.languages?.from).map(String));
  const langChoose = Number(bg.languages?.choose || 0);

  return {
    skills: { grant: grantedSkills, from: pool, choose: classChoose },
    languages: {
      grant: langGrant,
      from: langFrom.size ? langFrom : allLangNames,
      choose: langChoose,
    },
  };
}

function getChecked(name) {
  return Array.from(
    document.querySelectorAll(`input[name="${name}"]:checked`)
  ).map((i) => i.value);
}

function setMsg(kind, text) {
  const el = $('msg');
  el.className = 'msg ' + (kind || 'muted');
  el.textContent = text;
}

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
    h.push(
      `<div class="hint" style="margin:6px 0">${titleGrant} (${granted.length})</div>`
    );
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
          <input type="checkbox" name="${name}" value="${v}" ${
            prevChecked.has(v) ? 'checked' : ''
          }>
          ${v}
        </label>`
        )
        .join('')
    );
  }
  box.innerHTML = h.join('\n');

  // live limit enforcement (rebounds on each render)
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

function refreshPills() {
  const mode = modeValue();

  if (mode === 'freedom') {
    const allLangs = new Set(
      (cache.languages || []).map((x) =>
        typeof x === 'string' ? x : x.name || x.languageName
      )
    );
    const allSkills = new Set((cache.skills || []).map(String));
    renderChoicePills(
      'languagesBox',
      { titleGrant: 'Granted', titleChoose: 'Languages' },
      'languages',
      allLangs,
      new Set(),
      null,
      0
    );
    renderChoicePills(
      'skillsBox',
      { titleGrant: 'Granted', titleChoose: 'Skills' },
      'skills',
      allSkills,
      new Set(),
      null,
      0
    );
    return;
  }

  // RAW
  const rules = computeRAW({
    class: $('characterClass').value || null,
    background: $('characterBackground').value || null,
    race: $('characterRace').value || null,
  });

  const langAll = new Set([...rules.languages.grant, ...rules.languages.from]);
  renderChoicePills(
    'languagesBox',
    { titleGrant: 'Granted by Background', titleChoose: 'Languages (RAW)' },
    'languages',
    langAll,
    rules.languages.grant,
    rules.languages.from,
    rules.languages.choose
  );

  const allSkillNames = new Set((cache.skills || []).map(String));
  const skillPool = new Set(
    [...rules.skills.from].filter((s) => allSkillNames.has(s))
  );
  const skillAll = new Set([...rules.skills.grant, ...skillPool]);
  renderChoicePills(
    'skillsBox',
    { titleGrant: 'Granted by Background', titleChoose: 'Class Skill Choices (RAW)' },
    'skills',
    skillAll,
    rules.skills.grant,
    skillPool,
    rules.skills.choose
  );
}

/* =========================
   SUBMIT HANDLER / BOOT / WIRING
   ========================= */
const form = $('char-form');
const btn = $('submitBtn');

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  setMsg('muted', 'Submitting…');
  btn.disabled = true;

  const fd = new FormData(form);
  const payload = Object.fromEntries(fd.entries());

  [
    'characterLevel',
    'characterStrength',
    'characterDexterity',
    'characterConstitution',
    'characterIntelligence',
    'characterWisdom',
    'characterCharisma',
  ].forEach((k) => {
    if (payload[k] !== undefined) payload[k] = Number(payload[k]);
  });

  payload.languages = getChecked('languages');
  payload.skills = getChecked('skills');
  payload.mode = modeValue();
  // DEBUG: confirm armor/shield are being sent
   console.log('[submit] payload preview', {
     // if you changed your <select name="..."> to match the DTO:
     characterArmor: payload.characterArmor,
     characterShield: payload.characterShield,
     // if you’re still using armorName/shieldName in the form:
     armorName: payload.armorName,
     shieldName: payload.shieldName
   });
  try {
    const res = await fetch('/api/pdf/fill', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const ctype = res.headers.get('content-type') || '';
    if (!res.ok) {
		 if (ctype.includes('application/json')) {
		        const err = await res.json();
		        const lines = (err.errors || [])
		          .map(
		            (e) =>
		              (e.field || e?.cause?.field || 'field') +
		              ': ' +
		              (e.message || e?.defaultMessage || 'invalid')
		          )
		          .join('\n');
		        setMsg('err', `Error ${res.status}${lines ? '\n' + lines : ''}`);
		      } else {
		        setMsg('err', `Error ${res.status}: ` + (await res.text()));
		      }
		      return;
		    }
		    if (ctype.includes('application/pdf')) {
		      const blob = await res.blob();
		      if (blob.size < 5) {
		        setMsg('err', 'Server returned empty PDF bytes');
		        return;
		      }
		      const url = URL.createObjectURL(blob);
		      const a = Object.assign(document.createElement('a'), {
		        href: url,
		        download: 'character.pdf',
		      });
		      document.body.appendChild(a);
		      a.click();
		      a.remove();
		      URL.revokeObjectURL(url);
		      setMsg('ok', 'PDF generated. Check your downloads.');
		    } else {
		      setMsg('err', 'Unexpected response (not a PDF).');
		    }
		  } catch (err) {
		    setMsg('err', 'Network/JS error: ' + (err.message || err));
		  } finally {
		    btn.disabled = false;
		  }
		});

		// ----- Subclass visibility / lock logic -----
		const classEl = $('characterClass');
		const levelEl = $('characterLevel');
		const subclassGroup = $('subclassGroup');
		const subclassSelect = $('subclassName');

		function refreshSubclassUI() {
		  const hasClass = !!classEl?.value;
		  const level = parseInt(levelEl?.value || '1', 10) || 1;
		  const help = $('subclassHelp');

		  // No class chosen → hide entirely
		  if (!hasClass) {
		    if (subclassGroup) subclassGroup.hidden = true;
		    if (subclassSelect) {
		      subclassSelect.disabled = true;
		      subclassSelect.value = '';
		    }
		    if (help) help.textContent = 'Pick a class to see subclasses.';
		    return;
		  }

		  // Gate by level (only enforce in RAW mode)
		  if (isRAW() && level < 3) {
		    if (subclassGroup) subclassGroup.hidden = true; // or show but disabled if you prefer
		    if (subclassSelect) {
		      subclassSelect.disabled = true;
		      subclassSelect.value = '';
		    }
		    if (help) help.textContent = 'Subclass becomes available at level 3.';
		    return;
		  }

		  // Class picked and (RAW: level ≥3 or Freedom: any level) → show + enable
		  if (subclassGroup) subclassGroup.hidden = false;
		  if (subclassSelect) subclassSelect.disabled = false;
		  if (help) help.textContent = 'Choose a subclass.';
		}

		levelEl?.addEventListener('input', refreshSubclassUI);
		levelEl?.addEventListener('change', refreshSubclassUI);

		/* =========================
		   ABILITY SCORES: MODES + UI
		   ========================= */

		// ------------- config/constants -------------
		const ABILITY_IDS = [
		  'characterStrength',
		  'characterDexterity',
		  'characterConstitution',
		  'characterIntelligence',
		  'characterWisdom',
		  'characterCharisma'
		];

		const POINT_BUY_BUDGET = 27;
		// PHB-style point-buy costs, min 8, max 15
		const POINT_BUY_COST = { 8:0, 9:1, 10:2, 11:3, 12:4, 13:5, 14:7, 15:9 };

		// ------------- helpers -------------
		function abilityModeValue(){
		  return document.querySelector('input[name="abilityMode"]:checked')?.value || 'free';
		}
		function getAbilities(){
		  const vals = {};
		  ABILITY_IDS.forEach(id => {
		    const v = parseInt($(id)?.value || '0', 10) || 0;
		    vals[id] = v;
		  });
		  return vals;
		}
		function setAbilities(map){
		  ABILITY_IDS.forEach(id => {
		    if ($(id) && map[id] != null) $(id).value = map[id];
		  });
		}
		function clamp(n, lo, hi){ return Math.max(lo, Math.min(hi, n)); }

		// return total points spent by the current scores (relative to 8)
		function pointBuySpent(vals){
		  let spent = 0;
		  for (const id of ABILITY_IDS){
		    const s = clamp(vals[id], 8, 15);
		    if (!(s in POINT_BUY_COST)) return Infinity;
		    spent += POINT_BUY_COST[s];
		  }
		  return spent;
		}

		// ------------- Point Buy mode -------------
		let lastValidAbilities = null; // remember last valid set to revert overspend

		function enterPointBuyMode(){
		  // lock min/max for inputs (8..15) and initialize if needed
		  ABILITY_IDS.forEach(id => {
		    const el = $(id);
		    if (!el) return;
		    el.min = 8; el.max = 15; el.step = 1;
		  });
		  // If current values are not valid point-buy, reset all to 8
		  const cur = getAbilities();
		  const curSpent = pointBuySpent(cur);
		  if (curSpent === Infinity || curSpent > POINT_BUY_BUDGET){
		    const all8 = {}; ABILITY_IDS.forEach(id => all8[id] = 8);
		    setAbilities(all8);
		    lastValidAbilities = all8;
		  } else {
		    lastValidAbilities = cur;
		  }
		  updatePointBuyHint();
		  $('rollUI').innerHTML = '';
		}

		function updatePointBuyHint(){
		  const hint = $('abilityHint');
		  const spent = pointBuySpent(getAbilities());
		  if (spent === Infinity) {
		    hint.textContent = 'Point buy: invalid scores.';
		    return;
		  }
		  const left = POINT_BUY_BUDGET - spent;
		  hint.textContent = `Point buy: ${spent}/${POINT_BUY_BUDGET} spent • ${Math.max(0,left)} left`;
		  hint.className = 'hint' + (left < 0 ? ' err' : '');
		}

		function onAbilityInputChange_PointBuy(e){
		  if (abilityModeValue() !== 'pointbuy') return;
		  const id = e?.target?.id;
		  if (!id || !ABILITY_IDS.includes(id)) return;

		  // clamp to 8..15
		  const el = $(id);
		  el.value = clamp(parseInt(el.value || '0',10) || 0, 8, 15);

		  const now = getAbilities();
		  const spent = pointBuySpent(now);
		  if (spent > POINT_BUY_BUDGET){
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
		  const rolls = [1,2,3,4].map(() => 1 + Math.floor(Math.random()*6)).sort((a,b)=>a-b);
		  return rolls[1] + rolls[2] + rolls[3];
		}
		function rollSix(){
		  rolledScores = Array.from({length:6}, roll1);
		  renderRollUI();
		}
		function applyRolledInOrder(){
		  // Fill STR DEX CON INT WIS CHA in that order
		  const map = {};
		  ABILITY_IDS.forEach((id, idx) => map[id] = rolledScores[idx] ?? 8);
		  setAbilities(map);
		}

		function clearRolls(){
		  rolledScores = [];
		  renderRollUI();
		}

		function renderRollUI(){
		  const box = $('rollUI');
		  if (!box) return;
		  if (abilityModeValue() !== 'roll'){
		    box.innerHTML = '';
		    return;
		  }
		  if (!rolledScores.length){
		    box.innerHTML = `
		      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
		        <button type="button" id="btnRoll6">Roll 6</button>
		        <span class="hint">Rolls: none yet.</span>
		      </div>
		    `;
		  } else {
		    const pills = rolledScores.map(n => `<span class="pill" style="display:inline-flex;">${n}</span>`).join(' ');
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
		  $('btnRoll6')?.addEventListener('click', rollSix);
		  $('btnApplyRolls')?.addEventListener('click', applyRolledInOrder);
		  $('btnClearRolls')?.addEventListener('click', clearRolls);
		}

		function enterRollMode(){
		  // Make fields read-only-ish (we’ll allow edits but encourage using the roll UI)
		  ABILITY_IDS.forEach(id => {
		    const el = $(id);
		    if (!el) return;
		    el.min = 3; el.max = 18; el.step = 1;
		  });
		  $('abilityHint').textContent = 'Roll 4d6 (drop lowest) to generate six scores.';
		  renderRollUI();
		}

		// ------------- Free mode -------------
		function enterFreeMode(){
		  ABILITY_IDS.forEach(id => {
		    const el = $(id);
		    if (!el) return;
		    el.min = 1; el.max = 30; el.step = 1;
		  });
		  $('abilityHint').textContent = 'Free mode: set any values (1–30).';
		  $('rollUI').innerHTML = '';
		}

		// ------------- Mode switcher + wiring -------------
		function refreshAbilityModeUI(){
		  const mode = abilityModeValue();
		  if (mode === 'pointbuy') enterPointBuyMode();
		  else if (mode === 'roll') enterRollMode();
		  else enterFreeMode();
		  
		  renderRollUI();
		}

		// Attach listeners once
		(function wireAbilityInputs(){
		  // update point-buy constraints when user types
		  ABILITY_IDS.forEach(id => {
		    $(id)?.addEventListener('input', onAbilityInputChange_PointBuy);
		    $(id)?.addEventListener('change', onAbilityInputChange_PointBuy);
		  });
		  // mode radio
		  document.querySelectorAll('input[name="abilityMode"]').forEach(r => {
		    r.addEventListener('change', () => {
		      refreshAbilityModeUI();
		    });
		  });
		})();

		// ----- Boot -----
		(async function init() {
		  try {
		    await Promise.all([
		      loadClasses(),
		      loadRaces(),
		      loadBackgrounds(),
		      loadArmors(),
		      loadShields(),
		      loadAlignments(),
		    ]);

		    await loadSubclasses(isRAW() ? classEl.value || null : null);
		    refreshSubclassUI();

		    await Promise.all([loadLanguages(), loadSkills()]);
		    refreshPills();
		    refreshSubclassUI(); // ensure correct visibility once data is in

		    // Wiring
		    classEl.addEventListener('change', (e) => {
		      loadSubclasses(isRAW() ? e.target.value : null);
		      refreshPills();
		      refreshSubclassUI();
		    });
		    $('characterRace').addEventListener('change', refreshPills);
		    $('characterBackground').addEventListener('change', refreshPills);
		    document.querySelectorAll('input[name="mode"]').forEach((r) => {
		      r.addEventListener('change', () => {
		        loadSubclasses(isRAW() ? classEl.value || null : null);
		        refreshPills();
		        refreshSubclassUI();
		      });
		    });

		    setMsg('muted', 'Ready. Fill the form and click Generate.');
		  } catch (e) {
		    console.error(e);
		    setMsg('err', 'Failed to load reference data: ' + (e.message || e));
		  }
		})();