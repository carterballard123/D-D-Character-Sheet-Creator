package com.dndcharactercreator.pdfimport.service;

public interface CharacterMathService {
	int computeModifier(int score);
	int computeProficiency(int level);
	int computeMaxHP(String className, int level, int conMod);
	int computeAC(String className, String subClassName, int dexMod, int conMod, int wisMod, int chaMod, String armorName, String shieldName);

}
