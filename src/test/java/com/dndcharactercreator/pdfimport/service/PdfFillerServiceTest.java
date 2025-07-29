package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
  classes = PdfFillerService.class
)
class PdfFillerServiceTest {

    @Autowired
    private PdfFillerService pdfFillerService;

    @MockBean
    private CharacterMathService math;

    private int templatePageCount;

    @BeforeEach
    void setUp() throws Exception {
        // stub out CharacterMathService
        when(math.computeModifier(ArgumentMatchers.anyInt()))
            .thenAnswer(inv -> (inv.getArgument(0, Integer.class) - 10) / 2);
        when(math.computeProficiency(ArgumentMatchers.anyInt())).thenReturn(2);
        when(math.computeMaxHP(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()))
            .thenReturn(20);
        when(math.computeAC(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
            .thenReturn(18);

        // determine how many pages the blank template has
        var tpl = new ClassPathResource("templates/5ECharacterSheet.pdf");
        try (var in = tpl.getInputStream();
             var doc = Loader.loadPDF(new RandomAccessReadBuffer(in))) {
            templatePageCount = doc.getNumberOfPages();
        }
    }

    @Test
    void fill_shouldProduceFilledPdf_andMatchTemplatePageCount() throws Exception {
        // --- build a sample DTO ---
        CharacterDto dto = new CharacterDto();
        dto.setCharacterName("Test Hero");
        dto.setPlayerName("JUnit");
        dto.setCharacterClass("Fighter");
        dto.setCharacterLevel(3);
        dto.setCharacterBackground("Soldier");
        dto.setCharacterRace("Human");
        dto.setCharacterAlignment("Neutral");
        dto.setCharacterExperiencePoints(900);
        dto.setCharacterStrength(16);
        dto.setCharacterDexterity(12);
        dto.setCharacterConstitution(20);
        dto.setCharacterIntelligence(10);
        dto.setCharacterWisdom(8);
        dto.setCharacterCharisma(13);
        dto.setCharacterSubClass("Champion");
        dto.setCharacterArmor("Chain Mail");
        dto.setCharacterHasShield("Shield");

        // --- fill the PDF ---
        byte[] pdfBytes = pdfFillerService.fill(dto);

        // sanity checks on the byte[]:
        assertNotNull(pdfBytes, "fill(...) must not return null");
        assertTrue(pdfBytes.length > 0, "fill(...) must return a non-empty PDF");

        // write it out so you can eyeball it:
        Path out = Paths.get("target/filledTestSheet.pdf");
        Files.createDirectories(out.getParent());
        Files.write(out, pdfBytes);

        // now verify that the generated PDF has the same page count as the template
        try (var doc = Loader.loadPDF(pdfBytes)) {
            assertEquals(templatePageCount,
                         doc.getNumberOfPages(),
                         "The filled PDF should have the same number of pages as the blank template");
        }
    }
}
