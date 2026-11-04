
package com.petfinder;

import com.petfinder.model.Animal;
import com.petfinder.model.Pet;
import com.petfinder.model.Livestock;
import java.util.List;

public class ModelDemo {

    public static void main(String[] args) {

        List<Animal> animals = List.of(
            new Pet(
                "Duke", "Dog", "Doberman", "Adult",
                "Male", "Black", "Loyal"
            ),
            new Livestock(
                "Mama Brown", "Goat", "Boer goat",
                "Adult", "Female", "Brown and white",
                "Breeding"
            )
        );

        for (Animal animal : animals) {
            System.out.println(
                animal.getType() + ": " + animal.describe()
            );
        }
    }
}
