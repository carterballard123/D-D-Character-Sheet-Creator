package com.dndcharactercreator.pdfimport.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object representing a character build submitted by the frontend.
 *
 * <p>This class defines the JSON structure expected by REST endpoints (e.g.
 * {@code POST /api/pdf/fill}). It captures the character choices made by the user,
 * such as name, race, class, ability scores, equipment selections, and languages.
 *
 * <p>This class contains <b>input data only</b>. It does not perform validation
 * beyond basic structural checks (e.g., required fields, numeric ranges) and
 * contains no business or rules logic.
 *
 * <p>Derived values (such as armor class, proficiency bonuses, or skill modifiers)
 * should be computed by service-layer logic, not supplied by the client.
 *
 * @author Carter Ballard
 */
public class CharacterDto {

    /** Character name as displayed on the sheet. */
    @NotBlank
    private String characterName;

    /** Character level (1–20). */
    @NotNull
    @Min(1)
    @Max(20)
    private Integer characterLevel;

    /** Primary character class (e.g., "Fighter"). */
    private String characterClass;

    /** Selected subclass, if any (e.g., "Champion"). */
    private String characterSubClass;

    /** Selected background (e.g., "Soldier"). */
    @NotNull
    private String characterBackground;

    /** Player's real-world name. */
    private String playerName;

    /** Selected character race (e.g., "Dwarf"). */
    @NotNull
    private String characterRace;

    /** Character alignment (e.g., "Lawful Good"). */
    @NotNull
    private String characterAlignment;

    /** Total experience points, if tracked. */
    @Min(0)
    private Integer characterExperiencePoints;

    /**
     * Selected weapons.
     *
     * <p>The frontend enforces a maximum number of selections; this constraint
     * provides an additional safety check on the backend.
     */
    @Size(max = 3, message = "You can choose a maximum of 3 weapons")
    private List<String> characterWeapons;

    /** Name of the equipped armor, if any. */
    private String characterArmorName;

    /** Name of the equipped shield, if any. */
    private String characterShield;

    /** Initiative value (may be calculated later). */
    private Integer characterInitiative;

    /** Movement speed in feet (often race-dependent). */
    private Integer characterSpeed;

    /** Hit dice string (e.g., "1d10"). */
    private String characterHitDice;

    /**
     * Languages known by the character.
     *
     * <p>Mapped from the JSON property {@code "languages"}.
     */
    @JsonProperty("languages")
    private List<String> characterLanguages;

    /** Strength ability score (1–30). */
    @NotNull
    @Min(1)
    @Max(30)
    private Integer characterStrength;

    /** Dexterity ability score (1–30). */
    @NotNull
    @Min(1)
    @Max(30)
    private Integer characterDexterity;

    /** Constitution ability score (1–30). */
    @NotNull
    @Min(1)
    @Max(30)
    private Integer characterConstitution;

    /** Intelligence ability score (1–30). */
    @NotNull
    @Min(1)
    @Max(30)
    private Integer characterIntelligence;

    /** Wisdom ability score (1–30). */
    @NotNull
    @Min(1)
    @Max(30)
    private Integer characterWisdom;

    /** Charisma ability score (1–30). */
    @NotNull
    @Min(1)
    @Max(30)
    private Integer characterCharisma;

    // ----- Getters -----

    public String getCharacterName() {
        return characterName;
    }

    public Integer getCharacterLevel() {
        return characterLevel;
    }

    public String getCharacterClass() {
        return characterClass;
    }

    public String getCharacterSubClass() {
        return characterSubClass;
    }

    public String getCharacterBackground() {
        return characterBackground;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getCharacterRace() {
        return characterRace;
    }

    public String getCharacterAlignment() {
        return characterAlignment;
    }

    public Integer getCharacterExperiencePoints() {
        return characterExperiencePoints;
    }

    public List<String> getCharacterWeapons() {
        return characterWeapons;
    }

    public String getCharacterArmor() {
        return characterArmorName;
    }

    public String getCharacterShield() {
        return characterShield;
    }

    public Integer getCharacterInitiative() {
        return characterInitiative;
    }

    public Integer getCharacterSpeed() {
        return characterSpeed;
    }

    public String getCharacterHitDice() {
        return characterHitDice;
    }

    public List<String> getCharacterLanguages() {
        return characterLanguages;
    }

    public Integer getCharacterStrength() {
        return characterStrength;
    }

    public Integer getCharacterDexterity() {
        return characterDexterity;
    }

    public Integer getCharacterConstitution() {
        return characterConstitution;
    }

    public Integer getCharacterIntelligence() {
        return characterIntelligence;
    }

    public Integer getCharacterWisdom() {
        return characterWisdom;
    }

    public Integer getCharacterCharisma() {
        return characterCharisma;
    }

    // ----- Setters -----

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public void setCharacterLevel(Integer characterLevel) {
        this.characterLevel = characterLevel;
    }

    public void setCharacterClass(String characterClass) {
        this.characterClass = characterClass;
    }

    public void setCharacterSubClass(String characterSubClass) {
        this.characterSubClass = characterSubClass;
    }

    public void setCharacterBackground(String characterBackground) {
        this.characterBackground = characterBackground;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setCharacterRace(String characterRace) {
        this.characterRace = characterRace;
    }

    public void setCharacterAlignment(String characterAlignment) {
        this.characterAlignment = characterAlignment;
    }

    public void setCharacterExperiencePoints(Integer characterExperiencePoints) {
        this.characterExperiencePoints = characterExperiencePoints;
    }

    public void setCharacterWeapons(List<String> characterWeapons) {
        this.characterWeapons = characterWeapons;
    }

    public void setCharacterArmor(String characterArmorName) {
        this.characterArmorName = characterArmorName;
    }

    public void setCharacterShield(String characterShield) {
        this.characterShield = characterShield;
    }

    public void setCharacterInitiative(Integer characterInitiative) {
        this.characterInitiative = characterInitiative;
    }

    public void setCharacterSpeed(Integer characterSpeed) {
        this.characterSpeed = characterSpeed;
    }

    public void setCharacterHitDice(String characterHitDice) {
        this.characterHitDice = characterHitDice;
    }

    public void setCharacterLanguages(List<String> characterLanguages) {
        this.characterLanguages = characterLanguages;
    }

    public void setCharacterStrength(Integer characterStrength) {
        this.characterStrength = characterStrength;
    }

    public void setCharacterDexterity(Integer characterDexterity) {
        this.characterDexterity = characterDexterity;
    }

    public void setCharacterConstitution(Integer characterConstitution) {
        this.characterConstitution = characterConstitution;
    }

    public void setCharacterIntelligence(Integer characterIntelligence) {
        this.characterIntelligence = characterIntelligence;
    }

    public void setCharacterWisdom(Integer characterWisdom) {
        this.characterWisdom = characterWisdom;
    }

    public void setCharacterCharisma(Integer characterCharisma) {
        this.characterCharisma = characterCharisma;
    }
}