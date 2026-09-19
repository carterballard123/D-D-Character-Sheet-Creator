// ==============================
// FILE: /js/features/pdfPreview.js
// ==============================
import { $ } from '../core.js';
import { buildCharacterPayload } from '../payload.js';

// The blob URL currently loaded in the preview iframe, if any - tracked
// so it can be revoked right after being replaced. Without this, every
// refresh leaks another blob for the life of the page.
let currentObjectUrl = null;

/**
 * Rebuilds the current form state into a CharacterDto payload, posts it
 * to /api/pdf/fill, and points the live preview iframe at the result.
 *
 * On a validation failure (still possible even though most fields are
 * now optional - e.g. an out-of-range ability score, or too many
 * weapons) or a network error, the current preview is left exactly as
 * it is - a mid-typing invalid value shouldn't blank out an otherwise
 * good preview - and a small non-blocking status line reports the
 * failure instead.
 */
export async function refreshPdfPreview() {
  const frame = $('#pdfPreviewFrame');
  if (!frame) return;

  const status = $('#pdfPreviewStatus');
  const payload = buildCharacterPayload();

  try {
    const res = await fetch('/api/pdf/fill', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      if (status) status.textContent = 'Preview couldn’t update (check for an invalid value) — showing the last good version.';
      return;
    }

    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const previousUrl = currentObjectUrl;

    frame.src = url;
    currentObjectUrl = url;

    // Revoke the old URL only after the new one is in place, not before -
    // swapping src first means the iframe never has to (however briefly)
    // point at nothing.
    if (previousUrl) URL.revokeObjectURL(previousUrl);

    if (status) status.textContent = '';
  } catch (err) {
    if (status) status.textContent = 'Preview couldn’t update (network error) — showing the last good version.';
  }
}

/**
 * Wires the live preview to refresh on every completed edit anywhere in
 * the character form. Call once at boot.
 *
 * This uses a single delegated 'change' listener on the form itself,
 * rather than attaching a listener to each individual field, for two
 * reasons:
 *  - 'change' bubbles, so one listener on the form catches every input,
 *    select, checkbox and radio inside it - including the class/race
 *    card picker, which sets the underlying <select>'s value and
 *    dispatches a real bubbling 'change' event for exactly this reason.
 *  - The language/skill checkboxes are regenerated from scratch on
 *    every pill refresh (innerHTML replacement in ui/pills.js), which
 *    would silently destroy any listener attached directly to them.
 *    A listener on the stable <form> element is unaffected by that.
 */
export function wirePdfPreview() {
  const form = $('#char-form');
  form?.addEventListener('change', () => {
    refreshPdfPreview();
  });
}
