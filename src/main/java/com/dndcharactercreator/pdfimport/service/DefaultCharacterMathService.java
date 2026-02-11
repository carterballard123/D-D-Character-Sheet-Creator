package com.dndcharactercreator.pdfimport.service;

import org.springframework.stereotype.Service;

import com.dndcharactercreator.pdfimport.model.ArmorData;
import com.dndcharactercreator.pdfimport.model.ClassesData;
import com.dndcharactercreator.pdfimport.model.ShieldData;
import com.dndcharactercreator.pdfimport.repository.ArmorRepository;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;
import com.dndcharactercreator.pdfimport.repository.ShieldRepository;

/**
 * Default implementation of {@link CharacterMathService}.
 *
 * <p>This service applies standard D&D 5e-style rules to compute character
 * statistics using reference data loaded from JSON repositories.
 *
 * <p>It relies on repositories to resolve class, armor, and shield definitions
 * and does not store any state of its own.
 *
 * @author Carter Ballard
 */
@Service
public class DefaultCharacterMathService implements CharacterMathService {

    private final ClassesRepository classesRepo;
    private final ArmorRepository armorRepo;
    private final ShieldRepository shieldRepo;

    public DefaultCharacterMathService(
            ClassesRepository classesRepo,
            ArmorRepository armorRepo,
            ShieldRepository shieldRepo
    ) {
        this.classesRepo = classesRepo;
        this.armorRepo   = armorRepo;
        this.shieldRepo  = shieldRepo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int computeModifier(int abilityScore) {
        return (abilityScore - 10) / 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int computeProficiency(int level) {
        return 2 + (level - 1) / 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int computeMaxHP(String characterClass, int level, int conMod) {
        ClassesData cls = classesRepo.findByID(characterClass)
            .orElseThrow(() ->
                new IllegalArgumentException("Unknown class: " + characterClass)
            );

        int hitDie = cls.getHitDie();
        int avgHitDie = (hitDie / 2) + 1;

        // Level 1: maximum hit die + CON modifier
        int hp = hitDie + conMod;

        // Levels 2+
        for (int lvl = 2; lvl <= level; lvl++) {
            hp += avgHitDie + conMod;
        }

        return hp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int computeAC(
            String className,
            String subClassName,
            int dexMod,
            int conMod,
            int wisMod,
            int chaMod,
            String armorName,
            String shieldName
    ) {

        boolean unarmored =
            armorName == null || armorName.equalsIgnoreCase("Unarmored");

        int baseAC;
        int shieldBonus = 0;

        if (unarmored) {
            if (className.equalsIgnoreCase("Barbarian")) {
                baseAC = 10 + dexMod + conMod;
            } else if (className.equalsIgnoreCase("Monk")) {
                baseAC = 10 + dexMod + wisMod;
            } else if (
                "Draconic Sorcery".equalsIgnoreCase(subClassName)
                || "College of Dance".equalsIgnoreCase(subClassName)
            ) {
                baseAC = 10 + dexMod + chaMod;
            } else {
                baseAC = 10 + dexMod;
            }
        } else {
            ArmorData armor = armorRepo.findByName(armorName);

            if (armor == null) {
                baseAC = 10 + dexMod;
            } else {
                switch (armor.getArmorType().toLowerCase()) {
                    case "heavy armor":
                        baseAC = armor.getBaseArmorClass();
                        break;
                    case "medium armor":
                        baseAC = armor.getBaseArmorClass()
                               + Math.min(dexMod, armor.getMaxDexBonus());
                        break;
                    case "light armor":
                        baseAC = armor.getBaseArmorClass() + dexMod;
                        break;
                    default:
                        baseAC = armor.getBaseArmorClass();
                }
            }
        }

        if (shieldName != null && !shieldName.isBlank()) {
            ShieldData shield = shieldRepo.findByName(shieldName);
            if (shield != null) {
                shieldBonus = shield.getAcBonus();
            }
        }

        return baseAC + shieldBonus;
    }
}
