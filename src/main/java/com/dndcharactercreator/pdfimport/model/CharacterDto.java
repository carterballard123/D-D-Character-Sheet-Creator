package com.dndcharactercreator.pdfimport.model;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import jakarta.validation.constraints.Size;

public class CharacterDto {
	
	@NotBlank
	private String characterName;
	@NotNull
	@Min(1) @Max(20)
	private Integer characterLevel;
	private String characterClass;
	private String characterSubClass;
	@NotNull
	private String characterBackground;
	private String playerName;
	@NotNull
	private String characterRace;
	@NotNull
	private String characterAlignment;
	@Min(0)
	private Integer characterExperiencePoints;
	@Size(max = 3, message = "You can choose a maximum of 3 weapons")
	private List<String> characterWeapons;
	private String characterArmorName;
	
	@NotNull
	@Min(1) @Max(30)
	private Integer characterStrength;
	@NotNull
	@Min(1) @Max(30)
	private Integer characterDexterity;
	@NotNull
	@Min(1) @Max(30)
	private Integer characterConstitution;
	@NotNull
	@Min(1) @Max(30)
	private Integer characterIntelligence;
	@NotNull
	@Min(1) @Max(30)
	private Integer characterWisdom;
	@NotNull
	@Min(1) @Max(30)
	private Integer characterCharisma;
	
	// Getters:
	public String getCharacterName() { return characterName; }
	public Integer getCharacterLevel() { return characterLevel; }
	public String getCharacterClass() { return characterClass; }
	public String getCharacterSubClass() { return characterSubClass; }
	public String getCharacterBackground() { return characterBackground; }
	public String getPlayerName() { return playerName; }
	public String getCharacterRace() { return characterRace; }
	public String getCharacterAlignment() { return characterAlignment; }
	public Integer getCharacterExperiencePoints() { return characterExperiencePoints; }
	public List<String> getCharacterWeapons() { return characterWeapons; }
	public String getCharacterArmor() { return characterArmorName; }
	public Integer getCharacterStrength() { return characterStrength; }
	public Integer getCharacterDexterity() { return characterDexterity; }
	public Integer getCharacterConstitution() { return characterConstitution; }
	public Integer getCharacterIntelligence() { return characterIntelligence; }
	public Integer getCharacterWisdom() { return characterWisdom; }
	public Integer getCharacterCharisma() { return characterCharisma; }
	
	// Setters:
	public void setCharacterName(String characterName) { this.characterName = characterName; }
	public void setCharacterLevel(Integer characterLevel) { this.characterLevel = characterLevel; }
	public void setCharacterClass(String characterClass) { this.characterClass = characterClass; }
	public void setCharacterSubClass(String characterSubClass) { this.characterSubClass = characterSubClass; }
	public void setCharacterBackground(String characterBackground) { this.characterBackground = characterBackground; }
	public void setPlayerName(String playerName) { this.playerName = playerName; }
	public void setCharacterRace(String characterRace) { this.characterRace = characterRace; }
	public void setCharacterAlignment(String characterAlignment) { this.characterAlignment = characterAlignment; }
	public void setCharacterExperiencePoints(Integer characterExperiencePoints) { this.characterExperiencePoints = characterExperiencePoints; }
	public void setCharacterWeapons(List<String> characterWeapons) { this.characterWeapons = characterWeapons; }
	public void setCharacterArmor(String characterArmor) { this.characterArmorName = characterArmor; }
	public void setCharacterStrength(Integer characterStrength) { this.characterStrength = characterStrength; }
	public void setCharacterDexterity(Integer characterDexterity) { this.characterDexterity = characterDexterity; }
	public void setCharacterConstitution(Integer characterConstitution) { this.characterConstitution = characterConstitution; }
	public void setCharacterIntelligence(Integer characterIntelligence) { this.characterIntelligence = characterIntelligence; }
	public void setCharacterWisdom(Integer characterWisdom) { this.characterWisdom = characterWisdom; }
	public void setCharacterCharisma(Integer characterCharisma) { this.characterCharisma = characterCharisma; }
	
}
