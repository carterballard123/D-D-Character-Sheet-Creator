package com.dndcharactercreator.pdfimport.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.dndcharactercreator.pdfimport.model.WeaponPropertyDef;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class WeaponPropertiesRepository {
	
	private final Map<String, WeaponPropertyDef> weaponProperties;
	
	public WeaponPropertiesRepository(ObjectMapper objectMapper) throws IOException {
		this.weaponProperties = loadWeaponProperties(objectMapper);
	}

	private Map<String, WeaponPropertyDef> loadWeaponProperties(ObjectMapper objectMapper) throws IOException {
		ClassPathResource resource = new ClassPathResource("data/weapon_properties.json");
		
		try (InputStream inputStream = resource.getInputStream()) {
			return Collections.unmodifiableMap(
				objectMapper.readValue(inputStream, new TypeReference<Map<String, WeaponPropertyDef>>() {})
			);
		}
	}
	
	public WeaponPropertyDef getWeaponProperty(String propertyName) {
		return weaponProperties.get(propertyName);
	}
	
	public Map<String, WeaponPropertyDef> getAllWeaponProperties() {
		return weaponProperties;
	}
}
