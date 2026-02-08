package com.dndcharactercreator.pdfimport.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.service.PdfFillerService;

/**
 * REST controller responsible for generating filled character sheet PDFs.
 *
 * <p>This controller exposes a single endpoint that accepts character data
 * and returns a completed PDF character sheet using a fixed template.
 *
 * <p>Template inspection and field discovery are handled offline (e.g., via a one-time
 * development script/endpoint) and are not part of the runtime API.
 *
 * @author Carter Ballard
 */
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    /**
     * Service responsible for filling a PDF character sheet from a character DTO.
     */
    private final PdfFillerService filler;

    /**
     * Creates a {@code PdfController}.
     *
     * @param filler service used to generate filled PDFs
     */
    public PdfController(PdfFillerService filler) {
        this.filler = filler;
    }

    /**
     * Fills a character sheet PDF using the submitted character data.
     *
     * <p><b>Endpoint:</b> {@code POST /api/pdf/fill}
     *
     * <p>The request body must be JSON that matches {@link CharacterDto}. On success,
     * this method returns the generated PDF bytes and a {@code Content-Disposition}
     * header so browsers download the file.
     *
     * @param dto character data used to fill the PDF
     * @return a PDF file as a byte array
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
            // TODO: Add logging here if desired (e.g., log.error("PDF generation failed", e);)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
     * DEV-ONLY (removed from runtime):
     * This endpoint was used to inspect AcroForm field names/types in a template PDF.
     * Field names have been captured and saved externally (e.g., in a .txt file).
     *
     * Safe to re-enable if the PDF template changes and field discovery is needed again.
     */
    /*
    @GetMapping("/fields/{templateId}")
    public Map<String, String> listFields(@PathVariable String templateId) {
        String path = "templates/" + templateId + ".pdf";

        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            byte[] pdfBytes = in.readAllBytes();
            try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
                PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
                Map<String, String> out = new LinkedHashMap<>();

                if (form != null) {
                    for (PDField field : form.getFields()) {
                        out.put(field.getFullyQualifiedName(), field.getFieldType());
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
    */
}