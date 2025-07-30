package com.dndcharactercreator.pdfimport.controller;

import com.dndcharactercreator.pdfimport.model.ArmorData;
import com.dndcharactercreator.pdfimport.model.BackgroundData;
import com.dndcharactercreator.pdfimport.model.ClassesData;
import com.dndcharactercreator.pdfimport.model.RacesData;
import com.dndcharactercreator.pdfimport.model.ShieldData;
import com.dndcharactercreator.pdfimport.model.SubclassesData;
import com.dndcharactercreator.pdfimport.repository.ArmorRepository;
import com.dndcharactercreator.pdfimport.repository.BackgroundRepository;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;
import com.dndcharactercreator.pdfimport.repository.LanguagesRepository;
import com.dndcharactercreator.pdfimport.repository.RacesRepository;
import com.dndcharactercreator.pdfimport.repository.ShieldRepository;
import com.dndcharactercreator.pdfimport.repository.SkillsRepository;
import com.dndcharactercreator.pdfimport.repository.SubclassesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReferenceController.class)
class ReferenceControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean private RacesRepository racesRepo;
    @MockitoBean private ArmorRepository armorRepo;
    @MockitoBean private ShieldRepository shieldRepo;
    @MockitoBean private BackgroundRepository backgroundRepo;
    @MockitoBean private ClassesRepository classesRepo;
    @MockitoBean private LanguagesRepository languagesRepo;
    @MockitoBean private SkillsRepository skillsRepo;
    @MockitoBean private SubclassesRepository subclassesRepo;

    @Test
    void GET_racesReturnsJsonArray() throws Exception {
        RacesData r = new RacesData(); r.setName("Elf");
        when(racesRepo.findAll()).thenReturn(List.of(r));

        mvc.perform(get("/api/reference/races"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].name").value("Elf"));
    }

    @Test
    void GET_armorReturnsJsonArray() throws Exception {
        ArmorData a = new ArmorData(); a.setName("Chain Mail");
        when(armorRepo.findAll()).thenReturn(List.of(a));

        mvc.perform(get("/api/reference/armor"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].name").value("Chain Mail"));
    }

    @Test
    void GET_shieldsReturnsJsonArray() throws Exception {
        ShieldData s = new ShieldData(); s.setName("Kite Shield");
        when(shieldRepo.findAll()).thenReturn(List.of(s));

        mvc.perform(get("/api/reference/shields"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].name").value("Kite Shield"));
    }

    @Test
    void GET_backgroundsReturnsJsonArray() throws Exception {
        BackgroundData b = new BackgroundData(); b.setName("Soldier");
        when(backgroundRepo.findAll()).thenReturn(List.of(b));

        mvc.perform(get("/api/reference/backgrounds"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].name").value("Soldier"));
    }

    @Test
    void GET_classesReturnsJsonArray() throws Exception {
        ClassesData c = new ClassesData(); 
        c.setClassID("Fighter"); 
        c.setName("Fighter");
        when(classesRepo.findAll()).thenReturn(List.of(c));

        mvc.perform(get("/api/reference/classes"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].classID").value("Fighter"))
           .andExpect(jsonPath("$[0].name").value("Fighter"));
    }

    @Test
    void GET_languagesReturnsJsonArrayOfStrings() throws Exception {
        when(languagesRepo.findAll()).thenReturn(List.of("Common", "Elvish"));

        mvc.perform(get("/api/reference/languages"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0]").value("Common"))
           .andExpect(jsonPath("$[1]").value("Elvish"));
    }

    @Test
    void GET_skillsReturnsJsonArrayOfStrings() throws Exception {
        when(skillsRepo.findAll()).thenReturn(List.of("Stealth", "Arcana"));

        mvc.perform(get("/api/reference/skills"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0]").value("Stealth"))
           .andExpect(jsonPath("$[1]").value("Arcana"));
    }

    @Test
    void GET_subclassesReturnsJsonArray() throws Exception {
        SubclassesData sc = new SubclassesData(); sc.setName("Champion");
        when(subclassesRepo.findAll()).thenReturn(List.of(sc));

        mvc.perform(get("/api/reference/subclasses"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].name").value("Champion"));
    }
}
