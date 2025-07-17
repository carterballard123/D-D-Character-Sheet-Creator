package com.dndcharactercreator.pdfimport.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ArmorRepository {
	 // In-memory map: lowercase name → ArmorData
	 private final Map<String, ArmorData> byName = new HashMap<>();

	/**
	 * At application startup, read armors.json and populate a lookup map keyed by lower-case name.
	 * Called by Spring right after the bean is constructed:
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
	
	/** Find one armor by its exact name (case-insensitive). Returns null if not found. */
	public ArmorData findByName(String name) {
	    if (name == null) return null;
	    return byName.get(name.toLowerCase());
	}
	
	/** Returns a copy of all armors so you can list them in a dropdown. */
	public List<ArmorData> findAll() {
	    return List.copyOf(byName.values());
	}
}
