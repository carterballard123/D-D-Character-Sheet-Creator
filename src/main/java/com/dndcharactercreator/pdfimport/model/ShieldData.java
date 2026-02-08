package com.dndcharactercreator.pdfimport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model representing a shield definition loaded from reference JSON.
 *
 * <p>This class is used by Jackson to deserialize entries from {@code shields.json}.
 * It contains no business logic and serves only as a structured data holder.
 *
 * <p>Application of shield bonuses (such as proficiency checks or total AC
 * calculation) is handled elsewhere in the service layer.
 *
 * @author Carter Ballard
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShieldData {

    /** Display name of the shield (e.g., "Shield"). */
    @JsonProperty("name")
    private String name;

    /** Armor Class bonus granted by the shield. */
    @JsonProperty("acBonus")
    private int acBonus;

    /** Descriptive text or special rules for the shield. */
    @JsonProperty("description")
    private String description;

    // ----- Getters and setters -----

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAcBonus() {
        return acBonus;
    }

    public void setAcBonus(int acBonus) {
        this.acBonus = acBonus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}