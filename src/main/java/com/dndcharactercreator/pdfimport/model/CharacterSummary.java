package com.dndcharactercreator.pdfimport.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight view of a saved character for list endpoints.
 *
 * <p>{@code GET /api/characters} returns a list of these rather than full
 * {@link CharacterDto}s - a listing screen doesn't need every field (ability scores,
 * equipment, languages, ...), just enough to identify and pick a character.
 *
 * @param id the saved character's id
 * @param name the character's name, as it was when last saved (may be {@code null})
 * @param classId the character's class, as it was when last saved (may be {@code null})
 * @param race the character's race, as it was when last saved (may be {@code null})
 * @param level the character's level, as it was when last saved (may be {@code null})
 * @param updatedAt when this character was last saved
 */
public record CharacterSummary(
    UUID id,
    String name,
    String classId,
    String race,
    Integer level,
    Instant updatedAt
) {
}
