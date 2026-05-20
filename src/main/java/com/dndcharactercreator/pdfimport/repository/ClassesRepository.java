package com.dndcharactercreator.pdfimport.repository;

import com.dndcharactercreator.pdfimport.model.ClassesData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Repository responsible for loading and serving {@link ClassesData} definitions.
 * <p>
 * Class records are loaded once at application startup from
 * {@code classpath:data/classes.json} and cached in-memory for fast lookup by
 * {@code classID}.
 * </p>
 * <p>
 * Lookup keys are normalized to lower case using {@link Locale#ROOT} to ensure
 * consistent, locale-independent matching.
 * </p>
 */
@Repository
public class ClassesRepository {

    /**
     * In-memory cache of class data keyed by lower-cased {@code classID}.
     */
    private final Map<String, ClassesData> classes = new HashMap<>();

    /**
     * Loads all class definitions from {@code data/classes.json}.
     * <p>
     * This method is invoked once by Spring after bean construction.
     * It clears any existing cache entries and repopulates the map from JSON.
     * </p>
     * <p>
     * Validation: each loaded record must contain a non-null {@code classID}.
     * </p>
     *
     * @throws Exception if the classpath resource cannot be read or parsed
     * @throws IllegalStateException if any class record is missing {@code classID}
     */
    @PostConstruct
    public void loadAll() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Create a Resource for the one JSON file:
        var resource = new ClassPathResource("data/classes.json");
        try (InputStream in = resource.getInputStream()) {
            // read the entire array in one go
            List<ClassesData> list = mapper.readValue(in, new TypeReference<List<ClassesData>>() {});
            classes.clear();
            for (ClassesData cd : list) {
                if (cd.getClassID() == null) {
                    throw new IllegalStateException("Loaded class JSON from \"" + resource.getFilename() + "\" is missing `classID`");
                }
                classes.put(cd.getClassID().toLowerCase(Locale.ROOT), cd);
            }
        }
    }

    /**
     * Finds a class by its {@code classID}.
     * <p>
     * Matching is case-insensitive and locale-safe ({@link Locale#ROOT}).
     * </p>
     *
     * @param classID the class identifier to search for; may be {@code null}
     * @return an {@link Optional} containing the matching {@link ClassesData},
     *         or {@link Optional#empty()} if {@code classID} is {@code null}
     *         or no matching class exists
     */
    public Optional<ClassesData> findByID(String classID) {
        if (classID == null) return Optional.empty();
        return Optional.ofNullable(classes.get(classID.toLowerCase(Locale.ROOT)));
    }

    /**
     * Returns all loaded class definitions.
     *
     * @return an immutable snapshot list of all known classes
     */
    public List<ClassesData> findAll() {
        return List.copyOf(classes.values());
    }
}
