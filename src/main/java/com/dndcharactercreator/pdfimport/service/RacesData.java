package com.dndcharactercreator.pdfimport.service;

import java.util.List;

/*
 * {
    "name": "Aasimar",
    "creatureType": "Humanoid (Celestial)",
    "size": "Medium",
    "speed": 30,
    "traits": [
      {
        "name": "Darkvision",
        "description": "See in dim light within 60 ft. as if it were bright light, and in darkness as if it were dim light.",
        "type": "passive"
      },
      {
        "name": "Celestial Resistance",
        "description": "You have resistance to necrotic and radiant damage.",
        "type": "passive"
      },
      {
        "name": "Healing Hands",
        "description": "As an action, touch a creature and restore a number of hit points equal to your level. Once you use this trait, you can’t use it again until you finish a long rest.",
        "type": "active",
        "usage": {
          "times": 1,
          "refresh": "longRest"
        }
      },
      {
        "name": "Light Bearer",
        "description": "You know the _light_ cantrip. Charisma is your spellcasting ability for it.",
        "type": "spell"
      },
      {
        "name": "Celestial Revelation",
        "description": "Once per long rest, you can invoke a radiance that lasts 1 minute. Choose one option.",
        "type": "choice",
        "usage": {
          "times": 1,
          "refresh": "longRest"
        },
        "options": [
          {
            "name": "Heavenly Wings",
            "effect": "Sprout luminous, spectral wings for the duration. You have a flying speed of 30 feet."
          },
          {
            "name": "Radiant Soul",
            "effect": "Gain a +1d6 bonus to radiant damage once per turn when you deal radiant or necrotic damage."
          },
          {
            "name": "Necrotic Shroud",
            "effect": "Form a fearsome visage. Each creature of your choice within 10 feet must succeed on a Wisdom save or be frightened of you until the end of your next turn."
          }
        ]
      }
    ]
  }
 */
public class RacesData {
	private String name;
	private String creatureType;
	private String size;
	private int speed;
	private List<Trait> traits;
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getCreatureType() { return creatureType; }
	public void setCreatureType(String creatureType) { this.creatureType = creatureType; }
	
	public String getSize() { return size; }
	public void setSize( String size ) { this.size = size; }
	
	public int getSpeed() { return speed; }
	public void setSpeed( int speed ) { this.speed = speed; }
	
	public List<Trait> getTrait(){ return traits; }
	public void setTrait(List<Trait> traits) { this.traits = traits; }
	
	public class Trait {
		private String name;
		private String description;
		private String type;
		private Usage usage;
		private List<Option> option;
		
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		
		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		
		public Usage getUsage() { return usage; }
		public void setUsage(Usage usage) { this.usage = usage; }
		
		public List<Option> getOption(){ return option; }
		public void setOption(List<Option> option) { this.option = option; }
		
	}
	
	public class Usage {
		private int times;
		private String refresh;
		
		public int getTimes() { return times; }
		public void setTimes(int times) { this.times = times; }
		
		public String getRefresh() { return refresh; }
		public void setRefresh(String refresh) { this.refresh = refresh; }
		
		}
	
	public class Option{
		private String name;
		private String effect;
		
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		
		public String getEffect() { return effect; }
		public void setEffect(String effect) { this.effect = effect; }
	}
}
