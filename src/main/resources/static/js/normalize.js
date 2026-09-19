// ==============================
// FILE: /js/normalize.js
// ==============================
// Shapes raw backend JSON into the smaller, consistent objects the rest
// of the frontend actually works with.
import { toArray } from './core.js';

export function normalizeClass(c) {
  return {
    id: c.classID || c.name,
    name: c.name || c.classID,
    hitDie: c.hitDie,
    primaryAbility: c.primaryAbility,
    savingThrows: toArray(c?.proficiencies?.savingThrows).map(String),
    profSkills: {
      choose: c?.proficiencies?.skills?.choose ?? 0,
      from: toArray(c?.proficiencies?.skills?.from).map(String),
    },
    featuresByLevel: c?.featuresByLevel || {},
    _raw: c,
  };
}

export function normalizeBackground(b) {
  // Optional languages shape on background:
  // { grant:["Common"], choose:1, from:"ANY" } or from:["Dwarvish","Elvish"]
  const lang = b.languages || {};
  return {
    name: b.name,
    fixedSkills: toArray(b.skillProficiencies).map(String),
    languages: {
      grant: toArray(lang.grant).map(String),
      choose: Number(lang.choose || 0),
      from: lang.from || 'ANY',
    },
    _raw: b,
  };
}

export function normalizeRace(r) {
  return { name: r.name, _raw: r };
}

/**
 * Best-effort display name for an arbitrary reference-data object, tried
 * in order: a known name-ish field, any key ending in "name", then any
 * string field at all. Used by populateSelect() when no explicit
 * optionFor mapper is given.
 */
export function guessName(obj) {
  if (!obj || typeof obj !== 'object') return String(obj ?? '');
  const order = [
    'name',
    'armorName',
    'raceName',
    'className',
    'backgroundName',
    'shieldName',
    'subclassName',
    'displayName',
    'title',
  ];
  for (const k of order) {
    if (typeof obj[k] === 'string' && obj[k]) return obj[k];
  }
  for (const k of Object.keys(obj)) {
    const v = obj[k];
    if (typeof v === 'string' && /name$/i.test(k)) return v;
  }
  for (const k of Object.keys(obj)) {
    const v = obj[k];
    if (typeof v === 'string') return v;
  }
  return '';
}
