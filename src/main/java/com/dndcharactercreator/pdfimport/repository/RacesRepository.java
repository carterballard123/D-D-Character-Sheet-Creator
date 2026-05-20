package com.dndcharactercreator.pdfimport.repository;

import com.dndcharactercreator.pdfimport.model.RacesData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository responsible for loading and serving {@link RacesData} definitions.
 * <p>
 * Race records are loaded from {@code classpath:data/races.json} at application startup
 * and cached in-memory for fast, case-insensitive lookup by race name.
 * </p>
 */
@Repository
public class RacesRepository {

    /**
     * In-memory lookup map keyed by lower-cased race name.
     */
    private final Map<String, RacesData> byName = new HashMap<>();

    /**
     * Loads all race records from {@code data/races.json}.
     * <p>
     * Invoked automatically by Spring after bean construction.
     * </p>
     *
     * @throws Exception if the resource cannot be read or parsed
     */
    @PostConstruct
    public void loadAll() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = new ClassPathResource("data/races.json").getInputStream()) {
            List<RacesData> list = mapper.readValue(in, new TypeReference<List<RacesData>>() {});
            for (RacesData race : list) {
                byName.put(race.getName().toLowerCase(), race);

            }
        }
    }

    /**
     * Finds a race by name using case-insensitive matching.
     *
     * @param name race name to search for; may be {@code null}
     * @return matching {@link RacesData}, or {@code null} if {@code name} is {@code null}
     *         or no race with that name exists
     */
    public RacesData findByName(String name) {
        if (name == null) return null;
        return byName.get(name.toLowerCase());

    }

    /**
     * Returns all loaded race records.
     *
     * @return an immutable snapshot list of all known races
     */
    public List<RacesData> findAll() {
        return List.copyOf(byName.values());

    }

    /**
     * Checks whether a race with the given name exists.
     *
     * @param name race name to test; may be {@code null}
     * @return {@code true} if a matching race exists; otherwise {@code false}
     */
    public boolean exists(String name) {
        return findByName(name) != null;

    }

    /**
     * Finds race data by name as an {@link Optional}.
     *
     * @param name race name to search for; may be {@code null}
     * @return an {@link Optional} containing the matching {@link RacesData},
     *         or {@link Optional#empty()} if no match exists
     */
    public Optional<RacesData> findOptions(String name) {
        return Optional.ofNullable(findByName(name));

    }

}