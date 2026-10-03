// ==============================
// FILE: /js/features/missingFieldsWarning.js
// ==============================
import { $ } from '../core.js';
import { buildCharacterPayload } from '../payload.js';
import { findMissingFields } from '../missingFields.js';

/**
 * Re-checks the form and updates the soft "Missing: ..." warning next to the
 * Generate PDF button. Purely informational - it never blocks submitting.
 */
export function refreshMissingFieldsWarning() {
  const box = $('#missingWarning');
  if (!box) return;

  const missing = findMissingFields(buildCharacterPayload());
  const text = missing.length
    ? `Missing: ${missing.join(', ')}. You can still generate the PDF - these will be left blank to fill in by hand.`
    : '';

  // #missingWarning is an aria-live region: rewriting identical text on
  // every unrelated edit would make screen readers re-announce it.
  if (box.textContent === text) return;

  box.textContent = text;
  // Emptied rather than [hidden] when nothing is missing - a live region
  // taken out of the accessibility tree isn't reliably announced when it
  // comes back.
  box.classList.toggle('msg', !!text);
  box.classList.toggle('warn', !!text);
}

/**
 * Keeps the warning in sync with every completed edit in the form. Same
 * single delegated 'change' listener as the live PDF preview (see
 * wirePdfPreview() in features/pdfPreview.js for why). Call once at boot.
 */
export function wireMissingFieldsWarning() {
  $('#char-form')?.addEventListener('change', refreshMissingFieldsWarning);
}
