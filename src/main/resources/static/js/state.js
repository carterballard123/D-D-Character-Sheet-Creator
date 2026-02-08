// ==============================
// FILE: /js/state.js
// ==============================
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


// Only include feature bonuses you actually use
export const SKILL_FEATURE_BONUSES = {
barbarian: [{ nameRegex: /Primal Knowledge/i, choose: 1, from: 'classList' }],
};