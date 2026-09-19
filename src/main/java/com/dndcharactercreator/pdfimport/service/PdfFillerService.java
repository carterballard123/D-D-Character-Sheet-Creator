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
 * <p><b>Missing data:</b> most of {@link CharacterDto}'s fields are optional, to support
 * generating a preview PDF for a character that's still being built in the UI. Any field or
 * derived value that can't be filled in because something it depends on is missing is written
 * as {@code "—"} (em dash) rather than left however PDFBox defaults an unset field, or worse,
 * computed from an unboxed {@code null}. That null-checking intentionally lives here rather
 * than in {@link CharacterMathService}/{@link DefaultCharacterMathService}: the math service
 * stays a simple "given valid inputs, compute the answer" engine that still takes primitive
 * {@code int}s, and this class - which already owns the decision of what gets written to each
 * PDF field - is the one place that knows when a computation should be skipped in favor of a
 * placeholder.
 *
 * @author Carter Ballard
 */
@Service
public class PdfFillerService {

    private static final Logger log = LoggerFactory.getLogger(PdfFillerService.class);

    /** Placeholder written for any field or derived value that's missing required input. */
    private static final String MISSING = "—";

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
     * <p>Name, background, race, and alignment are all optional on {@link CharacterDto}; each
     * renders as {@value #MISSING} when absent rather than an empty field, so an in-progress
     * character's preview PDF reads as "not filled in yet" rather than blank/broken.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillTopTexts(PDAcroForm form, CharacterDto dto) throws Exception {
        setField(form, "CharacterName", orDash(dto.getCharacterName()));

        Integer level = dto.getCharacterLevel();
        setField(form, "ClassLevel", dto.getCharacterClass() + " " + (level == null ? MISSING : level));

        setField(form, "Background", orDash(dto.getCharacterBackground()));
        setField(form, "PlayerName", dto.getPlayerName());
        setField(form, "Race ", orDash(dto.getCharacterRace())); // NOTE: PDF field includes a trailing space
        setField(form, "Alignment", orDash(dto.getCharacterAlignment()));
        setField(form, "XP", dto.getCharacterExperiencePoints() == null ? "" : dto.getCharacterExperiencePoints().toString());
    }

