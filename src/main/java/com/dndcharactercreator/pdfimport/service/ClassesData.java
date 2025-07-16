package com.dndcharactercreator.pdfimport.service;

import java.util.List;
import java.util.Map;

/*
 * {
	  "classId": "barbarian",
	  "name": "Barbarian",
	  "hitDie": 12,
	  "primaryAbility": "Strength",
	  "proficiencies": {
	    "armor": ["Light Armor", "Medium Armor", "Shields"],
	    "weapons": ["Simple Weapons", "Martial Weapons"],
	    "tools": [],
	    "savingThrows": ["Strength", "Constitution"],
	    "skills": {
	      "choose": 2,
	      "from": ["Animal Handling", "Athletics", "Intimidation", "Nature", "Perception", "Survival"]
	    }
	  },
	  "startingEquipmentOptions": [
	    {
	      "label": "A",
	      "items": ["Greataxe", "Four Handaxes", "Explorer’s Pack", "15 GP"]
	    },
	    {
	      "label": "B",
	      "items": ["75 GP"]
	    }
	  ],
	  "featuresByLevel": {
	    "1": [
	      {
	        "name": "Rage",
	        "type": "classFeature",
	        "uses": 2,
	        "scalesWith": "barbarianLevel"
	      },
	      {
	        "name": "Unarmored Defense",
	        "type": "classFeature"
	      }
	    ],
	    "2": [
	      {
	        "name": "Reckless Attack",
	        "type": "classFeature"
	      },
	      {
	        "name": "Danger Sense",
	        "type": "classFeature"
	      }
	    ],
	    "3": [
	      {
	        "name": "Primal Knowledge",
	        "type": "classFeature",
	        "description": "Gain an extra skill proficiency from the Barbarian list."
	      },
	      {
	        "name": "Rage (3 times)",
	        "type": "classFeature",
	        "uses": 3
	      }
	    ],
	    "4": [
	      {
	        "name": "Ability Score Improvement",
	        "type": "ASI"
	      }
	    ],
	    "5": [
	      {
	        "name": "Extra Attack",
	        "type": "classFeature"
	      },
	      {
	        "name": "Fast Movement",
	        "type": "classFeature"
	      }
	    ],
	    "6": [
	      {
	        "name": "Rage (4 times)",
	        "type": "classFeature",
	        "uses": 4
	      }
	    ],
	    "7": [
	      {
	        "name": "Feral Instinct",
	        "type": "classFeature"
	      }
	    ],
	    "8": [
	      {
	        "name": "Ability Score Improvement",
	        "type": "ASI"
	      }
	    ],
	    "9": [
	      {
	        "name": "Brutal Critical",
	        "type": "classFeature",
	        "diceBonus": 1
	      }
	    ],
	    "10": [
	      {
	        "name": "Rage (5 times)",
	        "type": "classFeature",
	        "uses": 5
	      }
	    ],
	    "11": [
	      {
	        "name": "Relentless Rage",
	        "type": "classFeature"
	      }
	    ],
	    "12": [
	      {
	        "name": "Ability Score Improvement",
	        "type": "ASI"
	      }
	    ],
	    "13": [
	      {
	        "name": "Brutal Critical",
	        "type": "classFeature",
	        "diceBonus": 2
	      }
	    ],
	    "14": [
	      {
	        "name": "Rage (6 times)",
	        "type": "classFeature",
	        "uses": 6
	      }
	    ],
	    "15": [
	      {
	        "name": "Persistent Rage",
	        "type": "classFeature"
	      }
	    ],
	    "16": [
	      {
	        "name": "Ability Score Improvement",
	        "type": "ASI"
	      }
	    ],
	    "17": [
	      {
	        "name": "Brutal Critical",
	        "type": "classFeature",
	        "diceBonus": 3
	      }
	    ],
	    "18": [
	      {
	        "name": "Indomitable Might",
	        "type": "classFeature"
	      }
	    ],
	    "19": [
	      {
	        "name": "Ability Score Improvement",
	        "type": "ASI"
	      }
	    ],
	    "20": [
	      {
	        "name": "Primal Champion",
	        "type": "classFeature",
	        "description": "+4 to Strength and Constitution (max 24)"
	      }
	    ]
	  }
	}
	"classId": "barbarian",
	  "name": "Barbarian",
	  "hitDie": 12,
	  "primaryAbility": "Strength",
	  "proficiencies": {
	    "armor": ["Light Armor", "Medium Armor", "Shields"],
	    "weapons": ["Simple Weapons", "Martial Weapons"],
	    "tools": [],
	    "savingThrows": ["Strength", "Constitution"],
	    "skills": {
	      "choose": 2,
	      "from": ["Animal Handling", "Athletics", "Intimidation", "Nature", "Perception", "Survival"]
	    }
 */

public class ClassesData {
	
	private String classID;
	private String name;
	private int hitDie;
	private String primaryAbility;
	private Proficiencies proficiencies;
	private List<StartingEquipmentOptions> startingEquipmentOptions;
	private Map<String, List<Feature>> featuresByLevel;
	
	public String getClassID() { return this.classID; }
	public void setClassID(String classID) { this.classID = classID; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public int getHitDie() { return hitDie; }
	public void setHitDie(int hitDie) { this.hitDie = hitDie; }
	
	public String getPrimaryAbility() { return primaryAbility; }
	public void setPrimaryAbility(String primaryAbility) { this.primaryAbility = primaryAbility; }
	
	public Proficiencies getProficiencies() { return proficiencies; }
	public void setProficiencies(Proficiencies proficiencies) { this.proficiencies = proficiencies; }
	
	public List<StartingEquipmentOptions> getStartingEquipmentOptions() { return startingEquipmentOptions; }
	public void setStartingEquipmentOptions(List<StartingEquipmentOptions> startingEquipmentOptions) { this.startingEquipmentOptions = startingEquipmentOptions; }
	
	public Map<String, List<Feature>> getFeatureByLevel() { return featuresByLevel; }
	public void setFeaturesByLevel(Map<String, List<Feature>> featuresByLevel) { this.featuresByLevel = featuresByLevel; }
	
	
	
	
}
