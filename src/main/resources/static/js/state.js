// ==============================
// FILE: /js/state.js
// ==============================

// Shared, mutable cache of reference data fetched from the backend.
// Populated by the loaders in api.js; read by rules.js, ui/pills.js, etc.
export const cache = {
  classes: [],
  subclasses: [],
  races: [],
  backgrounds: [],
  armors: [],
  shields: [],
  languages: [],
  skills: [],
};

// RAW (Rules As Written) bonuses granted by specific class features.
// Only include entries you actually use — this drives extra skill
// picks in computeRAW() beyond a class's base proficiency choices.
export const SKILL_FEATURE_BONUSES = {
  barbarian: [{ nameRegex: /Primal Knowledge/i, choose: 1, from: 'classList' }],
};
