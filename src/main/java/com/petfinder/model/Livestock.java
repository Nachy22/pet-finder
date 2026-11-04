
package com.petfinder.model;

public class Livestock extends Animal {
    private final String purpose;

    public Livestock(String name, String species, String breed, String lifeStage,
                     String sex, String colour, String purpose) {
        super(name, species, breed, lifeStage, sex, colour);
        this.purpose = requireText(purpose, "Purpose");
    }

    public String getPurpose() {
        return purpose;
    }

    @Override
    public String getType() {
        return "Livestock";
    }

    @Override
    public String describe() {
        return getName() + " is a " + getBreed()
                + " kept for " + purpose + ".";
    }
}
