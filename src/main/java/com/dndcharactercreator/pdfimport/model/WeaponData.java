package com.dndcharactercreator.pdfimport.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model representing a weapon definition loaded from reference JSON.
 *
 * <p>This class is used by Jackson to deserialize entries from {@code weapons.json}.
 * It is a pure data holder (no business logic). Rule enforcement and calculations
 * should be handled in service-layer code.
 *
 * @author Carter Ballard
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeaponData {
	
	@JsonProperty("displayName")
	private String displayName;
	
	@JsonProperty("category")
	private WeaponCategory category;
	
	@JsonProperty("weaponType")
	private WeaponType weaponType;
	
	@JsonProperty("cost")
	private Cost cost;
	
	@JsonProperty("weightLb")
	private Integer weightLb;
	
	@JsonProperty("damage")
	private Damage damage;
	
	@JsonProperty("properties")
	private List<WeaponProperty> properties;
	
	@JsonProperty("range")
	private Range range;
	
	@JsonProperty("versatileDamageDice")
	private String versatileDamageDice;
	
	@JsonProperty("ammunitionType")
	private AmmunitionType ammunitionType;
	
	@JsonProperty("mastery")
	private WeaponMastery mastery;
	
	
	
	
	// ----- Nested data classes -----
	
	public static class Cost {
		@JsonProperty("amount")
		private Integer amount;
		
		@JsonProperty("unit")
		private CurrencyUnit unit;
		
		public Integer getAmount() {
			return amount;
		}
		public void setAmount(Integer amount) {
			this.amount = amount;
		}
		
		public CurrencyUnit getUnit() {
			return unit;
		}
		public void setUnit(CurrencyUnit unit) {
			this.unit = unit;
		}
	}
	
	public static class Damage {
		@JsonProperty("dice")
		private String dice;
		
		@JsonProperty("type")
		private DamageType type;
		
		public String getDice() {
			return dice;
		}
		public void setDice(String dice) {
			this.dice = dice;
		}
		
		public DamageType getType() {
			return type;
		}
		public void setType(DamageType type) {
			this.type = type;
		}
	}
	
	public static class Range {
		@JsonProperty("minRange")
		private Integer minRange;
		
		@JsonProperty("maxRange")
		private Integer maxRange;
		
		public Integer getMinRange() { 
			return minRange;
		}
		public void setMinRange(Integer minRange) {
			this.minRange = minRange;
		}
		
		public Integer getMaxRange() {
			return maxRange;
		}
		public void setMaxRange(Integer maxRange) {
			this.maxRange = maxRange;
		}
	}
}
