package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.repository.ArmorRepository;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;
import com.dndcharactercreator.pdfimport.repository.RacesRepository;
import com.dndcharactercreator.pdfimport.repository.ShieldRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * End-to-end check that the subclass actually changes the filled sheet where the rules say it
 * should. Unlike {@link PdfFillerServiceTest}, nothing is mocked: this uses the real
 * {@link DefaultCharacterMathService} and the JSON-backed repositories, because the point is to
 * prove the subclass flows all the way from the DTO into the computed AC.
 *
 * <p>The only subclass-dependent rule today is unarmored defense in
 * {@link DefaultCharacterMathService#computeAC}: Draconic Sorcery and College of Dance use
 * 10 + DEX + CHA instead of 10 + DEX.
 */
@SpringBootTest(
    classes = {
        PdfFillerService.class,
        DefaultCharacterMathService.class,
        ClassesRepository.class,
        RacesRepository.class,
        ArmorRepository.class,
        ShieldRepository.class,
        JacksonAutoConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class PdfFillerServiceSubclassAcTest {

    @Autowired
    private PdfFillerService pdfFillerService;

    /** Unarmored level-3 character with DEX 14 (+2) and CHA 16 (+3); other scores are +0/+1. */
    private CharacterDto unarmored(String className, String subclass) {
        CharacterDto dto = new CharacterDto();
        dto.setCharacterClass(className);
        dto.setCharacterSubClass(subclass);
        dto.setCharacterLevel(3);
        dto.setCharacterStrength(10);
        dto.setCharacterDexterity(14);
        dto.setCharacterConstitution(12);
        dto.setCharacterIntelligence(10);
        dto.setCharacterWisdom(10);
        dto.setCharacterCharisma(16);
        return dto;
    }

    private String ac(CharacterDto dto) throws Exception {
        try (var doc = Loader.loadPDF(pdfFillerService.fill(dto))) {
            PDField field = doc.getDocumentCatalog().getAcroForm().getField("AC");
            return field.getValueAsString();
        }
    }

    @Test
    void sorcerer_withoutSubclass_usesPlainUnarmoredAC() throws Exception {
        assertEquals("12", ac(unarmored("Sorcerer", null)), "10 + DEX(+2)");
    }

    @Test
    void sorcerer_withDraconicSorcery_addsCharismaToUnarmoredAC() throws Exception {
        assertEquals("15", ac(unarmored("Sorcerer", "Draconic Sorcery")), "10 + DEX(+2) + CHA(+3)");
    }

    @Test
    void sorcerer_withOtherSubclass_keepsPlainUnarmoredAC() throws Exception {
        assertEquals("12", ac(unarmored("Sorcerer", "Wild Magic Sorcery")),
            "Only Draconic Sorcery grants Draconic Resilience; other sorcerer subclasses don't");
    }

    @Test
    void bard_withCollegeOfDance_addsCharismaToUnarmoredAC() throws Exception {
        assertEquals("12", ac(unarmored("Bard", null)), "10 + DEX(+2) without the subclass");
        assertEquals("15", ac(unarmored("Bard", "College of Dance")), "10 + DEX(+2) + CHA(+3)");
    }

    @Test
    void draconicSorcery_inArmor_getsNoSubclassBonus() throws Exception {
        CharacterDto dto = unarmored("Sorcerer", "Draconic Sorcery");
        dto.setCharacterArmor("Leather Armor");

        assertEquals("13", ac(dto), "Leather Armor 11 + DEX(+2); the CHA bonus only applies unarmored");
    }
}
