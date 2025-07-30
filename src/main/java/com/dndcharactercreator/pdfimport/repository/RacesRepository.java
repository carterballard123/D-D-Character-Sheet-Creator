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

@Repository
public class RacesRepository {
	private final Map<String, RacesData> byName = new HashMap<>();
	
	@PostConstruct
	public void loadAll() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		try(InputStream in = new ClassPathResource("data/races.json").getInputStream()) {
			List<RacesData> list = mapper.readValue(in, new TypeReference<List<RacesData>>() {} );
			for ( RacesData race : list ) {
				byName.put(race.getName().toLowerCase(), race);
				
			}
		}
	}
	
	public RacesData findByName(String name) {
		if ( name == null ) return null;
		return byName.get(name.toLowerCase());
		
	}
	
	public List<RacesData> findAll() {
		return List.copyOf(byName.values());
		
	}
	
	public boolean exists(String name) {
		return findByName(name) != null;
		
	}
	
	public Optional<RacesData> findOptions(String name) {
		return Optional.ofNullable(findByName(name));
		
	}
	
}
