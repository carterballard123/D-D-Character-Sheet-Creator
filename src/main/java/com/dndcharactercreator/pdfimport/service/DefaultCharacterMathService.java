package com.dndcharactercreator.pdfimport.service;

import org.springframework.stereotype.Service;

@Service
public class DefaultCharacterMathService implements CharacterMathService {
	private final ClassesRepository classesRepo;
	private final ArmorRepository armorRepo;
	private final ShieldRepository shieldRepo;
	
	public DefaultCharacterMathService(ClassesRepository classesRepo, ArmorRepository armorRepo, ShieldRepository shieldRepo) {
		this.classesRepo = classesRepo;
		this.armorRepo   = armorRepo;
		this.shieldRepo  = shieldRepo;
		}
	
	@Override
	public int computeModifier(int abilityScore) {
		return (abilityScore - 10) / 2;
	}

	@Override
	public int computeProficiency(int level) {
		return 2 + (level - 1) / 4;
	}

	@Override
	public int computeMaxHP(String characterClass, int level, int conMod) {
		ClassesData cls = classesRepo.findByID(characterClass).orElseThrow(() -> new IllegalArgumentException("Unknown class: " + characterClass));
		
		int hitDie = cls.getHitDie();
		int avgHitDie = (hitDie / 2) + 1;
		int HP = hitDie + conMod;
		
		for( int lvl = 2; lvl <= level; lvl ++) {
			HP += conMod + avgHitDie;
		}
		
		return HP;
	}

	@Override
	public int computeAC(String className, String subClassName, int dexMod, int conMod, int wisMod, int chaMod, String armorName, String shieldName) {
		
		boolean unarmored = armorName == null || armorName.equalsIgnoreCase("Unarmored");
		
		int baseAC;
		int shieldBonus = 0;
		
		if ( unarmored ) {
			if ( className.equalsIgnoreCase("Barbarian") ) {
				baseAC = 10 + conMod + dexMod;
			} else if ( className.equalsIgnoreCase("Monk") ) {
				baseAC = 10 + dexMod + wisMod;
			} else if ( "Draconic Sorcerry".equalsIgnoreCase(subClassName) || "College of Dance".equalsIgnoreCase(subClassName) ) {
				baseAC = 10 + dexMod + chaMod;
			} else {
				baseAC = 10 + dexMod;
			}
		}
		else {
			ArmorData armor = armorRepo.findByName(armorName);
			
			if( armor == null) {
				baseAC = 10 + dexMod;
			} else {
				switch (armor.getArmorType().toLowerCase()) {
					case "heavy armor":
						baseAC = armor.getArmorClass();
						break;
					case "medium armor":
						baseAC = armor.getArmorClass() + Math.min(dexMod, armor.getMaxDexBonus());
						break;
					case "light armor":
						baseAC = armor.getArmorClass() + dexMod;
						break;
					default:
						baseAC = armor.getArmorClass();
				}
			}
		}
		
		if ( shieldName != null && !shieldName.isBlank() ) {
			ShieldData shield = shieldRepo.findByName(shieldName);
			if ( shield != null) {
				shieldBonus = shield.getAcBonus();
			}
		}
		
		return baseAC + shieldBonus;
	}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
}
