// ==============================
// FILE: /js/features/pdfPreview.js
// ==============================
import { $ } from '../core.js';
import { buildCharacterPayload } from '../payload.js';

// The blob URL currently loaded in the preview iframe, if any - tracked
// so it can be revoked right after being replaced. Without this, every
// refresh leaks another blob for the life of the page.
let currentObjectUrl = null;

// The AbortController of the newest refresh. Starting a refresh aborts the
// previous one, so at most one request is ever "live", and a refresh whose
// signal is aborted knows it has been superseded.
let latestController = null;

// Whether a refresh is already scheduled for the current task (see wirePdfPreview).
let refreshScheduled = false;

/**
 * Rebuilds the current form state into a CharacterDto payload, posts it
 * to /api/pdf/fill, and points the live preview iframe at the result.
 *
 * Latest request wins: only the newest refresh may touch the iframe or the
 * status line. Starting a refresh aborts the one before it, which cancels
 * that request if it's still in flight, and any older refresh that resumes
 * after a newer one has started - whether it succeeded, failed, or was
 * aborted - returns without touching the UI. Without this, responses that
 * came back out of order could leave the preview (or its status line)
 * showing an older version of the character than the form.
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

  latestController?.abort();
  const controller = new AbortController();
  latestController = controller;
  // Checked after every await: abort() alone isn't enough, because a
  // response that had already arrived can't be cancelled - only ignored.
  const superseded = () => controller.signal.aborted;

  try {
    const res = await fetch('/api/pdf/fill', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      signal: controller.signal,
    });
    if (superseded()) return;

    if (!res.ok) {
      if (status) status.textContent = 'Preview couldn’t update (check for an invalid value) — showing the last good version.';
      return;
    }

    const blob = await res.blob();
    if (superseded()) return;
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
    // An abort is how a newer refresh cancels this one - not an error, and
    // the newer refresh owns the status line now.
    if (superseded()) return;
    if (status) status.textContent = 'Preview couldn’t update (network error) — showing the last good version.';
  }
}

/**
 * Schedules one refresh for the end of the current task, however many
 * changes ask for it before then. Roll → "Apply in order" sets all six
 * ability scores in one click, each firing its own 'change'; this turns
 * that burst into a single request instead of six. A single edit still
 * refreshes right away - setTimeout(0) only waits for the current task
 * to finish, not for any noticeable amount of time.
 */
function scheduleRefresh() {
  if (refreshScheduled) return;
  refreshScheduled = true;
  setTimeout(() => {
    refreshScheduled = false;
    refreshPdfPreview();
  }, 0);
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
 *
 * Changes are funneled through scheduleRefresh() rather than refreshing
 * directly, so a burst of changes in one task becomes one request.
 */
export function wirePdfPreview() {
  const form = $('#char-form');
  form?.addEventListener('change', scheduleRefresh);
}
