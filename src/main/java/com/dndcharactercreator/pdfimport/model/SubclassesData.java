package com.dndcharactercreator.pdfimport.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model representing a subclass definition loaded from reference JSON.
 *
 * <p>This class is used by Jackson to deserialize entries from {@code subclasses.json}.
 * It is a pure data holder that describes which parent class a subclass belongs to
 * and which traits/features it grants by level.
 *
 * <p>Applying subclass traits to a character (validation, feature selection, output formatting)
 * is handled by service-layer logic.
 *
 * @author Carter Ballard
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubclassesData {

    /** Display name of the subclass (e.g., "Champion"). */
    @JsonProperty("name")
    private String name;

    /** Name or ID of the parent class this subclass belongs to (e.g., "Fighter"). */
    @JsonProperty("parentClass")
    private String parentClass;

    /** Traits/features granted by this subclass, typically keyed by level within each trait entry. */
    @JsonProperty("traits")
    private List<Trait> traits;

    // ----- Getters and setters -----

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentClass() {
        return parentClass;
    }

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }

    public List<Trait> getTraits() {
        return traits;
    }

    public void setTraits(List<Trait> traits) {
        this.traits = traits;
    }

    /**
     * Represents a subclass trait/feature gained at a specific class level.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trait {

        /** Level at which this trait is gained (e.g., 3). */
        @JsonProperty("level")
        private int level;

        /** Trait/feature name (e.g., "Improved Critical"). */
        @JsonProperty("name")
        private String name;

        /** Description text describing the trait's effect. */
        @JsonProperty("description")
        private String description;

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