    /**
     * Populates raw ability scores and their modifiers, plus saving throws and skills
     * using raw modifiers only (no proficiency bonus additions yet).
     *
     * <p>Each ability score is optional on {@link CharacterDto}. A missing score renders as
     * {@value #MISSING} for its own raw value and modifier, and cascades to every saving throw
     * and skill that's governed by that ability - e.g. a missing Wisdom score means the Wisdom
     * modifier, the Wisdom saving throw, and Animal Handling/Insight/Medicine/Perception/Survival
     * all render {@value #MISSING}, while everything governed by a present ability computes
     * normally.
     *
     * <p>Important: Several field names in this PDF template include trailing spaces.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillAbilityScores(PDAcroForm form, CharacterDto dto) throws Exception {
        Integer STR = dto.getCharacterStrength();
        Integer DEX = dto.getCharacterDexterity();
        Integer CON = dto.getCharacterConstitution();
        Integer INT = dto.getCharacterIntelligence();
        Integer WIS = dto.getCharacterWisdom();
        Integer CHA = dto.getCharacterCharisma();

        // Raw ability scores
        setField(form, "STR", fmtScore(STR));
        setField(form, "DEX", fmtScore(DEX));
        setField(form, "CON", fmtScore(CON));
        setField(form, "INT", fmtScore(INT));
        setField(form, "WIS", fmtScore(WIS));
        setField(form, "CHA", fmtScore(CHA));

        // Each modifier is computed once (null if its score is missing) and reused below -
        // fmtSigned() already renders a null modifier as "—".
        Integer strMod = safeModifier(STR);
        Integer dexMod = safeModifier(DEX);
        Integer conMod = safeModifier(CON);
        Integer intMod = safeModifier(INT);
        Integer wisMod = safeModifier(WIS);
        Integer chaMod = safeModifier(CHA);

        // Ability modifiers
        setField(form, "STRmod", fmtSigned(strMod));
        setField(form, "DEXmod ", fmtSigned(dexMod)); // NOTE: trailing space
        setField(form, "CONmod", fmtSigned(conMod));
        setField(form, "INTmod", fmtSigned(intMod));
        setField(form, "WISmod", fmtSigned(wisMod));
        setField(form, "CHamod", fmtSigned(chaMod));

        // Saving throws (currently raw modifiers only)
        setField(form, "ST Strength",     fmtSigned(strMod));
        setField(form, "ST Dexterity",    fmtSigned(dexMod));
        setField(form, "ST Constitution", fmtSigned(conMod));
        setField(form, "ST Intelligence", fmtSigned(intMod));
        setField(form, "ST Wisdom",       fmtSigned(wisMod));
        setField(form, "ST Charisma",     fmtSigned(chaMod));

        // Skills (currently raw modifiers only)
        setField(form, "Acrobatics",     fmtSigned(dexMod));
        setField(form, "Animal",         fmtSigned(wisMod));
        setField(form, "Arcana",         fmtSigned(intMod));
        setField(form, "Athletics",      fmtSigned(strMod));
        setField(form, "Deception ",     fmtSigned(chaMod)); // NOTE: trailing space
        setField(form, "History ",       fmtSigned(intMod)); // NOTE: trailing space
        setField(form, "Insight",        fmtSigned(wisMod));
        setField(form, "Intimidation",   fmtSigned(chaMod));
        setField(form, "Investigation ", fmtSigned(intMod)); // NOTE: trailing space
        setField(form, "Medicine",       fmtSigned(wisMod));
        setField(form, "Nature",         fmtSigned(intMod));
        setField(form, "Perception ",    fmtSigned(wisMod)); // NOTE: trailing space
        setField(form, "Performance",    fmtSigned(chaMod));
        setField(form, "Persuasion",     fmtSigned(chaMod));
        setField(form, "Religion",       fmtSigned(intMod));
        setField(form, "SleightofHand",  fmtSigned(dexMod));
        setField(form, "Stealth ",       fmtSigned(dexMod)); // NOTE: trailing space
        setField(form, "Survival",       fmtSigned(wisMod));
    }

    /**
     * Fills the proficiency bonus field from character level.
     *
     * <p>Level is optional on {@link CharacterDto}; when absent this renders {@value #MISSING}
     * rather than calling {@link CharacterMathService#computeProficiency(int)}.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillProficiencyMod(PDAcroForm form, CharacterDto dto) throws Exception {
        setField(form, "ProfBonus", fmtSigned(safeProficiency(dto.getCharacterLevel())));
    }

    /**
     * Computes and fills maximum hit points (HPMax) using class hit die, level,
     * and Constitution modifier.
     *
     * <p>Level and Constitution are both optional on {@link CharacterDto}; if either is
     * missing there's nothing to compute HP from, so this renders {@value #MISSING} instead
     * of calling {@link CharacterMathService#computeMaxHP(String, int, int)}.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillHP(PDAcroForm form, CharacterDto dto) throws Exception {
        Integer conMod = safeModifier(dto.getCharacterConstitution());
        Integer level = dto.getCharacterLevel();

        if (conMod == null || level == null) {
            setField(form, "HPMax", MISSING);
            return;
        }

        int maxHP = math.computeMaxHP(dto.getCharacterClass(), level, conMod);
        setField(form, "HPMax", String.valueOf(maxHP));
    }

    /**
     * Computes and fills Armor Class (AC) based on class/subclass and equipment.
     *
     * <p>AC depends on the Dexterity, Constitution, Wisdom, and Charisma modifiers (which
     * combination actually matters depends on class/subclass unarmored-defense rules). All four
     * ability scores are optional on {@link CharacterDto}; if any is missing this renders
     * {@value #MISSING} rather than guessing which rule would have applied.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillAC(PDAcroForm form, CharacterDto dto) throws Exception {
        Integer dexMod = safeModifier(dto.getCharacterDexterity());
        Integer conMod = safeModifier(dto.getCharacterConstitution());
        Integer wisMod = safeModifier(dto.getCharacterWisdom());
        Integer chaMod = safeModifier(dto.getCharacterCharisma());

        if (dexMod == null || conMod == null || wisMod == null || chaMod == null) {
            setField(form, "AC", MISSING);
            return;
        }

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
     * <p>Dexterity is optional on {@link CharacterDto}; when absent this renders
     * {@value #MISSING} (via {@link #fmtSigned}) rather than computing from a missing score.
     *
     * @param form PDF form to populate
     * @param dto character build input
     * @throws Exception if any PDF field set operation fails
     */
    private void fillInitiative(PDAcroForm form, CharacterDto dto) throws Exception {
        setField(form, "Initiative", fmtSigned(safeModifier(dto.getCharacterDexterity())));
    }

