package com.dndcharactercreator.pdfimport.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model representing a D&D class definition loaded from reference JSON.
 *
 * <p>This class is used by Jackson to deserialize entries from {@code classes.json}.
 * It is a pure data holder (no business logic). Rule enforcement and calculations
 * (e.g., proficiency bonus, class feature application, hit point totals) should be
 * handled in service-layer code.
 *
 * <p>Nested classes represent structured sections of a class definition such as
 * proficiencies, skill choice rules, starting equipment options, and features.
 *
 * @author Carter Ballard
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassesData {

    /** Unique identifier for the class (used for lookups; stable even if display name changes). */
    @JsonProperty("classID")
    private String classID;

    /** Display name of the class (e.g., "Fighter"). */
    @JsonProperty("name")
    private String name;

    /** Hit die size for the class (e.g., 10 means d10). */
    @JsonProperty("hitDie")
    private Integer hitDie;

    /** Primary ability for the class (e.g., "Strength", "Charisma"). */
    @JsonProperty("primaryAbility")
    private String primaryAbility;

    /** Proficiencies granted by the class. */
    @JsonProperty("proficiencies")
    private Proficiencies proficiencies;

    /**
     * Starting equipment options for the class.
     *
     * <p>Usually represents labeled choice sets (e.g., Option A or B).
     */
    @JsonProperty("startingEquipmentOptions")
    private List<StartingEquipmentOptions> startingEquipmentOptions;

    /**
     * Class features organized by level.
     *
     * <p>Key is typically the level as a string (e.g., "1", "2", ...),
     * and the value is the list of features gained at that level.
     */
    @JsonProperty("featuresByLevel")
    private Map<String, List<Feature>> featuresByLevel;

    // ----- Getters and setters -----

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHitDie() {
        return hitDie;
    }

    public void setHitDie(Integer hitDie) {
        this.hitDie = hitDie;
    }

    public String getPrimaryAbility() {
        return primaryAbility;
    }

    public void setPrimaryAbility(String primaryAbility) {
        this.primaryAbility = primaryAbility;
    }

    public Proficiencies getProficiencies() {
        return proficiencies;
    }

    public void setProficiencies(Proficiencies proficiencies) {
        this.proficiencies = proficiencies;
    }

    public List<StartingEquipmentOptions> getStartingEquipmentOptions() {
        return startingEquipmentOptions;
    }

    public void setStartingEquipmentOptions(List<StartingEquipmentOptions> startingEquipmentOptions) {
        this.startingEquipmentOptions = startingEquipmentOptions;
    }

    /**
     * Returns the features map keyed by class level.
     *
     * @return map of level -> list of features
     */
    public Map<String, List<Feature>> getFeaturesByLevel() {
        return featuresByLevel;
    }

    public void setFeaturesByLevel(Map<String, List<Feature>> featuresByLevel) {
        this.featuresByLevel = featuresByLevel;
    }

    /**
     * Represents the proficiencies granted by a class.
     *
     * <p>This includes armor/weapon/tool proficiencies, saving throws, and the
     * rules for skill choices (how many skills to choose and from what list).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Proficiencies {

        /** Armor categories/proficiencies granted by the class. */
        @JsonProperty("armorProficiencies")
        private List<String> armorProficiencies;

        /** Weapon proficiencies granted by the class. */
        @JsonProperty("weaponProficiencies")
        private List<String> weaponProficiencies;

        /** Tool proficiencies granted by the class. */
        @JsonProperty("toolProficiencies")
        private List<String> toolProficiencies;

        /** Saving throw proficiencies granted by the class. */
        @JsonProperty("savingThrowProficiencies")
        private List<String> savingThrowProficiencies;

        /** Skill choice rule (how many skills, and optional limited list). */
        @JsonProperty("skills")
        private SkillChoices skills;

        public List<String> getArmorProficiencies() {
            return armorProficiencies;
        }

        public void setArmorProficiencies(List<String> armorProficiencies) {
            this.armorProficiencies = armorProficiencies;
        }

        public List<String> getWeaponProficiencies() {
            return weaponProficiencies;
        }

        public void setWeaponProficiencies(List<String> weaponProficiencies) {
            this.weaponProficiencies = weaponProficiencies;
        }

        public List<String> getToolProficiencies() {
            return toolProficiencies;
        }

        public void setToolProficiencies(List<String> toolProficiencies) {
            this.toolProficiencies = toolProficiencies;
        }

        public List<String> getSavingThrowProficiencies() {
            return savingThrowProficiencies;
        }

        public void setSavingThrowProficiencies(List<String> savingThrowProficiencies) {
            this.savingThrowProficiencies = savingThrowProficiencies;
        }

        /**
         * Returns the skill choice rule for the class.
         *
         * @return skill choice rule
         */
        public SkillChoices getSkillChoices() {
            return skills;
        }

        public void setSkillChoices(SkillChoices skills) {
            this.skills = skills;
        }
    }

    /**
     * Represents a rule for choosing a number of skills from a list.
     *
     * <p>Example: choose 2 from a limited list of 6 skills.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillChoices {

        /** How many skills must be chosen. */
        @JsonProperty("choose")
        private Integer choose;

        /**
         * Optional limited list of skill names the user may choose from.
         *
         * <p>If absent/null, the UI/rules may treat it as "choose from all skills".
         */
        @JsonProperty("limitedSkillList")
        private List<String> limitedSkillList;

        public Integer getChoose() {
            return choose;
        }

        public void setChoose(Integer choose) {
            this.choose = choose;
        }

        public List<String> getLimitedSkillList() {
            return limitedSkillList;
        }

        public void setLimitedSkillList(List<String> limitedSkillList) {
            this.limitedSkillList = limitedSkillList;
        }
    }

    /**
     * Represents one labeled starting equipment option set for a class.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StartingEquipmentOptions {

        /** Label for the option (e.g., "A", "B"). */
        @JsonProperty("label")
        private String label;

        /** List of item description strings for this option. */
        @JsonProperty("items")
        private List<String> items;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items = items;
        }
    }

    /**
     * Represents a class feature granted at a particular level.
     *
     * <p>Features are grouped by level in {@link #featuresByLevel}.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {

        /** Feature name (e.g., "Second Wind"). */
        @JsonProperty("name")
        private String name;

        /** Feature type/category (if your data distinguishes them). */
        @JsonProperty("type")
        private String type;

        /** Description text for the feature. */
        @JsonProperty("description")
        private String description;

        /** Usage rules (e.g., "PB/LR", "1/LR"), if present. */
        @JsonProperty("uses")
        private String uses;

        /** Scaling rules (e.g., "level", "proficiency bonus"), if present. */
        @JsonProperty("scalesWith")
        private String scalesWith;

        /** Dice bonus if the feature provides a die-based bonus (optional). */
        @JsonProperty("diceBonus")
        private Integer diceBonus;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUses() {
            return uses;
        }

        public void setUses(String uses) {
            this.uses = uses;
        }

        public String getScalesWith() {
            return scalesWith;
        }

        public void setScalesWith(String scalesWith) {
            this.scalesWith = scalesWith;
        }

        public Integer getDiceBonus() {
            return diceBonus;
        }

        public void setDiceBonus(Integer diceBonus) {
            this.diceBonus = diceBonus;
        }
    }
}