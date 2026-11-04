
package com.petfinder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Animal {
    private String name;
    private final String species;
    private final String breed;
    private final String lifeStage;
    private final String sex;
    private final String colour;
    private final List<HealthRecord> healthRecords = new ArrayList<>();

    protected Animal(String name, String species, String breed,
                     String lifeStage, String sex, String colour) {
        this.name = requireText(name, "Name");
        this.species = requireText(species, "Species");
        this.breed = requireText(breed, "Breed");
        this.lifeStage = requireText(lifeStage, "Life stage");
        this.sex = requireText(sex, "Sex");
        this.colour = requireText(colour, "Colour");
    }

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return value.trim();
    }

    public String getName() { return name; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public String getLifeStage() { return lifeStage; }
    public String getSex() { return sex; }
    public String getColour() { return colour; }

    public void rename(String newName) {
        this.name = requireText(newName, "Name");
    }

    public void addHealthRecord(HealthRecord record) {
        healthRecords.add(
            Objects.requireNonNull(record, "Health record is required")
        );
    }

    public List<HealthRecord> getHealthRecords() {
        return List.copyOf(healthRecords);
    }

    public abstract String getType();
    public abstract String describe();
}
