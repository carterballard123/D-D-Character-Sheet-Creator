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
      'LAWFUL_GOOD',
      'NEUTRAL_GOOD',
      'CHAOTIC_GOOD',
      'LAWFUL_NEUTRAL',
      'NEUTRAL',
      'CHAOTIC_NEUTRAL',
      'LAWFUL_EVIL',
      'NEUTRAL_EVIL',
      'CHAOTIC_EVIL',
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