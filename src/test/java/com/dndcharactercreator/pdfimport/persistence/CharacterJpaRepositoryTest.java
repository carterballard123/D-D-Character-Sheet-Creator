package com.dndcharactercreator.pdfimport.persistence;

import com.dndcharactercreator.pdfimport.model.CharacterDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trips a {@link CharacterEntity} through a real, ephemeral Postgres (via Testcontainers) -
 * deliberately not an in-memory database. The {@code data} column's JSONB mapping
 * ({@code @JdbcTypeCode(SqlTypes.JSON)}) and {@code V1__create_characters.sql}'s
 * {@code jsonb} column type are both genuinely Postgres-specific; H2 wouldn't exercise either
 * honestly, even in "Postgres compatibility mode".
 *
 * <p>{@code @AutoConfigureTestDatabase(Replace.NONE)} stops {@code @DataJpaTest}'s normal
 * behavior of swapping in an embedded database, so the {@code @ServiceConnection} container
 * below is what actually gets used.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CharacterJpaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CharacterJpaRepository repo;

    @Test
    void save_thenFindById_roundTripsTheFullCharacterData() {
        CharacterDto dto = new CharacterDto();
        dto.setCharacterName("Grognak");
        dto.setCharacterClass("Barbarian");
        dto.setCharacterRace("Half-Orc");
        dto.setCharacterLevel(3);
        dto.setCharacterStrength(18);

        CharacterEntity entity = new CharacterEntity();
        entity.setName(dto.getCharacterName());
        entity.setClassId(dto.getCharacterClass());
        entity.setRace(dto.getCharacterRace());
        entity.setLevel(dto.getCharacterLevel());
        entity.setData(dto);

        // persistFlushFind() flushes to the database, clears Hibernate's first-level cache,
        // then re-reads by id - a genuine round trip through Postgres, not just a Java object
        // reference Hibernate happened to still have in memory from the save.
        CharacterEntity found = entityManager.persistFlushFind(entity);

        assertNotNull(found.getId(), "Saving should assign an id");
        assertNotNull(found.getCreatedAt(), "Saving should stamp createdAt");
        assertNotNull(found.getUpdatedAt(), "Saving should stamp updatedAt");

        CharacterDto roundTripped = found.getData();
        assertEquals("Grognak", roundTripped.getCharacterName());
        assertEquals("Barbarian", roundTripped.getCharacterClass());
        assertEquals(18, roundTripped.getCharacterStrength(),
            "A field with no dedicated column of its own (like an ability score) must still "
            + "survive the JSONB round trip, not just the four fields pulled out into columns");
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        Optional<CharacterEntity> found = repo.findById(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }
}
