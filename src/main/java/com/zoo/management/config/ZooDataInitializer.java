package com.zoo.management.config;

import com.zoo.management.model.*;
import com.zoo.management.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ZooDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ZooDataInitializer.class);

    private final CageRepository cageRepository;
    private final AnimalRepository animalRepository;
    private final EmployeeRepository employeeRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ZooTaskRepository taskRepository;

    public ZooDataInitializer(CageRepository cageRepository,
                              AnimalRepository animalRepository,
                              EmployeeRepository employeeRepository,
                              VeterinarianRepository veterinarianRepository,
                              AppUserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              ZooTaskRepository taskRepository) {
        this.cageRepository = cageRepository;
        this.animalRepository = animalRepository;
        this.employeeRepository = employeeRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. אתחול משתמשי מערכת (אם לא קיימים)
        if (userRepository.count() == 0) {
            log.info("🔐 מאתחל משתמשי מערכת ב-zoo_users...");
            userRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), "דוד כהן (מנהל ראשי)", UserRole.ADMIN));
            userRepository.save(new AppUser("vet", passwordEncoder.encode("vet123"), "ד\"ר שרה מילר (וטרינרית ראשית)", UserRole.VET));
            userRepository.save(new AppUser("keeper", passwordEncoder.encode("keeper123"), "מאיה לוי (מטפלת חיות ראשית)", UserRole.KEEPER));
            log.info("✅ 3 משתמשי מערכת (admin, vet, keeper) נוצרו בהצלחה במסד הנתונים!");
        }

        // 1.1 אתחול משימות צוות
        if (taskRepository.count() == 0) {
            log.info("📋 מאתחל משימות עבודה לצוות הגן...");
            taskRepository.save(new ZooTask("האכלת בוקר לטורפים במתחם חתוליים", "חלוקת נתחי בשר בקר טרי לסימבה ונלה ובדיקת תיאבון", UserRole.KEEPER, "מאיה לוי", "Simba", "HIGH", "היום ב-09:30"));
            taskRepository.save(new ZooTask("בדיקת שיניים תקופתית לטיגריס שיר חאן", "ביצוע הרדמה קלה, בדיקת חניכיים וסתימת שן טוחנת", UserRole.VET, "ד\"ר שרה מילר", "Shere Khan", "URGENT", "היום ב-11:00"));
            taskRepository.save(new ZooTask("ניקיון וחיטוי בריכת הפינגווינים ועיטם", "החלפת מי בריכה, בדיקת רמת PH והזנת אצות", UserRole.KEEPER, "מאיה לוי", "Pingu", "MEDIUM", "היום ב-13:00"));
            taskRepository.save(new ZooTask("שקילת פנדה פו ומעקב צריכת במבוק", "שקילה במשטח הדיגיטלי ותיעוד ביומן המעקב", UserRole.VET, "ד\"ר רון כץ", "Po", "LOW", "מחר ב-10:00"));
            taskRepository.save(new ZooTask("ביקורת בטיחות מקיפה בכלובי הקופים והפילים", "בדיקת תקינות מנעולים, גידור חשמלי ומצלמות לילה", UserRole.ADMIN, "דוד כהן", "George", "HIGH", "סוף השבוע"));
            log.info("✅ 5 משימות עבודה נוצרו בהצלחה בטבלת zoo_tasks!");
        }

        // 2. אתחול נתוני חיות וכלובים
        if (cageRepository.count() > 0) {
            log.info("מסד הנתונים כבר מכיל כלובים - מעדכן תמונות NaNoBanana 2.1 עבור חיות קיימות במידת הצורך...");
            for (Animal a : animalRepository.findAll()) {
                if (a.getImageUrl() == null || a.getImageUrl().isEmpty()) {
                    String img = null;
                    if ("Simba".equals(a.getName()) || "Nala".equals(a.getName())) img = "images/simba.jpg";
                    else if ("Shere Khan".equals(a.getName())) img = "images/shere_khan.jpg";
                    else if ("Dumbo".equals(a.getName())) img = "images/dumbo.jpg";
                    else if ("Po".equals(a.getName())) img = "images/po.jpg";
                    else if ("Flipper".equals(a.getName())) img = "images/flipper.jpg";
                    else if ("Pingu".equals(a.getName())) img = "images/pingu.jpg";
                    else if ("Koko".equals(a.getName())) img = "images/koko.jpg";
                    else if ("Pinky".equals(a.getName())) img = "images/pinky.jpg";
                    if (img != null) {
                        a.setImageUrl(img);
                        animalRepository.save(a);
                    }
                }
            }
            return;
        }

        log.info("🌱 מאתחל נתוני דוגמה של גן החיות ב-Spring Data JPA...");

        // כלובים לכל המינים
        Cage felineCage = cageRepository.save(new Cage(Species.FELINE));
        Cage primateCage = cageRepository.save(new Cage(Species.PRIMATE));
        Cage birdCage = cageRepository.save(new Cage(Species.BIRD));
        Cage aquaticCage = cageRepository.save(new Cage(Species.AQUATIC));
        Cage mammalCage = cageRepository.save(new Cage(Species.MAMMAL));
        Cage reptileCage = cageRepository.save(new Cage(Species.REPTILE));
        Cage amphibianCage = cageRepository.save(new Cage(Species.AMPHIBIAN));

        // 20 חיות מגוונות עם מאפיינים מקיפים והיסטוריה
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

        // עובדים
        Employee manager = new Employee("David Cohen", Role.MANAGER);
        Employee keeper = new Employee("Maya Levi", Role.EMPLOYEE);
        keeper.assignCage(primateCage);
        keeper.assignCage(felineCage);
        employeeRepository.save(manager);
        employeeRepository.save(keeper);

        // וטרינרים
        veterinarianRepository.save(new Veterinarian("Dr. Sarah Miller", Species.AQUATIC, "sarah@zoo.com", "050-1234567"));
        veterinarianRepository.save(new Veterinarian("Dr. Ron Katz", Species.FELINE, "ron@zoo.com", "052-7654321"));

        log.info("✅ אתחול נתוני הדוגמה הסתיים בהצלחה! סך הכל חיות: {}", animalRepository.count());
    }

    private void createSampleAnimal(String name, Species species, SubSpecies subSpecies, HealthStatus healthStatus,
                                     Cage cage, Gender gender, Integer age, Double weightKg, DietType dietType,
                                     String favoriteFood, String originCountry, ConservationStatus conservationStatus,
                                     String microchipId, String feedingSchedule, String notes, int hoursAgoFed) {
        Animal animal = new Animal();
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

        String img = null;
        if ("Simba".equals(name) || "Nala".equals(name)) img = "images/simba.jpg";
        else if ("Shere Khan".equals(name)) img = "images/shere_khan.jpg";
        else if ("Dumbo".equals(name)) img = "images/dumbo.jpg";
        else if ("Po".equals(name)) img = "images/po.jpg";
        else if ("Flipper".equals(name)) img = "images/flipper.jpg";
        else if ("Pingu".equals(name)) img = "images/pingu.jpg";
        else if ("Koko".equals(name)) img = "images/koko.jpg";
        else if ("Pinky".equals(name)) img = "images/pinky.jpg";
        animal.setImageUrl(img);

        LocalDateTime fedTime = LocalDateTime.now().minusHours(hoursAgoFed);
        animal.setLastFedTime(fedTime);

        // Feeding history sample
        animal.addFeedingRecord(new FeedingRecord(fedTime, favoriteFood, Math.max(0.5, weightKg * 0.03), "מאיה לוי", "האכלה שגרתית מוצלחת"));

        // Medical history sample
        animal.addMedicalRecord(new MedicalRecord(LocalDateTime.now().minusDays(15), healthStatus,
                "בדיקה תקופתית שנתית", "חיסון שגרתי ותילוע", "ד\"ר שרה מילר", "חיוניות גבוהה ומדדים תקינים"));

        animalRepository.save(animal);
    }
}
