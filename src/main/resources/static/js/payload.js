// ==============================
// FILE: /js/payload.js
// ==============================
import { $, modeValue } from './core.js';
import { getChecked } from './ui/pills.js';

/**
 * Builds the CharacterDto-shaped request body from the current form
 * state, ready to JSON.stringify() and POST to /api/pdf/fill.
 *
 * This is the single place that knows how form fields map onto the API
 * payload - both the "Generate PDF" submit handler (form.js) and the
 * live preview (features/pdfPreview.js) call this rather than each
 * building their own copy, so the two can't quietly drift apart.
 */
export function buildCharacterPayload() {
  const form = $('#char-form');
  const fd = new FormData(form);
  const payload = Object.fromEntries(fd.entries());

  // Drop optional selects left at their placeholder value.
  ['armorName', 'shieldName', 'subclassName'].forEach((k) => {
    if (payload[k] === '' || payload[k] === '(optional)') delete payload[k];
  });

  // Coerce numeric fields; blank inputs become undefined rather than 0.
  ['characterLevel', 'characterStrength', 'characterDexterity', 'characterConstitution', 'characterIntelligence', 'characterWisdom', 'characterCharisma']
    .forEach((k) => {
      if (payload[k] !== undefined) {
        const raw = String(payload[k]).trim();
        payload[k] = raw === '' ? undefined : Number(raw);
      }
    });

  payload.languages = getChecked('languages');
  payload.skills = getChecked('skills');
  payload.mode = modeValue();

  return payload;
}
