package com.dndcharactercreator.pdfimport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArmorData {
	@JsonProperty("armorName")
	String armorName;
	@JsonProperty("armorType")
	String armorType;
	@JsonProperty("baseArmorClass")
	int baseArmorClass;
	@JsonProperty("maxDexBonus")
	int maxDexBonus;
	@JsonProperty("minStrengthReq")
	int minStrengthReq;
	@JsonProperty("stealthDisadvantage")
	boolean stealthDisadvantage;
	@JsonProperty("armorWeight")
	int armorWeight;
	@JsonProperty("armorCost")
	int armorCost;
	@JsonProperty("armorNotes")
	String armorNotes;
	
	// getters & setters
	public String getName() { return armorName; }
	public void setName(String armorName) { this.armorName = armorName; }
	
	public String getArmorType() { return armorType; }
	public void setArmorType(String armorType) { this.armorType = armorType; }
	
	public int getArmorClass() { return baseArmorClass; }
	public void setArmorClass(int baseArmorClass) { this.baseArmorClass = baseArmorClass; }
	
	public int getMaxDexBonus() { return maxDexBonus; }
	public void setMaxDexBonus(int maxDexBonus) { this.maxDexBonus = maxDexBonus; }
	
	public int getMinStrengthReq() { return minStrengthReq; }
	public void setMinStrengthReq(int minStrengthReq) { this.minStrengthReq = minStrengthReq; }
	
	public boolean getStealthDisadvantage() { return stealthDisadvantage; }
	public void setStealthDisadvatange(boolean stealthDisadvantage) { this.stealthDisadvantage = stealthDisadvantage; }
	
	public int getArmorWeight() { return armorWeight; }
	public void setArmorWeight(int armorWeight) { this.armorWeight = armorWeight; }
	
	public int getArmorCost() { return armorCost; }
	public void setArmorCost(int armorCost) { this.armorCost = armorCost; }
	
	public String getArmorNotes() { return armorNotes; }
	public void setArmorNotes(String armorNotes) { this.armorNotes = armorNotes; }

}
