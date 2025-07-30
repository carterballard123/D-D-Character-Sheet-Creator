package com.dndcharactercreator.pdfimport.repository;

import com.dndcharactercreator.pdfimport.model.BackgroundData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BackgroundRepository {
    // In-memory map: lowercase background name → BackgroundData
    private final Map<String, BackgroundData> byName = new HashMap<>();

    /**
     * At application startup, read backgrounds.json and populate a lookup map
     * keyed by lower-case name. Called by Spring right after the bean is constructed.
     */
    @PostConstruct
    public void loadAll() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1) Load the JSON file from classpath (put your file in src/main/resources/data/)
        InputStream in = new ClassPathResource("data/backgrounds.json").getInputStream();

        // 2) Deserialize into a List<BackgroundData>
        List<BackgroundData> list = mapper.readValue(in, new TypeReference<List<BackgroundData>>() {});

        // 3) Populate our map for quick lookups by name
        for (BackgroundData bg : list) {
            byName.put(bg.getName().toLowerCase(), bg);
        }
    }

    /** Find one background by its exact name (case-insensitive). Returns null if not found. */
    public BackgroundData findByName(String name) {
        if (name == null) return null;
        return byName.get(name.toLowerCase());
    }

    /** Returns an immutable list of all backgrounds (for dropdowns, etc.). */
    public List<BackgroundData> findAll() {
        return List.copyOf(byName.values());
    }
}
