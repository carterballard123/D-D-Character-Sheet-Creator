package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.persistence.CharacterEntity;
import com.dndcharactercreator.pdfimport.persistence.CharacterJpaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises {@link CharacterService}'s update and delete against a real, ephemeral Postgres (via
 * Testcontainers) - see {@code CharacterJpaRepositoryTest} for why not an in-memory database.
 *
 * <p>{@code @DataJpaTest} normally wraps each test in one transaction that's rolled back at the
 * end. {@code Propagation.NOT_SUPPORTED} switches that off, so every service call below runs in
 * (and commits) its own transaction, exactly as it does behind a real HTTP request. That matters
 * here: {@code update()} relies on Hibernate writing the changes when its transaction commits,
 * and each later read has to come back from Postgres rather than from an entity Hibernate still
 * has in memory. Rows are left behind as a result, which is fine - the container is thrown away
 * after this class, and no test here depends on the table being empty.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(CharacterService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CharacterServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    private CharacterService characters;

    @Autowired
    private CharacterJpaRepository repo;

    @Test
    void update_replacesTheCharacter_keepsCreatedAt_andAdvancesUpdatedAt() {
        UUID id = characters.save(grognak());
        CharacterEntity before = repo.findById(id).orElseThrow();

        CharacterDto changed = new CharacterDto();
        changed.setCharacterName("Grognak the Wise");
        changed.setCharacterClass("Wizard");
        changed.setCharacterRace("Half-Orc");
        changed.setCharacterLevel(4);
        changed.setCharacterIntelligence(16);

        assertTrue(characters.update(id, changed));

        CharacterEntity after = repo.findById(id).orElseThrow();

        assertEquals("Grognak the Wise", after.getName());
        assertEquals("Wizard", after.getClassId());
        assertEquals("Half-Orc", after.getRace());
        assertEquals(4, after.getLevel());

        CharacterDto data = after.getData();
        assertEquals("Grognak the Wise", data.getCharacterName());
        assertEquals("Wizard", data.getCharacterClass());
        assertEquals(4, data.getCharacterLevel());
        assertEquals(16, data.getCharacterIntelligence());
        assertNull(data.getCharacterStrength(),
            "An update is a full replace: a field the new build leaves out must be cleared, "
            + "not carried over from the old build");

        assertEquals(before.getCreatedAt(), after.getCreatedAt(), "Updating must not change createdAt");
        assertTrue(after.getUpdatedAt().isAfter(before.getUpdatedAt()),
            "Updating must advance updatedAt (was " + before.getUpdatedAt() + ", now " + after.getUpdatedAt() + ")");
    }

    @Test
    void update_withAnIdenticalBuild_stillAdvancesUpdatedAt() {
        UUID id = characters.save(grognak());
        CharacterEntity before = repo.findById(id).orElseThrow();

        assertTrue(characters.update(id, grognak()));

        CharacterEntity after = repo.findById(id).orElseThrow();
        assertEquals(before.getCreatedAt(), after.getCreatedAt());
        assertTrue(after.getUpdatedAt().isAfter(before.getUpdatedAt()),
            "Re-saving an unchanged character still counts as an update");
    }

    @Test
    void update_unknownId_returnsFalseAndCreatesNothing() {
        UUID id = UUID.randomUUID();

        assertFalse(characters.update(id, grognak()));

        assertTrue(repo.findById(id).isEmpty(), "Updating a missing character must not create it");
    }

    @Test
    void delete_removesTheCharacter() {
        UUID id = characters.save(grognak());
        UUID other = characters.save(grognak());

        assertTrue(characters.delete(id));

        assertTrue(characters.findById(id).isEmpty(), "A deleted character must be gone");
        assertTrue(characters.listAll().stream().noneMatch(c -> c.id().equals(id)),
            "A deleted character must not appear in the list either");
        assertTrue(characters.findById(other).isPresent(), "Deleting one character must leave others alone");
    }

    @Test
    void delete_unknownId_returnsFalse() {
        assertFalse(characters.delete(UUID.randomUUID()));
    }

    private static CharacterDto grognak() {
        CharacterDto dto = new CharacterDto();
        dto.setCharacterName("Grognak");
        dto.setCharacterClass("Barbarian");
        dto.setCharacterRace("Half-Orc");
        dto.setCharacterLevel(3);
        dto.setCharacterStrength(18);
        return dto;
    }
}
