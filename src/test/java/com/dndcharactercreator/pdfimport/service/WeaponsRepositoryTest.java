package com.dndcharactercreator.pdfimport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.dndcharactercreator.pdfimport.model.AmmunitionType;
import com.dndcharactercreator.pdfimport.model.WeaponCategory;
import com.dndcharactercreator.pdfimport.model.WeaponData;
import com.dndcharactercreator.pdfimport.model.WeaponMastery;
import com.dndcharactercreator.pdfimport.model.WeaponProperty;
import com.dndcharactercreator.pdfimport.model.WeaponType;
import com.dndcharactercreator.pdfimport.repository.WeaponsRepository;

@SpringBootTest(
    classes = {
        WeaponsRepository.class,
        JacksonAutoConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class WeaponsRepositoryTest {

    @Autowired
    private WeaponsRepository weaponsRepository;

    @Test
    void loadAll_shouldReturnWeaponDefinitions() {
        Map<String, WeaponData> all = weaponsRepository.getAllWeapons();

        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertEquals(38, all.size());
    }

    @Test
    void getWeapon_knownWeapon_shouldDeserializeFullWeaponShape() {
        WeaponData dagger = weaponsRepository.getWeapon("DAGGER");

        assertNotNull(dagger);
        assertEquals("Dagger", dagger.getDisplayName());
        assertEquals(WeaponCategory.SIMPLE, dagger.getCategory());
        assertEquals(WeaponType.MELEE, dagger.getWeaponType());
        assertEquals(1.0, dagger.getWeightLb());
        assertEquals("1d4", dagger.getDamage().getDice());
        assertEquals(WeaponProperty.FINESSE, dagger.getProperties().get(0));
        assertEquals(20, dagger.getRange().getMinRange());
        assertEquals(60, dagger.getRange().getMaxRange());
        assertEquals(WeaponMastery.NICK, dagger.getMastery());
    }

    @Test
    void getWeapon_fractionalWeightAndAmmunition_shouldDeserialize() {
        WeaponData dart = weaponsRepository.getWeapon("DART");
        WeaponData longbow = weaponsRepository.getWeapon("LONGBOW");

        assertEquals(0.25, dart.getWeightLb());
        assertEquals(AmmunitionType.ARROW, longbow.getAmmunitionType());
    }

    @Test
    void getWeapon_versatileWeapons_shouldDeserializeVersatileDamageDice() {
        assertEquals("1d8", weaponsRepository.getWeapon("QUARTERSTAFF").getVersatileDamageDice());
        assertEquals("1d10", weaponsRepository.getWeapon("WARHAMMER").getVersatileDamageDice());
        assertEquals("1d10", weaponsRepository.getWeapon("WAR_PICK").getVersatileDamageDice());
    }
}
