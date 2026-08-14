package com.dndcharactercreator.pdfimport.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.dndcharactercreator.pdfimport.model.ArmorData;
import com.dndcharactercreator.pdfimport.repository.ArmorRepository;

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

    @Test
    void findByName_unarmored_hasCorrectBaseArmorClass() {
        // Regression test: baseArmorClass previously deserialized to 0 for every
        // armor because @JsonProperty("baseArmorClass") didn't match the JSON key
        // "armorClass". Assert the real value, not just that the field exists.
        ArmorData unarmored = armorRepository.findByName("Unarmored");
        assertNotNull(unarmored, "Unarmored should be a known armor entry");
        assertEquals(10, unarmored.getBaseArmorClass(),
                     "Unarmored base AC should be 10, per armors.json");
    }
}
