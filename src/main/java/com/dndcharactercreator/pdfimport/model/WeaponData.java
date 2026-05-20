package com.dndcharactercreator.pdfimport.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model representing a weapon definition loaded from reference JSON.
 *
 * <p>This class is used by Jackson to deserialize entries from {@code weapons.json}.
 * It is a pure data holder (no business logic). Rule enforcement and calculations
 * (e.g., attack modifiers, damage selection, proficiency checks) should be handled
 * in service-layer code.
 *
 * <p>Nested classes represent structured components of a weapon such as cost,
 * damage profile, and range.
 *
 * @author Carter Ballard
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeaponData {
	
	/** Display name of the weapon (e.g., "Longsword", "Light Crossbow"). */
	@JsonProperty("displayName")
	private String displayName;
	
	/** Weapon category used for proficiency grouping (e.g., SIMPLE, MARTIAL). */
	@JsonProperty("category")
	private WeaponCategory category;
	
	/** Weapon type indicating its primary use (e.g., MELEE or RANGED). */
	@JsonProperty("weaponType")
	private WeaponType weaponType;
	
	/** Cost of the weapon in a specific currency unit. */
	@JsonProperty("cost")
	private Cost cost;
	
	/** Weight of the weapon in pounds. */
	@JsonProperty("weightLb")
	private Double weightLb;
	
	/** Damage profile of the weapon (dice and damage type). */
	@JsonProperty("damage")
	private Damage damage;
	
	/** List of properties that define how the weapon behaves mechanically. */
	@JsonProperty("properties")
	private List<WeaponProperty> properties;
	
	/**
	 * Range profile of the weapon.
	 *
	 * <p>Present for ranged weapons and weapons with the THROWN property.
	 */
	@JsonProperty("range")
	private Range range;
	
	/**
	 * Alternate damage dice used when the weapon is wielded with two hands.
	 *
	 * <p>Only applicable if the weapon has the VERSATILE property.
	 */
	@JsonProperty("versatileDamageDice")
	private String versatileDamageDice;
	
	/**
	 * Type of ammunition required by the weapon.
	 *
	 * <p>Only applicable if the weapon has the AMMUNITION property.
	 */
	@JsonProperty("ammunitionType")
	private AmmunitionType ammunitionType;
	
	/** Weapon mastery property (2024 rules), if applicable. */
	@JsonProperty("mastery")
	private WeaponMastery mastery;
	
	// Getters / Setters
	
	/** @return display name of the weapon */
	public String getDisplayName() {
		return displayName;
	}
	
	/** @param displayName sets the display name of the weapon */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	/** @return weapon category (SIMPLE or MARTIAL) */
	public WeaponCategory getCategory() {
		return category;
	}
	
	/** @param category sets the weapon category */
	public void setCategory(WeaponCategory category) {
		this.category = category;
	}
	
	/** @return weapon type (MELEE or RANGED) */
	public WeaponType getWeaponType() {
		return weaponType;
	}
	
	/** @param weaponType sets the weapon type */
	public void setWeaponType(WeaponType weaponType) {
		this.weaponType = weaponType;
	}
	
	/** @return cost object containing amount and currency */
	public Cost getCost() {
		return cost;
	}
	
	/** @param cost sets the weapon cost */
	public void setCost(Cost cost) {
		this.cost = cost;
	}
	
	/** @return weight of the weapon in pounds */
	public Double getWeightLb() {
		return weightLb;
	}
	
	/** @param weightLb sets the weapon weight in pounds */
	public void setWeightLb(Double weightLb) {
		this.weightLb = weightLb;
	}
	
	/** @return damage profile of the weapon */
	public Damage getDamage() {
		return damage;
	}
	
	/** @param damage sets the damage profile */
	public void setDamage(Damage damage) {
		this.damage = damage;
	}
	
	/** @return list of weapon properties */
	public List<WeaponProperty> getProperties() {
		return properties;
	}
	
	/** @param properties sets the weapon properties */
	public void setProperties(List<WeaponProperty> properties) {
		this.properties = properties;
	}
	
	/** @return range profile of the weapon */
	public Range getRange() {
		return range;
	}
	
	/** @param range sets the weapon range profile */
	public void setRange(Range range) { 
		this.range = range;
	}
	
	/** @return versatile damage dice (if applicable) */
	public String getVersatileDamageDice() {
		return versatileDamageDice;
	}
	
	/** @param versatileDamageDice sets the versatile damage dice */
	public void setVersatileDamageDice(String versatileDamageDice) {
		this.versatileDamageDice = versatileDamageDice;
	}
	
	/** @return ammunition type used by the weapon */
	public AmmunitionType getAmmunitionType() {
		return ammunitionType;
	}
	
	/** @param ammunitionType sets the ammunition type */
	public void setAmmunitionType(AmmunitionType ammunitionType) {
		this.ammunitionType = ammunitionType;
	}
	
	/** @return weapon mastery property */
	public WeaponMastery getMastery() {
		return mastery;
	}
	
	/** @param mastery sets the weapon mastery */
	public void setMastery(WeaponMastery mastery) {
		this.mastery = mastery;
	}
	
	// ----- Nested data classes -----
	
	/**
	 * Represents the cost of a weapon.
	 *
	 * <p>Includes both the amount and the currency unit.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Cost {
		
		/** Numeric amount of the cost. */
		@JsonProperty("amount")
		private Integer amount;
		
		/** Currency unit (e.g., CP, SP, GP, PP). */
		@JsonProperty("unit")
		private CurrencyUnit unit;
		
		/** @return cost amount */
		public Integer getAmount() {
			return amount;
		}
		
		/** @param amount sets the cost amount */
		public void setAmount(Integer amount) {
			this.amount = amount;
		}
		
		/** @return currency unit */
		public CurrencyUnit getUnit() {
			return unit;
		}
		
		/** @param unit sets the currency unit */
		public void setUnit(CurrencyUnit unit) {
			this.unit = unit;
		}
	}
	
	/**
	 * Represents the damage profile of a weapon.
	 *
	 * <p>Includes both the dice expression and the damage type.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Damage {
		
		/** Dice expression for damage (e.g., "1d8"). */
		@JsonProperty("dice")
		private String dice;
		
		/** Type of damage dealt (e.g., SLASHING, PIERCING). */
		@JsonProperty("type")
		private DamageType type;
		
		/** @return damage dice expression */
		public String getDice() {
			return dice;
		}
		
		/** @param dice sets the damage dice expression */
		public void setDice(String dice) {
			this.dice = dice;
		}
		
		/** @return damage type */
		public DamageType getType() {
			return type;
		}
		
		/** @param type sets the damage type */
		public void setType(DamageType type) {
			this.type = type;
		}
	}
	
	/**
	 * Represents the range profile of a weapon.
	 *
	 * <p>Used for ranged weapons and weapons with the THROWN property.
	 * The minimum range is the normal range, and the maximum range is
	 * the long range (attacks beyond normal range are typically made with disadvantage).
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Range {
		
		/** Normal (effective) range in feet. */
		@JsonProperty("minRange")
		private Integer minRange;
		
		/** Maximum range in feet. */
		@JsonProperty("maxRange")
		private Integer maxRange;
		
		/** @return normal range in feet */
		public Integer getMinRange() { 
			return minRange;
		}
		
		/** @param minRange sets the normal range in feet */
		public void setMinRange(Integer minRange) {
			this.minRange = minRange;
		}
		
		/** @return maximum range in feet */
		public Integer getMaxRange() {
			return maxRange;
		}
		
		/** @param maxRange sets the maximum range in feet */
		public void setMaxRange(Integer maxRange) {
			this.maxRange = maxRange;
		}
	}
}
