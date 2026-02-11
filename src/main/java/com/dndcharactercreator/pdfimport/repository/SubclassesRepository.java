package com.dndcharactercreator.pdfimport.repository;

import com.dndcharactercreator.pdfimport.model.SubclassesData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;

/**
 * Repository responsible for loading and serving {@link SubclassesData} definitions.
 * <p>
 * Subclass data is loaded once at application startup from
 * {@code classpath:data/subclasses.json}, then indexed in-memory for:
 * </p>
 * <ul>
 *   <li>lookup by subclass name (case-insensitive), and</li>
 *   <li>lookup by parent class (case-insensitive, one-to-many).</li>
 * </ul>
 */
@Repository
public class SubclassesRepository {

    /**
     * Immutable-source-style cache of all loaded subclasses.
     */
    private final List<SubclassesData> subclasses = new ArrayList<>();

    /**
     * Index keyed by lower-cased subclass name.
     */
    private final Map<String, SubclassesData> byName = new HashMap<>();

    /**
     * Index keyed by lower-cased parent class name.
     */
    private final Multimap<String, SubclassesData> byParent = new Multimap<>();

    /**
     * Loads all subclass definitions from {@code data/subclasses.json} and rebuilds indexes.
     * <p>
     * Invoked automatically by Spring after bean construction.
     * Existing cached data and indexes are cleared before reloading.
     * </p>
     *
     * @throws Exception if the JSON resource cannot be found, read, or parsed
     */
    @PostConstruct
    public void loadAll() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = new ClassPathResource("data/subclasses.json").getInputStream()) {
            List<SubclassesData> list = mapper.readValue(in, new TypeReference<List<SubclassesData>>() {});
            subclasses.clear();
            subclasses.addAll(list);

            byName.clear();
            byParent.clear();

            for (SubclassesData sc : list) {
                byName.put(sc.getName().toLowerCase(Locale.ROOT), sc);
                byParent.put(sc.getParentClass().toLowerCase(Locale.ROOT), sc);
            }
        }
    }

    /**
     * Returns all loaded subclass records.
     *
     * @return an immutable snapshot list of all known subclasses
     */
    public List<SubclassesData> findAll() {
        return List.copyOf(subclasses);
    }

    /**
     * Finds a subclass by name using case-insensitive matching.
     *
     * @param name subclass name to search for; may be {@code null}
     * @return an {@link Optional} containing the matching subclass, or empty if no match exists
     */
    public Optional<SubclassesData> findByName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(byName.get(name.toLowerCase(Locale.ROOT)));
    }

    /**
     * Finds all subclasses that belong to a given parent class.
     * Matching is case-insensitive.
     *
     * @param parentClass parent class name to search for; may be {@code null}
     * @return list of matching subclasses, or an empty list if {@code parentClass} is {@code null}
     *         or no matches exist
     */
    public List<SubclassesData> findByParentClass(String parentClass) {
        if (parentClass == null) return List.of();
        return byParent.get(parentClass.toLowerCase(Locale.ROOT));
    }

    /**
     * Checks whether a subclass exists by name.
     *
     * @param name subclass name to test; may be {@code null}
     * @return {@code true} if a matching subclass exists, otherwise {@code false}
     */
    public boolean exists(String name) {
        return findByName(name).isPresent();
    }

    /**
     * Finds a trait within a subclass by level and optional trait name.
     *
     * @param subclassName subclass to search within; may be {@code null}
     * @param level required trait level
     * @param traitName optional trait name filter (case-insensitive); if {@code null}, first trait
     *                  matching {@code level} is returned
     * @return optional matching trait
     */
    public Optional<SubclassesData.Trait> findTrait(String subclassName, int level, String traitName) {
        return findByName(subclassName).stream()
                .flatMap(sc -> sc.getTraits().stream())
                .filter(t -> t.getLevel() == level
                        && (traitName == null || t.getName().equalsIgnoreCase(traitName)))
                .findFirst();
    }

    /**
     * Minimal multimap implementation used for one-to-many parent-class indexing.
     *
     * @param <K> key type
     * @param <V> value type
     */
    private static class Multimap<K, V> {

        /**
         * Backing storage for key-to-list mapping.
         */
        private final Map<K, List<V>> map = new HashMap<>();

        /**
         * Adds a value for a key.
         *
         * @param key map key
         * @param value value to append
         */
        void put(K key, V value) {
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        /**
         * Returns all values for the given key.
         *
         * @param key map key
         * @return list of values, or an empty list when absent
         */
        List<V> get(K key) {
            return map.getOrDefault(key, List.of());
        }

        /**
         * Clears all stored mappings.
         */
        void clear() {
            map.clear();
        }
    }
}
