package com.dndcharactercreator.pdfimport.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShieldData {
	private String name;
	private int acBonus;
	private String description;
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public int getAcBonus() { return acBonus; }
	public void setAcBonus(int acBonus) { this.acBonus = acBonus; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	
}
