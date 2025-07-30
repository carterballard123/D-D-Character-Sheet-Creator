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

@Repository
public class LanguagesRepository {
	
	private final List<String> Languages = new ArrayList<>();
	
	// loads languages.json on startup - expects array of strings
	@PostConstruct 
	public void loadAll() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		try ( InputStream in = new ClassPathResource("data/languages.json").getInputStream()) {
			List<String> list = mapper.readValue(in, new TypeReference<List<String>>() {} );
			Languages.clear();
			Languages.addAll(list);
			
		}
	}
	
	// returns total list of languages
	public List<String> findAll(){
		return List.copyOf(Languages);
		
	}
	
	// NOT case sensitive - lookup of a single language
	public Optional<String> findByName(String lang) {
		if( lang == null) return Optional.empty();
		return Languages.stream().filter(l -> l.equalsIgnoreCase(lang)).findFirst();
		
	}
	
	// true if language does exist
	public boolean exits(String lang) {
		return findByName(lang).isPresent();
		
	}
	
}
