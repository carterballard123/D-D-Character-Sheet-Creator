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
    RacesRepository.class,               // only load your repository bean
    JacksonAutoConfiguration.class      // plus Jackson support
  },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class RacesRepositoryTest {

    @Autowired
    private RacesRepository racesRepository;

    @Test
    void loadAll_shouldReturnNonEmptyList() {
        List<RacesData> all = racesRepository.findAll();
        assertNotNull(all, "findAll() must not return null");
        assertFalse(all.isEmpty(), "There should be at least one race loaded");
    }

    @Test
    void findByName_knownRace_shouldReturnRace() {
        RacesData aasimar = racesRepository.findByName("Aasimar");
        assertNotNull(aasimar, "findByName(\"Aasimar\") should not be null");
        assertEquals("Aasimar", aasimar.getName());
    }

    @Test
    void exists_knownAndUnknownNames() {
        assertTrue(racesRepository.exists("Aasimar"));
        assertFalse(racesRepository.exists("NoSuchRace"));
    }

    @Test
    void findOptions_returnsOptional() {
        Optional<RacesData> opt = racesRepository.findOptions("Aasimar");
        assertTrue(opt.isPresent(), "findOptions(\"Aasimar\") should be present");
        assertEquals("Aasimar", opt.get().getName());

        Optional<RacesData> missing = racesRepository.findOptions("Nope");
        assertFalse(missing.isPresent(), "findOptions(\"Nope\") should be empty");
    }
}