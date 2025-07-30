package com.dndcharactercreator.pdfimport.controller;

import com.dndcharactercreator.pdfimport.model.ArmorData;
import com.dndcharactercreator.pdfimport.model.BackgroundData;
import com.dndcharactercreator.pdfimport.model.ClassesData;
import com.dndcharactercreator.pdfimport.model.RacesData;
import com.dndcharactercreator.pdfimport.model.ShieldData;
import com.dndcharactercreator.pdfimport.model.SubclassesData;
import com.dndcharactercreator.pdfimport.repository.RacesRepository;
import com.dndcharactercreator.pdfimport.repository.ArmorRepository;
import com.dndcharactercreator.pdfimport.repository.ShieldRepository;
import com.dndcharactercreator.pdfimport.repository.BackgroundRepository;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;
import com.dndcharactercreator.pdfimport.repository.LanguagesRepository;
import com.dndcharactercreator.pdfimport.repository.SkillsRepository;
import com.dndcharactercreator.pdfimport.repository.SubclassesRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reference")
public class ReferenceController {
	 private final ArmorRepository armorRepo;
	 private final BackgroundRepository backgroundRepo;
	 private final ClassesRepository classesRepo;
	 private final LanguagesRepository languagesRepo;
	 private final RacesRepository racesRepo;
	 private final ShieldRepository shieldRepo;
	 private final SkillsRepository skillsRepo;
	 private final SubclassesRepository subclassesRepo;
	 
	 public ReferenceController(RacesRepository racesRepo, ArmorRepository armorRepo, ShieldRepository shieldRepo, BackgroundRepository backgroundRepo, ClassesRepository classesRepo, LanguagesRepository languagesRepo, SkillsRepository skillsRepo, SubclassesRepository subclassesRepo) {
		 this.armorRepo = armorRepo;
		 this.backgroundRepo = backgroundRepo;
		 this.classesRepo = classesRepo;
		 this.languagesRepo = languagesRepo;
		 this.racesRepo = racesRepo;
		 this.shieldRepo = shieldRepo;
		 this.skillsRepo = skillsRepo;
		 this.subclassesRepo = subclassesRepo;
		 
	 }
	 
	   @GetMapping("/races")
	    public List<RacesData> allRaces() {
	        return racesRepo.findAll();
	    }

	    @GetMapping("/armor")
	    public List<ArmorData> allArmor() {
	        return armorRepo.findAll();
	    }

	    @GetMapping("/shields")
	    public List<ShieldData> allShields() {
	        return shieldRepo.findAll();
	    }

	    @GetMapping("/backgrounds")
	    public List<BackgroundData> allBackgrounds() {
	        return backgroundRepo.findAll();
	    }

	    @GetMapping("/classes")
	    public List<ClassesData> allClasses() {
	        return classesRepo.findAll();
	    }

	    @GetMapping("/languages")
	    public List<String> allLanguages() {
	        return languagesRepo.findAll();
	    }

	    @GetMapping("/skills")
	    public List<String> allSkills() {
	        return skillsRepo.findAll();
	    }

	    @GetMapping("/subclasses")
	    public List<SubclassesData> allSubclasses() {
	        return subclassesRepo.findAll();
	    }
}
