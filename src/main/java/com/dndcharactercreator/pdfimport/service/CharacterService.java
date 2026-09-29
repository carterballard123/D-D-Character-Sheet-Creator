package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.model.CharacterSummary;
import com.dndcharactercreator.pdfimport.persistence.CharacterEntity;
import com.dndcharactercreator.pdfimport.persistence.CharacterJpaRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for saving, loading, and listing saved characters.
 *
 * <p>This is entirely separate from the JSON-file-backed reference-data services elsewhere in
 * this package (classes, races, backgrounds, etc.) - those describe the rules; this describes a
 * specific player's saved characters, persisted in Postgres.
 *
 * @author Carter Ballard
 */
@Service
public class CharacterService {

    private final CharacterJpaRepository repo;

    public CharacterService(CharacterJpaRepository repo) {
        this.repo = repo;
    }

    /**
     * Saves a character build, returning the id it was saved under.
     *
     * <p>Always creates a new row - this is a "save" (create), not an "update"; nothing here
     * distinguishes a brand-new character from re-saving one already saved once, since
     * {@link CharacterDto} carries no id of its own yet.
     *
     * @param dto the character build to save
     * @return the id the saved character can be looked up by
     */
    public UUID save(CharacterDto dto) {
        CharacterEntity entity = new CharacterEntity();
        entity.setName(dto.getCharacterName());
        entity.setClassId(dto.getCharacterClass());
        entity.setRace(dto.getCharacterRace());
        entity.setLevel(dto.getCharacterLevel());
        entity.setData(dto);
        return repo.save(entity).getId();
    }

    /**
     * Looks up a previously-saved character by id.
     *
     * @param id the character's id
     * @return the full character build, or empty if no character exists with that id
     */
    public Optional<CharacterDto> findById(UUID id) {
        return repo.findById(id).map(CharacterEntity::getData);
    }

    /**
     * Lists every saved character as a lightweight summary (see {@link CharacterSummary}).
     *
     * @return all saved characters, most-recently-updated first
     */
    public List<CharacterSummary> listAll() {
        return repo.findAll().stream()
            .map(e -> new CharacterSummary(e.getId(), e.getName(), e.getClassId(), e.getRace(), e.getLevel(), e.getUpdatedAt()))
            .sorted((a, b) -> b.updatedAt().compareTo(a.updatedAt()))
            .toList();
    }
}
