
package com.petfinder.service;

import com.petfinder.exception.ListingNotFoundException;
import com.petfinder.model.Listing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Temporary in-memory storage.
// Phase 3 will replace this with MySQL persistence.
public class ListingService {

    private final Map<Integer, Listing> listings = new HashMap<>();

    public void addListing(Listing listing) {
        Objects.requireNonNull(listing, "Listing is required");

        if (listings.containsKey(listing.getId())) {
            throw new IllegalArgumentException(
                "Duplicate listing ID: " + listing.getId()
            );
        }

        listings.put(listing.getId(), listing);
    }

    public Listing getListing(int id) {
        Listing listing = listings.get(id);

        if (listing == null) {
            throw new ListingNotFoundException(id);
        }

        return listing;
    }

    public List<Listing> getAllListings() {
        List<Listing> result = new ArrayList<>(listings.values());

        result.sort(
            Comparator.comparingInt(Listing::getId)
        );

        return result;
    }

    public List<Listing> getListingsBySeller(int sellerId) {
        return getAllListings().stream()
            .filter(listing -> listing.getSellerId() == sellerId)
            .toList();
    }

    public void updatePrice(int id, BigDecimal price) {
        getListing(id).setPrice(price);
    }

    public void updateStatus(int id, String status) {
        getListing(id).setStatus(status);
    }

    public void deleteListing(int id) {
        if (listings.remove(id) == null) {
            throw new ListingNotFoundException(id);
        }
    }
}
