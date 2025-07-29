package com.dndcharactercreator.pdfimport.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
  classes = {
    BackgroundRepository.class,
    JacksonAutoConfiguration.class
  },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class BackgroundRepositoryTest {

    @Autowired
    private BackgroundRepository backgroundRepository;

    @Test
    void loadAll_shouldReturnNonEmptyList() {
        List<BackgroundData> all = backgroundRepository.findAll();
        assertNotNull(all, "findAll() must not return null");
        assertFalse(all.isEmpty(), "There should be at least one background loaded");
    }

    @Test
    void findByName_knownName_shouldReturnData() {
        // Grab the first entry’s name
        String firstName = backgroundRepository.findAll().get(0).getName();
        BackgroundData bg = backgroundRepository.findByName(firstName);
        assertNotNull(bg, "findByName(known) should not return null");
        assertEquals(firstName, bg.getName(), "Returned background should have matching name");
    }

    @Test
    void findByName_unknownName_shouldReturnNull() {
        BackgroundData missing = backgroundRepository.findByName("NoSuchBackground");
        assertNull(missing, "findByName(unknown) should return null");
    }

    @Test
    void findByName_caseInsensitiveLookup() {
        String firstName = backgroundRepository.findAll().get(0).getName();
        String altCase = firstName.toUpperCase();
        BackgroundData bg = backgroundRepository.findByName(altCase);
        assertNotNull(bg, "Lookup should be case-insensitive");
        assertEquals(firstName, bg.getName());
    }

    @Test
    void mandatoryFields_loadedCorrectly() {
        BackgroundData bg = backgroundRepository.findAll().get(0);
        // abilityScores and skillProficiencies should at least be non-null
        assertNotNull(bg.getAbilityScores(), "abilityScores list should not be null");
        assertNotNull(bg.getSkillProficiencies(), "skillProficiencies list should not be null");
        // feat should be set (may be empty string, but not null)
        assertNotNull(bg.getFeat(), "feat should not be null");
        // starting equipment options should be non-null (may be empty)
        assertNotNull(bg.getStartingEquipmentOptions(), "startingEquipmentOptions should not be null");
    }
}