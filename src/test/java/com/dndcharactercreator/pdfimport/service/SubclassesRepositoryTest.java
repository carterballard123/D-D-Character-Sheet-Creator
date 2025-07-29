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
    SubclassesRepository.class,
    JacksonAutoConfiguration.class
  },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class SubclassesRepositoryTest {

    @Autowired
    private SubclassesRepository subclassesRepository;

    @Test
    void loadAll_shouldReturnNonEmptyList() {
        List<SubclassesData> all = subclassesRepository.findAll();
        assertNotNull(all, "findAll() must not return null");
        assertFalse(all.isEmpty(), "There should be at least one subclass loaded");
    }

    @Test
    void findByName_knownName_shouldReturnOptional() {
        String first = subclassesRepository.findAll().get(0).getName();
        Optional<SubclassesData> opt = subclassesRepository.findByName(first);
        assertTrue(opt.isPresent(), "findByName(known) should be present");
        assertEquals(first, opt.get().getName());
    }

    @Test
    void exists_knownAndUnknownNames() {
        String known = subclassesRepository.findAll().get(0).getName();
        assertTrue(subclassesRepository.exists(known), "exists(known) should be true");
        assertFalse(subclassesRepository.exists("NoSuchSubclass"), "exists(unknown) should be false");
    }

    @Test
    void findByParentClass_knownParent_shouldReturnList() {
        // grab the parentClass of the first entry
        String parent = subclassesRepository.findAll().get(0).getParentClass();
        List<SubclassesData> list = subclassesRepository.findByParentClass(parent);
        assertNotNull(list, "findByParentClass() must not return null");
        assertFalse(list.isEmpty(), "There should be at least one subclass for parent: " + parent);
        // verify each has the same parent
        list.forEach(sc -> assertEquals(parent, sc.getParentClass()));
    }

    @Test
    void findTrait_existingTrait_returnsTrait() {
        SubclassesData sc = subclassesRepository.findAll().get(0);
        int level = sc.getTraits().get(0).getLevel();
        String traitName = sc.getTraits().get(0).getName();
        Optional<SubclassesData.Trait> t = 
            subclassesRepository.findTrait(sc.getName(), level, traitName);
        assertTrue(t.isPresent(), "findTrait should find level " + level + " / " + traitName);
        assertEquals(level, t.get().getLevel());
        assertEquals(traitName, t.get().getName());
    }

    @Test
    void findTrait_nonexistent_returnsEmpty() {
        Optional<SubclassesData.Trait> t = 
            subclassesRepository.findTrait("NoSuchSubclass", 1, "NoTrait");
        assertFalse(t.isPresent(), "findTrait on unknown should be empty");
    }
}
