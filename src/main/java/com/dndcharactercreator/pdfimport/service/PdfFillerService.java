package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class PdfFillerService {
    private static final Logger log = LoggerFactory.getLogger(PdfFillerService.class);

    /**
     * Loads the fillable PDF from classpath, populates both raw and computed fields,
     * then returns the finished PDF as a byte array.
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

            // Force PDFBox to regenerate appearance streams, avoiding stale visuals
            form.setNeedAppearances(true);

            // ─── DEBUG: Dump all field names (uncomment to see every name) ───
           log.info("Listing all PDF fields:");
           for (PDField top : form.getFields()) {
               dumpAllFields(top, "");
           }

            // 3) Fill each section
            fillTopTexts(form, dto);
            fillAbilityScores(form, dto);
            fillProficiencyMod(form, dto);
            // ... you can chain more helpers here (armor, weapons, etc.) ...

            // 4) Serialize and return
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            return out.toByteArray();
        }
    }

    /**
     * Helper to set a single text field. Logs a warning if the field name is not found,
     * or if the provided value is null/empty (in which case it writes an empty string).
     */
    private void setField(PDAcroForm form, String name, String value) throws Exception {
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
     * Recursively logs every field’s fully qualified name.
     * Useful for debugging nested or grouped fields.
     */
    @SuppressWarnings("unused")
    private void dumpAllFields(PDField field, String indent) {
        log.info("{}- {}", indent, field.getFullyQualifiedName());
        if (field instanceof PDNonTerminalField) {
            for (PDField child : ((PDNonTerminalField) field).getChildren()) {
                dumpAllFields(child, indent + "  ");
            }
        }
    }

    // ─── Section 1: Basic character info ───

    /**
     * Populates the top portion of the sheet: name, class+level, background, etc.
     * Uses exact field‐names from the PDF.
     */
    private void fillTopTexts(PDAcroForm form, CharacterDto dto) throws Exception {
        setField(form, "CharacterName", dto.getCharacterName());
        setField(form, "ClassLevel",
                 dto.getCharacterClass() + " " + dto.getCharacterLevel());
        setField(form, "Background", dto.getCharacterBackground());
        setField(form, "PlayerName", dto.getPlayerName());

        // NOTE: If your PDF field for Race has a trailing space ("Race "),
        //       update below to exactly match: setField(form, "Race ", dto.getCharacterRace());
        setField(form, "Race ", dto.getCharacterRace());

        setField(form, "Alignment", dto.getCharacterAlignment());
        setField(form, "XP",
                 dto.getCharacterExperiencePoints() == null
                  ? ""
                  : dto.getCharacterExperiencePoints().toString());
    }

    // ─── Section 2: Abilities, modifiers, saves, and skills ───

    /**
     * Fills raw ability scores (STR, DEX, CON, INT, WIS, CHA), their modifiers,
     * the saving throws, and basic skills (no proficiency additions yet).
     */
    private void fillAbilityScores(PDAcroForm form, CharacterDto dto) throws Exception {
        int str = dto.getCharacterStrength();
        int dex = dto.getCharacterDexterity();
        int con = dto.getCharacterConstitution();
        int intel = dto.getCharacterIntelligence();
        int wis = dto.getCharacterWisdom();
        int cha = dto.getCharacterCharisma();

        // 2.1) Raw ability scores
        setField(form, "STR", String.valueOf(str));
        setField(form, "DEX", String.valueOf(dex));
        setField(form, "CON", String.valueOf(con));
        setField(form, "INT", String.valueOf(intel));
        setField(form, "WIS", String.valueOf(wis));
        setField(form, "CHA", String.valueOf(cha));

        // 2.2) Ability modifiers
        setField(form, "STRmod", String.valueOf(computeModifier(str)));
        setField(form, "DEXmod ", String.valueOf(computeModifier(dex)));   // trailing space example
        setField(form, "CONmod", String.valueOf(computeModifier(con)));
        setField(form, "INTmod", String.valueOf(computeModifier(intel)));
        setField(form, "WISmod", String.valueOf(computeModifier(wis)));
        setField(form, "CHamod", String.valueOf(computeModifier(cha)));

        // 2.3) Saving throws (using raw modifiers for now)
        setField(form, "ST Strength",     String.valueOf(computeModifier(str)));
        setField(form, "ST Dexterity",    String.valueOf(computeModifier(dex)));
        setField(form, "ST Constitution", String.valueOf(computeModifier(con)));
        setField(form, "ST Intelligence", String.valueOf(computeModifier(intel)));
        setField(form, "ST Wisdom",       String.valueOf(computeModifier(wis)));
        setField(form, "ST Charisma",     String.valueOf(computeModifier(cha)));

        // 2.4) Skills (no proficiency bonus applied yet)
        // Note: Many of these PDF fields have trailing spaces—fix them by copying from dumpAllFields if needed.
        setField(form, "Acrobatics",      String.valueOf(computeModifier(dex)));
        setField(form, "Animal",          String.valueOf(computeModifier(wis)));
        setField(form, "Arcana",          String.valueOf(computeModifier(intel)));
        setField(form, "Athletics",       String.valueOf(computeModifier(str)));
        setField(form, "Deception ",      String.valueOf(computeModifier(cha)));  // trailing space
        setField(form, "History ",        String.valueOf(computeModifier(intel))); // trailing space
        setField(form, "Insight",         String.valueOf(computeModifier(wis)));
        setField(form, "Intimidation",    String.valueOf(computeModifier(cha)));
        setField(form, "Investigation ",  String.valueOf(computeModifier(intel))); // trailing space
        setField(form, "Medicine",        String.valueOf(computeModifier(wis)));
        setField(form, "Nature",          String.valueOf(computeModifier(intel)));
        setField(form, "Perception ",     String.valueOf(computeModifier(wis)));  // trailing space
        setField(form, "Performance",     String.valueOf(computeModifier(cha)));
        setField(form, "Persuasion",      String.valueOf(computeModifier(cha)));
        setField(form, "Religion",        String.valueOf(computeModifier(intel)));
        setField(form, "SleightofHand",   String.valueOf(computeModifier(dex)));
        setField(form, "Stealth ",        String.valueOf(computeModifier(dex)));  // trailing space
        setField(form, "Survival",        String.valueOf(computeModifier(wis)));
    }

    /**
     * Fills the proficiency bonus field from character level.
     */
    private void fillProficiencyMod(PDAcroForm form, CharacterDto dto) throws Exception {
        int prof = computeProficiency(dto.getCharacterLevel());
        setField(form, "ProfBonus", String.valueOf(prof));
    }

    // ─── Core calculators ───

    /** Computes a 5e ability modifier = floor((score - 10) / 2). */
    private int computeModifier(int abilityScore) {
        return (abilityScore - 10) / 2;
    }

    /**
     * Computes 5e proficiency bonus from level: 
     * Levels 1–4→+2, 5–8→+3, 9–12→+4, 13–16→+5, 17–20→+6.
     */
    private int computeProficiency(int level) {
        int clamped = Math.max(1, level);
        return 2 + (clamped - 1) / 4;
    }
}
