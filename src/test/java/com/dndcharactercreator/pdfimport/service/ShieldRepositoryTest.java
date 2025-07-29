package com.dndcharactercreator.pdfimport.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
  classes = {
    ShieldRepository.class,
    JacksonAutoConfiguration.class
  },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class ShieldRepositoryTest {

    @Autowired
    private ShieldRepository shieldRepository;

    @Test
    void loadAll_shouldReturnNonEmptyNames() {
        List<String> names = shieldRepository.findAllNames();
        assertNotNull(names, "findAllNames() must not return null");
        assertFalse(names.isEmpty(), "There should be at least one shield loaded");
    }

    @Test
    void findByName_knownShield_shouldReturnData() {
        // pick the first shield name from findAllNames()
        String nameKey = shieldRepository.findAllNames().get(0);
        ShieldData shield = shieldRepository.findByName(nameKey);

        assertNotNull(shield, "findByName(known) should not return null");
        assertEquals(nameKey.toLowerCase(), shield.getName().toLowerCase(),
                     "Returned ShieldData.name should match lookup key");
        // sanity checks
        assertTrue(shield.getAcBonus() >= 0, "AC bonus should be non-negative");
        assertNotNull(shield.getDescription(), "Description should not be null");
        assertFalse(shield.getDescription().isBlank(), "Description should not be blank");
    }

    @Test
    void findByName_unknownOrNull_shouldReturnNull() {
        assertNull(shieldRepository.findByName("NoSuchShield"),
                   "findByName(unknown) should return null");
        assertNull(shieldRepository.findByName(null),
                   "findByName(null) should return null");
    }
}
