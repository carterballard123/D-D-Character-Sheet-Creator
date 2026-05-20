package com.dndcharactercreator.pdfimport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.dndcharactercreator.pdfimport.model.WeaponPropertyDef;
import com.dndcharactercreator.pdfimport.repository.WeaponPropertiesRepository;

@SpringBootTest(
    classes = {
        WeaponPropertiesRepository.class,
        JacksonAutoConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class WeaponPropertiesRepositoryTest {

    @Autowired
    private WeaponPropertiesRepository weaponPropertiesRepository;

    @Test
    void loadAll_shouldReturnPropertyDefinitions() {
        Map<String, WeaponPropertyDef> all = weaponPropertiesRepository.getAllWeaponProperties();

        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertEquals(10, all.size());
    }

    @Test
    void getWeaponProperty_knownProperty_shouldDeserializeDisplayNameAndRulesText() {
        WeaponPropertyDef twoHanded = weaponPropertiesRepository.getWeaponProperty("TWO_HANDED");

        assertNotNull(twoHanded);
        assertEquals("Two-Handed", twoHanded.getDisplayName());
        assertEquals("This weapon requires two hands to make an attack with it.", twoHanded.getRulesText());
    }
}
