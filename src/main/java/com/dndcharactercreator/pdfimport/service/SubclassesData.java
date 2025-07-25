package com.dndcharactercreator.pdfimport.service;

import java.util.List;
import java.util.Map;

/*
 *  {
    "name": "Path of the Berserker",
    "parentClass": "Barbarian",
    "traits": [
      {
        "level": 3,
        "name": "Frenzy",
        "description": "When you enter your rage, you can choose to go into a frenzy. If you do, for the duration of your rage you can make a single melee weapon attack as a bonus action on each of your turns after this one. When your rage ends, you suffer one level of exhaustion."
      },
      {
        "level": 6,
        "name": "Mindless Rage",
        "description": "While raging, you can’t be charmed or frightened. If you are charmed or frightened when you enter your rage, the effect is suspended for the duration of the rage."
      },
      {
        "level": 10,
        "name": "Intimidating Presence",
        "description": "You can use your action to frighten someone with your menacing presence. Choose one creature you can see within 30 feet of you. If they fail a Wisdom save (DC = 8 + your proficiency bonus + your Strength or Charisma modifier), they are frightened of you until the end of your next turn."
      },
      {
        "level": 14,
        "name": "Retaliation",
        "description": "When you take damage from a creature that is within 5 feet of you, you can use your reaction to make a melee weapon attack against that creature."
      }
    ]
  }
 */
public class SubclassesData {
	private String name;
	private String parentClass;
	private List<Trait> traits;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getParentClass() { return parentClass; }
	public void setParentClass(String parentClass) { this.parentClass = parentClass; }
	
	public List<Trait> getTrait() { return traits; }
	public void setTrait(List<Trait> traits) { this.traits = traits; }
	
	
	public class Trait {
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
