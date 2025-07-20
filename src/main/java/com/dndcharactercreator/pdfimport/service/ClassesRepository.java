package com.dndcharactercreator.pdfimport.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;        // ①
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ClassesRepository {
	
	// in-memory cache of all classes, keyed by classID
	private final Map<String, ClassDefinition> classes = new HashMap<>();
	
	/*
	 * Called once *after* Spring creates this bean.
     * Loads every JSON under data/classes/*.json into our map.
	 */
	@PostConstruct
	public void loadAll() throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		
		// Finds every JSON file under the classpath:data/classes/
		var resolver = new PathMatchingResourcePatternResolver();
		var resources = resolver.getResources("classpath:data/classes/*.json");
		
		// For each file, deserialize into ClassDefinition and put it in the map
		for (var res: resources) {
			
			try (InputStream in = res.getInputStream()) {
				
				ClassDefinition cd = mapper.readValue(in, new TypeReference<ClassDefinition>() {});
				classes.put(cd.getClassID(), cd);
			}
		}
	}

	public ClassDefinition findByID(String classID) {
		
		if( classID == null) return null;
		return classes.get(classID);
	}
	
	public List<ClassDefinition> findAll() {
		
		return List.copyOf(classes.values());
	}

}
