// ==============================
// FILE: /js/features/subclass.js
// ==============================
import { $, isRAW } from '../core.js';
import { loadSubclasses } from '../api.js';


export function refreshSubclassUI() {
const classEl = $('#characterClass');
const levelEl = $('#characterLevel');
const subclassGroup = $('#subclassGroup');
const subclassSelect = $('#subclassName');
const help = $('#subclassHelp');


const hasClass = !!classEl?.value;
const level = parseInt(levelEl?.value || '1', 10) || 1;


if (!hasClass) {
if (subclassGroup) subclassGroup.hidden = true;
if (subclassSelect) { subclassSelect.disabled = true; subclassSelect.value = ''; }
if (help) help.textContent = 'Pick a class to see subclasses.';
return;
}


if (isRAW() && level < 3) {
if (subclassGroup) subclassGroup.hidden = true;
if (subclassSelect) { subclassSelect.disabled = true; subclassSelect.value = ''; }
if (help) help.textContent = 'Subclass becomes available at level 3.';
return;
}


if (subclassGroup) subclassGroup.hidden = false;
if (subclassSelect) subclassSelect.disabled = false;
if (help) help.textContent = 'Choose a subclass.';
}


export function wireSubclassUI() {
const classEl = $('#characterClass');
const levelEl = $('#characterLevel');


levelEl?.addEventListener('input', refreshSubclassUI);
levelEl?.addEventListener('change', refreshSubclassUI);


classEl?.addEventListener('change', async (e) => {
await loadSubclasses(isRAW() ? e.target.value : null);
refreshSubclassUI();
});
}