// ==============================
// FILE: /js/form.js
// ==============================
import { $, modeValue, setMsg } from './core.js';
import { getChecked } from './ui/pills.js';


export function wireFormSubmit() {
const form = $('#char-form');
const btn = $('#submitBtn');
form?.addEventListener('submit', async (e) => {
e.preventDefault();
setMsg('muted', 'Submitting…');
if (btn) btn.disabled = true;


const fd = new FormData(form);
const payload = Object.fromEntries(fd.entries());


['armorName', 'shieldName', 'subclassName'].forEach((k) => {
if (payload[k] === '' || payload[k] === '(optional)') delete payload[k];
});


['characterLevel','characterStrength','characterDexterity','characterConstitution','characterIntelligence','characterWisdom','characterCharisma']
.forEach((k) => {
if (payload[k] !== undefined) {
const raw = String(payload[k]).trim();
payload[k] = raw === '' ? undefined : Number(raw);
}
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
.map((e) => (e.field || e?.cause?.field || 'field') + ': ' + (e.message || e?.defaultMessage || 'invalid'))
.join('\n');
setMsg('err', `Error ${res.status}${lines ? '\n' + lines : ''}`);
} else {
setMsg('err', `Error ${res.status}: ` + (await res.text()));
}
return;
}
if (ctype.includes('application/pdf')) {
const blob = await res.blob();
if (blob.size < 5) { setMsg('err', 'Server returned empty PDF bytes'); return; }
const url = URL.createObjectURL(blob);
const a = Object.assign(document.createElement('a'), { href: url, download: 'character.pdf' });
document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
setMsg('ok', 'PDF generated. Check your downloads.');
} else {
setMsg('err', 'Unexpected response (not a PDF).');
}
} catch (err) {
setMsg('err', 'Network/JS error: ' + (err.message || err));
} finally {
if (btn) btn.disabled = false;
}
});
}