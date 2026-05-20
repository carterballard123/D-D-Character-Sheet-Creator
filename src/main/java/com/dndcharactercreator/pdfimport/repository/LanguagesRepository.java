package com.dndcharactercreator.pdfimport.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository responsible for loading and serving supported language names.
 * <p>
 * Language values are loaded from {@code classpath:data/languages.json} at startup.
 * The JSON file is expected to contain a simple array of strings.
 * </p>
 */
@Repository
public class LanguagesRepository {

    /**
     * In-memory cache of language names loaded from JSON.
     */
    private final List<String> Languages = new ArrayList<>();

    /**
     * Loads all language names from {@code data/languages.json}.
     * <p>
     * Invoked automatically by Spring after bean construction.
     * Existing cached values are cleared before loading fresh data.
     * </p>
     *
     * @throws Exception if the resource cannot be read or parsed
     */
    @PostConstruct
    public void loadAll() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = new ClassPathResource("data/languages.json").getInputStream()) {
            List<String> list = mapper.readValue(in, new TypeReference<List<String>>() {});
            Languages.clear();
            Languages.addAll(list);

        }
    }

    /**
     * Returns all loaded language names.
     *
     * @return an immutable snapshot list of all configured languages
     */
    public List<String> findAll() {
        return List.copyOf(Languages);

    }

    /**
     * Finds a language by name using case-insensitive matching.
     *
     * @param lang language name to search for; may be {@code null}
     * @return an {@link Optional} containing the matching language string,
     *         or {@link Optional#empty()} if {@code lang} is {@code null}
     *         or no match exists
     */
    public Optional<String> findByName(String lang) {
        if (lang == null) return Optional.empty();
        return Languages.stream().filter(l -> l.equalsIgnoreCase(lang)).findFirst();

    }

    /**
     * Checks whether a language exists in the repository.
     *
     * @param lang language name to test; may be {@code null}
     * @return {@code true} if a case-insensitive match exists; otherwise {@code false}
     */
    public boolean exits(String lang) {
        return findByName(lang).isPresent();

    }

}