    /**
     * Fills hit dice total (e.g., "3d10") based on class hit die and character level.
     *
     * <p>This method uses {@link ClassesRepository} to resolve the hit die for the class.
     * If the class cannot be found, the field is left unchanged (pre-existing behavior,
     * unrelated to level being optional). If level is missing there's no dice total to show,
     * so this renders {@value #MISSING} instead.
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

        Integer level = dto.getCharacterLevel();
        if (level == null) {
            setField(form, "HDTotal", MISSING);
            return;
        }

        String dice = Math.max(1, level) + "d" + die;
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
     * @return formatted string, or {@value #MISSING} if {@code i} is null
     */
    private static String fmtSigned(Integer i) {
        if (i == null) return MISSING;
        return (i >= 0) ? ("+" + i) : String.valueOf(i);
    }

    /**
     * Renders a possibly-missing plain numeric value (no +/- sign) - used for raw ability
     * scores, as opposed to their signed modifiers.
     *
     * @param i integer value
     * @return the value as a string, or {@value #MISSING} if {@code i} is null
     */
    private static String fmtScore(Integer i) {
        return (i == null) ? MISSING : String.valueOf(i);
    }

    /**
     * Returns the given string, or {@value #MISSING} if it's null or blank.
     *
     * <p>Used for fields that used to be required (name/background/race/alignment) and are now
     * optional - once a field isn't required, "the client sent an empty string" and "the client
     * didn't send anything" both mean the same thing for display purposes: this information is
     * missing.
     *
     * @param value the raw string value
     * @return {@code value}, or {@value #MISSING} if it's null or blank
     */
    private static String orDash(String value) {
        return (value == null || value.isBlank()) ? MISSING : value;
    }

    /**
     * Ability modifier for a possibly-missing score, or {@code null} if the score itself
     * wasn't provided.
     *
     * <p>Ability scores are optional on {@link CharacterDto} (to support the live preview
     * rendering an in-progress character), so there may be nothing to compute a modifier from.
     * Feed the result straight into {@link #fmtSigned}, which already renders a null modifier
     * as {@value #MISSING}.
     *
     * @param score the ability score, or null if not provided
     * @return the computed modifier, or null if {@code score} is null
     */
    private Integer safeModifier(Integer score) {
        return (score == null) ? null : math.computeModifier(score);
    }

    /**
     * Proficiency bonus for a possibly-missing level, or {@code null} if the level itself
     * wasn't provided. Same reasoning as {@link #safeModifier}.
     *
     * @param level the character level, or null if not provided
     * @return the computed proficiency bonus, or null if {@code level} is null
     */
    private Integer safeProficiency(Integer level) {
        return (level == null) ? null : math.computeProficiency(level);
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
