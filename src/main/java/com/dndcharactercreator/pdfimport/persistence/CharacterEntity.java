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

    // ----- Getters and setters -----

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public CharacterDto getData() {
        return data;
    }

    public void setData(CharacterDto data) {
        this.data = data;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
