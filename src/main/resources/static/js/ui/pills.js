// ==============================
h.push(`<div class="hint" style="margin:10px 0 6px">${titleChoose} — pick <b>${chooseCount}</b> (left: <b id="${name}-left">${left}</b>)</div>`);
h.push(choosable.map((v) => {
const checked = prevChecked.has(v);
const dis = !checked && maxed ? 'disabled' : '';
return `
<label class="pill ${dis ? 'disabled' : ''}">
<input type="checkbox" name="${name}" value="${v}" ${checked ? 'checked' : ''} ${dis}>
${v}
</label>`;
}).join(''));
}
if (chooseCount === 0 && choosable.length) {
h.push(`<div class="hint" style="margin:10px 0 6px">Optional</div>`);
h.push(choosable.map((v) => `
<label class="pill">
<input type="checkbox" name="${name}" value="${v}" ${prevChecked.has(v) ? 'checked' : ''}>
${v}
</label>`).join(''));
}
box.innerHTML = h.join('\n');


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


export function refreshPills() {
const mode = modeValue();


if (mode === 'freedom') {
const allLangs = new Set((cache.languages || []).map((x) => (typeof x === 'string' ? x : x.name || x.languageName)));
const allSkills = new Set((cache.skills || []).map(String));
renderChoicePills('languagesBox', { titleGrant: 'Granted', titleChoose: 'Languages' }, 'languages', allLangs, new Set(), null, 0);
renderChoicePills('skillsBox', { titleGrant: 'Granted', titleChoose: 'Skills' }, 'skills', allSkills, new Set(), null, 0);
return;
}


const rules = computeRAW({
class: document.getElementById('characterClass')?.value || null,
background: document.getElementById('characterBackground')?.value || null,
race: document.getElementById('characterRace')?.value || null,
});


const langAll = new Set([...rules.languages.grant, ...rules.languages.from]);
renderChoicePills('languagesBox', { titleGrant: 'Granted by Background', titleChoose: 'Languages (RAW)' }, 'languages', langAll, rules.languages.grant, rules.languages.from, rules.languages.choose);


const allSkillNames = new Set((cache.skills || []).map(String));
const skillPool = new Set([...rules.skills.from].filter((s) => allSkillNames.has(s)));
const skillAll = new Set([...rules.skills.grant, ...skillPool]);
renderChoicePills('skillsBox', { titleGrant: 'Granted by Background', titleChoose: 'Class Skill Choices (RAW)' }, 'skills', skillAll, rules.skills.grant, skillPool, rules.skills.choose);
}