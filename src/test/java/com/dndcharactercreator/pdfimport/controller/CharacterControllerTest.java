package com.dndcharactercreator.pdfimport.controller;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.service.CharacterService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void PUT_existingCharacter_returns204() throws Exception {
        String json = """
            {
              "characterName":"Grognak",
              "characterClass":"Barbarian",
              "characterLevel":4
            }
            """;
        UUID id = UUID.randomUUID();
        when(characters.update(eq(id), any(CharacterDto.class))).thenReturn(true);

        mvc.perform(put("/api/characters/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
           .andExpect(status().isNoContent())
           .andExpect(content().string(""));

        // The service is a mock, so also check the request body actually reached it intact.
        ArgumentCaptor<CharacterDto> sent = ArgumentCaptor.forClass(CharacterDto.class);
        verify(characters).update(eq(id), sent.capture());
        assertEquals("Grognak", sent.getValue().getCharacterName());
        assertEquals(4, sent.getValue().getCharacterLevel());
    }

    @Test
    void PUT_unknownId_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(characters.update(eq(id), any(CharacterDto.class))).thenReturn(false);

        mvc.perform(put("/api/characters/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "characterName":"Nobody"
                    }
                    """))
           .andExpect(status().isNotFound());
    }

    @Test
    void PUT_invalidCharacter_returns400() throws Exception {
        // characterLevel is @Min(1) @Max(20) - 99 is out of range.
        String invalidJson = """
            {
              "characterName":"Overleveled Hero",
              "characterLevel":99
            }
            """;

        mvc.perform(put("/api/characters/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
           .andExpect(status().isBadRequest());

        // Validation must reject the request before anything is written.
        verify(characters, never()).update(any(), any());
    }

    @Test
    void PUT_partiallyFilledCharacter_updatesSuccessfully() throws Exception {
        // Same shape as the partial POST above: only characterName, every
        // @Min/@Max-constrained field left null.
        String partialJson = """
            {
              "characterName":"Work In Progress"
            }
            """;
        UUID id = UUID.randomUUID();
        when(characters.update(eq(id), any(CharacterDto.class))).thenReturn(true);

        mvc.perform(put("/api/characters/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(partialJson))
           .andExpect(status().isNoContent());
    }

    @Test
    void DELETE_existingCharacter_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        when(characters.delete(id)).thenReturn(true);

        mvc.perform(delete("/api/characters/{id}", id))
           .andExpect(status().isNoContent())
           .andExpect(content().string(""));
    }

    @Test
    void DELETE_unknownId_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(characters.delete(id)).thenReturn(false);

        mvc.perform(delete("/api/characters/{id}", id))
           .andExpect(status().isNotFound());
    }
}
