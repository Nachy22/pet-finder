
package com.petfinder.exception;

public class ListingNotFoundException extends RuntimeException {

    public ListingNotFoundException(int listingId) {
        super("Listing not found: " + listingId);
    }
}
