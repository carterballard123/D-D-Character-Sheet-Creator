// ==============================
// FILE: /js/rules.js
// ==============================
import { $, toArray } from './core.js';
import { cache, SKILL_FEATURE_BONUSES } from './state.js';

// Looks up a class/subclass/background by its display name, checking the
// handful of name-ish fields different reference-data shapes use.
const findByName = (list, name) => {
  const key = String(name || '').toLowerCase();
  return list.find((it) =>
    [it?.name, it?.className, it?.classID, it?.subclassName].some(
      (n) => String(n || '').toLowerCase() === key
    )
  );
};

/**
 * Computes RAW (Rules As Written) language and skill entitlements for a
 * character: what's granted by class/background, the pool to choose
 * from, and how many picks are left, given the current character level.
 *
 * @param {{class: string|null, background: string|null, race: string|null}} ctx
 */
export function computeRAW(ctx) {
  const cls =
    findByName(cache.classes, ctx.class) || {
      profSkills: { choose: 0, from: [] },
      featuresByLevel: {},
    };
  const bg =
    findByName(cache.backgrounds, ctx.background) || {
      fixedSkills: [],
      languages: { grant: [], choose: 0, from: 'ANY' },
    };
  const level = Number($('#characterLevel').value || 1);

  // class picks
  let classChoose = Number(cls.profSkills.choose || 0);
  const pool = new Set(cls.profSkills.from.map(String));

  // feature-based picks up to current level
  const featureDefs = SKILL_FEATURE_BONUSES[(cls.id || '').toLowerCase()] || [];
  for (let L = 1; L <= level; L++) {
    const feats = toArray(cls.featuresByLevel?.[String(L)]);
    for (const f of feats) {
      const nm = f?.name || '';
      for (const def of featureDefs) {
        if (def.nameRegex.test(nm)) classChoose += def.choose || 0;
      }
    }
  }

  // background grants
  const grantedSkills = new Set(bg.fixedSkills.map(String));

  // languages
  const allLangNames = new Set(
    (cache.languages || []).map((x) =>
      typeof x === 'string' ? x : x.name || x.languageName || String(x)
    )
  );
  const langGrant = new Set((bg.languages?.grant || []).map(String));
  const langFrom =
    bg.languages?.from === 'ANY'
      ? new Set(allLangNames)
      : new Set(toArray(bg.languages?.from).map(String));
  const langChoose = Number(bg.languages?.choose || 0);

  return {
    skills: { grant: grantedSkills, from: pool, choose: classChoose },
    languages: {
      grant: langGrant,
      from: langFrom.size ? langFrom : allLangNames,
      choose: langChoose,
    },
  };
}