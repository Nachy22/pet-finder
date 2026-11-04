package com.petfinder.service;

import com.petfinder.exception.ListingNotFoundException;
import com.petfinder.model.HealthRecord;
import com.petfinder.model.Listing;
import com.petfinder.model.Pet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListingServiceTest {

    private Listing listing(int listingId, int sellerId) {
        Pet pet = new Pet("Duke", "Dog", "Doberman", "Adult", "Male", "Black", "Loyal");
        pet.addHealthRecord(new HealthRecord("Rabies", "2026-08-12"));
        return new Listing(listingId, sellerId, pet, new BigDecimal("3500"), 1, "");
    }

    @Test
    void collectionBackedCrudWorks() {
        ListingService service = new ListingService();
        service.addListing(listing(2, 1));
        service.addListing(listing(1, 2));
        assertEquals(List.of(1, 2), service.getAllListings().stream().map(Listing::getId).toList());
        assertEquals(2, service.getListing(2).getId());

        service.updatePrice(2, new BigDecimal("4000"));
        service.updateStatus(2, "Reserved");
        assertEquals(0, new BigDecimal("4000").compareTo(service.getListing(2).getPrice()));
        assertEquals("Reserved", service.getListing(2).getStatus());

        service.deleteListing(2);
        assertEquals(1, service.getAllListings().size());
        assertThrows(ListingNotFoundException.class, () -> service.getListing(2));
    }

    @Test
    void findsOnlyTheRequestedSellersListings() {
        ListingService service = new ListingService();
        service.addListing(listing(1, 2));
        service.addListing(listing(2, 3));
        service.addListing(listing(3, 2));
        assertEquals(List.of(1, 3),
            service.getListingsBySeller(2).stream().map(Listing::getId).toList());
    }

    @Test
    void duplicateIdsAndMissingListingsThrowExpectedExceptions() {
        ListingService service = new ListingService();
        service.addListing(listing(5, 1));
        assertThrows(IllegalArgumentException.class, () -> service.addListing(listing(5, 1)));
        ListingNotFoundException error = assertThrows(ListingNotFoundException.class,
            () -> service.deleteListing(999));
        assertEquals("Listing not found: 999", error.getMessage());
        assertThrows(ListingNotFoundException.class,
            () -> service.updateStatus(999, "Reserved"));
    }
}
