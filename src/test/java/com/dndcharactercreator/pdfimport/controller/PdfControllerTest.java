package com.dndcharactercreator.pdfimport.controller;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.service.PdfFillerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PdfController.class)
class PdfControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PdfFillerService filler;

    private static final String VALID_JSON = """
        {
          "characterName":"Test Hero",
          "playerName":"JUnit",
          "characterClass":"Fighter",
          "characterLevel":3,
          "characterBackground":"Soldier",
          "characterRace":"Human",
          "characterAlignment":"Neutral",
          "characterExperiencePoints":900,
          "characterStrength":16,
          "characterDexterity":12,
          "characterConstitution":14,
          "characterIntelligence":10,
          "characterWisdom":8,
          "characterCharisma":13,
          "characterSubClass":"Champion",
          "characterArmor":"Chain Mail",
          "characterShield":"Shield"
        }
        """;

    @Test
    void POST_fillWithValidDto_returnsPdfBytes() throws Exception {
        byte[] fakePdf = new byte[]{0x25,0x50,0x44,0x46}; // "%PDF"
        when(filler.fill(any(CharacterDto.class))).thenReturn(fakePdf);

        mvc.perform(post("/api/pdf/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_PDF))
           .andExpect(header().string("Content-Disposition",
                                     "attachment; filename=character-sheet.pdf"))
           .andExpect(content().bytes(fakePdf));
    }

    /** Posts {@code json} to /api/pdf/fill and returns the CharacterDto Spring actually bound from it. */
    private CharacterDto boundDto(String json) throws Exception {
        when(filler.fill(any(CharacterDto.class))).thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

        mvc.perform(post("/api/pdf/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
           .andExpect(status().isOk());

        ArgumentCaptor<CharacterDto> captor = ArgumentCaptor.forClass(CharacterDto.class);
        verify(filler).fill(captor.capture());
        return captor.getValue();
    }

    @Test
    void POST_fill_withSubclassName_key_isSilentlyIgnored() throws Exception {
        // "subclassName" is what the frontend sent before #11 (the subclass <select>'s name). The
        // DTO has no such property and unknown JSON keys are ignored, so the subclass was dropped
        // without any error. Kept as a guard: the backend deliberately has no alias for this key.
        CharacterDto dto = boundDto("""
            {"characterClass":"Sorcerer","characterLevel":3,"subclassName":"Draconic Sorcery"}
            """);

        assertNull(dto.getCharacterSubClass(),
            "\"subclassName\" is not a CharacterDto property, so it must not populate the subclass");
    }

    @Test
    void POST_fill_withCharacterSubClass_key_bindsSubclass() throws Exception {
        CharacterDto dto = boundDto("""
            {"characterClass":"Sorcerer","characterLevel":3,"characterSubClass":"Draconic Sorcery"}
            """);

        assertEquals("Draconic Sorcery", dto.getCharacterSubClass());
    }

    @Test
    void POST_fill_whenServiceThrows_returns500() throws Exception {
        when(filler.fill(any())).thenThrow(new RuntimeException("service failure"));

        mvc.perform(post("/api/pdf/fill")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_JSON))
           .andExpect(status().isInternalServerError());
    }
}
