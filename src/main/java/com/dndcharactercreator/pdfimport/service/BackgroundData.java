package com.dndcharactercreator.pdfimport.service;

import java.util.List;

/*
 * {
    "name": "Acolyte",
    "abilityScores": ["Intelligence", "Wisdom", "Charisma"],
    "feat": "Magic Initiate (Cleric)",
    "skillProficiencies": ["Insight", "Religion"],
    "startingEquipmentOptions": [
      {
        "label": "A",
        "items": [
          "Holy Symbol",
          "Prayer Book",
          "5 sticks of incense",
          "Vestments",
          "Common clothes",
          "Pouch (15 gp)"
        ]
      },
      {
        "label": "B",
        "items": []
      }
    ]
  }
 */

public class BackgroundData {

    /** JSON “name” field, e.g. “Acolyte” */
    private String backgroundName;

    /** JSON “abilityScores” array */
    private List<String> abilityScores;

    /** JSON “feat” field */
    private String feat;

    /** JSON “skillProficiencies” array */
    private List<String> skillProficiencies;

    /** JSON “startingEquipmentOptions” array */
    private List<StartingEquipmentOption> startingEquipmentOptions;

    public String getName() {
        return backgroundName;
    }

    public void setBackGroundName(String backgroundName) {
        this.backgroundName = backgroundName;
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
