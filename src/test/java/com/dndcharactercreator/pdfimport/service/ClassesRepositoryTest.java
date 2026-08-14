package com.dndcharactercreator.pdfimport.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.dndcharactercreator.pdfimport.model.ClassesData;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
  classes = {
    ClassesRepository.class,
    JacksonAutoConfiguration.class
  },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class ClassesRepositoryTest {

    @Autowired
    private ClassesRepository classesRepository;

    @Test
    void loadAll_shouldReturnNonEmptyList() {
        List<ClassesData> all = classesRepository.findAll();
        assertNotNull(all, "findAll() must not return null");
        assertFalse(all.isEmpty(), "There should be at least one class loaded");
    }

    @Test
    void findByID_knownID_shouldReturnOptional() {
        ClassesData first = classesRepository.findAll().get(0);
        String classID = first.getClassID();
        Optional<ClassesData> opt = classesRepository.findByID(classID);
        assertTrue(opt.isPresent(), "findByID(known) should be present");
        assertEquals(classID, opt.get().getClassID(),
                     "Returned class should have matching ID");
    }

    @Test
    void findByID_unknownID_shouldReturnEmpty() {
        Optional<ClassesData> missing = classesRepository.findByID("NoSuchClassID");
        assertFalse(missing.isPresent(), "findByID(unknown) should return empty");
    }

    @Test
    void findByID_caseInsensitiveLookup() {
        ClassesData first = classesRepository.findAll().get(0);
        String classID = first.getClassID();
        String altCase = classID.toUpperCase();
        Optional<ClassesData> opt = classesRepository.findByID(altCase);
        assertTrue(opt.isPresent(), "Lookup should be case-insensitive");
        assertEquals(classID, opt.get().getClassID());
    }

    @Test
    void mandatoryFields_loadedCorrectly() {
        List<ClassesData> all = classesRepository.findAll();
        assertNotNull(all, "findAll() must not return null");
        assertFalse(all.isEmpty(), "findAll() should contain at least one class");

        ClassesData cd = all.get(0);
        assertNotNull(cd.getClassID(),  "classID should not be null");
        assertNotNull(cd.getName(),     "name should not be null");
        assertNotNull(cd.getHitDie(),   "hitDie should not be null");
        assertNotNull(cd.getPrimaryAbility(), "primaryAbility should not be null");
        assertNotNull(cd.getProficiencies(),  "proficiencies should not be null");
        assertNotNull(cd.getStartingEquipmentOptions(),
                      "startingEquipmentOptions should not be null");

        Map<String, List<ClassesData.Feature>> features = cd.getFeaturesByLevel();
        assertNotNull(features, "featuresByLevel should not be null");
        assertFalse(features.isEmpty(), "featuresByLevel should contain entries");
    }

    @Test
    void proficiencies_nestedFields_loadedCorrectly() {
        // Regression test: the Proficiencies object itself was never null (Jackson
        // still constructs it because the "proficiencies" key exists), but every
        // field inside it deserialized to null because the @JsonProperty keys
        // ("armorProficiencies", "weaponProficiencies", etc.) didn't match the
        // actual JSON keys ("armor", "weapons", etc.). Assert on real values here,
        // not just non-null on the parent object, so this can't regress silently.
        ClassesData barbarian = classesRepository.findByID("barbarian")
                .orElseThrow(() -> new AssertionError("Expected a 'barbarian' class entry in classes.json"));

        ClassesData.Proficiencies proficiencies = barbarian.getProficiencies();
        assertNotNull(proficiencies, "proficiencies should not be null");

        assertEquals(List.of("Light Armor", "Medium Armor", "Shields"),
                     proficiencies.getArmorProficiencies(),
                     "armorProficiencies should match classes.json");
        assertEquals(List.of("Simple Weapons", "Martial Weapons"),
                     proficiencies.getWeaponProficiencies(),
                     "weaponProficiencies should match classes.json");
        assertNotNull(proficiencies.getToolProficiencies(),
                      "toolProficiencies should not be null (empty list is fine)");
        assertEquals(List.of("Strength", "Constitution"),
                     proficiencies.getSavingThrowProficiencies(),
                     "savingThrowProficiencies should match classes.json");

        ClassesData.SkillChoices skills = proficiencies.getSkillChoices();
        assertNotNull(skills, "skills should not be null");
        assertEquals(2, skills.getChoose(), "barbarian should choose 2 skills");
        assertNotNull(skills.getLimitedSkillList(),
                      "limitedSkillList should not be null");
        assertTrue(skills.getLimitedSkillList().contains("Athletics"),
                   "barbarian skill list should include Athletics");
    }
}
