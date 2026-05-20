package com.dndcharactercreator.pdfimport.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model representing a character background definition.
 *
 * <p>This class is used to deserialize background reference data from JSON files
 * (e.g., {@code backgrounds.json}). It defines the benefits and options granted
 * by a background, but contains no business logic.
 *
 * <p>Application of these benefits (such as granting proficiencies, feats,
 * or equipment) is handled by higher-level services.
 *
 * @author Carter Ballard
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BackgroundData {

    /** Display name of the background (e.g., "Acolyte"). */
    @JsonProperty("name")
    private String name;

    /**
     * Ability scores associated with this background.
     *
     * <p>Interpretation (e.g., choose one, apply bonus, etc.) is handled
     * by rules logic elsewhere.
     */
    @JsonProperty("abilityScores")
    private List<String> abilityScores;

    /** Feat granted by this background, if any. */
    @JsonProperty("feat")
    private String feat;

    /** Skill proficiencies granted by this background. */
    @JsonProperty("skillProficiencies")
    private List<String> skillProficiencies;

    /**
     * Starting equipment choice sets granted by this background.
     *
     * <p>Typically represents mutually exclusive options (e.g., Option A or B).
     */
    @JsonProperty("startingEquipmentOptions")
    private List<StartingEquipmentOption> startingEquipmentOptions;

    // ----- Getters and setters -----

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAbilityScores() {
        return abilityScores;
    }

    public void setAbilityScores(List<String> abilityScores) {
        this.abilityScores = abilityScores;
    }

    public String getFeat() {
        return feat;
    }

    public void setFeat(String feat) {
        this.feat = feat;
    }

    public List<String> getSkillProficiencies() {
        return skillProficiencies;
    }

    public void setSkillProficiencies(List<String> skillProficiencies) {
        this.skillProficiencies = skillProficiencies;
    }

    public List<StartingEquipmentOption> getStartingEquipmentOptions() {
        return startingEquipmentOptions;
    }

    public void setStartingEquipmentOptions(List<StartingEquipmentOption> startingEquipmentOptions) {
        this.startingEquipmentOptions = startingEquipmentOptions;
    }

    /**
     * Represents one selectable starting equipment option for a background.
     *
     * <p>Each option is typically labeled (e.g., "A" or "B") and contains
     * a list of item descriptions.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StartingEquipmentOption {

        /** Label identifying this option (e.g., "A", "B"). */
        @JsonProperty("label")
        private String label;

        /** List of item descriptions included in this option. */
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
}
