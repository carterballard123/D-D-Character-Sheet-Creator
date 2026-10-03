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

    /** What PdfFillerService writes for any missing-data field: nothing, so it can be filled in
     *  by hand. Kept in sync manually with PdfFillerService.MISSING, which is private. */
    private static final String BLANK = "";

    /**
     * Every field that used to render the old "—" placeholder when its input (or an input it's
     * derived from) was missing. An entirely empty character must leave all of them blank.
     */
    private static final List<String> MISSING_DATA_FIELDS = List.of(
        "CharacterName", "ClassLevel", "Background", "Race ", "Alignment",
        "STR", "DEX", "CON", "INT", "WIS", "CHA",
        "STRmod", "DEXmod ", "CONmod", "INTmod", "WISmod", "CHamod",
        "ST Strength", "ST Dexterity", "ST Constitution", "ST Intelligence", "ST Wisdom", "ST Charisma",
        "Acrobatics", "Animal", "Arcana", "Athletics", "Deception ", "History ", "Insight",
        "Intimidation", "Investigation ", "Medicine", "Nature", "Perception ", "Performance",
        "Persuasion", "Religion", "SleightofHand", "Stealth ", "Survival",
        "ProfBonus", "HPMax", "AC", "Initiative", "HDTotal"
    );

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
    void fill_withNothingFilledIn_leavesEveryMissingDataFieldBlank_andDoesNotThrow() throws Exception {
        // An entirely empty character - what the very first preview after page load would send
        // if the user also cleared the default level.
        CharacterDto dto = new CharacterDto();

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "An entirely empty character must not throw");

        for (String field : MISSING_DATA_FIELDS) {
            assertEquals(BLANK, fieldValue(pdfBytes, field),
                "\"" + field + "\" should be left blank (no dash, no +0) when nothing is filled in");
        }

        // Belt and braces: no text field anywhere on the sheet carries the old dash placeholder
        // or a zero standing in for a missing value.
        try (var doc = Loader.loadPDF(pdfBytes)) {
            for (PDField field : doc.getDocumentCatalog().getAcroForm().getFieldTree()) {
                String value = field.getValueAsString();
                assertFalse(value.contains("—"), field.getFullyQualifiedName() + " still contains a dash: " + value);
                assertNotEquals("+0", value, field.getFullyQualifiedName() + " shows +0 for a missing value");
                assertNotEquals("0", value, field.getFullyQualifiedName() + " shows 0 for a missing value");
            }
        }
    }

    @Test
    void fill_withMissingAbilityScore_leavesThatScoreAndEverythingDerivedFromItBlank() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterWisdom(null); // Wisdom left unset, as if the form isn't finished yet

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "A missing ability score must not throw - it should be left blank instead");

        assertEquals(BLANK, fieldValue(pdfBytes, "WIS"), "The missing score's own raw value should be blank");
        assertEquals(BLANK, fieldValue(pdfBytes, "WISmod"), "The missing score's modifier should be blank, not +0");
        assertEquals(BLANK, fieldValue(pdfBytes, "ST Wisdom"), "Its saving throw should be blank");
        for (String skill : List.of("Animal", "Insight", "Medicine", "Perception ", "Survival")) {
            assertEquals(BLANK, fieldValue(pdfBytes, skill), "Wisdom skill \"" + skill + "\" should be blank");
        }

        // AC depends on DEX/CON/WIS/CHA, so it should also be blank - Wisdom, the score missing
        // in this test, is one of its four inputs.
        assertEquals(BLANK, fieldValue(pdfBytes, "AC"),
            "AC depends on WIS among others, so it should be blank when WIS is missing");

        // Everything NOT dependent on the missing ability should still compute normally -
        // including a genuine +0 (INT 10), which must stay distinguishable from "missing".
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"),
            "An unrelated, present ability score should be unaffected by the missing one");
        assertEquals("+0", fieldValue(pdfBytes, "INTmod"),
            "A real score of 10 should still show +0; only missing scores are blank");
    }

    @Test
    void fill_withMissingDexterity_leavesDexDerivedSkillsInitiativeAndACBlank() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterDexterity(null);

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto));

        for (String field : List.of("DEX", "DEXmod ", "ST Dexterity", "Acrobatics", "SleightofHand",
                                    "Stealth ", "Initiative", "AC")) {
            assertEquals(BLANK, fieldValue(pdfBytes, field), "\"" + field + "\" depends on DEX and should be blank");
        }
        assertEquals("+3", fieldValue(pdfBytes, "Athletics"), "A STR skill should be unaffected by missing DEX");
    }

    @Test
    void fill_withMissingLevel_leavesProficiencyHPAndHitDiceBlank_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterLevel(null);

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "A missing level must not throw - it should be left blank instead");

        assertEquals(BLANK, fieldValue(pdfBytes, "ProfBonus"),
            "Proficiency bonus depends on level and should be blank without it");
        assertEquals(BLANK, fieldValue(pdfBytes, "HPMax"),
            "Max HP depends on level (and CON) and should be blank without it");
        assertEquals(BLANK, fieldValue(pdfBytes, "HDTotal"),
            "Hit dice total depends on level and should be blank without it");
        assertEquals("Fighter", fieldValue(pdfBytes, "ClassLevel"),
            "Only the class should show, with no trailing space or placeholder for the missing level");

        // Ability scores are untouched by a missing level and should still compute normally.
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"));
    }

    @Test
    void fill_withUnresolvableClass_leavesHPBlank_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        // An empty (but non-null) class name - e.g. a raw API caller that explicitly sends "".
        // (Note: a <select> whose only selected option is disabled - i.e. the browser's own
        // placeholder, before anything is picked - is NOT submitted as "" via FormData; it's
        // omitted entirely, which Jackson then leaves as a true null. See the null-class test
        // below for that actual browser scenario.)
        dto.setCharacterClass("");

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "An empty/unrecognized class must not throw - HP should be left blank instead. "
            + "(This used to throw: computeMaxHP's classesRepo.findByID(...).orElseThrow(...) "
            + "had no guard.)");

        assertEquals(BLANK, fieldValue(pdfBytes, "HPMax"),
            "Max HP depends on a resolvable class and should be blank without one");

        // Everything not dependent on the class should still compute normally.
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"));
        assertEquals("+2", fieldValue(pdfBytes, "ProfBonus"));
    }

    @Test
    void fill_withNullClass_leavesACAndHPBlank_andDoesNotThrow() throws Exception {
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

        assertEquals(BLANK, fieldValue(pdfBytes, "AC"),
            "AC depends on a known class (for unarmored-defense rules) and should be blank without one");
        assertEquals(BLANK, fieldValue(pdfBytes, "HPMax"),
            "Max HP depends on a resolvable class and should be blank without one");

        // Everything not dependent on the class should still compute normally.
        assertEquals("+3", fieldValue(pdfBytes, "STRmod"));
        assertEquals("+2", fieldValue(pdfBytes, "ProfBonus"));
    }

    @Test
    void fill_withNullClassAndLevel_leavesClassLevelBlank_andDoesNotThrow() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterClass(null);
        dto.setCharacterLevel(null);

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto),
            "A null class and level must not throw.");

        // This is the actual reported bug: concatenating two nullable fields with `+` doesn't
        // throw the way calling a method on a null one does - it silently produces the literal
        // text "null" instead. Each piece must be guarded independently before joining.
        assertEquals(BLANK, fieldValue(pdfBytes, "ClassLevel"),
            "An all-missing ClassLevel should be blank - not the literal word \"null\", and not a lone space");
    }

    @Test
    void fill_withOnlyClassMissing_showsLevelAloneInClassLevel() throws Exception {
        CharacterDto dto = buildCompleteDto();
        dto.setCharacterClass(null);
        // Level (3) stays set.

        byte[] pdfBytes = assertDoesNotThrow(() -> pdfFillerService.fill(dto));

        assertEquals("3", fieldValue(pdfBytes, "ClassLevel"),
            "Only the present piece (level) should show, with no leading space for the missing class");
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
