package com.dndcharactercreator.pdfimport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeaponPropertyDef {
	
	@JsonProperty("displayName")
	private String displayName;
	
	@JsonProperty("rulesText")
	private String rulesText;
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getRulesText() {
		return rulesText;
	}
	public void setRulesText(String rulesText) {
		this.rulesText = rulesText;
	}
}
