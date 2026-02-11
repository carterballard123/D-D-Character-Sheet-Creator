package com.dndcharactercreator.pdfimport.repository;

import com.dndcharactercreator.pdfimport.model.ArmorData;
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
 * Repository responsible for loading and serving {@link ArmorData} definitions.
 * <p>
 * Armor records are read once at application startup from
 * {@code classpath:data/armors.json}, then stored in-memory for fast, case-insensitive lookup
 * by armor name.
 * </p>
 * <p>
 * This bean is managed by Spring and initialized via {@link PostConstruct}.
 * </p>
 */
@Repository
public class ArmorRepository {

    /**
     * In-memory lookup map keyed by lower-cased armor name.
     * <p>
     * Example key: {@code "chain mail"}.
     * </p>
     */
    private final Map<String, ArmorData> byName = new HashMap<>();

    /**
     * Loads all armor data from {@code data/armors.json} on startup.
     * <p>
     * Lifecycle: invoked automatically by Spring after bean construction and dependency injection.
     * </p>
     * <p>
     * Behavior:
     * <ol>
     *   <li>Reads JSON from classpath.</li>
     *   <li>Deserializes into {@code List<ArmorData>}.</li>
     *   <li>Indexes records by lower-cased {@code armorName} for case-insensitive lookup.</li>
     * </ol>
     * </p>
     *
     * @throws Exception if the resource cannot be read or parsed
     */
    @PostConstruct
    public void loadAll() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1) Load the JSON file from classpath:
        InputStream in = new ClassPathResource("data/armors.json").getInputStream();

        // 2) Deserialize into a List<ArmorData>:
        List<ArmorData> list = mapper.readValue(in, new TypeReference<>() {});

        // 3) Populate our map for quick lookups:
        for (ArmorData a : list) {
            byName.put(a.getArmorName().toLowerCase(), a);
        }
    }

    /**
     * Finds an armor by name using a case-insensitive key.
     *
     * @param name armor name to search for; may be {@code null}
     * @return matching {@link ArmorData}, or {@code null} if {@code name} is {@code null}
     *         or no matching record exists
     */
    public ArmorData findByName(String name) {
        if (name == null) return null;
        return byName.get(name.toLowerCase());
    }

    /**
     * Returns all loaded armor records.
     *
     * @return immutable snapshot list of all known armors
     */
    public List<ArmorData> findAll() {
        return List.copyOf(byName.values());
    }
}
