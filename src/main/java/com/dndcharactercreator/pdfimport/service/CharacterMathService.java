package com.dndcharactercreator.pdfimport.service;

/**
 * Service interface defining core character rules and calculations.
 *
 * <p>This service encapsulates all rule-based numeric calculations for a
 * character, such as ability modifiers, proficiency bonus, hit points, and
 * armor class (AC).
 *
 * <p>The interface allows different implementations (e.g., different editions
 * or rule variants) while keeping controllers and other services decoupled
 * from the underlying math logic.
 *
 * @author Carter Ballard
 */
public interface CharacterMathService {

    /**
     * Computes an ability modifier from a raw ability score.
     *
     * <p>Uses the standard D&D formula:
     * {@code floor((score - 10) / 2)}.
     *
     * @param score the ability score (typically 1–30)
     * @return the corresponding ability modifier
     */
    int computeModifier(int score);

    /**
     * Computes the proficiency bonus based on character level.
     *
     * <p>Uses the standard progression:
     * +2 at levels 1–4, +3 at 5–8, +4 at 9–12, etc.
     *
     * @param level the character level (1–20)
     * @return the proficiency bonus
     */
    int computeProficiency(int level);

    /**
     * Computes the maximum hit points (HP) for a character.
     *
     * <p>This calculation assumes:
     * <ul>
     *   <li>Maximum hit die value at level 1</li>
     *   <li>Average hit die value for subsequent levels</li>
     *   <li>Constitution modifier applied at every level</li>
     * </ul>
     *
     * @param className the character's class identifier or name
     * @param level the character level
     * @param conMod the Constitution modifier
     * @return the computed maximum hit points
     * @throws IllegalArgumentException if the class is unknown
     */
    int computeMaxHP(String className, int level, int conMod);

    /**
     * Computes the character's Armor Class (AC).
     *
     * <p>This method accounts for:
     * <ul>
     *   <li>Unarmored defense rules (e.g., Barbarian, Monk)</li>
     *   <li>Subclass-specific unarmored bonuses</li>
     *   <li>Armor type (light, medium, heavy)</li>
     *   <li>Dexterity bonus limits</li>
     *   <li>Shield bonuses</li>
     * </ul>
     *
     * @param className the character's class
     * @param subClassName the character's subclass (may be null)
     * @param dexMod Dexterity modifier
     * @param conMod Constitution modifier
     * @param wisMod Wisdom modifier
     * @param chaMod Charisma modifier
     * @param armorName equipped armor name (or "Unarmored")
     * @param shieldName equipped shield name (may be null)
     * @return the computed Armor Class value
     */
    int computeAC(
        String className,
        String subClassName,
        int dexMod,
        int conMod,
        int wisMod,
        int chaMod,
        String armorName,
        String shieldName
    );
}
