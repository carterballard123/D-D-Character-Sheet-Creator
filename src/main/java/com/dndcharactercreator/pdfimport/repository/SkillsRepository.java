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
 * Repository responsible for loading and serving supported skill names.
 * <p>
 * Skills are loaded from {@code classpath:data/skills.json} at startup.
 * The JSON file is expected to contain an array of strings.
 * </p>
 */
@Repository
public class SkillsRepository {

    /**
     * In-memory cache of skill names.
     */
    private final List<String> skills = new ArrayList<>();

    /**
     * Loads all skill names from {@code data/skills.json}.
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
        try (InputStream in = new ClassPathResource("data/skills.json").getInputStream()) {
            List<String> list = mapper.readValue(in, new TypeReference<List<String>>() {});
            skills.clear();
            skills.addAll(list);
        }
    }

    /**
     * Returns all loaded skill names.
     *
     * @return an immutable snapshot list of all configured skills
     */
    public List<String> findAll() {
        return List.copyOf(skills);
    }

    /**
     * Finds a skill by name using case-insensitive matching.
     *
     * @param skill skill name to search for; may be {@code null}
     * @return an {@link Optional} containing the matching skill name,
     *         or {@link Optional#empty()} if {@code skill} is {@code null}
     *         or no match exists
     */
    public Optional<String> findByName(String skill) {
        if (skill == null) return Optional.empty();
        return skills.stream().filter(l -> l.equalsIgnoreCase(skill)).findFirst();
    }

    /**
     * Checks whether a skill exists in the repository.
     *
     * @param skill skill name to test; may be {@code null}
     * @return {@code true} if a case-insensitive match exists; otherwise {@code false}
     */
    public boolean exists(String skill) {
        return findByName(skill).isPresent();
    }
}
