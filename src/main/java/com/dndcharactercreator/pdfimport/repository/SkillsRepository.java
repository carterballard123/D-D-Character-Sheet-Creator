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
public class SkillsRepository {
	
	private final List<String> skills = new ArrayList<>();
	
	@PostConstruct
	public void loadAll() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		try ( InputStream in = new ClassPathResource("data/skills.json").getInputStream() ) {
			List<String> list = mapper.readValue(in, new TypeReference<List<String>>() {});
			skills.clear();
			skills.addAll(list);
		}
	}
	
	// returns all skills
	public List<String> findAll(){
		return List.copyOf(skills);
	}
	
	// returns a skill
	public Optional<String> findByName(String skill) {
		if ( skill == null ) return Optional.empty();
		return skills.stream().filter(l -> l.equalsIgnoreCase(skill)).findFirst();
	}
	
	// returns weather or not that skill exists
	public boolean exists(String skill) {
		return findByName(skill).isPresent();
	}
}
