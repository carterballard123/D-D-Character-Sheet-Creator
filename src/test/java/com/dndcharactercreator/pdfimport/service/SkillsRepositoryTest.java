package com.dndcharactercreator.pdfimport.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.dndcharactercreator.pdfimport.repository.SkillsRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
  classes = {
    SkillsRepository.class,
    JacksonAutoConfiguration.class
  },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class SkillsRepositoryTest {

    @Autowired
    private SkillsRepository skillsRepository;

    @Test
    void loadAll_shouldReturnNonEmptyList() {
        List<String> all = skillsRepository.findAll();
        assertNotNull(all, "findAll() must not return null");
        assertFalse(all.isEmpty(), "There should be at least one skill loaded");
    }

    @Test
    void findByName_knownSkill_shouldReturnOptional() {
        // pick first skill from the loaded list
        String first = skillsRepository.findAll().get(0);
        Optional<String> opt = skillsRepository.findByName(first.toUpperCase());
        assertTrue(opt.isPresent(), "findByName(known) should return a present Optional");
        assertEquals(first, opt.get(), "Returned skill should match the lookup (case-insensitive)");
    }

    @Test
    void findByName_unknownOrNull_shouldReturnEmptyOptional() {
        assertFalse(skillsRepository.findByName("NoSuchSkill").isPresent(),
                    "findByName(unknown) should return Optional.empty()");
        assertFalse(skillsRepository.findByName(null).isPresent(),
                    "findByName(null) should return Optional.empty()");
    }

    @Test
    void exists_knownAndUnknown() {
        String known = skillsRepository.findAll().get(0);
        assertTrue(skillsRepository.exists(known), "exists(known) should be true");
        assertTrue(skillsRepository.exists(known.toLowerCase()), "exists should be case-insensitive");
        assertFalse(skillsRepository.exists("NoSuchSkill"), "exists(unknown) should be false");
    }
}
