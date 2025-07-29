package com.dndcharactercreator.pdfimport.service;

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

@Repository
public class ClassesRepository {
	
	// in-memory cache of all classes, keyed by classID
	private final Map<String, ClassesData> classes = new HashMap<>();
	
	/*
	 * Called once *after* Spring creates this bean.
     * Loads every JSON under data/classes/*.json into our map.
	 */
	@PostConstruct
	public void loadAll() throws Exception {
	    ObjectMapper mapper = new ObjectMapper();
	    // Create a Resource for the one JSON file:
	    var resource = new ClassPathResource("data/classes.json");
	    try (InputStream in = resource.getInputStream()) {
	        // read the entire array in one go
	        List<ClassesData> list = mapper.readValue( in, new TypeReference<List<ClassesData>>() {} );
	        classes.clear();
	        for (ClassesData cd : list) {
	            if (cd.getClassID() == null) {
	                throw new IllegalStateException( "Loaded class JSON from \"" + resource.getFilename() + "\" is missing `classID`" );
	            }
	            classes.put(cd.getClassID().toLowerCase(Locale.ROOT), cd);
	        }
	    }
	}

	public Optional<ClassesData> findByID(String classID) {
		if (classID == null) return Optional.empty();
			return Optional.ofNullable(classes.get(classID.toLowerCase(Locale.ROOT)));
		}
	
	public List<ClassesData> findAll() {
		return List.copyOf(classes.values());
	}

}
