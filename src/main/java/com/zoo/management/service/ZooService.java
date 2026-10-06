package com.zoo.management.service;

import com.zoo.management.dto.AnimalStatsResponse;
import com.zoo.management.dto.FeedingRequest;
import com.zoo.management.dto.MedicalRecordRequest;
import com.zoo.management.exception.AnimalNotFoundException;
import com.zoo.management.exception.AnimalValidationException;
import com.zoo.management.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ZooService {

    private static final Logger log = LoggerFactory.getLogger(ZooService.class);

    private final List<Cage> cages = new ArrayList<>();
    private final List<Animal> animals = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    private final List<Veterinarian> veterinarians = new ArrayList<>();

    private final AtomicLong animalIdCounter = new AtomicLong(1);
    private final AtomicLong cageIdCounter = new AtomicLong(1);
    private final AtomicLong employeeIdCounter = new AtomicLong(1);
    private final AtomicLong vetIdCounter = new AtomicLong(1);
    private final AtomicLong medicalIdCounter = new AtomicLong(1);

    public ZooService() {
        initSampleData();
    }

    private void initSampleData() {
        // 1. הגדרת כלובים לכל סוגי המינים
        Cage felineCage = new Cage(cageIdCounter.getAndIncrement(), Species.FELINE);
        Cage primateCage = new Cage(cageIdCounter.getAndIncrement(), Species.PRIMATE);
        Cage birdCage = new Cage(cageIdCounter.getAndIncrement(), Species.BIRD);
        Cage aquaticCage = new Cage(cageIdCounter.getAndIncrement(), Species.AQUATIC);
        Cage mammalCage = new Cage(cageIdCounter.getAndIncrement(), Species.MAMMAL);
        Cage reptileCage = new Cage(cageIdCounter.getAndIncrement(), Species.REPTILE);
        Cage amphibianCage = new Cage(cageIdCounter.getAndIncrement(), Species.AMPHIBIAN);

        cages.add(felineCage);
        cages.add(primateCage);
        cages.add(birdCage);
        cages.add(aquaticCage);
        cages.add(mammalCage);
        cages.add(reptileCage);
        cages.add(amphibianCage);

        // 2. הגדרת חיות עשירות עם מאפיינים מקיפים
        createSampleAnimal("Simba", Species.FELINE, SubSpecies.LION, HealthStatus.HEALTHY, felineCage,
                Gender.MALE, 6, 190.5, DietType.CARNIVORE, "בשר בקר טרי", "קניה",
                ConservationStatus.VULNERABLE, "CHIP-FEL-101", "פעמיים ביום ב-09:00 וב-17:00",
                "מנהיג הלהקה, בעל רעמה מרשימה ובריאות מעולה.", 3);

        createSampleAnimal("Nala", Species.FELINE, SubSpecies.LION, HealthStatus.HEALTHY, felineCage,
                Gender.FEMALE, 5, 130.0, DietType.CARNIVORE, "בשר עוף ובקר", "טנזניה",
                ConservationStatus.VULNERABLE, "CHIP-FEL-102", "פעמיים ביום ב-09:00 וב-17:00",
                "לביאה פעילה וחברותית במיוחד.", 5);

        createSampleAnimal("Shere Khan", Species.FELINE, SubSpecies.TIGER, HealthStatus.UNDER_OBSERVATION, felineCage,
                Gender.MALE, 8, 220.0, DietType.CARNIVORE, "בשר טרי", "הודו",
                ConservationStatus.ENDANGERED, "CHIP-FEL-103", "פעם ביום בשעה 14:00",
                "נמצא במעקב שגרתי לאחר בדיקת שיניים תקופתית.", 14);

        createSampleAnimal("Bagheera", Species.FELINE, SubSpecies.PANTHER, HealthStatus.HEALTHY, felineCage,
                Gender.MALE, 4, 65.0, DietType.CARNIVORE, "עוף ודגים", "קונגו",
                ConservationStatus.NEAR_THREATENED, "CHIP-FEL-104", "פעם ביום ב-18:00",
                "פנתר שחור זריז ומיומן.", 2);

        createSampleAnimal("George", Species.PRIMATE, SubSpecies.CHIMPANZEE, HealthStatus.HEALTHY, primateCage,
                Gender.MALE, 10, 52.0, DietType.OMNIVORE, "בננות, תפוחים ואגוזים", "אוגנדה",
                ConservationStatus.ENDANGERED, "CHIP-PRI-201", "שלוש פעמים ביום",
                "מאוד אינטליגנטי, אוהב משחקי חידה והעשרה.", 1);

        createSampleAnimal("Koko", Species.PRIMATE, SubSpecies.GORILLA, HealthStatus.HEALTHY, primateCage,
                Gender.FEMALE, 14, 140.0, DietType.HERBIVORE, "ענפי במבוק ופירות יער", "רואנדה",
                ConservationStatus.CRITICALLY_ENDANGERED, "CHIP-PRI-202", "ארבע פעמים ביום",
                "גורילת שפלה עדינה, מגיבה לסימני ידיים.", 4);

        createSampleAnimal("King Julien", Species.PRIMATE, SubSpecies.LEMUR, HealthStatus.HEALTHY, primateCage,
                Gender.MALE, 5, 2.5, DietType.HERBIVORE, "פירות מתוקים ופרחים", "מדגסקר",
                ConservationStatus.ENDANGERED, "CHIP-PRI-203", "פעמיים ביום",
                "למור זנב-טבעת שובב ואנרגטי.", 6);

        createSampleAnimal("Majestic", Species.BIRD, SubSpecies.EAGLE, HealthStatus.HEALTHY, birdCage,
                Gender.MALE, 4, 4.5, DietType.CARNIVORE, "דגים ומכרסמים", "ארה\"ב",
                ConservationStatus.LEAST_CONCERN, "CHIP-BRD-301", "פעם ביום בצהריים",
                "עיטם לבן-ראש בעל מוטת כנפיים של 2.2 מטר.", 8);

        createSampleAnimal("Rio", Species.BIRD, SubSpecies.PARROT, HealthStatus.HEALTHY, birdCage,
                Gender.MALE, 3, 1.2, DietType.HERBIVORE, "זרעי חמנייה ופפאיה", "ברזיל",
                ConservationStatus.VULNERABLE, "CHIP-BRD-302", "שלוש פעמים ביום",
                "תוכי ארה כחולה-צהובה דברן וידידותי.", 2);

        createSampleAnimal("Pinky", Species.BIRD, SubSpecies.FLAMINGO, HealthStatus.HEALTHY, birdCage,
                Gender.FEMALE, 5, 3.0, DietType.OMNIVORE, "סרטנים קטנים ואצות עשירות בבטא-קרוטן", "בהאמה",
                ConservationStatus.LEAST_CONCERN, "CHIP-BRD-303", "הזנה חופשית בבריכה",
                "נוצות ורודות בוהקות, עומדת ביציבות על רגל אחת.", 7);

        createSampleAnimal("Pingu", Species.BIRD, SubSpecies.PENGUIN, HealthStatus.HEALTHY, birdCage,
                Gender.MALE, 2, 16.0, DietType.PISCIVORE, "דגי הרינג וקריל", "אנטארקטיקה",
                ConservationStatus.NEAR_THREATENED, "CHIP-BRD-304", "פעמיים ביום במים",
                "פינגווין קיסרי צעיר ושחיין מצטיין.", 16);

        createSampleAnimal("Flipper", Species.AQUATIC, SubSpecies.DOLPHIN, HealthStatus.HEALTHY, aquaticCage,
                Gender.MALE, 9, 175.0, DietType.PISCIVORE, "מקרלים ודיונונים טריים", "האוקיינוס השקט",
                ConservationStatus.LEAST_CONCERN, "CHIP-AQU-401", "שלוש פעמים ביום באימונים",
                "דולפין רונן פעלתן, מתקשר היטב עם המטפלים.", 3);

        createSampleAnimal("Crush", Species.AQUATIC, SubSpecies.SEA_TURTLE, HealthStatus.HEALTHY, aquaticCage,
                Gender.MALE, 45, 120.0, DietType.HERBIVORE, "עשבי ים ומדוזות", "אוסטרליה",
                ConservationStatus.ENDANGERED, "CHIP-AQU-402", "פעם ביום בבוקר",
                "צב ים ירוק ותיק ושליו ביותר.", 20);

        createSampleAnimal("Dumbo", Species.MAMMAL, SubSpecies.ELEPHANT, HealthStatus.HEALTHY, mammalCage,
                Gender.MALE, 12, 4100.0, DietType.HERBIVORE, "חציר, ענפים, תפוחים ואבטיחים", "קניה",
                ConservationStatus.ENDANGERED, "CHIP-MAM-501", "האכלה מתמשכת לאורך היום",
                "פיל סוואנה אפריקאי מרשים, אוהב אמבטיות בוץ.", 4);

        createSampleAnimal("Melman", Species.MAMMAL, SubSpecies.GIRAFFE, HealthStatus.HEALTHY, mammalCage,
                Gender.MALE, 8, 1150.0, DietType.HERBIVORE, "עלי שיטה גבוהים", "טנזניה",
                ConservationStatus.VULNERABLE, "CHIP-MAM-502", "האכלה ממתקנים מוגבהים",
                "ג'ירפה מרושתת גבוהה ורגועה.", 5);

        createSampleAnimal("Marty", Species.MAMMAL, SubSpecies.ZEBRA, HealthStatus.HEALTHY, mammalCage,
                Gender.MALE, 6, 340.0, DietType.HERBIVORE, "עשב טרי וחציר שיבולת", "דרום אפריקה",
                ConservationStatus.NEAR_THREATENED, "CHIP-MAM-503", "פעמיים ביום",
                "זברה מצויה בעלת פסים חדים וברורים.", 9);

        createSampleAnimal("Po", Species.MAMMAL, SubSpecies.PANDA, HealthStatus.HEALTHY, mammalCage,
                Gender.MALE, 7, 110.0, DietType.HERBIVORE, "גבעולי במבוק ירוקים וגזר", "סין",
                ConservationStatus.VULNERABLE, "CHIP-MAM-504", "שלוש פעמים ביום",
                "פנדה ענקית חובבת שינה וכרסום במבוק.", 2);

        createSampleAnimal("Kaa", Species.REPTILE, SubSpecies.PYTHON, HealthStatus.HEALTHY, reptileCage,
                Gender.FEMALE, 6, 45.0, DietType.CARNIVORE, "מכרסמים מפוקחים", "הודו",
                ConservationStatus.LEAST_CONCERN, "CHIP-REP-601", "פעם בעשרה ימים",
                "פיתון הודי באורך 4 מטרים, רגוע ואיטי.", 72);

        createSampleAnimal("Pascal", Species.REPTILE, SubSpecies.CHAMELEON, HealthStatus.HEALTHY, reptileCage,
                Gender.MALE, 2, 0.2, DietType.INSECTIVORE, "צרצרים חיים וזבובי פירות", "מדגסקר",
                ConservationStatus.LEAST_CONCERN, "CHIP-REP-602", "מדי יום בבוקר",
                "זיקית תימנית בעלת יכולת החלפת צבעים מדהימה.", 10);

        createSampleAnimal("Kermit", Species.AMPHIBIAN, SubSpecies.FROG, HealthStatus.HEALTHY, amphibianCage,
                Gender.MALE, 1, 0.08, DietType.INSECTIVORE, "חרקי מים וזחלים", "קוסטה ריקה",
                ConservationStatus.LEAST_CONCERN, "CHIP-AMP-701", "פעמיים ביום",
                "אילנית אדומת-עיניים זוהרת ומיוחדת.", 6);

        // 3. הגדרת עובדים
        Employee manager = new Employee(employeeIdCounter.getAndIncrement(), "David Cohen", Role.MANAGER);
        Employee keeper = new Employee(employeeIdCounter.getAndIncrement(), "Maya Levi", Role.EMPLOYEE);
        keeper.assignCage(primateCage);
        keeper.assignCage(felineCage);

        employees.add(manager);
        employees.add(keeper);

        // 4. הגדרת וטרינרים
        veterinarians.add(new Veterinarian(vetIdCounter.getAndIncrement(), "Dr. Sarah Miller", Species.AQUATIC, "sarah@zoo.com", "050-1234567"));
        veterinarians.add(new Veterinarian(vetIdCounter.getAndIncrement(), "Dr. Ron Katz", Species.FELINE, "ron@zoo.com", "052-7654321"));
    }

    private void createSampleAnimal(String name, Species species, SubSpecies subSpecies, HealthStatus healthStatus,
                                     Cage cage, Gender gender, Integer age, Double weightKg, DietType dietType,
                                     String favoriteFood, String originCountry, ConservationStatus conservationStatus,
                                     String microchipId, String feedingSchedule, String notes, int hoursAgoFed) {
        Animal animal = new Animal();
        animal.setId(animalIdCounter.getAndIncrement());
        animal.setName(name);
        animal.setSpecies(species);
        animal.setSubSpecies(subSpecies);
        animal.setHealthStatus(healthStatus);
        animal.setCage(cage);
        animal.setGender(gender);
        animal.setAge(age);
        animal.setWeightKg(weightKg);
        animal.setDietType(dietType);
        animal.setFavoriteFood(favoriteFood);
        animal.setOriginCountry(originCountry);
        animal.setConservationStatus(conservationStatus);
        animal.setMicrochipId(microchipId);
        animal.setFeedingSchedule(feedingSchedule);
        animal.setNotes(notes);

        LocalDateTime fedTime = LocalDateTime.now().minusHours(hoursAgoFed);
        animal.setLastFedTime(fedTime);

        // Feeding history sample
        animal.addFeedingRecord(new FeedingRecord(fedTime, favoriteFood, Math.max(0.5, weightKg * 0.03), "מאיה לוי", "האכלה שגרתית מוצלחת"));

        // Medical history sample
        animal.addMedicalRecord(new MedicalRecord(medicalIdCounter.getAndIncrement(),
                LocalDateTime.now().minusDays(15), healthStatus, "בדיקה תקופתית שנתית", "חיסון שגרתי ותילוע", "ד\"ר שרה מילר", "חיוניות גבוהה ומדדים תקינים"));

        animals.add(animal);
    }

    // ==========================================
    // Animals CRUD & Filtering
    // ==========================================
    public List<Animal> getAllAnimals() {
        return new ArrayList<>(animals);
    }

    public Optional<Animal> getAnimalById(Long id) {
        return animals.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public Animal addAnimal(Animal animal) {
        if (animal.getName() == null || animal.getName().trim().isEmpty()) {
            throw new AnimalValidationException("שם החיה הינו שדה חובה");
        }

        if (animal.getId() == null) {
            animal.setId(animalIdCounter.getAndIncrement());
        }
        if (animal.getSubSpecies() != null && animal.getSpecies() == null) {
            animal.setSpecies(animal.getSubSpecies().getSpecies());
        }

        // ולידציה של תאימות הכלוב לסוג החיה
        if (animal.getCage() != null && animal.getCage().getId() != null) {
            Cage cage = getCageById(animal.getCage().getId())
                    .orElseThrow(() -> new AnimalValidationException("הכלוב שנבחר (מזהה " + animal.getCage().getId() + ") אינו קיים במערכת"));
            if (animal.getSpecies() != null && cage.getSpecies() != null && !cage.getSpecies().equals(animal.getSpecies())) {
                throw new AnimalValidationException("סוג הכלוב (" + cage.getSpecies().getHebrewName() + ") אינו תואם לסוג החיה (" + animal.getSpecies().getHebrewName() + ")");
            }
            animal.setCage(cage);
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

        animals.add(animal);
        log.info("🐾 [ZOO-ACTION: הוספת חיה חדשה] שם: '{}' (ID: {}) | סוג: {} | תת-סוג: {} | משקל: {} ק\"ג | בריאות: {} | כלוב: {}",
                animal.getName(), animal.getId(), animal.getSpecies(), animal.getSubSpecies(),
                animal.getWeightKg(), animal.getHealthStatus(),
                animal.getCage() != null ? "#" + animal.getCage().getId() : "ללא");
        return animal;
    }

    public Optional<Animal> updateAnimal(Long id, Animal updated) {
        return getAnimalById(id).map(existing -> {
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
            if (updated.getConservationStatus() != null) existing.setConservationStatus(updated.getConservationStatus());
            if (updated.getMicrochipId() != null) existing.setMicrochipId(updated.getMicrochipId());
            if (updated.getFeedingSchedule() != null) existing.setFeedingSchedule(updated.getFeedingSchedule());
            if (updated.getNotes() != null) existing.setNotes(updated.getNotes());

            if (updated.getCage() != null) {
                if (updated.getCage().getId() != null) {
                    Cage cage = getCageById(updated.getCage().getId())
                            .orElseThrow(() -> new AnimalValidationException("הכלוב שנבחר אינו קיים"));
                    if (existing.getSpecies() != null && cage.getSpecies() != null && !cage.getSpecies().equals(existing.getSpecies())) {
                        throw new AnimalValidationException("סוג הכלוב אינו תואם לסוג החיה");
                    }
                    existing.setCage(cage);
                } else {
                    existing.setCage(null);
                }
            }

            log.info("✏️  [ZOO-ACTION: עדכון פרטי חיה] עודכנו פרטי חיה '{}' (ID: {}) | בריאות: {} | משקל: {} ק\"ג | כלוב: {}",
                    existing.getName(), id, existing.getHealthStatus(), existing.getWeightKg(),
                    existing.getCage() != null ? "#" + existing.getCage().getId() : "ללא");

            return existing;
        });
    }

    public boolean deleteAnimal(Long id) {
        Optional<Animal> animalOpt = getAnimalById(id);
        String name = animalOpt.map(Animal::getName).orElse("לא ידוע");
        boolean removed = animals.removeIf(a -> a.getId().equals(id));
        if (removed) {
            log.warn("🗑️  [ZOO-ACTION: מחיקת חיה] חיה '{}' (ID: {}) הוסרה לצמיתות מהמערכת", name, id);
        } else {
            log.warn("⚠️ [ZOO-ACTION: מחיקת חיה] ניסיון מחיקה נכשל - חיה עם מזהה ID: {} לא נמצאה", id);
        }
        return removed;
    }

    // ==========================================
    // Animal Operations (האכלה, טיפול רפואי, בידוד, העברה)
    // ==========================================
    public Animal feedAnimal(Long id, FeedingRequest request) {
        Animal animal = getAnimalById(id)
                .orElseThrow(() -> new AnimalNotFoundException(id));

        String food = (request.getFoodItem() != null && !request.getFoodItem().isBlank())
                ? request.getFoodItem()
                : (animal.getFavoriteFood() != null ? animal.getFavoriteFood() : "מזון שגרתי");

        Double amount = request.getAmountKg() != null ? request.getAmountKg() : 1.0;
        String fedBy = (request.getFedBy() != null && !request.getFedBy().isBlank()) ? request.getFedBy() : "מטפל תורן";

        FeedingRecord record = new FeedingRecord(LocalDateTime.now(), food, amount, fedBy, request.getNotes());
        animal.addFeedingRecord(record);

        log.info("🥩 [ZOO-ACTION: האכלת חיה] בוצעה האכלה עבור '{}' (ID: {}) | מזון: {} | כמות: {} ק\"ג | מאכיל: {} | הערות: {}",
                animal.getName(), id, food, amount, fedBy,
                (request.getNotes() != null && !request.getNotes().isBlank()) ? request.getNotes() : "ללא");

        return animal;
    }

    public Animal recordMedicalCheckup(Long id, MedicalRecordRequest request) {
        Animal animal = getAnimalById(id)
                .orElseThrow(() -> new AnimalNotFoundException(id));

        MedicalRecord record = new MedicalRecord(
                medicalIdCounter.getAndIncrement(),
                LocalDateTime.now(),
                request.getHealthStatus(),
                request.getDiagnosis(),
                request.getTreatment(),
                request.getPerformedBy(),
                request.getNotes()
        );

        animal.addMedicalRecord(record);

        log.info("🩺 [ZOO-ACTION: בדיקה רפואית] נרשם עדכון רפואי עבור '{}' (ID: {}) | סטטוס מעודכן: {} | טיפול: {} | אבחון: {} | בוצע ע\"י: {}",
                animal.getName(), id, request.getHealthStatus(),
                request.getTreatment() != null ? request.getTreatment() : "שגרתי",
                request.getDiagnosis() != null ? request.getDiagnosis() : "תקין",
                request.getPerformedBy() != null ? request.getPerformedBy() : "וטרינר");

        return animal;
    }

    public Animal quarantineAnimal(Long id, String reason, String vetName) {
        Animal animal = getAnimalById(id)
                .orElseThrow(() -> new AnimalNotFoundException(id));

        animal.setHealthStatus(HealthStatus.QUARANTINED);
        MedicalRecord record = new MedicalRecord(
                medicalIdCounter.getAndIncrement(),
                LocalDateTime.now(),
                HealthStatus.QUARANTINED,
                "העברה לבידוד מונע / רפואי: " + (reason != null ? reason : "ללא פירוט"),
                "בידוד קפדני ומעקב וטרינרי צמוד",
                vetName != null ? vetName : "וטרינר ראשי",
                reason
        );
        animal.addMedicalRecord(record);

        log.warn("🚨 [ZOO-ACTION: בידוד רפואי] חיה '{}' (ID: {}) הועברה לבידוד! | סיבה: {} | וטרינר אחראי: {}",
                animal.getName(), id, reason != null ? reason : "ללא פירוט", vetName != null ? vetName : "וטרינר ראשי");

        return animal;
    }

    public Animal transferAnimalToCage(Long animalId, Long newCageId) {
        Animal animal = getAnimalById(animalId)
                .orElseThrow(() -> new AnimalNotFoundException(animalId));

        if (newCageId == null) {
            animal.setCage(null);
            log.info("🏠 [ZOO-ACTION: הוצאה מכלוב] חיה '{}' (ID: {}) הוצאה מהכלוב והוגדרה ללא כלוב",
                    animal.getName(), animalId);
            return animal;
        }

        Cage newCage = getCageById(newCageId)
                .orElseThrow(() -> new AnimalValidationException("כלוב מזהה " + newCageId + " לא נמצא"));

        if (animal.getSpecies() != null && newCage.getSpecies() != null && !animal.getSpecies().equals(newCage.getSpecies())) {
            throw new AnimalValidationException("לא ניתן להעביר חיה ממין " + animal.getSpecies().getHebrewName()
                    + " לכלוב המיועד למין " + newCage.getSpecies().getHebrewName());
        }

        animal.setCage(newCage);
        log.info("🏠 [ZOO-ACTION: העברת כלוב] חיה '{}' (ID: {}) הועברה בהצלחה לכלוב: #{} ({})",
                animal.getName(), animalId, newCageId, newCage.getSpecies() != null ? newCage.getSpecies().getHebrewName() : "");
        return animal;
    }

    // ==========================================
    // Filter & Search Methods
    // ==========================================
    public List<Animal> getAnimalsBySpecies(Species species) {
        return animals.stream()
                .filter(a -> a.getSpecies() == species)
                .collect(Collectors.toList());
    }

    public List<Animal> getAnimalsBySubSpecies(SubSpecies subSpecies) {
        return animals.stream()
                .filter(a -> a.getSubSpecies() == subSpecies)
                .collect(Collectors.toList());
    }

    public List<Animal> getAnimalsByHealth(HealthStatus healthStatus) {
        return animals.stream()
                .filter(a -> a.getHealthStatus() == healthStatus)
                .collect(Collectors.toList());
    }

    public List<Animal> getAnimalsByDiet(DietType dietType) {
        return animals.stream()
                .filter(a -> a.getDietType() == dietType)
                .collect(Collectors.toList());
    }

    public List<Animal> getEndangeredAnimals() {
        return animals.stream()
                .filter(Animal::isEndangered)
                .collect(Collectors.toList());
    }

    public List<Animal> getAnimalsNeedingFood(int hoursThreshold) {
        return animals.stream()
                .filter(a -> a.isNeedsFeeding(hoursThreshold))
                .collect(Collectors.toList());
    }

    public List<Animal> searchAnimals(String query) {
        if (query == null || query.isBlank()) {
            return getAllAnimals();
        }
        String q = query.trim().toLowerCase();
        return animals.stream().filter(a -> {
            boolean nameMatch = a.getName() != null && a.getName().toLowerCase().contains(q);
            boolean speciesMatch = a.getSpecies() != null && (a.getSpecies().name().toLowerCase().contains(q) || a.getSpecies().getHebrewName().contains(q));
            boolean subSpeciesMatch = a.getSubSpecies() != null && (a.getSubSpecies().name().toLowerCase().contains(q) || a.getSubSpecies().getHebrewName().contains(q));
            boolean chipMatch = a.getMicrochipId() != null && a.getMicrochipId().toLowerCase().contains(q);
            boolean countryMatch = a.getOriginCountry() != null && a.getOriginCountry().toLowerCase().contains(q);
            boolean notesMatch = a.getNotes() != null && a.getNotes().toLowerCase().contains(q);
            return nameMatch || speciesMatch || subSpeciesMatch || chipMatch || countryMatch || notesMatch;
        }).collect(Collectors.toList());
    }

    public List<Animal> filterAnimals(Species species, HealthStatus health, DietType diet, Boolean endangered, Long cageId, String search) {
        return animals.stream()
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
    public AnimalStatsResponse getAnimalStats() {
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
    public List<Cage> getAllCages() {
        return new ArrayList<>(cages);
    }

    public Optional<Cage> getCageById(Long id) {
        return cages.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Cage addCage(Cage cage) {
        if (cage.getId() == null) {
            cage.setId(cageIdCounter.getAndIncrement());
        }
        cages.add(cage);
        log.info("🏗️  [ZOO-ACTION: הוספת כלוב] נוצר כלוב חדש: #{} עבור מין: {}", cage.getId(), cage.getSpecies());
        return cage;
    }

    public Optional<Cage> updateCage(Long id, Cage updated) {
        return getCageById(id).map(existing -> {
            if (updated.getSpecies() != null) existing.setSpecies(updated.getSpecies());
            log.info("🔧 [ZOO-ACTION: עדכון כלוב] עודכן כלוב: #{} | מין מיועד: {}", id, existing.getSpecies());
            return existing;
        });
    }

    public boolean deleteCage(Long id) {
        boolean removed = cages.removeIf(c -> c.getId().equals(id));
        if (removed) {
            animals.stream()
                    .filter(a -> a.getCage() != null && a.getCage().getId().equals(id))
                    .forEach(a -> a.setCage(null));
            employees.forEach(e -> e.getAssignedCages().removeIf(c -> c.getId().equals(id)));
            log.warn("🗑️  [ZOO-ACTION: מחיקת כלוב] נמחק כלוב: #{} ושוחררו החיות שהיו בו", id);
        }
        return removed;
    }

    // ==========================================
    // Employees CRUD
    // ==========================================
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employees.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public Employee addEmployee(Employee employee) {
        if (employee.getId() == null) {
            employee.setId(employeeIdCounter.getAndIncrement());
        }
        employees.add(employee);
        log.info("👤 [ZOO-ACTION: הוספת עובד] עובד חדש: '{}' (ID: {}) בתפקיד: {}", employee.getName(), employee.getId(), employee.getRole());
        return employee;
    }

    public Optional<Employee> updateEmployee(Long id, Employee updated) {
        return getEmployeeById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getRole() != null) existing.setRole(updated.getRole());
            if (updated.getAssignedCages() != null) existing.setAssignedCages(updated.getAssignedCages());
            log.info("👤 [ZOO-ACTION: עדכון עובד] עודכנו פרטי עובד '{}' (ID: {})", existing.getName(), id);
            return existing;
        });
    }

    public boolean deleteEmployee(Long id) {
        boolean removed = employees.removeIf(e -> e.getId().equals(id));
        if (removed) {
            log.warn("🗑️  [ZOO-ACTION: מחיקת עובד] עובד ID: {} הוסר מהמערכת", id);
        }
        return removed;
    }

    public Optional<Employee> assignCageToEmployee(Long employeeId, Long cageId) {
        Optional<Employee> employeeOpt = getEmployeeById(employeeId);
        Optional<Cage> cageOpt = getCageById(cageId);
        if (employeeOpt.isPresent() && cageOpt.isPresent()) {
            employeeOpt.get().assignCage(cageOpt.get());
            log.info("🔑 [ZOO-ACTION: שיוך כלוב לעובד] עובד '{}' שויך לכלוב: #{}", employeeOpt.get().getName(), cageId);
            return employeeOpt;
        }
        return Optional.empty();
    }

    public Optional<Employee> removeCageFromEmployee(Long employeeId, Long cageId) {
        Optional<Employee> employeeOpt = getEmployeeById(employeeId);
        Optional<Cage> cageOpt = getCageById(cageId);
        if (employeeOpt.isPresent() && cageOpt.isPresent()) {
            employeeOpt.get().removeCage(cageOpt.get());
            log.info("🔑 [ZOO-ACTION: ביטול שיוך כלוב] לעובד '{}' בוטל שיוך לכלוב: #{}", employeeOpt.get().getName(), cageId);
            return employeeOpt;
        }
        return Optional.empty();
    }

    // ==========================================
    // Veterinarians CRUD
    // ==========================================
    public List<Veterinarian> getAllVeterinarians() {
        return new ArrayList<>(veterinarians);
    }

    public Optional<Veterinarian> getVeterinarianById(Long id) {
        return veterinarians.stream().filter(v -> v.getId().equals(id)).findFirst();
    }

    public Veterinarian addVeterinarian(Veterinarian vet) {
        if (vet.getId() == null) {
            vet.setId(vetIdCounter.getAndIncrement());
        }
        veterinarians.add(vet);
        log.info("👨‍⚕️ [ZOO-ACTION: הוספת וטרינר] נוסף וטרינר חדש: '{}' (ID: {}) בהתמחות: {}", vet.getName(), vet.getId(), vet.getSpecialization());
        return vet;
    }

    public Optional<Veterinarian> updateVeterinarian(Long id, Veterinarian updated) {
        return getVeterinarianById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getSpecialization() != null) existing.setSpecialization(updated.getSpecialization());
            if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
            if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
            log.info("👨‍⚕️ [ZOO-ACTION: עדכון וטרינר] עודכנו פרטי וטרינר: '{}' (ID: {})", existing.getName(), id);
            return existing;
        });
    }

    public boolean deleteVeterinarian(Long id) {
        boolean removed = veterinarians.removeIf(v -> v.getId().equals(id));
        if (removed) {
            log.warn("🗑️  [ZOO-ACTION: מחיקת וטרינר] וטרינר ID: {} הוסר מהמערכת", id);
        }
        return removed;
    }
}
