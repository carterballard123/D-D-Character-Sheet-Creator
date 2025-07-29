package com.dndcharactercreator.pdfimport.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;

@Repository
public class ShieldRepository {
	private final Map<String, ShieldData> shields = new HashMap<>();
	
	@PostConstruct
	public void loadAll() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		try ( InputStream in = new ClassPathResource("data/shields.json").getInputStream() ) {
			List<ShieldData> list = mapper.readValue(in, new TypeReference<List<ShieldData>>() {});
			shields.clear();
			for ( ShieldData s : list) {
				shields.put(s.getName().toLowerCase(Locale.ROOT), s);
			}
		}
	}
	
	public ShieldData findByName(String name) {
		if ( name == null) return null;
		return shields.get(name.toLowerCase(Locale.ROOT));
	}
	
	public List<String> findAllNames() {
		return new ArrayList<>(shields.keySet());
	}
	
}
