package com.petfinder.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ListingTest {

    private Pet vaccinatedPet() {
        Pet pet = new Pet("Duke", "Dog", "Doberman", "Adult", "Male", "Black", "Loyal");
        pet.addHealthRecord(new HealthRecord("Rabies", "2026-08-12"));
        return pet;
    }

    private Listing listing() {
        return new Listing(10, 5, vaccinatedPet(), new BigDecimal("3500.00"), 1, "");
    }

    @Test
    void listingIncludesValidAnimalSellerAndStatus() {
        Listing listing = listing();
        assertEquals(10, listing.getId());
        assertEquals(5, listing.getSellerId());
        assertEquals("Duke", listing.getAnimal().getName());
        assertEquals("Available", listing.getStatus());
        assertEquals(0, new BigDecimal("3500.00").compareTo(listing.getPrice()));
    }

    @Test
    void priceCannotBeNegativeAndValidPriceCanBeUpdated() {
        Listing listing = listing();
        listing.setPrice(new BigDecimal("4500.50"));
        assertEquals(0, new BigDecimal("4500.50").compareTo(listing.getPrice()));
        assertThrows(IllegalArgumentException.class,
            () -> listing.setPrice(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> listing.setPrice(null));
    }

    @Test
    void quantityMustBePositive() {
        Listing listing = listing();
        listing.setQuantity(3);
        assertEquals(3, listing.getQuantity());
        assertThrows(IllegalArgumentException.class, () -> listing.setQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> listing.setQuantity(-1));
    }

    @Test
    void onlyKnownListingStatusesAreAccepted() {
        Listing listing = listing();
        listing.setStatus("Reserved");
        assertEquals("Reserved", listing.getStatus());
        listing.setStatus("Rehomed");
        assertEquals("Rehomed", listing.getStatus());
        assertThrows(IllegalArgumentException.class, () -> listing.setStatus("Sold"));
    }

    @Test
    void listingRequiresHealthRecordAndPositiveIds() {
        Pet unvaccinated = new Pet("Duke", "Dog", "Doberman", "Adult", "Male", "Black", "Loyal");
        assertThrows(IllegalArgumentException.class,
            () -> new Listing(1, 1, unvaccinated, BigDecimal.ONE, 1, ""));
        assertThrows(IllegalArgumentException.class,
            () -> new Listing(0, 1, vaccinatedPet(), BigDecimal.ONE, 1, ""));
        assertThrows(IllegalArgumentException.class,
            () -> new Listing(1, -1, vaccinatedPet(), BigDecimal.ONE, 1, ""));
        assertThrows(NullPointerException.class,
            () -> new Listing(1, 1, null, BigDecimal.ONE, 1, ""));
    }
}
