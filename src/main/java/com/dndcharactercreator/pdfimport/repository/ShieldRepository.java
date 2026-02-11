package com.dndcharactercreator.pdfimport.repository;

import com.dndcharactercreator.pdfimport.model.ShieldData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;

/**
 * Repository responsible for loading and serving {@link ShieldData} definitions.
 * <p>
 * Shield records are loaded once at application startup from
 * {@code classpath:data/shields.json} and cached in-memory for fast, case-insensitive lookup.
 * </p>
 */
@Repository
public class ShieldRepository {

    /**
     * In-memory cache keyed by lower-cased shield name.
     */
    private final Map<String, ShieldData> shields = new HashMap<>();

    /**
     * Loads all shield records from {@code data/shields.json}.
     * <p>
     * Invoked automatically by Spring after bean construction.
     * Existing entries are cleared before reloading.
     * </p>
     *
     * @throws Exception if the resource cannot be read or parsed
     */
    @PostConstruct
    public void loadAll() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = new ClassPathResource("data/shields.json").getInputStream()) {
            List<ShieldData> list = mapper.readValue(in, new TypeReference<List<ShieldData>>() {});
            shields.clear();
            for (ShieldData s : list) {
                shields.put(s.getName().toLowerCase(Locale.ROOT), s);
            }
        }
    }

    /**
     * Finds a shield by name using case-insensitive matching.
     *
     * @param name shield name to search for; may be {@code null}
     * @return matching {@link ShieldData}, or {@code null} if {@code name} is {@code null}
     *         or no shield with that name exists
     */
    public ShieldData findByName(String name) {
        if (name == null) return null;
        return shields.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns all loaded shield records.
     *
     * @return a list containing all known shields
     */
    public List<ShieldData> findAll() {
        return new ArrayList<>(shields.values());
    }

}
