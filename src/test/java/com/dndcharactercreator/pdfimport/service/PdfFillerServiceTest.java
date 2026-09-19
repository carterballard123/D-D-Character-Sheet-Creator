package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.model.ClassesData;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;
import com.dndcharactercreator.pdfimport.repository.RacesRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
  classes = PdfFillerService.class
)
class PdfFillerServiceTest {

    /** Placeholder PdfFillerService writes for any missing-data field. Kept in sync manually
     *  with PdfFillerService.MISSING, which is private - there's nothing to import here. */
    private static final String MISSING = "—";

    @Autowired
    private PdfFillerService pdfFillerService;

    @MockitoBean
    private CharacterMathService math;

    @MockitoBean
    private RacesRepository racesRepo;

    @MockitoBean
    private ClassesRepository classesRepo;

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

        ClassesData fighter = new ClassesData();
        fighter.setHitDie(10);
        when(classesRepo.findByID("fighter")).thenReturn(Optional.of(fighter));

        // determine how many pages the blank template has
        var tpl = new ClassPathResource("templates/5ECharacterSheet.pdf");
        try (var in = tpl.getInputStream();
             var doc = Loader.loadPDF(new RandomAccessReadBuffer(in))) {
            templatePageCount = doc.getNumberOfPages();
        }
    }

    /**
     * A fully-populated DTO - every field this service reads has a real value.
     * Individual tests null out whichever field they want to exercise as missing.
     */
    private CharacterDto buildCompleteDto() {
        CharacterDto dto = new CharacterDto();
        dto.setCharacterName("Test Hero");
        dto.setPlayerName("JUnit");
        dto.setCharacterClass("Fighter");
        dto.setCharacterLevel(3);
        dto.setCharacterBackground("Soldier");
        dto.setCharacterRace("Human");
        dto.setCharacterAlignment("Neutral");
        dto.setCharacterExperiencePoints(900);
        dto.setCharacterSpeed(30);
        dto.setCharacterStrength(16);
        dto.setCharacterDexterity(12);
        dto.setCharacterConstitution(20);
        dto.setCharacterIntelligence(10);
        dto.setCharacterWisdom(8);
        dto.setCharacterCharisma(13);
        dto.setCharacterSubClass("Champion");
        dto.setCharacterArmor("Chain Mail");
        dto.setCharacterShield("Shield");
        return dto;
    }

    /** Reads a single AcroForm field's value back out of a filled PDF's bytes. */
    private String fieldValue(byte[] pdfBytes, String fieldName) throws Exception {
        try (var doc = Loader.loadPDF(pdfBytes)) {
            PDField field = doc.getDocumentCatalog().getAcroForm().getField(fieldName);
            return field == null ? null : field.getValueAsString();
        }
    }

    /**
     * Whether a checkbox field is checked in a filled PDF's bytes. Uses PDCheckBox.isChecked()
     * rather than comparing fieldValue(...) against a guessed "on" string, for the same reason
     * PdfFillerService itself uses PDCheckBox.check() to set it: the field's actual "on" export
     * value for this template was never confirmed, and isChecked()/check() don't need it.
     */
    private boolean isChecked(byte[] pdfBytes, String fieldName) throws Exception {
        try (var doc = Loader.loadPDF(pdfBytes)) {
            PDField field = doc.getDocumentCatalog().getAcroForm().getField(fieldName);
            return (field instanceof PDCheckBox checkBox) && checkBox.isChecked();
        }
    }

    @Test
    void fill_shouldProduceFilledPdf_andMatchTemplatePageCount() throws Exception {
        CharacterDto dto = buildCompleteDto();

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

        // --- lock in concrete field values for a fully-populated character, so a future
        // change to the missing-data handling can't silently alter the all-present case ---
        assertEquals("Test Hero", fieldValue(pdfBytes, "CharacterName"));
        assertEquals("Soldier", fieldValue(pdfBytes, "Background"));
        assertEquals("Fighter 3", fieldValue(pdfBytes, "ClassLevel"));
        assertEquals("16", fieldValue(pdfBytes, "STR"));
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"));
        assertEquals("-1", fieldValue(pdfBytes, "WISmod"));
        assertEquals("+2", fieldValue(pdfBytes, "ProfBonus"));
        assertEquals("20", fieldValue(pdfBytes, "HPMax"));
        assertEquals("18", fieldValue(pdfBytes, "AC"));
        assertEquals("3d10", fieldValue(pdfBytes, "HDTotal"));
    }

    @Test
    void fill_withMissingAbilityScore_rendersDashForThatScoresModifier_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterWisdom(null); // Wisdom left unset, as if the form isn't finished yet

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "A missing ability score must not throw - it should render as a dash instead");

        assertEquals(MISSING, fieldValue(pdfBytes, "WIS"),
            "The missing score's own raw value should render as a dash");
        assertEquals(MISSING, fieldValue(pdfBytes, "WISmod"),
            "The missing score's own modifier should render as a dash");
        assertEquals(MISSING, fieldValue(pdfBytes, "ST Wisdom"),
            "A saving throw governed by the missing ability should render as a dash");
        assertEquals(MISSING, fieldValue(pdfBytes, "Insight"),
            "A skill governed by the missing ability should render as a dash");

        // AC depends on DEX/CON/WIS/CHA, so it should also cascade to a dash - Wisdom, the
        // score missing in this test, is one of its four inputs.
        assertEquals(MISSING, fieldValue(pdfBytes, "AC"),
            "AC depends on WIS among others, so it should render as a dash when WIS is missing");

        // Everything NOT dependent on the missing ability should still compute normally.
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"),
            "An unrelated, present ability score should be unaffected by the missing one");
    }

    @Test
    void fill_withMissingLevel_rendersDashForProficiencyAndHP_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterLevel(null);

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "A missing level must not throw - it should render as a dash instead");

        assertEquals(MISSING, fieldValue(pdfBytes, "ProfBonus"),
            "Proficiency bonus depends on level and should render as a dash without it");
        assertEquals(MISSING, fieldValue(pdfBytes, "HPMax"),
            "Max HP depends on level (and CON) and should render as a dash without it");
        assertEquals(MISSING, fieldValue(pdfBytes, "HDTotal"),
            "Hit dice total depends on level and should render as a dash without it");

        // Ability scores are untouched by a missing level and should still compute normally.
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"));
    }

    @Test
    void fill_withUnresolvableClass_rendersDashForHP_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        // An empty (but non-null) class name - e.g. a raw API caller that explicitly sends "".
        // (Note: a <select> whose only selected option is disabled - i.e. the browser's own
        // placeholder, before anything is picked - is NOT submitted as "" via FormData; it's
        // omitted entirely, which Jackson then leaves as a true null. See the null-class test
        // below for that actual browser scenario.)
        dto.setCharacterClass("");

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "An empty/unrecognized class must not throw - HP should render as a dash instead. "
            + "(This used to throw: computeMaxHP's classesRepo.findByID(...).orElseThrow(...) "
            + "had no guard.)");

        assertEquals(MISSING, fieldValue(pdfBytes, "HPMax"),
            "Max HP depends on a resolvable class and should render as a dash without one");

        // Everything not dependent on the class should still compute normally.
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"));
        assertEquals("+2", fieldValue(pdfBytes, "ProfBonus"));
    }

    @Test
    void fill_withNullClass_rendersDashForACAndHP_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        // This is what the live preview actually sends before a class is picked: the
        // <select>'s only selected option is its disabled placeholder, and a disabled
        // selected option is excluded from FormData entirely - the key never makes it into
        // the JSON body at all, so Jackson leaves this field as a true null (not "").
        dto.setCharacterClass(null);

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "A null class must not throw. computeAC calls className.equalsIgnoreCase(...) "
            + "directly to check for Barbarian/Monk unarmored defense - unlike an empty or "
            + "unrecognized class name, a null one crashes that call outright, and this is "
            + "the actual value a real page load produces, not just a hypothetical edge case.");

        assertEquals(MISSING, fieldValue(pdfBytes, "AC"),
            "AC depends on a known class (for unarmored-defense rules) and should render as a dash without one");
        assertEquals(MISSING, fieldValue(pdfBytes, "HPMax"),
            "Max HP depends on a resolvable class and should render as a dash without one");

        // Everything not dependent on the class should still compute normally.
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"));
        assertEquals("+2", fieldValue(pdfBytes, "ProfBonus"));
    }

    @Test
    void fill_withNullClassAndLevel_rendersDashPiecesForClassLevel_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterClass(null);
        dto.setCharacterLevel(null);

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "A null class and level must not throw.");

        // This is the actual reported bug: concatenating two nullable fields with `+` doesn't
        // throw the way calling a method on a null one does - it silently produces the literal
        // text "null" instead. Each piece must be guarded independently before joining.
        assertEquals(MISSING + " " + MISSING, fieldValue(pdfBytes, "ClassLevel"),
            "An all-missing ClassLevel should show dash pieces, not the literal word \"null\"");
    }

    @Test
    void fill_withOnlyClassMissing_rendersDashForClassPieceOnly() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterClass(null);
        // Level (3) stays set.

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto));

        assertEquals(MISSING + " 3", fieldValue(pdfBytes, "ClassLevel"),
            "Only the missing piece (class) should become a dash; the present piece (level) is unaffected");
    }

    /** All six saving-throw checkbox field names, for iterating in the "checks none" test. */
    private static final List<String> ALL_SAVING_THROW_CHECKBOXES = List.of(
        "Check Box 11", "Check Box 18", "Check Box 19", "Check Box 20", "Check Box 21", "Check Box 22"
    );

    @Test
    void fill_withBarbarianClass_checksExactlyStrengthAndConstitutionSavingThrows() throws Exception {
        ClassesData barbarian = new ClassesData();
        barbarian.setHitDie(12);
        ClassesData.Proficiencies profs = new ClassesData.Proficiencies();
        profs.setSavingThrowProficiencies(List.of("Strength", "Constitution"));
        barbarian.setProficiencies(profs);
        when(classesRepo.findByID("barbarian")).thenReturn(Optional.of(barbarian));

        CharacterDto dto = buildCompleteDto();
        dto.setCharacterClass("Barbarian");

        byte[] pdfBytes = pdfFillerService.fill(dto);

        assertTrue(isChecked(pdfBytes, "Check Box 11"), "Strength save should be checked for a Barbarian");
        assertTrue(isChecked(pdfBytes, "Check Box 19"), "Constitution save should be checked for a Barbarian");
        assertFalse(isChecked(pdfBytes, "Check Box 18"), "Dexterity save should not be checked");
        assertFalse(isChecked(pdfBytes, "Check Box 20"), "Intelligence save should not be checked");
        assertFalse(isChecked(pdfBytes, "Check Box 21"), "Wisdom save should not be checked");
        assertFalse(isChecked(pdfBytes, "Check Box 22"), "Charisma save should not be checked");
    }

    @Test
    void fill_withUnresolvableClass_checksNoSavingThrowBoxes_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterClass(null);

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "A null/unresolvable class must not throw when checking saving-throw boxes.");

        for (String box : ALL_SAVING_THROW_CHECKBOXES) {
            assertFalse(isChecked(pdfBytes, box), box + " should not be checked when the class is unresolvable");
        }
    }
}
