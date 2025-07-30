package com.dndcharactercreator.pdfimport.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BackgroundData {

    /** JSON “name” field, e.g. “Acolyte” */
    private String name;

    /** JSON “abilityScores” array */
    private List<String> abilityScores;

    /** JSON “feat” field */
    private String feat;

    /** JSON “skillProficiencies” array */
    private List<String> skillProficiencies;

    /** JSON “startingEquipmentOptions” array */
    private List<StartingEquipmentOption> startingEquipmentOptions;

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
     * Represents one of the “startingEquipmentOptions” entries,
     * with a label (“A” or “B”) and the list of item strings.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StartingEquipmentOption {
        private String label;
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
