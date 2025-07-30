package com.dndcharactercreator.pdfimport.controller;

import com.dndcharactercreator.pdfimport.model.CharacterDto;
import com.dndcharactercreator.pdfimport.service.PdfFillerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
