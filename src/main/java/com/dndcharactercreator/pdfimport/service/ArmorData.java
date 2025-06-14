package com.dndcharactercreator.pdfimport.service;

/*
 * "name": "Padded Armor",
	    "type": "Light Armor",
	    "baseAc": 11,
	    "maxDexBonus": 255,
	    "minStrength": 0,
	    "stealthDisadvantage": true,
	    "weightLbs": 8,
	    "costGp": 5,
	    "notes": ""
 */
public class ArmorData {
	String armorName;
	String armorType;
	int baseArmorClass;
	int maxDexBonus;
	int minStrengthReq;
	boolean stealthDisadvantage;
	int armorWeight;
	int armorCost;
	String armorNotes;
	
	// getters & setters
	public String getArmorName() { return armorName; }
	public void setArmorName(String armorName) { this.armorName = armorName; }
	
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
	public int getBaseAC() {
		// TODO Auto-generated method stub
		return 0;
	}

}
