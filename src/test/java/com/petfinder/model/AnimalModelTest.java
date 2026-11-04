package com.petfinder.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnimalModelTest {

    private Pet pet() {
        return new Pet("Duke", "Dog", "Doberman", "Adult", "Male", "Black", "Loyal");
    }

    @Test
    void petAndLivestockShareAnimalParentAndUsePolymorphism() {
        List<Animal> animals = List.of(
            pet(),
            new Livestock("Mama Brown", "Goat", "Boer", "Adult", "Female", "Brown", "Breeding")
        );

        assertEquals("Pet", animals.get(0).getType());
        assertEquals("Livestock", animals.get(1).getType());
        assertTrue(animals.get(0).describe().contains("Loyal"));
        assertTrue(animals.get(1).describe().contains("Breeding"));
        assertInstanceOf(Pet.class, animals.get(0));
        assertInstanceOf(Livestock.class, animals.get(1));
    }

    @Test
    void animalFieldsArePrivateAndNameCanBeChangedThroughMethod() throws Exception {
        assertTrue(Modifier.isPrivate(Animal.class.getDeclaredField("name").getModifiers()));
        assertTrue(Modifier.isPrivate(Animal.class.getDeclaredField("species").getModifiers()));
        Pet pet = pet();
        pet.rename("Duke II");
        assertEquals("Duke II", pet.getName());
        assertThrows(IllegalArgumentException.class, () -> pet.rename("  "));
        assertEquals("Duke II", pet.getName());
    }

    @Test
    void missingRequiredAnimalPropertiesAreRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new Pet("", "Dog", "Doberman", "Adult", "Male", "Black", "Loyal"));
        assertThrows(IllegalArgumentException.class,
            () -> new Pet("Duke", "Dog", "Doberman", "Adult", "Male", "Black", null));
        assertThrows(IllegalArgumentException.class,
            () -> new Livestock("Goat", "Goat", "Boer", "Adult", "Female", "Brown", " "));
    }

    @Test
    void healthRecordsCanOnlyBeAddedThroughMethod() {
        Pet pet = pet();
        HealthRecord record = new HealthRecord("Rabies", "2026-08-12");
        pet.addHealthRecord(record);
        assertEquals(1, pet.getHealthRecords().size());
        assertEquals("Rabies", pet.getHealthRecords().get(0).getVaccine());
        assertThrows(UnsupportedOperationException.class,
            () -> pet.getHealthRecords().clear());
        assertThrows(NullPointerException.class, () -> pet.addHealthRecord(null));
        assertEquals(1, pet.getHealthRecords().size());
    }

    @Test
    void vaccinationDatesMustBeRealIsoDates() {
        assertEquals("2026-08-12", new HealthRecord("Rabies", "2026-08-12").getDate());
        assertThrows(IllegalArgumentException.class,
            () -> new HealthRecord("Rabies", "2026-02-30"));
        assertThrows(IllegalArgumentException.class,
            () -> new HealthRecord("Rabies", "12/08/2026"));
        assertThrows(IllegalArgumentException.class,
            () -> new HealthRecord(" ", "2026-08-12"));
        assertThrows(IllegalArgumentException.class,
            () -> new HealthRecord("Rabies", null));
    }
}
