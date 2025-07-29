package com.dndcharactercreator.pdfimport.service;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RacesData {
	private String name;
	private String creatureType;
	private String size;
	private int speed;
	
	@JsonProperty("traits")
	private List<Trait> traits;
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getCreatureType() { return creatureType; }
	public void setCreatureType(String creatureType) { this.creatureType = creatureType; }
	
	public String getSize() { return size; }
	public void setSize( String size ) { this.size = size; }
	
	public int getSpeed() { return speed; }
	public void setSpeed( int speed ) { this.speed = speed; }
	
	public List<Trait> getTraits(){ return traits; }
	public void setTraits(List<Trait> traits) { this.traits = traits; }
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Trait {
		private String name;
		private String description;
		private String type;
		private Usage usage;
		
		@JsonProperty("options")
		@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
		private List<Option> options;
		
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		
		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		
		public Usage getUsage() { return usage; }
		public void setUsage(Usage usage) { this.usage = usage; }
		
		public List<Option> getOption(){ return options; }
		public void setOption(List<Option> options) { this.options = options; }
		
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Usage {
		private int times;
		private String refresh;
		
		public int getTimes() { return times; }
		public void setTimes(int times) { this.times = times; }
		
		public String getRefresh() { return refresh; }
		public void setRefresh(String refresh) { this.refresh = refresh; }
		
		}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Option{
		private String name;
		private String effect;
		
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		
		public String getEffect() { return effect; }
		public void setEffect(String effect) { this.effect = effect; }
	}
}
