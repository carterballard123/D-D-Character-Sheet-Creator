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

/**
 * Repository responsible for loading and serving {@link BackgroundData} definitions.
 * <p>
 * Background records are loaded once at application startup from
 * {@code classpath:data/backgrounds.json}, then cached in memory for fast,
 * case-insensitive lookup by name.
 * </p>
 * <p>
 * This class is a Spring-managed {@link Repository} and initializes its cache in
 * {@link #loadAll()} via {@link PostConstruct}.
 * </p>
 */
@Repository
public class BackgroundRepository {

    /**
     * In-memory lookup map keyed by lower-cased background name.
     */
    private final Map<String, BackgroundData> byName = new HashMap<>();

    /**
     * Loads all background records from {@code data/backgrounds.json} and builds
     * the internal lookup map.
     * <p>
     * This method is called automatically by Spring after bean construction.
     * </p>
     *
     * @throws Exception if the JSON resource cannot be found, read, or parsed
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

    /**
     * Finds a background by name using case-insensitive matching.
     *
     * @param name the background name to search for; may be {@code null}
     * @return the matching {@link BackgroundData}, or {@code null} if {@code name} is {@code null}
     *         or no match exists
     */
    public BackgroundData findByName(String name) {
        if (name == null) return null;
        return byName.get(name.toLowerCase());
    }

    /**
     * Returns all loaded backgrounds.
     *
     * @return an immutable snapshot list of all known background records
     */
    public List<BackgroundData> findAll() {
        return List.copyOf(byName.values());
    }
}
