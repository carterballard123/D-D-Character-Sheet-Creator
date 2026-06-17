package com.dndcharactercreator.pdfimport.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.dndcharactercreator.pdfimport.model.WeaponMasteryDef;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class WeaponMasteriesRepository {
	private final Map<String, WeaponMasteryDef> weaponMasteries;
	
	public WeaponMasteriesRepository(ObjectMapper objectMapper) throws IOException {
		this.weaponMasteries = loadWeaponMasteries(objectMapper);
	}
	
	private Map<String, WeaponMasteryDef> loadWeaponMasteries(ObjectMapper objectMapper) throws IOException {
		ClassPathResource resource = new ClassPathResource("data/weapon_masteries.json");

		try (InputStream inputStream = resource.getInputStream()) {
			return Collections.unmodifiableMap(
				objectMapper.readValue(inputStream, new TypeReference<Map<String, WeaponMasteryDef>>() {})
			);
		}
	}
	
	public WeaponMasteryDef getWeaponMastery(String masteryName) {
		return weaponMasteries.get(masteryName);
	}
	
	public Map<String, WeaponMasteryDef> getAllWeaponMasteries() {
		return weaponMasteries;
	}
}
