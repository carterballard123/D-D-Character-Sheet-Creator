package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;
import com.dndcharactercreator.pdfimport.repository.RacesRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Service responsible for generating a filled character sheet PDF.
 *
 * <p>This service loads a fillable PDF template (AcroForm) from the application classpath,
 * populates the PDF fields using both:
 * <ul>
 *   <li>Raw values directly supplied by the client ({@link CharacterDto})</li>
 *   <li>Computed values derived from rule logic ({@link CharacterMathService})</li>
 * </ul>
 *
 * <p>The main entry point is {@link #fill(CharacterDto)}, which returns a PDF as a byte array.
 * Controllers can return those bytes directly to the browser with {@code application/pdf}.
 *
 * <p>PDF field names must match the field names in the template exactly (including any
 * trailing spaces found in the PDF).
 *
 * @author Carter Ballard
 */
@Service
public class PdfFillerService { 

    private static final Logger log = LoggerFactory.getLogger(PdfFillerService.class);

    /** Rules engine used to compute derived values like modifiers, HP, AC, etc. */
    private final CharacterMathService math;

    /** Repository used to look up race data (e.g., movement speed). */
    private final RacesRepository racesRepo;

    /** Repository used to look up class data (e.g., hit die). */
    private final ClassesRepository classesRepo;

    /**
     * Constructs the PDF filler service with required dependencies.
     *
     * @param math rules engine used for computed values
     * @param racesRepo repository for race reference data
     * @param classesRepo repository for class reference data
     */
    public PdfFillerService(CharacterMathService math, RacesRepository racesRepo, ClassesRepository classesRepo) {
        this.math = math;
        this.racesRepo = racesRepo;
        this.classesRepo = classesRepo;
    }

    /**
     * Loads the fillable PDF template from the classpath, populates fields using values from the
     * provided {@link CharacterDto}, and returns the finished PDF as a byte array.
     *
     * <p>Template location: {@code src/main/resources/templates/5ECharacterSheet.pdf}
     *
     * @param dto character build data provided by the frontend
     * @return the filled PDF as bytes
     * @throws Exception if the template cannot be read or the PDF cannot be filled
     */
    public byte[] fill(CharacterDto dto) throws Exception {
        // 1) Load template from classpath
        ClassPathResource template = new ClassPathResource("templates/5ECharacterSheet.pdf");

        try (InputStream in = template.getInputStream();
             PDDocument pdf = Loader.loadPDF(new RandomAccessReadBuffer(in))) {

            // 2) Grab the AcroForm
            PDAcroForm form = pdf.getDocumentCatalog().getAcroForm();
            if (form == null) {
                log.error("PDF template has no AcroForm.");
                throw new IllegalStateException("PDF template has no AcroForm.");
            }

            // Ensure appearance streams are regenerated (helps avoid blank/stale visible values).
            form.setNeedAppearances(true);

            // 3) Fill each section
            fillTopTexts(form, dto);
            fillAbilityScores(form, dto);
            fillProficiencyMod(form, dto);
            fillHP(form, dto);
            fillAC(form, dto);
            fillSpeed(form, dto);
            fillInitiative(form, dto);
            fillHitDie(form, dto);
            fillLanguages(form, dto);

            // 4) Serialize and return
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            return out.toByteArray();
        }
    }

    /**
     * Helper for setting the value of a single AcroForm field by its exact name.
     *
     * <p>If the field does not exist in the PDF template, a warning is logged and the method returns.
     * If the value is {@code null} or empty, an empty string is written (and a warning is logged).
     *
     * @param form the PDF's AcroForm
     * @param name exact PDF field name (must match template; may include trailing spaces)
     * @param value value to write into the field (null/empty writes "")
     * @throws Exception if PDFBox fails to set the field value
     */
    private static void setField(PDAcroForm form, String name, String value) throws Exception {
        PDField field = form.getField(name);
        if (field == null) {
            log.warn("No field named \"{}\" found in PDF.", name);
            return;
        }

        if (value == null || value.isEmpty()) {
            log.warn("Field \"{}\" is being set to empty or null value.", name);
            field.setValue("");
        } else {
            field.setValue(value);
        }
    }

    /**
     * Populates the top text fields on the PDF: character name, class/level, background,
     * player name, race, alignment, and XP.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillTopTexts(PDAcroForm form, CharacterDto dto) throws Exception {
        setField(form, "CharacterName", dto.getCharacterName());
        setField(form, "ClassLevel", dto.getCharacterClass() + " " + dto.getCharacterLevel());
        setField(form, "Background", dto.getCharacterBackground());
        setField(form, "PlayerName", dto.getPlayerName());
        setField(form, "Race ", dto.getCharacterRace()); // NOTE: PDF field includes a trailing space
        setField(form, "Alignment", dto.getCharacterAlignment());
        setField(form, "XP", dto.getCharacterExperiencePoints() == null ? "" : dto.getCharacterExperiencePoints().toString());
    }

    /**
     * Populates raw ability scores and their modifiers, plus saving throws and skills
     * using raw modifiers only (no proficiency bonus additions yet).
     *
     * <p>Important: Several field names in this PDF template include trailing spaces.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillAbilityScores(PDAcroForm form, CharacterDto dto) throws Exception {
        int STR = dto.getCharacterStrength();
        int DEX = dto.getCharacterDexterity();
        int CON = dto.getCharacterConstitution();
        int INT = dto.getCharacterIntelligence();
        int WIS = dto.getCharacterWisdom();
        int CHA = dto.getCharacterCharisma();

        // Raw ability scores
        setField(form, "STR", String.valueOf(STR));
        setField(form, "DEX", String.valueOf(DEX));
        setField(form, "CON", String.valueOf(CON));
        setField(form, "INT", String.valueOf(INT));
        setField(form, "WIS", String.valueOf(WIS));
        setField(form, "CHA", String.valueOf(CHA));

        // Ability modifiers
        setField(form, "STRmod", fmtSigned(math.computeModifier(STR)));
        setField(form, "DEXmod ", fmtSigned(math.computeModifier(DEX))); // NOTE: trailing space
        setField(form, "CONmod", fmtSigned(math.computeModifier(CON)));
        setField(form, "INTmod", fmtSigned(math.computeModifier(INT)));
        setField(form, "WISmod", fmtSigned(math.computeModifier(WIS)));
        setField(form, "CHamod", fmtSigned(math.computeModifier(CHA)));

        // Saving throws (currently raw modifiers only)
        setField(form, "ST Strength",     fmtSigned(math.computeModifier(STR)));
        setField(form, "ST Dexterity",    fmtSigned(math.computeModifier(DEX)));
        setField(form, "ST Constitution", fmtSigned(math.computeModifier(CON)));
        setField(form, "ST Intelligence", fmtSigned(math.computeModifier(INT)));
        setField(form, "ST Wisdom",       fmtSigned(math.computeModifier(WIS)));
        setField(form, "ST Charisma",     fmtSigned(math.computeModifier(CHA)));

        // Skills (currently raw modifiers only)
        setField(form, "Acrobatics",     fmtSigned(math.computeModifier(DEX)));
        setField(form, "Animal",         fmtSigned(math.computeModifier(WIS)));
        setField(form, "Arcana",         fmtSigned(math.computeModifier(INT)));
        setField(form, "Athletics",      fmtSigned(math.computeModifier(STR)));
        setField(form, "Deception ",     fmtSigned(math.computeModifier(CHA))); // NOTE: trailing space
        setField(form, "History ",       fmtSigned(math.computeModifier(INT))); // NOTE: trailing space
        setField(form, "Insight",        fmtSigned(math.computeModifier(WIS)));
        setField(form, "Intimidation",   fmtSigned(math.computeModifier(CHA)));
        setField(form, "Investigation ", fmtSigned(math.computeModifier(INT))); // NOTE: trailing space
        setField(form, "Medicine",       fmtSigned(math.computeModifier(WIS)));
        setField(form, "Nature",         fmtSigned(math.computeModifier(INT)));
        setField(form, "Perception ",    fmtSigned(math.computeModifier(WIS))); // NOTE: trailing space
        setField(form, "Performance",    fmtSigned(math.computeModifier(CHA)));
        setField(form, "Persuasion",     fmtSigned(math.computeModifier(CHA)));
        setField(form, "Religion",       fmtSigned(math.computeModifier(INT)));
        setField(form, "SleightofHand",  fmtSigned(math.computeModifier(DEX)));
        setField(form, "Stealth ",       fmtSigned(math.computeModifier(DEX))); // NOTE: trailing space
        setField(form, "Survival",       fmtSigned(math.computeModifier(WIS)));
    }

    /**
     * Fills the proficiency bonus field from character level.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillProficiencyMod(PDAcroForm form, CharacterDto dto) throws Exception {
        setField(form, "ProfBonus", fmtSigned(math.computeProficiency(dto.getCharacterLevel())));
    }

    /**
     * Computes and fills maximum hit points (HPMax) using class hit die, level,
     * and Constitution modifier.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillHP(PDAcroForm form, CharacterDto dto) throws Exception {
        int conMod = math.computeModifier(dto.getCharacterConstitution());
        int maxHP = math.computeMaxHP(dto.getCharacterClass(), dto.getCharacterLevel(), conMod);
        setField(form, "HPMax", String.valueOf(maxHP));
    }

    /**
     * Computes and fills Armor Class (AC) based on class/subclass and equipment.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillAC(PDAcroForm form, CharacterDto dto) throws Exception {
        int dexMod = math.computeModifier(dto.getCharacterDexterity());
        int conMod = math.computeModifier(dto.getCharacterConstitution());
        int wisMod = math.computeModifier(dto.getCharacterWisdom());
        int chaMod = math.computeModifier(dto.getCharacterCharisma());

        int AC = math.computeAC(
            dto.getCharacterClass(),
            dto.getCharacterSubClass(),
            dexMod,
            conMod,
            wisMod,
            chaMod,
            dto.getCharacterArmor(),
            dto.getCharacterShield()
        );

        setField(form, "AC", String.valueOf(AC));
    }

    /**
     * Fills movement speed on the sheet.
     *
     * <p>Prefers the speed supplied by the client, but if absent, attempts to
     * look up race speed from {@link RacesRepository}.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillSpeed(PDAcroForm form, CharacterDto dto) throws Exception {
        Integer speed = dto.getCharacterSpeed(); // use what the client sent (if any)

        if (speed == null) {
            String raceName = dto.getCharacterRace();
            if (raceName != null && !raceName.isBlank()) {
                var race = racesRepo.findByName(raceName); // assumes repo returns null if not found
                if (race != null) {
                    speed = race.getSpeed();
                }
            }
        }

        if (speed != null) {
            setField(form, "Speed", String.valueOf(speed));
        }
    }

    /**
     * Fills initiative using Dexterity modifier only.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillInitiative(PDAcroForm form, CharacterDto dto) throws Exception {
        int dexMod = math.computeModifier(dto.getCharacterDexterity());
        setField(form, "Initiative", fmtSigned(dexMod));
    }

    /**
     * Fills hit dice total (e.g., "3d10") based on class hit die and character level.
     *
     * <p>This method uses {@link ClassesRepository} to resolve the hit die for the class.
     * If the class cannot be found, the field is left unchanged.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillHitDie(PDAcroForm form, CharacterDto dto) throws Exception {
        String raw = dto.getCharacterClass();
        if (raw == null || raw.isBlank()) return;

        String key = raw.trim().toLowerCase(java.util.Locale.ROOT);

        java.util.Optional<com.dndcharactercreator.pdfimport.model.ClassesData> clsOpt = classesRepo.findByID(key);
        if (clsOpt.isEmpty()) return;

        var cls = clsOpt.get();
        int die = cls.getHitDie();
        int level = Math.max(1, dto.getCharacterLevel());
        String dice = level + "d" + die;

        setField(form, "HDTotal", dice);
    }

    /**
     * Formats an integer value with a leading "+" for non-negative numbers.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code 2 -> "+2"}</li>
     *   <li>{@code 0 -> "+0"}</li>
     *   <li>{@code -1 -> "-1"}</li>
     * </ul>
     *
     * @param i integer value
     * @return formatted string, or empty string if {@code i} is null
     */
    private static String fmtSigned(Integer i) {
        if (i == null) return "";
        return (i >= 0) ? ("+" + i) : String.valueOf(i);
    }

    /**
     * Fills the "ProficienciesLang" field with a human-readable language list.
     *
     * <p>This method de-duplicates and trims the language strings before joining.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillLanguages(PDAcroForm form, CharacterDto dto) throws Exception {
        var langs = dto.getCharacterLanguages();

        var unique = new java.util.LinkedHashSet<String>();
        if (langs != null) {
            for (var s : langs) {
                if (s == null) continue;
                var t = s.trim();
                if (!t.isEmpty()) unique.add(t);
            }
        }

        setField(form, "ProficienciesLang", "Languages: " + String.join(", ", unique));
    }

    /**
     * Placeholder for logic that checks/unchecks saving throw proficiency boxes.
     *
     * <p>Unfinished: left intentionally blank for future implementation.
     *
     * @param form PDF form to modify
     * @param dto character build input
     * @throws Exception if PDFBox fails to set checkbox values
     */
    private static void checkSavingThrowBoxes(PDAcroForm form, CharacterDto dto) throws Exception {
        // TODO: not yet implemented, and currently unreachable (never called).
        // Blocked on CharacterDto: it has no saving-throw-proficiency data yet
        // (class-level proficiencies now load correctly via ClassesData.Proficiencies,
        // see getSavingThrowProficiencies(), but nothing wires that into CharacterDto
        // for a specific character). Once that's added, this should mirror whatever
        // pattern the other PDF checkbox-filling methods use in this class.
    }
}
