package com.dndcharactercreator.pdfimport.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
  classes = {
    LanguagesRepository.class,
    JacksonAutoConfiguration.class
  },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class LanguagesRepositoryTest {

    @Autowired
    private LanguagesRepository languagesRepository;

    @Test
    void loadAll_shouldReturnNonEmptyList() {
        List<String> all = languagesRepository.findAll();
        assertNotNull(all, "findAll() must not return null");
        assertFalse(all.isEmpty(), "There should be at least one language loaded");
    }

    @Test
    void findByName_knownAndUnknown() {
        // assume the JSON has at least one entry; pick the first
        String first = languagesRepository.findAll().get(0);

        Optional<String> found = languagesRepository.findByName(first);
        assertTrue(found.isPresent(), "findByName(known) should return a present Optional");
        assertEquals(first.toLowerCase(), found.get().toLowerCase());

        assertFalse(languagesRepository.findByName("NoSuchLanguage").isPresent(),
                    "findByName(unknown) should return Optional.empty()");
        assertFalse(languagesRepository.findByName(null).isPresent(),
                    "findByName(null) should return Optional.empty()");
    }

    @Test
    void exists_knownAndUnknown() {
        String known = languagesRepository.findAll().get(0);
        assertTrue(languagesRepository.exits(known), "exists(known) should be true");
        assertFalse(languagesRepository.exits("NoSuchLanguage"), "exists(unknown) should be false");
        assertFalse(languagesRepository.exits(null), "exists(null) should be false");
    }
}
