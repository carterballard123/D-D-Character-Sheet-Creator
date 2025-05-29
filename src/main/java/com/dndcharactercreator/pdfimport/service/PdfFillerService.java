package com.dndcharactercreator.pdfimport.service;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class PdfFillerService {

    /**
     * Loads the fillable PDF from classpath, populates both raw and computed fields,
     * then returns the finished PDF as a byte array.
     */
    public byte[] fill(CharacterDto dto) throws Exception {
        // 1) Load the PDF template from src/main/resources/templates/5ECharacterSheet.pdf
        ClassPathResource template = new ClassPathResource("templates/5ECharacterSheet.pdf");
        
        // Open the template stream and the PDF document in one try-with-resources
        try (InputStream in = template.getInputStream();
             PDDocument pdf = Loader.loadPDF(new RandomAccessReadBuffer(in))) {

            // 2) Grab the AcroForm so we can set field values
            PDAcroForm form = pdf.getDocumentCatalog().getAcroForm();
            if (form == null) {
                throw new IllegalStateException("PDF template has no AcroForm.");
            }

            // 3) Fill the simple text fields
            setField(form, "characterName",        dto.getCharacterName());
            setField(form, "playerName",           dto.getPlayerName());
            setField(form, "characterBackground",  dto.getCharacterBackground());
            setField(form, "characterClass",       dto.getCharacterClass());
            setField(form, "characterLevel",       dto.getCharacterLevel().toString());
            setField(form, "characterRace",        dto.getCharacterRace());
            setField(form, "characterAlignment",   dto.getCharacterAlignment());

            // 4) Fill up to 3 weapons
            fillList(form, "weapon", dto.getCharacterWeapons(), 3);

            // 5) Fill the six ability scores
            setField(form, "strength",    dto.getCharacterStrength().toString());
            setField(form, "dexterity",   dto.getCharacterDexterity().toString());
            setField(form, "constitution",dto.getCharacterConstitution().toString());
            setField(form, "intelligence",dto.getCharacterIntelligence().toString());
            setField(form, "wisdom",      dto.getCharacterWisdom().toString());
            setField(form, "charisma",    dto.getCharacterCharisma().toString());

            // 6) Compute and fill Armor Class (AC = 10 + Dex mod + armor bonus)
            int dexMod     = (dto.getCharacterDexterity() - 10) / 2;
            int armorBonus = 0;
            try {
                armorBonus = dto.getCharacterArmor() != null ? Integer.parseInt(dto.getCharacterArmor()) : 0;
            } catch (NumberFormatException e) {
                // leave armorBonus = 0 on parse failure
            }
            setField(form, "armorClass", String.valueOf(10 + dexMod + armorBonus));

            // 7) Write out to bytes and return
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            return out.toByteArray();
        }
    }

    /** Helper to set a single text field (blanks it if name not found or value null). */
    private void setField(PDAcroForm form, String name, String value) throws Exception {
        PDField field = form.getField(name);
        if (field != null) {
            field.setValue(value != null ? value : "");
        }
    }

    /** Helper to fill a numbered list of fields (e.g. weapon1, weapon2, …). */
    private void fillList(PDAcroForm form, String baseName, List<String> values, int maxSlots) throws Exception {
        for (int i = 1; i <= maxSlots; i++) {
            String val = (values != null && values.size() >= i) ? values.get(i - 1) : "";
            setField(form, baseName + i, val);
        }
    }
}
