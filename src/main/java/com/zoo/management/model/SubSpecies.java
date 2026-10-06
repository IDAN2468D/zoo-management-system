package com.zoo.management.model;

public enum SubSpecies {
    // FELINE
    LION(Species.FELINE, "אריה"),
    TIGER(Species.FELINE, "טיגריס"),
    LEOPARD(Species.FELINE, "נמר"),
    PANTHER(Species.FELINE, "פנתר"),
    CHEETAH(Species.FELINE, "ברדלס"),
    JAGUAR(Species.FELINE, "יגואר"),

    // PRIMATE
    CHIMPANZEE(Species.PRIMATE, "שימפנזה"),
    GORILLA(Species.PRIMATE, "גורילה"),
    ORANGUTAN(Species.PRIMATE, "אורנגאוטן"),
    LEMUR(Species.PRIMATE, "למור"),

    // BIRD
    EAGLE(Species.BIRD, "עיט"),
    PARROT(Species.BIRD, "תוכי"),
    FLAMINGO(Species.BIRD, "פלמינגו"),
    PENGUIN(Species.BIRD, "פינגווין"),
    OWL(Species.BIRD, "ינשוף"),

    // REPTILE
    PYTHON(Species.REPTILE, "פיתון"),
    CROCODILE(Species.REPTILE, "תנין"),
    IGUANA(Species.REPTILE, "איגואנה"),
    CHAMELEON(Species.REPTILE, "זיקית"),
    TORTOISE(Species.REPTILE, "צב יבשה"),

    // MAMMAL
    ELEPHANT(Species.MAMMAL, "פיל"),
    GIRAFFE(Species.MAMMAL, "ג'ירפה"),
    ZEBRA(Species.MAMMAL, "זברה"),
    KANGAROO(Species.MAMMAL, "קנגורו"),
    PANDA(Species.MAMMAL, "פנדה ענקית"),
    KOALA(Species.MAMMAL, "קואלה"),

    // AQUATIC
    DOLPHIN(Species.AQUATIC, "דולפין"),
    SEAL(Species.AQUATIC, "כלב ים"),
    SHARK(Species.AQUATIC, "כריש"),
    SEA_TURTLE(Species.AQUATIC, "צב ים"),
    OTTER(Species.AQUATIC, "לוטרה"),

    // AMPHIBIAN
    FROG(Species.AMPHIBIAN, "צפרדע"),
    SALAMANDER(Species.AMPHIBIAN, "סלמנדרה");

    private final Species species;
    private final String hebrewName;

    SubSpecies(Species species, String hebrewName) {
        this.species = species;
        this.hebrewName = hebrewName;
    }

    public Species species() {
        return species;
    }

    public Species getSpecies() {
        return species;
    }

    public String getHebrewName() {
        return hebrewName;
    }
}
