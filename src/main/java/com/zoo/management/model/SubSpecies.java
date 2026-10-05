package com.zoo.management.model;

public enum SubSpecies {
    // FELINE
    LION(Species.FELINE),
    TIGER(Species.FELINE),
    LEOPARD(Species.FELINE),

    // PRIMATE
    CHIMPANZEE(Species.PRIMATE),
    GORILLA(Species.PRIMATE),
    ORANGUTAN(Species.PRIMATE),

    // BIRD
    EAGLE(Species.BIRD),
    PARROT(Species.BIRD),
    FLAMINGO(Species.BIRD),

    // REPTILE
    PYTHON(Species.REPTILE),
    CROCODILE(Species.REPTILE),
    IGUANA(Species.REPTILE),

    // MAMMAL
    ELEPHANT(Species.MAMMAL),
    GIRAFFE(Species.MAMMAL),
    ZEBRA(Species.MAMMAL),

    // AQUATIC
    DOLPHIN(Species.AQUATIC),
    SEAL(Species.AQUATIC),
    SHARK(Species.AQUATIC);

    private final Species species;

    SubSpecies(Species species) {
        this.species = species;
    }

    public Species species() {
        return species;
    }

    public Species getSpecies() {
        return species;
    }
}
