
package com.petfinder.model;

public class Pet extends Animal {
    private final String temperament;

    public Pet(String name, String species, String breed, String lifeStage,
               String sex, String colour, String temperament) {
        super(name, species, breed, lifeStage, sex, colour);
        this.temperament = requireText(temperament, "Temperament");
    }

    public String getTemperament() {
        return temperament;
    }

    @Override
    public String getType() {
        return "Pet";
    }

    @Override
    public String describe() {
        return getName() + " is a " + getBreed()
                + " with a " + temperament + " temperament.";
    }
}
