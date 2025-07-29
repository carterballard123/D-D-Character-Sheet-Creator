package com.dndcharactercreator.pdfimport.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
  classes = {
    ArmorRepository.class,
    JacksonAutoConfiguration.class
  },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class ArmorRepositoryTest {

    @Autowired
    private ArmorRepository armorRepository;

    @Test
    void loadAll_shouldReturnNonEmptyList() {
        List<ArmorData> all = armorRepository.findAll();
        assertNotNull(all, "findAll() must not return null");
        assertFalse(all.isEmpty(), "There should be at least one armor entry loaded");
    }

    @Test
    void findByName_knownName_shouldReturnData() {
        // Grab the first entry’s name
        String firstName = armorRepository.findAll().get(0).getArmorName();
        ArmorData data = armorRepository.findByName(firstName);
        assertNotNull(data, "findByName(known) should not return null");
        assertEquals(firstName, data.getArmorName(), "Returned armor should have matching name");
    }

    @Test
    void findByName_unknownName_shouldReturnNull() {
        ArmorData missing = armorRepository.findByName("NoSuchArmor");
        assertNull(missing, "findByName(unknown) should return null");
    }

    @Test
    void findByName_caseInsensitiveLookup() {
        // Use uppercase / mixed case to verify case-insensitivity
        String firstName = armorRepository.findAll().get(0).getArmorName();
        String altCase = firstName.toUpperCase();
        ArmorData data = armorRepository.findByName(altCase);
        assertNotNull(data, "Lookup should be case-insensitive");
        assertEquals(firstName, data.getArmorName());
    }
}
