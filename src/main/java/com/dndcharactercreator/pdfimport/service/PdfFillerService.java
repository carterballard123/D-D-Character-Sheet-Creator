package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.model.ClassesData;
// import com.dndcharactercreator.pdfimport.model.RacesData;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;
import com.dndcharactercreator.pdfimport.repository.RacesRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

@Service
public class PdfFillerService {
	
    private static final Logger log = LoggerFactory.getLogger(PdfFillerService.class);
    
    private final CharacterMathService math;
    private final RacesRepository racesRepo;
    private final ClassesRepository classesRepo;
    
    public PdfFillerService(CharacterMathService math, RacesRepository racesRepo, ClassesRepository classesRepo) {
        this.math = math;
        this.racesRepo = racesRepo;
        this.classesRepo = classesRepo;
    }
    
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

            // 3) Fill each section
            fillTopTexts(form, dto);
            fillAbilityScores(form, dto);
            fillProficiencyMod(form, dto);
            fillHP(form, dto);
            fillAC(form, dto);
            fillSpeed(form, dto);
            fillInitiative(form, dto);
            fillHitDie(form, dto);

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
     * Populates the top portion of the sheet: name, class+level, background, etc.
     * Uses exact field‐names from the PDF.
     */
    private void fillTopTexts(PDAcroForm form, CharacterDto dto) throws Exception {
        setField(form, "CharacterName", dto.getCharacterName());
        setField(form, "ClassLevel", dto.getCharacterClass() + " " + dto.getCharacterLevel());
        setField(form, "Background", dto.getCharacterBackground());
        setField(form, "PlayerName", dto.getPlayerName());
        setField(form, "Race ", dto.getCharacterRace());
        setField(form, "Alignment", dto.getCharacterAlignment());
        setField(form, "XP", dto.getCharacterExperiencePoints() == null ? "" : dto.getCharacterExperiencePoints().toString());
        
    }

    /**
     * Fills raw ability scores (STR, DEX, CON, INT, WIS, CHA), their modifiers,
     * the saving throws, and basic skills (no proficiency additions yet).
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
        setField(form, "DEXmod ",fmtSigned(math.computeModifier(DEX))); // trailing space
        setField(form, "CONmod", fmtSigned(math.computeModifier(CON)));
        setField(form, "INTmod", fmtSigned(math.computeModifier(INT)));
        setField(form, "WISmod", fmtSigned(math.computeModifier(WIS)));
        setField(form, "CHamod", fmtSigned(math.computeModifier(CHA)));

        // Saving throws (using raw modifiers for now
        setField(form, "ST Strength",     fmtSigned(math.computeModifier(STR)));
        setField(form, "ST Dexterity",    fmtSigned(math.computeModifier(DEX)));
        setField(form, "ST Constitution", fmtSigned(math.computeModifier(CON)));
        setField(form, "ST Intelligence", fmtSigned(math.computeModifier(INT)));
        setField(form, "ST Wisdom",       fmtSigned(math.computeModifier(WIS)));
        setField(form, "ST Charisma",     fmtSigned(math.computeModifier(CHA)));

        // Skills (no proficiency bonus applied yet
        setField(form, "Acrobatics",      fmtSigned(math.computeModifier(DEX)));
        setField(form, "Animal",          fmtSigned(math.computeModifier(WIS)));
        setField(form, "Arcana",          fmtSigned(math.computeModifier(INT)));
        setField(form, "Athletics",       fmtSigned(math.computeModifier(STR)));
        setField(form, "Deception ",      fmtSigned(math.computeModifier(CHA))); // trailing space
        setField(form, "History ",        fmtSigned(math.computeModifier(INT))); // trailing space
        setField(form, "Insight",         fmtSigned(math.computeModifier(WIS)));
        setField(form, "Intimidation",    fmtSigned(math.computeModifier(CHA)));
        setField(form, "Investigation ",  fmtSigned(math.computeModifier(INT))); // trailing space
        setField(form, "Medicine",        fmtSigned(math.computeModifier(WIS)));
        setField(form, "Nature",          fmtSigned(math.computeModifier(INT)));
        setField(form, "Perception ",     fmtSigned(math.computeModifier(WIS))); // trailing space
        setField(form, "Performance",     fmtSigned(math.computeModifier(CHA)));
        setField(form, "Persuasion",      fmtSigned(math.computeModifier(CHA)));
        setField(form, "Religion",        fmtSigned(math.computeModifier(INT)));
        setField(form, "SleightofHand",   fmtSigned(math.computeModifier(DEX)));
        setField(form, "Stealth ",        fmtSigned(math.computeModifier(DEX))); // trailing space
        setField(form, "Survival",        fmtSigned(math.computeModifier(WIS)));
    }

   
    // Fills the proficiency bonus field from character level.
    private void fillProficiencyMod(PDAcroForm form, CharacterDto dto) throws Exception {
        setField(form, "ProfBonus", fmtSigned(math.computeProficiency(dto.getCharacterLevel())));
    }

    private void fillHP(PDAcroForm form, CharacterDto dto) throws Exception {	
        int conMod = math.computeModifier(dto.getCharacterConstitution());
        int maxHP = math.computeMaxHP(dto.getCharacterClass(), dto.getCharacterLevel(), conMod);

        // System.out.printf(">>> fillHP: class=%s, level=%d, conMod=%d%n", dto.getCharacterClass(), dto.getCharacterLevel(), conMod);
        setField(form, "HPMax", String.valueOf(maxHP));
        // System.out.printf(">>> computed maxHP = %d%n", maxHP);
    }
    
    private void fillAC(PDAcroForm form, CharacterDto dto) throws Exception {
    	
    	// System.out.printf("DTO armor='%s', shield='%s'%n", dto.getCharacterArmor(), dto.getCharacterShield());

        int dexMod = math.computeModifier(dto.getCharacterDexterity());
        int conMod = math.computeModifier(dto.getCharacterConstitution());
        int wisMod = math.computeModifier(dto.getCharacterWisdom());
        int chaMod = math.computeModifier(dto.getCharacterCharisma());
        
        int AC = math.computeAC(dto.getCharacterClass(), dto.getCharacterSubClass(), dexMod, conMod, wisMod, chaMod, dto.getCharacterArmor(), dto.getCharacterShield());
        
        setField(form, "AC", String.valueOf(AC));
    }
    
    private void fillSpeed(PDAcroForm form, CharacterDto dto) throws Exception {
        Integer speed = dto.getCharacterSpeed(); // use what the client sent (if any)

        if (speed == null) {
            String raceName = dto.getCharacterRace();
            if (raceName != null && !raceName.isBlank()) {
                var race = racesRepo.findByName(raceName); // adjust if your repo returns Optional
                if (race != null) {
                    speed = race.getSpeed(); // direct from JSON/model
                }
            }
        }

        if (speed != null) {
            setField(form, "Speed", String.valueOf(speed)); // PDF field is "Speed"
        }
    }
    
    private void fillInitiative(PDAcroForm form, CharacterDto dto) throws Exception {
    	int dexMod = math.computeModifier(dto.getCharacterDexterity());
    	setField(form, "Initiative", fmtSigned(dexMod));
    }
    
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
    
    private static String fmtSigned(Integer i) {
    	if (i == null) return "";
    	if(i >= 0) return "+" + String.valueOf(i);
    	else return String.valueOf(i);
    }
}