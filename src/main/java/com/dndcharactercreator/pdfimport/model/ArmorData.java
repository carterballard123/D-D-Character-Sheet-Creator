package com.dndcharactercreator.pdfimport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model representing an armor definition loaded from JSON reference data.
 *
 * <p>This class is used by Jackson to deserialize armor entries from
 * {@code armors.json}. It contains no business logic and serves purely
 * as a structured data holder.
 *
 * <p>All rule enforcement and calculations (e.g., armor proficiency,
 * Dexterity limits, AC calculation) are handled elsewhere.
 *
 * @author Carter Ballard
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArmorData {

    /** Display name of the armor (e.g., "Chain Mail"). */
    @JsonProperty("armorName")
    private String armorName;

    /** Armor category (e.g., Light, Medium, Heavy). */
    @JsonProperty("armorType")
    private String armorType;

    /** Base Armor Class provided by the armor. */
    @JsonProperty("armorClass")
    private int baseArmorClass;

    /** Maximum Dexterity modifier allowed (0 for heavy armor). */
    @JsonProperty("maxDexBonus")
    private int maxDexBonus;

    /** Minimum Strength required to wear without penalty. */
    @JsonProperty("minStrengthReq")
    private int minStrengthReq;

    /** Whether wearing this armor imposes disadvantage on Stealth checks. */
    @JsonProperty("stealthDisadvantage")
    private boolean stealthDisadvantage;

    /** Weight of the armor (units defined by your data convention). */
    @JsonProperty("armorWeight")
    private int armorWeight;

    /** Cost of the armor (units defined by your data convention). */
    @JsonProperty("armorCost")
    private int armorCost;

    /** Additional notes or special rules for the armor. */
    @JsonProperty("armorNotes")
    private String armorNotes;

    // ----- Getters and setters -----

    public String getArmorName() {
        return armorName;
    }

    public void setArmorName(String armorName) {
        this.armorName = armorName;
    }

    public String getArmorType() {
        return armorType;
    }

    public void setArmorType(String armorType) {
        this.armorType = armorType;
    }

    public int getBaseArmorClass() {
        return baseArmorClass;
    }

    public void setBaseArmorClass(int baseArmorClass) {
        this.baseArmorClass = baseArmorClass;
    }

    public int getMaxDexBonus() {
        return maxDexBonus;
    }

    public void setMaxDexBonus(int maxDexBonus) {
        this.maxDexBonus = maxDexBonus;
    }

    public int getMinStrengthReq() {
        return minStrengthReq;
    }

    public void setMinStrengthReq(int minStrengthReq) {
        this.minStrengthReq = minStrengthReq;
    }

    public boolean isStealthDisadvantage() {
        return stealthDisadvantage;
    }

    public void setStealthDisadvantage(boolean stealthDisadvantage) {
        this.stealthDisadvantage = stealthDisadvantage;
    }

    public int getArmorWeight() {
        return armorWeight;
    }

    public void setArmorWeight(int armorWeight) {
        this.armorWeight = armorWeight;
    }

    public int getArmorCost() {
        return armorCost;
    }

    public void setArmorCost(int armorCost) {
        this.armorCost = armorCost;
    }

    public String getArmorNotes() {
        return armorNotes;
    }

    public void setArmorNotes(String armorNotes) {
        this.armorNotes = armorNotes;
    }
}
