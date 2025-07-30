package com.dndcharactercreator.pdfimport.repository;

import com.dndcharactercreator.pdfimport.model.SubclassesData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;

@Repository
public class SubclassesRepository {
	
	private final List<SubclassesData> subclasses = new ArrayList<>();
	private final Map<String, SubclassesData> byName = new HashMap<>();
	private final Multimap<String, SubclassesData> byParent = new Multimap<>();
	
	@PostConstruct
	public void loadAll() throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		try ( InputStream in = new ClassPathResource("data/subclasses.json").getInputStream()) {
			List<SubclassesData> list = mapper.readValue(in, new TypeReference<List<SubclassesData>>() {} );
			subclasses.clear();
			subclasses.addAll(list);
			
			byName.clear();
			byParent.clear();
			
			for ( SubclassesData sc : list) {
				byName.put(sc.getName().toLowerCase(Locale.ROOT), sc );
				byParent.put(sc.getParentClass().toLowerCase(Locale.ROOT), sc);
			}
		}
	}
	
	public List<SubclassesData> findAll() {
		return List.copyOf(subclasses);
	}
	
	/** Case-insensitive name lookup. */
    public Optional<SubclassesData> findByName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(byName.get(name.toLowerCase(Locale.ROOT)));
    }

    /** Case-insensitive parent class lookup. */
    public List<SubclassesData> findByParentClass(String parentClass) {
        if (parentClass == null) return List.of();
        return byParent.get(parentClass.toLowerCase(Locale.ROOT));
    }

    public boolean exists(String name) {
        return findByName(name).isPresent();
    }

    /**
     * Convenience trait lookup.
     */
    public Optional<SubclassesData.Trait> findTrait(String subclassName, int level, String traitName) {
        return findByName(subclassName).stream().flatMap(sc -> sc.getTraits().stream()).filter(t -> t.getLevel() == level && (traitName == null || t.getName().equalsIgnoreCase(traitName))).findFirst();
    }
    
	 private static class Multimap<K, V> {
	        private final Map<K, List<V>> map = new HashMap<>();
	        void put(K key, V value) {
	            map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
	        }
	        List<V> get(K key) {
	            return map.getOrDefault(key, List.of());
	        }
	        void clear() {
	            map.clear();
	        }
	    }
}
