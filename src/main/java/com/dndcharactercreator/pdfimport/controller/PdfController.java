package com.dndcharactercreator.pdfimport.controller;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.service.PdfFillerService;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.server.ResponseStatusException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfFillerService filler;

    public PdfController(PdfFillerService filler) {
        this.filler = filler;
    }

    /**
     * POST /api/pdf/fill
     *
     * Accepts a JSON body matching CharacterDto, fills the PDF, and returns
     * the resulting PDF bytes with a download header.
     */
    @PostMapping(
        path = "/fill",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> fillPdf(@RequestBody @Validated CharacterDto dto) {
        try {
            byte[] pdfBytes = filler.fill(dto);
            return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=character-sheet.pdf")
                .body(pdfBytes);
        } catch (Exception e) {
            // optionally log: log.error("PDF generation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/fields/{templateId}")
    public Map<String, String> listFields(@PathVariable String templateId) {
        String path = "templates/" + templateId + ".pdf";

        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            byte[] pdfBytes = in.readAllBytes();                 // ← read the stream
            try (PDDocument doc = Loader.loadPDF(pdfBytes)) {    // ← load from bytes
                PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
                Map<String, String> out = new LinkedHashMap<>();
                if (form != null) {
                    for (PDField f : form.getFields()) {
                        out.put(f.getFullyQualifiedName(), f.getFieldType());
                    }
                }
                return out;
            }
        } catch (IOException e) {
            boolean notFound = e instanceof java.io.FileNotFoundException;
            throw new ResponseStatusException(
                notFound ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR,
                (notFound ? "Template not found: " : "Failed reading template: ") + path,
                e
            );
        }
    }
}
