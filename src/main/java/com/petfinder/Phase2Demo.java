
package com.petfinder;

import com.petfinder.exception.ListingNotFoundException;
import com.petfinder.model.Animal;
import com.petfinder.model.Enquiry;
import com.petfinder.model.HealthRecord;
import com.petfinder.model.Livestock;
import com.petfinder.model.Listing;
import com.petfinder.model.Pet;
import com.petfinder.model.Seller;
import com.petfinder.service.ListingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Phase2Demo {

    public static void main(String[] args) {

        Seller seller = new Seller(
            1, "Copperbelt Pet Haven", "Breeder", "Sample Contact",
            "0970000001", "haven@example.com",
            "Ndola", "Town Centre", false
        );

        Pet pet = new Pet(
            "Duke", "Dog", "Doberman", "Adult",
            "Male", "Black", "Loyal"
        );

        pet.addHealthRecord(
            new HealthRecord("Rabies", "2026-08-12")
        );

        Livestock goat = new Livestock(
            "Mama Brown", "Goat", "Boer goat",
            "Adult", "Female", "Brown and white", "Breeding"
        );

        goat.addHealthRecord(
            new HealthRecord("Clostridial vaccine", "2026-08-02")
        );

        // Inheritance, polymorphism and a List collection
        List<Animal> animals = List.of(pet, goat);

        System.out.println("=== Polymorphism ===");

        for (Animal animal : animals) {
            System.out.println(
                animal.getType() + ": " + animal.describe()
            );
        }

        ListingService service = new ListingService();

        service.addListing(
            new Listing(
                1, seller.getId(), pet,
                new BigDecimal("3500"), 1,
                "/images/doberman-adult-dog.jpeg"
            )
        );

        service.addListing(
            new Listing(
                2, seller.getId(), goat,
                new BigDecimal("1800"), 1,
                "/images/adult-goat.jpeg"
            )
        );

        // HashMap-backed service, CRUD operations and validation
        service.updateStatus(1, "Reserved");
        service.updatePrice(2, new BigDecimal("1750"));

        System.out.println("\n=== Listings ===");

        for (Listing listing :
                service.getListingsBySeller(seller.getId())) {

            System.out.println(
                listing.getId() + ": "
                + listing.getAnimal().getName()
                + " | K" + listing.getPrice()
                + " | " + listing.getStatus()
                + " | records="
                + listing.getAnimal().getHealthRecords().size()
            );
        }

        Enquiry enquiry = new Enquiry(
            1, 1, "Buyer Example", "0970000002",
            "Is Duke available for viewing?",
            LocalDateTime.of(2026, 11, 3, 10, 30)
        );

        System.out.println("\n=== Enquiry ===");

        System.out.println(
            enquiry.getBuyerName()
            + " asked about listing "
            + enquiry.getListingId()
            + " at " + enquiry.getSentAt()
        );

        // A custom exception is thrown and handled here
        System.out.println("\n=== Exception handling ===");

        try {
            service.getListing(999);

            throw new IllegalStateException(
                "Expected ListingNotFoundException"
            );

        } catch (ListingNotFoundException e) {
            System.out.println(
                "Handled: " + e.getMessage()
            );
        }

        // Final verification
        if (service.getAllListings().size() != 2) {
            throw new IllegalStateException(
                "Expected two listings"
            );
        }

        if (!"Reserved".equals(
                service.getListing(1).getStatus())) {
            throw new IllegalStateException(
                "Status update did not work"
            );
        }

        System.out.println("\nPHASE 2 CHECKS PASSED");
    }
}
