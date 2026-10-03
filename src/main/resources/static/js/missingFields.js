// ==============================
// FILE: /js/missingFields.js
// ==============================
// Pure "what's still missing?" check behind the soft warning next to the
// Generate PDF button. No DOM access: it reads the same payload object
// buildCharacterPayload() sends, so the warning always describes exactly
// what the backend will receive.

/** Fields worth warning about, in form order: [payload key, display label]. */
const WARN_FIELDS = [
  ['characterName', 'Name'],
  ['characterLevel', 'Level'],
  ['characterClass', 'Class'],
  ['characterRace', 'Race'],
  ['characterBackground', 'Background'],
  ['characterAlignment', 'Alignment'],
];

const ABILITY_FIELDS = [
  ['characterStrength', 'Strength'],
  ['characterDexterity', 'Dexterity'],
  ['characterConstitution', 'Constitution'],
  ['characterIntelligence', 'Intelligence'],
  ['characterWisdom', 'Wisdom'],
  ['characterCharisma', 'Charisma'],
];

/**
 * Whether a payload value counts as "not filled in". Covers every shape an
 * empty field actually arrives in (verified in the browser, in all three
 * ability-score modes):
 *  - name: '' or whitespace - a text input always submits its value
 *  - class/race/background/alignment: key absent - an unpicked <select>'s
 *    only selected option is its disabled placeholder, which FormData skips
 *  - level and ability scores: key present but undefined - payload.js maps
 *    a blank number input to undefined
 */
function isMissing(value) {
  return value == null || (typeof value === 'string' && value.trim() === '');
}

/**
 * Lists the warn-worthy fields a payload is missing. If all six ability
 * scores are missing they collapse into a single "Ability scores" entry;
 * otherwise each missing one is listed by name.
 *
 * @param {Object} payload - output of buildCharacterPayload()
 * @returns {string[]} display labels in form order; empty when nothing is missing
 */
export function findMissingFields(payload) {
  const missing = WARN_FIELDS.filter(([key]) => isMissing(payload[key])).map(([, label]) => label);
  const abilities = ABILITY_FIELDS.filter(([key]) => isMissing(payload[key])).map(([, label]) => label);

  if (abilities.length === ABILITY_FIELDS.length) missing.push('Ability scores');
  else missing.push(...abilities);

  return missing;
}
