package com.dndcharactercreator.pdfimport.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model representing a race/species definition loaded from reference JSON.
 *
 * <p>This class is used by Jackson to deserialize entries from {@code races.json}.
 * It is a pure data holder and contains no rule enforcement or calculations.
 *
 * <p>Races may define a list of traits. Traits may optionally include usage limits
 * (e.g., times per rest) and selectable options (subchoices). Some JSON sources may
 * represent "options" as either a single object or a list; this model supports both.
 *
 * @author Carter Ballard
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RacesData {

    /** Display name of the race/species (e.g., "Elf"). */
    @JsonProperty("name")
    private String name;

    /** Creature type (e.g., "Humanoid"). */
    @JsonProperty("creatureType")
    private String creatureType;

    /** Size category (e.g., "Small", "Medium"). */
    @JsonProperty("size")
    private String size;

    /** Base walking speed (typically in feet). */
    @JsonProperty("speed")
    private int speed;

    /** Traits granted by this race/species. */
    @JsonProperty("traits")
    private List<Trait> traits;

    // ----- Getters and setters -----

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatureType() {
        return creatureType;
    }

    public void setCreatureType(String creatureType) {
        this.creatureType = creatureType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public List<Trait> getTraits() {
        return traits;
    }

    public void setTraits(List<Trait> traits) {
        this.traits = traits;
    }

    /**
     * Represents a race/species trait.
     *
     * <p>Traits may include descriptive text, an optional usage limit, and
     * optional selectable options.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trait {

        /** Trait name (e.g., "Darkvision"). */
        @JsonProperty("name")
        private String name;

        /** Trait description text. */
        @JsonProperty("description")
        private String description;

        /** Trait type/category if your data distinguishes types. */
        @JsonProperty("type")
        private String type;

        /** Optional usage information (e.g., uses per long rest). */
        @JsonProperty("usage")
        private Usage usage;

        /**
         * Optional selectable options for this trait.
         *
         * <p>Some JSON sources may provide a single option object instead of an array;
         * {@link JsonFormat.Feature#ACCEPT_SINGLE_VALUE_AS_ARRAY} allows both.
         */
        @JsonProperty("options")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<Option> options;

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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Usage getUsage() {
            return usage;
        }

        public void setUsage(Usage usage) {
            this.usage = usage;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }
    }

    /**
     * Represents usage limits for a trait (e.g., number of uses and refresh rule).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {

        /** Number of times the trait can be used before refresh (optional meaning depends on rules). */
        @JsonProperty("times")
        private int times;

        /** Refresh rule (e.g., "LR", "SR", "Daily"), if present. */
        @JsonProperty("refresh")
        private String refresh;

        public int getTimes() {
            return times;
        }

        public void setTimes(int times) {
            this.times = times;
        }

        public String getRefresh() {
            return refresh;
        }

        public void setRefresh(String refresh) {
            this.refresh = refresh;
        }
    }

    /**
     * Represents one selectable option within a trait (subchoice).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Option {

        /** Option name (e.g., a subtrait choice name). */
        @JsonProperty("name")
        private String name;

        /** Effect text describing what the option does. */
        @JsonProperty("effect")
        private String effect;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEffect() {
            return effect;
        }

        public void setEffect(String effect) {
            this.effect = effect;
        }
    }
}