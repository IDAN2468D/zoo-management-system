package com.zoo.management.service;

import com.zoo.management.dto.AnimalStatsResponse;
import com.zoo.management.dto.FeedingRequest;
import com.zoo.management.dto.MedicalRecordRequest;
import com.zoo.management.exception.AnimalNotFoundException;
import com.zoo.management.exception.AnimalValidationException;
import com.zoo.management.model.*;
import com.zoo.management.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ZooService {

    private static final Logger log = LoggerFactory.getLogger(ZooService.class);

    private final AnimalRepository animalRepository;
    private final CageRepository cageRepository;
    private final FeedingRecordRepository feedingRecordRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final InventoryRepository inventoryRepository;


    public ZooService(AnimalRepository animalRepository,
                      CageRepository cageRepository,
                      FeedingRecordRepository feedingRecordRepository,
                      MedicalRecordRepository medicalRecordRepository,
                      EmployeeRepository employeeRepository,
                      VeterinarianRepository veterinarianRepository,
                      InventoryRepository inventoryRepository) {
        this.animalRepository = animalRepository;
        this.cageRepository = cageRepository;
        this.feedingRecordRepository = feedingRecordRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.employeeRepository = employeeRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // ==========================================
    // Animals CRUD & Filtering
    // ==========================================
    @Transactional(readOnly = true)
    public List<Animal> getAllAnimals() {
        return animalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Animal> getAnimalById(Long id) {
        return animalRepository.findById(id);
    }

    public Animal addAnimal(Animal animal) {
        if (animal.getName() == null || animal.getName().trim().isEmpty()) {
            throw new AnimalValidationException("שם החיה הינו שדה חובה");
        }

        if (animal.getSubSpecies() != null && animal.getSpecies() == null) {
            animal.setSpecies(animal.getSubSpecies().getSpecies());
        }

        // ולידציה של תאימות הכלוב לסוג החיה
        if (animal.getCage() != null && animal.getCage().getId() != null) {
            Cage cage = cageRepository.findById(animal.getCage().getId())
                    .orElseThrow(() -> new AnimalValidationException("הכלוב שנבחר (מזהה " + animal.getCage().getId() + ") אינו קיים במערכת"));
            if (animal.getSpecies() != null && cage.getSpecies() != null && !cage.getSpecies().equals(animal.getSpecies())) {
                throw new AnimalValidationException("סוג הכלוב (" + cage.getSpecies().getHebrewName() + ") אינו תואם לסוג החיה (" + animal.getSpecies().getHebrewName() + ")");
            }
            animal.setCage(cage);
        } else {
            animal.setCage(null);
        }

        if (animal.getHealthStatus() == null) {
            animal.setHealthStatus(HealthStatus.HEALTHY);
        }
        if (animal.getConservationStatus() == null) {
            animal.setConservationStatus(ConservationStatus.LEAST_CONCERN);
        }
        if (animal.getDietType() == null) {
            animal.setDietType(DietType.OMNIVORE);
        }
        if (animal.getGender() == null) {
            animal.setGender(Gender.UNKNOWN);
        }

        Animal saved = animalRepository.save(animal);
        log.info("🐾 [ZOO-ACTION: הוספת חיה חדשה] שם: '{}' (ID: {}) | סוג: {} | תת-סוג: {} | משקל: {} ק\"ג | בריאות: {} | כלוב: {}",
                saved.getName(), saved.getId(), saved.getSpecies(), saved.getSubSpecies(),
                saved.getWeightKg(), saved.getHealthStatus(),
                saved.getCage() != null ? "#" + saved.getCage().getId() : "ללא");
        return saved;
    }

    public Optional<Animal> updateAnimal(Long id, Animal updated) {
        return animalRepository.findById(id).map(existing -> {
            if (updated.getName() != null && !updated.getName().trim().isEmpty()) {
                existing.setName(updated.getName().trim());
            }
            if (updated.getSubSpecies() != null) {
                existing.setSubSpecies(updated.getSubSpecies());
                existing.setSpecies(updated.getSubSpecies().getSpecies());
            } else if (updated.getSpecies() != null) {
                existing.setSpecies(updated.getSpecies());
            }

            if (updated.getHealthStatus() != null) existing.setHealthStatus(updated.getHealthStatus());
            if (updated.getGender() != null) existing.setGender(updated.getGender());
            if (updated.getAge() != null) existing.setAge(updated.getAge());
            if (updated.getWeightKg() != null) existing.setWeightKg(updated.getWeightKg());
            if (updated.getDietType() != null) existing.setDietType(updated.getDietType());
            if (updated.getFavoriteFood() != null) existing.setFavoriteFood(updated.getFavoriteFood());
            if (updated.getOriginCountry() != null) existing.setOriginCountry(updated.getOriginCountry());
            if (updated.getConservationStatus() != null)
                existing.setConservationStatus(updated.getConservationStatus());
            if (updated.getMicrochipId() != null) existing.setMicrochipId(updated.getMicrochipId());
            if (updated.getFeedingSchedule() != null) existing.setFeedingSchedule(updated.getFeedingSchedule());
            if (updated.getNotes() != null) existing.setNotes(updated.getNotes());
            if (updated.getImageUrl() != null) existing.setImageUrl(updated.getImageUrl());

            if (updated.getCage() != null) {
                if (updated.getCage().getId() != null) {
                    Cage cage = cageRepository.findById(updated.getCage().getId())
                            .orElseThrow(() -> new AnimalValidationException("הכלוב שנבחר אינו קיים"));
                    if (existing.getSpecies() != null && cage.getSpecies() != null && !cage.getSpecies().equals(existing.getSpecies())) {
                        throw new AnimalValidationException("סוג הכלוב אינו תואם לסוג החיה");
                    }
                    existing.setCage(cage);
                } else {
                    existing.setCage(null);
                }
            }

            Animal saved = animalRepository.save(existing);
            log.info("✏️  [ZOO-ACTION: עדכון פרטי חיה] עודכנו פרטי חיה '{}' (ID: {}) | בריאות: {} | משקל: {} ק\"ג | כלוב: {}",
                    saved.getName(), id, saved.getHealthStatus(), saved.getWeightKg(),
                    saved.getCage() != null ? "#" + saved.getCage().getId() : "ללא");

            return saved;
        });
    }

    public boolean deleteAnimal(Long id) {
        Optional<Animal> animalOpt = animalRepository.findById(id);
        if (animalOpt.isEmpty()) {
            log.warn("⚠️ [ZOO-ACTION: מחיקת חיה] ניסיון מחיקה נכשל - חיה עם מזהה ID: {} לא נמצאה", id);
            return false;
        }
        Animal animal = animalOpt.get();
        String name = animal.getName();
        animalRepository.delete(animal);
        log.warn("🗑️  [ZOO-ACTION: מחיקת חיה] חיה '{}' (ID: {}) הוסרה לצמיתות מהמערכת", name, id);
        return true;
    }

    // ==========================================
    // Animal Operations (האכלה, טיפול רפואי, בידוד, העברה)
    // ==========================================
    public Animal feedAnimal(Long id, FeedingRequest request) {


        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new AnimalNotFoundException(id));

        String food = (request.getFoodItem() != null && !request.getFoodItem().isBlank())
                ? request.getFoodItem()
                : (animal.getFavoriteFood() != null ? animal.getFavoriteFood() : "מזון שגרתי");

        Double amount = request.getAmountKg() != null ? request.getAmountKg() : 1.0;

        inventoryRepository.findByNameIgnoreCase(food).ifPresent(item -> {
            item.deduct(amount);
            inventoryRepository.save(item);
            if (item.isLowStock()) {
                log.warn("⚠️ [מלאי נמוך] פריט מזון '{}' ירד מתחת לסף המינימום! נותרו: {} {}",
                        item.getName(), item.getQuantity(), item.getUnit());
            }
        });

        String fedBy = (request.getFedBy() != null && !request.getFedBy().isBlank()) ? request.getFedBy() : "מטפל תורן";

        FeedingRecord record = new FeedingRecord(LocalDateTime.now(), food, amount, fedBy, request.getNotes());
        animal.addFeedingRecord(record);
        feedingRecordRepository.save(record);
        Animal saved = animalRepository.save(animal);

        log.info("🥩 [ZOO-ACTION: האכלת חיה] בוצעה האכלה עבור '{}' (ID: {}) | מזון: {} | כמות: {} ק\"ג | מאכיל: {} | הערות: {}",
                saved.getName(), id, food, amount, fedBy,
                (request.getNotes() != null && !request.getNotes().isBlank()) ? request.getNotes() : "ללא");

        return saved;
    }

    public Animal recordMedicalCheckup(Long id, MedicalRecordRequest request) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new AnimalNotFoundException(id));

        MedicalRecord record = new MedicalRecord(
                request.getHealthStatus(),
                request.getDiagnosis(),
                request.getTreatment(),
                request.getPerformedBy(),
                request.getNotes()
        );

        animal.addMedicalRecord(record);
        medicalRecordRepository.save(record);
        Animal saved = animalRepository.save(animal);

        log.info("🩺 [ZOO-ACTION: בדיקה רפואית] נרשם עדכון רפואי עבור '{}' (ID: {}) | סטטוס מעודכן: {} | טיפול: {} | אבחון: {} | בוצע ע\"י: {}",
                saved.getName(), id, request.getHealthStatus(),
                request.getTreatment() != null ? request.getTreatment() : "שגרתי",
                request.getDiagnosis() != null ? request.getDiagnosis() : "תקין",
                request.getPerformedBy() != null ? request.getPerformedBy() : "וטרינר");

        return saved;
    }

    public Animal quarantineAnimal(Long id, String reason, String vetName) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new AnimalNotFoundException(id));

        animal.setHealthStatus(HealthStatus.QUARANTINED);
        MedicalRecord record = new MedicalRecord(
                HealthStatus.QUARANTINED,
                "העברה לבידוד מונע / רפואי: " + (reason != null ? reason : "ללא פירוט"),
                "בידוד קפדני ומעקב וטרינרי צמוד",
                vetName != null ? vetName : "וטרינר ראשי",
                reason
        );
        animal.addMedicalRecord(record);
        medicalRecordRepository.save(record);
        Animal saved = animalRepository.save(animal);

        log.warn("🚨 [ZOO-ACTION: בידוד רפואי] חיה '{}' (ID: {}) הועברה לבידוד! | סיבה: {} | וטרינר אחראי: {}",
                saved.getName(), id, reason != null ? reason : "ללא פירוט", vetName != null ? vetName : "וטרינר ראשי");

        return saved;
    }

    public Animal transferAnimalToCage(Long animalId, Long newCageId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new AnimalNotFoundException(animalId));

        if (newCageId == null) {
            animal.setCage(null);
            Animal saved = animalRepository.save(animal);
            log.info("🏠 [ZOO-ACTION: הוצאה מכלוב] חיה '{}' (ID: {}) הוצאה מהכלוב והוגדרה ללא כלוב",
                    saved.getName(), animalId);
            return saved;
        }

        Cage newCage = cageRepository.findById(newCageId)
                .orElseThrow(() -> new AnimalValidationException("כלוב מזהה " + newCageId + " לא נמצא"));

        if (animal.getSpecies() != null && newCage.getSpecies() != null && !animal.getSpecies().equals(newCage.getSpecies())) {
            throw new AnimalValidationException("לא ניתן להעביר חיה ממין " + animal.getSpecies().getHebrewName()
                    + " לכלוב המיועד למין " + newCage.getSpecies().getHebrewName());
        }

        animal.setCage(newCage);
        Animal saved = animalRepository.save(animal);
        log.info("🏠 [ZOO-ACTION: העברת כלוב] חיה '{}' (ID: {}) הועברה בהצלחה לכלוב: #{} ({})",
                saved.getName(), animalId, newCageId, newCage.getSpecies() != null ? newCage.getSpecies().getHebrewName() : "");
        return saved;
    }

    // ==========================================
    // Filter & Search Methods
    // ==========================================
    @Transactional(readOnly = true)
    public List<Animal> getAnimalsBySpecies(Species species) {
        return animalRepository.findBySpecies(species);
    }

    @Transactional(readOnly = true)
    public List<Animal> getAnimalsBySubSpecies(SubSpecies subSpecies) {
        return animalRepository.findBySubSpecies(subSpecies);
    }

    @Transactional(readOnly = true)
    public List<Animal> getAnimalsByHealth(HealthStatus healthStatus) {
        return animalRepository.findByHealthStatus(healthStatus);
    }

    @Transactional(readOnly = true)
    public List<Animal> getAnimalsByDiet(DietType dietType) {
        return animalRepository.findByDietType(dietType);
    }

    @Transactional(readOnly = true)
    public List<Animal> getEndangeredAnimals() {
        return animalRepository.findAll().stream()
                .filter(Animal::isEndangered)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Animal> getAnimalsNeedingFood(int hoursThreshold) {
        return animalRepository.findAll().stream()
                .filter(a -> a.isNeedsFeeding(hoursThreshold))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Animal> searchAnimals(String query) {
        if (query == null || query.isBlank()) {
            return animalRepository.findAll();
        }
        String q = query.trim();
        Set<Animal> results = new LinkedHashSet<>(animalRepository.searchAnimals(q));

        // התאמה גם לערכי Enums ושמות בעברית
        for (Species s : Species.values()) {
            if (s.name().equalsIgnoreCase(q) || s.getHebrewName().contains(q)) {
                results.addAll(animalRepository.findBySpecies(s));
            }
        }
        for (SubSpecies ss : SubSpecies.values()) {
            if (ss.name().equalsIgnoreCase(q) || ss.getHebrewName().contains(q)) {
                results.addAll(animalRepository.findBySubSpecies(ss));
            }
        }
        return new ArrayList<>(results);
    }

    @Transactional(readOnly = true)
    public List<Animal> filterAnimals(Species species, HealthStatus health, DietType diet, Boolean endangered, Long cageId, String search) {
        return animalRepository.findAll().stream()
                .filter(a -> species == null || a.getSpecies() == species)
                .filter(a -> health == null || a.getHealthStatus() == health)
                .filter(a -> diet == null || a.getDietType() == diet)
                .filter(a -> endangered == null || (endangered ? a.isEndangered() : !a.isEndangered()))
                .filter(a -> cageId == null || (a.getCage() != null && a.getCage().getId().equals(cageId)))
                .filter(a -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    boolean nameMatch = a.getName() != null && a.getName().toLowerCase().contains(q);
                    boolean chipMatch = a.getMicrochipId() != null && a.getMicrochipId().toLowerCase().contains(q);
                    boolean subSpeciesMatch = a.getSubSpecies() != null && (a.getSubSpecies().name().toLowerCase().contains(q) || a.getSubSpecies().getHebrewName().contains(q));
                    return nameMatch || chipMatch || subSpeciesMatch;
                })
                .collect(Collectors.toList());
    }

    // ==========================================
    // Statistics & Dashboard Metrics
    // ==========================================
    @Transactional(readOnly = true)
    public AnimalStatsResponse getAnimalStats() {
        List<Animal> animals = animalRepository.findAll();
        AnimalStatsResponse stats = new AnimalStatsResponse();
        stats.setTotalAnimals(animals.size());

        long healthy = animals.stream().filter(a -> a.getHealthStatus() == HealthStatus.HEALTHY).count();
        long sickOrInjured = animals.stream().filter(a -> a.getHealthStatus() == HealthStatus.SICK || a.getHealthStatus() == HealthStatus.INJURED).count();
        long quarantined = animals.stream().filter(a -> a.getHealthStatus() == HealthStatus.QUARANTINED).count();
        long observation = animals.stream().filter(a -> a.getHealthStatus() == HealthStatus.UNDER_OBSERVATION).count();
        long endangered = animals.stream().filter(Animal::isEndangered).count();
        long hungry = animals.stream().filter(a -> a.isNeedsFeeding(8)).count();

        stats.setHealthyCount(healthy);
        stats.setSickOrInjuredCount(sickOrInjured);
        stats.setQuarantinedCount(quarantined);
        stats.setObservationCount(observation);
        stats.setEndangeredCount(endangered);
        stats.setHungryCount(hungry);

        double avgAge = animals.stream().filter(a -> a.getAge() != null).mapToInt(Animal::getAge).average().orElse(0.0);
        double avgWeight = animals.stream().filter(a -> a.getWeightKg() != null).mapToDouble(Animal::getWeightKg).average().orElse(0.0);
        stats.setAverageAge(Math.round(avgAge * 10.0) / 10.0);
        stats.setAverageWeightKg(Math.round(avgWeight * 10.0) / 10.0);

        // Distributions
        Map<String, Long> speciesDist = new LinkedHashMap<>();
        for (Species s : Species.values()) {
            long count = animals.stream().filter(a -> a.getSpecies() == s).count();
            if (count > 0) speciesDist.put(s.getHebrewName(), count);
        }
        stats.setSpeciesDistribution(speciesDist);

        Map<String, Long> healthDist = new LinkedHashMap<>();
        for (HealthStatus hs : HealthStatus.values()) {
            long count = animals.stream().filter(a -> a.getHealthStatus() == hs).count();
            if (count > 0) healthDist.put(hs.getHebrewName(), count);
        }
        stats.setHealthDistribution(healthDist);

        Map<String, Long> dietDist = new LinkedHashMap<>();
        for (DietType dt : DietType.values()) {
            long count = animals.stream().filter(a -> a.getDietType() == dt).count();
            if (count > 0) dietDist.put(dt.getHebrewName(), count);
        }
        stats.setDietDistribution(dietDist);

        Map<String, Long> consDist = new LinkedHashMap<>();
        for (ConservationStatus cs : ConservationStatus.values()) {
            long count = animals.stream().filter(a -> a.getConservationStatus() == cs).count();
            if (count > 0) consDist.put(cs.getHebrewName(), count);
        }
        stats.setConservationDistribution(consDist);

        return stats;
    }

    // ==========================================
    // Cages CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<Cage> getAllCages() {
        return cageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cage> getCageById(Long id) {
        return cageRepository.findById(id);
    }

    public Cage addCage(Cage cage) {
        Cage saved = cageRepository.save(cage);
        log.info("🏗️  [ZOO-ACTION: הוספת כלוב] נוצר כלוב חדש: #{} עבור מין: {}", saved.getId(), saved.getSpecies());
        return saved;
    }

    public Optional<Cage> updateCage(Long id, Cage updated) {
        return cageRepository.findById(id).map(existing -> {
            if (updated.getSpecies() != null) existing.setSpecies(updated.getSpecies());
            Cage saved = cageRepository.save(existing);
            log.info("🔧 [ZOO-ACTION: עדכון כלוב] עודכן כלוב: #{} | מין מיועד: {}", id, saved.getSpecies());
            return saved;
        });
    }

    public boolean deleteCage(Long id) {
        Optional<Cage> cageOpt = cageRepository.findById(id);
        if (cageOpt.isEmpty()) {
            return false;
        }
        Cage cage = cageOpt.get();

        // ניתוק חיות מהכלוב הנמחק
        List<Animal> animalsInCage = animalRepository.findByCageId(id);
        for (Animal animal : animalsInCage) {
            animal.setCage(null);
            animalRepository.save(animal);
        }

        // ניתוק הכלוב מעובדים
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            if (emp.getAssignedCages().remove(cage)) {
                employeeRepository.save(emp);
            }
        }

        cageRepository.delete(cage);
        log.warn("🗑️  [ZOO-ACTION: מחיקת כלוב] נמחק כלוב: #{} ושוחררו החיות שהיו בו", id);
        return true;
    }

    // ==========================================
    // Employees CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee addEmployee(Employee employee) {
        Employee saved = employeeRepository.save(employee);
        log.info("👤 [ZOO-ACTION: הוספת עובד] עובד חדש: '{}' (ID: {}) בתפקיד: {}", saved.getName(), saved.getId(), saved.getRole());
        return saved;
    }

    public Optional<Employee> updateEmployee(Long id, Employee updated) {
        return employeeRepository.findById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getRole() != null) existing.setRole(updated.getRole());
            if (updated.getAssignedCages() != null) existing.setAssignedCages(updated.getAssignedCages());
            Employee saved = employeeRepository.save(existing);
            log.info("👤 [ZOO-ACTION: עדכון עובד] עודכנו פרטי עובד '{}' (ID: {})", saved.getName(), id);
            return saved;
        });
    }

    public boolean deleteEmployee(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            log.warn("🗑️  [ZOO-ACTION: מחיקת עובד] עובד ID: {} הוסר מהמערכת", id);
            return true;
        }
        return false;
    }

    public Optional<Employee> assignCageToEmployee(Long employeeId, Long cageId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        Optional<Cage> cageOpt = cageRepository.findById(cageId);
        if (employeeOpt.isPresent() && cageOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.assignCage(cageOpt.get());
            employeeRepository.save(employee);
            log.info("🔑 [ZOO-ACTION: שיוך כלוב לעובד] עובד '{}' שויך לכלוב: #{}", employee.getName(), cageId);
            return Optional.of(employee);
        }
        return Optional.empty();
    }

    public Optional<Employee> removeCageFromEmployee(Long employeeId, Long cageId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        Optional<Cage> cageOpt = cageRepository.findById(cageId);
        if (employeeOpt.isPresent() && cageOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.removeCage(cageOpt.get());
            employeeRepository.save(employee);
            log.info("🔑 [ZOO-ACTION: ביטול שיוך כלוב] לעובד '{}' בוטל שיוך לכלוב: #{}", employee.getName(), cageId);
            return Optional.of(employee);
        }
        return Optional.empty();
    }

    // ==========================================
    // Veterinarians CRUD
    // ==========================================
    @Transactional(readOnly = true)
    public List<Veterinarian> getAllVeterinarians() {
        return veterinarianRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Veterinarian> getVeterinarianById(Long id) {
        return veterinarianRepository.findById(id);
    }

    public Veterinarian addVeterinarian(Veterinarian vet) {
        Veterinarian saved = veterinarianRepository.save(vet);
        log.info("👨‍⚕️ [ZOO-ACTION: הוספת וטרינר] נוסף וטרינר חדש: '{}' (ID: {}) בהתמחות: {}", saved.getName(), saved.getId(), saved.getSpecialization());
        return saved;
    }

    public Optional<Veterinarian> updateVeterinarian(Long id, Veterinarian updated) {
        return veterinarianRepository.findById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getSpecialization() != null) existing.setSpecialization(updated.getSpecialization());
            if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
            if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
            Veterinarian saved = veterinarianRepository.save(existing);
            log.info("👨‍⚕️ [ZOO-ACTION: עדכון וטרינר] עודכנו פרטי וטרינר: '{}' (ID: {})", saved.getName(), id);
            return saved;
        });
    }

    public boolean deleteVeterinarian(Long id) {
        if (veterinarianRepository.existsById(id)) {
            veterinarianRepository.deleteById(id);
            log.warn("🗑️  [ZOO-ACTION: מחיקת וטרינר] וטרינר ID: {} הוסר מהמערכת", id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public InventoryItem addOrUpdateInventory(InventoryItem item) {
        return inventoryRepository.save(item);
    }

    public InventoryItem restockItem(Long id, Double amount) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new AnimalValidationException("פריט מלאי לא נמצא"));
        item.addStock(amount);
        return inventoryRepository.save(item);
    }

    // עריכת פריט מלאי
    public Optional<InventoryItem> updateInventoryItem(Long id, InventoryItem updated) {
        return inventoryRepository.findById(id).map(existing -> {
            if (updated.getName() != null && !updated.getName().isBlank()) {
                existing.setName(updated.getName().trim());
            }
            if (updated.getQuantity() != null) {
                existing.setQuantity(updated.getQuantity());
            }
            if (updated.getMinThreshold() != null) {
                existing.setMinThreshold(updated.getMinThreshold());
            }
            if (updated.getUnit() != null && !updated.getUnit().isBlank()) {
                existing.setUnit(updated.getUnit().trim());
            }
            log.info("📦 [ZOO-ACTION: עדכון מלאי] עודכן פריט '{}' (ID: {})", existing.getName(), id);
            return inventoryRepository.save(existing);
        });
    }

    // מחיקת פריט מלאי
    public boolean deleteInventoryItem(Long id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
            log.warn("🗑️ [ZOO-ACTION: מחיקת מלאי] נמחק פריט מלאי מזהה: {}", id);
            return true;
        }
        return false;
    }
}
