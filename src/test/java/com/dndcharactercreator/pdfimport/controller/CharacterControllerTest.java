package com.dndcharactercreator.pdfimport.controller;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.service.CharacterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CharacterController.class)
class CharacterControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CharacterService characters;

    @Test
    void POST_partiallyFilledCharacter_savesSuccessfully() throws Exception {
        // No level, class, race, etc. - a character still being filled out - only
        // characterName is set, and every @Min/@Max-constrained field is left null.
        String partialJson = """
            {
              "characterName":"Work In Progress"
            }
            """;
        UUID id = UUID.randomUUID();
        when(characters.save(any(CharacterDto.class))).thenReturn(id);

        mvc.perform(post("/api/characters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(partialJson))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void POST_invalidCharacter_returns400() throws Exception {
        // characterLevel is @Min(1) @Max(20) - 99 is out of range.
        String invalidJson = """
            {
              "characterName":"Overleveled Hero",
              "characterLevel":99
            }
            """;

        mvc.perform(post("/api/characters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
           .andExpect(status().isBadRequest());
    }
}
