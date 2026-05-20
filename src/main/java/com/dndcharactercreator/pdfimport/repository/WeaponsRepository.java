package com.dndcharactercreator.pdfimport.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.dndcharactercreator.pdfimport.model.WeaponData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class WeaponsRepository {
	
	private final Map<String, WeaponData> weapons;
	
	public WeaponsRepository(ObjectMapper objectMapper) throws IOException {
		this.weapons = loadWeapons(objectMapper);
	}

	private Map<String, WeaponData> loadWeapons (ObjectMapper objectMapper) throws IOException {
		ClassPathResource resource = new ClassPathResource("data/weapons.json");
		
		try (InputStream inputStream = resource.getInputStream()) {
			return objectMapper.readValue(inputStream, new TypeReference<Map<String, WeaponData>>() {});
		}
	}
	
	public WeaponData getWeapon(String weaponID) {
		return weapons.get(weaponID);
	}
	
	public Map<String, WeaponData> getAllWeapons() {
		return weapons;
	}
	
}
