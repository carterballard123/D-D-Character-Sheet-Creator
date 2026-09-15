// ==============================
// FILE: /js/core.js
// ==============================
// Small, dependency-free helpers shared across the whole app.

/** querySelector shorthand — pass a full CSS selector, e.g. $('#id'). */
export const $ = (sel) => document.querySelector(sel);

/** Turns an ENUM_TOKEN into "Enum Token" for display. */
export function prettyEnum(tok) {
  return String(tok || '')
    .toLowerCase()
    .split('_')
    .map((s) => s.charAt(0).toUpperCase() + s.slice(1))
    .join(' ');
}

/** Wraps a value in an array unless it already is one (null/undefined → []). */
export const toArray = (v) => (Array.isArray(v) ? v : v != null ? [v] : []);

/** Currently selected mode radio value ('raw' or 'freedom'), default 'raw'. */
export const modeValue = () =>
  document.querySelector('input[name="mode"]:checked')?.value || 'raw';

export const isRAW = () => modeValue() === 'raw';

/** Updates the status message bar (#msg) with a style class and text. */
export function setMsg(kind, text) {
  const el = $('#msg');
  el.className = 'msg ' + (kind || 'muted');
  el.textContent = text;
}