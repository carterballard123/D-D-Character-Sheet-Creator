// ==============================
// FILE: /js/ui/selects.js
// ==============================
import { guessName } from '../normalize.js';


export function populateSelect(sel, items, { placeholder, sort = true, optionFor } = {}) {
const arr = Array.from(items || []);
const pairs = arr
.map((it) => {
if (optionFor) return optionFor(it);
const label = (typeof it === 'string') ? it : guessName(it);
return { label, value: label };
})
.filter((x) => x.label && x.value);


if (sort) pairs.sort((a, b) => a.label.localeCompare(b.label));


const opts = [];
if (placeholder) opts.push(`<option value="" selected disabled hidden>${placeholder}</option>`);
for (const it of pairs) opts.push(`<option value="${it.value}">${it.label}</option>`);
sel.innerHTML = opts.join('');
}