package com.dndcharactercreator.pdfimport.persistence;

import com.dndcharactercreator.pdfimport.model.CharacterDto;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity backing the {@code characters} table (see {@code V1__create_characters.sql}).
 *
 * <p>The full {@link CharacterDto} is stored as-is in the {@code data} JSONB column via
 * {@link JdbcTypeCode}, which tells Hibernate to (de)serialize the field as JSON rather than
 * trying to map it to individual columns. {@code name}/{@code class_id}/{@code race}/
 * {@code level} are pulled out as their own columns too, purely so {@link #findAll()}-style
 * list queries don't need to deserialize the full JSON blob just to show a summary row.
 *
 * <p>Deliberately separate from the {@code .model}/{@code .repository} packages, which hold the
 * JSON-file-backed reference data (races, classes, backgrounds, etc.) - that data isn't part of
 * this migration and isn't going into the database.
 *
 * @author Carter Ballard
 */
@Entity
@Table(name = "characters")
public class CharacterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "class_id")
    private String classId;

    private String race;

    private Integer level;

    /** The full character build, stored as JSONB. See the class-level Javadoc. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private CharacterDto data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Replaces this entity's character build with {@code dto}: the full build goes into
     * {@code data}, and the summary columns are re-extracted from it.
     *
     * <p>This is the only place those columns are written (there are deliberately no individual
     * setters), so creating and updating a character can't drift apart, and the summary columns
     * can't end up disagreeing with the JSON they were pulled from.
     *
     * @param dto the character build this entity should now hold
     */
    public void apply(CharacterDto dto) {
        name = dto.getCharacterName();
        classId = dto.getCharacterClass();
        race = dto.getCharacterRace();
        level = dto.getCharacterLevel();
        data = dto;
    }

    // ----- Getters -----

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getClassId() {
        return classId;
    }

    public String getRace() {
        return race;
    }

    public Integer getLevel() {
        return level;
    }

    public CharacterDto getData() {
        return data;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
