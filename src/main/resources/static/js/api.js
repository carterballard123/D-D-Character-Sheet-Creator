// ==============================


export async function fetchJson(url) {
const r = await fetch(url);
if (!r.ok) throw new Error(url + ' → HTTP ' + r.status);
return r.json();
}


export async function loadClasses() {
const data = await fetchJson('/api/reference/classes');
cache.classes = (data || []).map(normalizeClass);
populateSelect($('#characterClass'), cache.classes, { placeholder: 'Select a class' });
}


export async function loadSubclasses(forClass) {
try {
if (!cache.subclasses.length) cache.subclasses = await fetchJson('/api/reference/subclasses');
const filtered = forClass
? cache.subclasses.filter((sc) => {
const parent = sc.className ?? sc.parentClass ?? sc.forClass ?? sc.baseClass;
return String(parent || '').toLowerCase() === String(forClass || '').toLowerCase();
})
: [];
populateSelect($('#subclassName'), filtered, { placeholder: '(optional)' });
} catch (e) {
$('#subclassName').innerHTML = '<option value="">(no subclasses)</option>';
}
}


export async function loadRaces() {
const data = await fetchJson('/api/reference/races');
cache.races = (data || []).map(normalizeRace);
populateSelect($('#characterRace'), cache.races, { placeholder: 'Select a race' });
}


export async function loadBackgrounds() {
const data = await fetchJson('/api/reference/backgrounds');
cache.backgrounds = (data || []).map(normalizeBackground);
populateSelect($('#characterBackground'), cache.backgrounds, { placeholder: 'Select a background' });
}


export async function loadArmors() {
const data = await fetchJson('/api/reference/armor');
cache.armors = data || [];
populateSelect($('#armorName'), cache.armors, {
placeholder: 'Choose armor',
optionFor: (a) => {
const label = guessName(a);
const canonical = a?.name || a?.armorName || a?.displayName || label;
return { label, value: String(canonical) };
},
});
const status = $('#armorStatus');
if (status) status.textContent = `Loaded ${cache.armors.length} armors`;
}


export async function loadShields() {
const data = await fetchJson('/api/reference/shields');
cache.shields = data || [];
populateSelect($('#shieldName'), cache.shields, {
placeholder: 'Choose shield',
optionFor: (s) => {
const label = guessName(s);
const canonical = s?.name || s?.shieldName || s?.displayName || label;
return { label, value: String(canonical) };
},
});
}


export async function loadLanguages() {
try {
const data = await fetchJson('/api/reference/languages');
cache.languages = (data || [])
.map((x) => (typeof x === 'string' ? x : x.name || x.languageName))
.filter(Boolean);
} catch {
cache.languages = [];
}
}


export async function loadSkills() {
try {
const data = await fetchJson('/api/reference/skills');
cache.skills = (data || []).map(String);
} catch {
cache.skills = [];
}
}


export async function loadAlignments() {
try {
const data = await fetchJson('/api/enums/alignments');
const items = (data || []).map((x) => ({ label: prettyEnum(x), value: String(x) }));
const sel = $('#characterAlignment');
sel.innerHTML = ['<option value="" selected disabled hidden>Select alignment</option>']
.concat(items.map((it) => `<option value="${it.value}">${it.label}</option>`))
.join('');
} catch (e) {
const tokens = [
'LAWFUL_GOOD','NEUTRAL_GOOD','CHAOTIC_GOOD','LAWFUL_NEUTRAL','NEUTRAL','CHAOTIC_NEUTRAL','LAWFUL_EVIL','NEUTRAL_EVIL','CHAOTIC_EVIL',
];
const items = tokens.map((x) => ({ label: prettyEnum(x), value: x }));
$('#characterAlignment').innerHTML = ['<option value="" selected disabled hidden>Select alignment</option>']
.concat(items.map((it) => `<option value="${it.value}">${it.label}</option>`))
.join('');
}
}