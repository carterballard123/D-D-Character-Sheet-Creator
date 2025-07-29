package com.dndcharactercreator.pdfimport.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubclassesData {
	private String name;
	private String parentClass;
	
	@JsonProperty("traits")
	private List<Trait> traits;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getParentClass() { return parentClass; }
	public void setParentClass(String parentClass) { this.parentClass = parentClass; }
	
	public List<Trait> getTraits() { return traits; }
	public void setTraits(List<Trait> traits) { this.traits = traits; }
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Trait {
		private int level;
		private String name;
		private String description;
		
		public int getLevel() { return level; }
		public void setLevel(int level) { this.level = level; }
		
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		
	}
}
