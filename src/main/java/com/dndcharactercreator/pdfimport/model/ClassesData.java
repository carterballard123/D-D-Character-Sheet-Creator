package com.dndcharactercreator.pdfimport.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassesData {
	
	private String classID;
	private String name;
	private Integer hitDie;
	private String primaryAbility;
	private Proficiencies proficiencies;
	private List<StartingEquipmentOptions> startingEquipmentOptions;
	private Map<String, List<Feature>> featuresByLevel;
	
	public String getClassID() { return this.classID; }
	public void setClassID(String classID) { this.classID = classID; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public Integer getHitDie() { return hitDie; }
	public void setHitDie(Integer hitDie) { this.hitDie = hitDie; }
	
	public String getPrimaryAbility() { return primaryAbility; }
	public void setPrimaryAbility(String primaryAbility) { this.primaryAbility = primaryAbility; }
	
	public Proficiencies getProficiencies() { return proficiencies; }
	public void setProficiencies(Proficiencies proficiencies) { this.proficiencies = proficiencies; }
	
	public List<StartingEquipmentOptions> getStartingEquipmentOptions() { return startingEquipmentOptions; }
	public void setStartingEquipmentOptions(List<StartingEquipmentOptions> startingEquipmentOptions) { this.startingEquipmentOptions = startingEquipmentOptions; }
	
	public Map<String, List<Feature>> getFeatureByLevel() { return featuresByLevel; }
	public void setFeaturesByLevel(Map<String, List<Feature>> featuresByLevel) { this.featuresByLevel = featuresByLevel; }
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Proficiencies {
		
		private List<String> armorProficiencies;
		private List<String> weaponProficiencies;
		private List<String> toolProficiencies;
		private List<String> savingThrowProficiencies;
		private skillChoices skills;
		
		public List<String> getArmorProficiencies() { return armorProficiencies; }
		public void setArmorProficiencies(List<String> armorProficiencies) { this.armorProficiencies = armorProficiencies; }
		
		public List<String> getWeaponProficiencies() { return weaponProficiencies; }
		public void setWeaponProficiencies(List<String> weaponProficiencies) { this.weaponProficiencies = weaponProficiencies; }
		
		public List<String> getToolProficiencies() { return toolProficiencies; }
		public void setToolProficiencies(List<String> toolProficiencies) { this.toolProficiencies = toolProficiencies; }
		
		public List<String> getSavingThrowProficiencies() { return savingThrowProficiencies; }
		public void setSavingThrowProficiencies(List<String> savingThrowProficiencies) { this.savingThrowProficiencies = savingThrowProficiencies; }
		
		public skillChoices getSkillChoice() { return skills; }
		public void setSkillChoices(skillChoices skills) { this.skills = skills; }
		
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class skillChoices {
		
		private Integer choose;
		private List<String> limitedSkillList;
		
		public Integer getChoose() { return choose; }
		public void setChoose(Integer choose) { this.choose = choose; }
		
		public List<String> getLimitedSkillList() { return limitedSkillList; }
		public void setLimitedSkillList(List<String> limitedSkillList) { this.limitedSkillList = limitedSkillList; }
		
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StartingEquipmentOptions {
		
		private String label;
		private List<String> items;
		
		public String getLabel() { return label; }
		public void setLabel(String label) { this.label = label; }
		
		public List<String> getItems() { return items; }
		public void setItems(List<String> items) { this.items = items; }
		
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Feature {
		
		private String name;
        private String type;
        private String description;
        private String uses;
        private String scalesWith;
        private Integer diceBonus;
        
        public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		
		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		
		public String getUses() { return uses; }
		public void setUses(String uses) { this.uses = uses; }
		
		public String getScalesWith() { return scalesWith; }
		public void setScalesWith(String scalesWith) { this.scalesWith = scalesWith; }
		
		public Integer getDiceBonus() { return diceBonus; }
		public void setDiceBonus(Integer diceBonus) { this.diceBonus = diceBonus; }
		
	}
	
}
