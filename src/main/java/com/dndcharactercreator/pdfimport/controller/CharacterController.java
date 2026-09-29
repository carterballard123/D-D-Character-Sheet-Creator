package com.dndcharactercreator.pdfimport.controller;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.model.CharacterSummary;
import com.dndcharactercreator.pdfimport.service.CharacterService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for saving, loading, and listing saved characters.
 *
 * <p>Backed by Postgres via {@link CharacterService}; unrelated to the JSON-file-backed
 * reference-data endpoints in {@link ReferenceController}.
 *
 * @author Carter Ballard
 */
@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characters;

    public CharacterController(CharacterService characters) {
        this.characters = characters;
    }

    /**
     * Saves a character build.
     *
     * <p><b>Endpoint:</b> {@code POST /api/characters}
     *
     * @param dto the character build to save
     * @return 201 Created with the new character's id, e.g. {@code {"id": "..."}}
     */
    @PostMapping
    public ResponseEntity<Map<String, UUID>> save(@RequestBody @Validated CharacterDto dto) {
        UUID id = characters.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    /**
     * Loads a previously-saved character.
     *
     * <p><b>Endpoint:</b> {@code GET /api/characters/{id}}
     *
     * @param id the character's id
     * @return the full character build
     * @throws ResponseStatusException 404 if no character exists with that id
     */
    @GetMapping("/{id}")
    public CharacterDto get(@PathVariable UUID id) {
        return characters.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No character found with id " + id));
    }

    /**
     * Lists every saved character as a lightweight summary.
     *
     * <p><b>Endpoint:</b> {@code GET /api/characters}
     *
     * @return all saved characters, most-recently-updated first
     */
    @GetMapping
    public List<CharacterSummary> list() {
        return characters.listAll();
    }
}
