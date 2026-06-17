package com.dndcharactercreator.pdfimport.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dndcharactercreator.pdfimport.model.ArmorData;
import com.dndcharactercreator.pdfimport.model.BackgroundData;
import com.dndcharactercreator.pdfimport.model.ClassesData;
import com.dndcharactercreator.pdfimport.model.RacesData;
import com.dndcharactercreator.pdfimport.model.ShieldData;
import com.dndcharactercreator.pdfimport.model.SubclassesData;
import com.dndcharactercreator.pdfimport.model.WeaponData;
import com.dndcharactercreator.pdfimport.model.WeaponMasteryDef;
import com.dndcharactercreator.pdfimport.model.WeaponPropertyDef;
import com.dndcharactercreator.pdfimport.repository.ArmorRepository;
import com.dndcharactercreator.pdfimport.repository.BackgroundRepository;
import com.dndcharactercreator.pdfimport.repository.ClassesRepository;
import com.dndcharactercreator.pdfimport.repository.LanguagesRepository;
import com.dndcharactercreator.pdfimport.repository.RacesRepository;
import com.dndcharactercreator.pdfimport.repository.ShieldRepository;
import com.dndcharactercreator.pdfimport.repository.SkillsRepository;
import com.dndcharactercreator.pdfimport.repository.SubclassesRepository;
import com.dndcharactercreator.pdfimport.repository.WeaponMasteriesRepository;
import com.dndcharactercreator.pdfimport.repository.WeaponPropertiesRepository;
import com.dndcharactercreator.pdfimport.repository.WeaponsRepository;

/**
 * REST controller that exposes reference (compendium) data used by the character builder UI.
 *
 * <p>This controller provides read-only endpoints that return lists of races, classes, backgrounds,
 * equipment, and other lookup data. The frontend uses these endpoints to populate selection UI
 * without hardcoding the data in JavaScript.
 *
 * <p>Data is sourced from repository classes which typically load and parse JSON files stored in
 * {@code src/main/resources/data}.
 *
 * @author Carter Ballard
 */
@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    /** Repository for armor definitions. */
    private final ArmorRepository armorRepo;

    /** Repository for background definitions. */
    private final BackgroundRepository backgroundRepo;

    /** Repository for class definitions. */
    private final ClassesRepository classesRepo;

    /** Repository for language lookup values. */
    private final LanguagesRepository languagesRepo;

    /** Repository for race definitions. */
    private final RacesRepository racesRepo;

    /** Repository for shield definitions. */
    private final ShieldRepository shieldRepo;

    /** Repository for skill lookup values. */
    private final SkillsRepository skillsRepo;

    /** Repository for subclass definitions. */
    private final SubclassesRepository subclassesRepo;

    /** Repository for weapon definitions. */
    private final WeaponsRepository weaponsRepo;

    /** Repository for weapon property definitions. */
    private final WeaponPropertiesRepository weaponPropertiesRepo;

    /** Repository for weapon mastery definitions. */
    private final WeaponMasteriesRepository weaponMasteriesRepo;

    /**
     * Creates a {@code ReferenceController}.
     *
     * @param racesRepo repository for races
     * @param armorRepo repository for armor
     * @param shieldRepo repository for shields
     * @param backgroundRepo repository for backgrounds
     * @param classesRepo repository for classes
     * @param languagesRepo repository for languages
     * @param skillsRepo repository for skills
     * @param subclassesRepo repository for subclasses
     */
    public ReferenceController(
            RacesRepository racesRepo,
            ArmorRepository armorRepo,
            ShieldRepository shieldRepo,
            BackgroundRepository backgroundRepo,
            ClassesRepository classesRepo,
            LanguagesRepository languagesRepo,
            SkillsRepository skillsRepo,
            SubclassesRepository subclassesRepo,
            WeaponsRepository weaponsRepo,
            WeaponPropertiesRepository weaponPropertiesRepo,
            WeaponMasteriesRepository weaponMasteriesRepo) {

        this.armorRepo = armorRepo;
        this.backgroundRepo = backgroundRepo;
        this.classesRepo = classesRepo;
        this.languagesRepo = languagesRepo;
        this.racesRepo = racesRepo;
        this.shieldRepo = shieldRepo;
        this.skillsRepo = skillsRepo;
        this.subclassesRepo = subclassesRepo;
        this.weaponsRepo = weaponsRepo;
        this.weaponPropertiesRepo = weaponPropertiesRepo;
        this.weaponMasteriesRepo = weaponMasteriesRepo;
    }

    /**
     * Returns all available races.
     *
     * @return list of race definitions
     */
    @GetMapping("/races")
    public List<RacesData> allRaces() {
        return racesRepo.findAll();
    }

    /**
     * Returns all available armor options.
     *
     * @return list of armor definitions
     */
    @GetMapping("/armor")
    public List<ArmorData> allArmor() {
        return armorRepo.findAll();
    }

    /**
     * Returns all available shield options.
     *
     * @return list of shield definitions
     */
    @GetMapping("/shields")
    public List<ShieldData> allShields() {
        return shieldRepo.findAll();
    }

    /**
     * Returns all available backgrounds.
     *
     * @return list of background definitions
     */
    @GetMapping("/backgrounds")
    public List<BackgroundData> allBackgrounds() {
        return backgroundRepo.findAll();
    }

    /**
     * Returns all available classes.
     *
     * @return list of class definitions
     */
    @GetMapping("/classes")
    public List<ClassesData> allClasses() {
        return classesRepo.findAll();
    }

    /**
     * Returns all language options available to the builder.
     *
     * @return list of language names
     */
    @GetMapping("/languages")
    public List<String> allLanguages() {
        return languagesRepo.findAll();
    }

    /**
     * Returns all skill names available to the builder.
     *
     * @return list of skill names
     */
    @GetMapping("/skills")
    public List<String> allSkills() {
        return skillsRepo.findAll();
    }

    /**
     * Returns all available subclasses.
     *
     * @return list of subclass definitions
     */
    @GetMapping("/subclasses")
    public List<SubclassesData> allSubclasses() {
        return subclassesRepo.findAll();
    }

    /**
     * Returns all available weapons keyed by weapon ID.
     *
     * @return map of weapon IDs to weapon definitions
     */
    @GetMapping("/weapons")
    public Map<String, WeaponData> allWeapons() {
        return weaponsRepo.getAllWeapons();
    }

    /**
     * Returns all weapon property definitions keyed by property ID.
     *
     * @return map of weapon property IDs to definitions
     */
    @GetMapping("/weapon-properties")
    public Map<String, WeaponPropertyDef> allWeaponProperties() {
        return weaponPropertiesRepo.getAllWeaponProperties();
    }

    /**
     * Returns all weapon mastery definitions keyed by mastery ID.
     *
     * @return map of weapon mastery IDs to definitions
     */
    @GetMapping("/weapon-masteries")
    public Map<String, WeaponMasteryDef> allWeaponMasteries() {
        return weaponMasteriesRepo.getAllWeaponMasteries();
    }
}
