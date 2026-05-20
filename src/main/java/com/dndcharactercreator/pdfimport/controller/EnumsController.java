package com.dndcharactercreator.pdfimport.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST controller that exposes "enum-like" reference values used by the frontend.
 *
 * <p>This controller serves small, simple lookup lists (e.g., alignments) that are stored
 * as JSON files in {@code src/main/resources/data}. These endpoints allow the UI to load
 * consistent option lists without hardcoding them in JavaScript.
 *
 * <p>Endpoints in this controller return JSON responses.
 *
 * @author Carter Ballard
 */
@RestController
@RequestMapping("/api/enums")
public class EnumsController {

    /**
     * JSON parser used to convert classpath resources into Java objects.
     *
     * <p>Injected from Spring Boot so configuration (modules/features) is consistent
     * across the application.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates an {@code EnumsController}.
     *
     * @param objectMapper the application's configured Jackson mapper
     */
    public EnumsController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Returns the list of available alignments.
     *
     * <p>Data is loaded from {@code data/alignments.json} on the application classpath.
     *
     * @return list of alignment names
     * @throws IOException if the resource exists but cannot be parsed as JSON
     */
    @GetMapping("/alignments")
    public List<String> getAlignments() throws IOException {
        try (InputStream in = getResourceOrThrow("data/alignments.json")) {
            return objectMapper.readValue(in, new TypeReference<List<String>>() {});
        }
    }

    /**
     * Opens a classpath resource or throws a 404 if it cannot be found.
     *
     * @param path classpath-relative resource path (e.g., {@code data/alignments.json})
     * @return open {@link InputStream} for the resource
     * @throws ResponseStatusException if the resource does not exist
     */
    private InputStream getResourceOrThrow(String path) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, path + " not found");
        }
        return in;
    }
}
