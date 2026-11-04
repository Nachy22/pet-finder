package com.petfinder;

import com.petfinder.db.Database;
import com.petfinder.model.Enquiry;
import com.petfinder.model.HealthRecord;
import com.petfinder.model.Listing;
import com.petfinder.model.Pet;
import com.petfinder.model.Seller;
import com.petfinder.repository.EnquiryRepository;
import com.petfinder.repository.ListingRepository;
import com.petfinder.repository.SellerRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/** Run in separate processes: write first, then read, to prove persistence. */
public class Phase3Demo {
    private static final int SELLER_ID = 900001;
    private static final int LISTING_ID = 900001;
    private static final int ENQUIRY_ID = 900001;

    public static void main(String[] args) {
        if (args.length != 1 || (!"write".equals(args[0]) && !"read".equals(args[0]))) {
            System.out.println("Usage: Phase3Demo write | read");
            return;
        }
        try (Connection ignored = Database.connect()) {
            System.out.println("MySQL connection successful.");
        } catch (SQLException | IllegalStateException error) {
            System.err.println("Unable to connect: " + error.getMessage());
            System.exit(1);
        }

        try {
            SellerRepository sellers = new SellerRepository();
            ListingRepository listings = new ListingRepository();
            EnquiryRepository enquiries = new EnquiryRepository();

            if ("write".equals(args[0])) {
                writeSample(sellers, listings, enquiries);
            } else {
                readSample(sellers, listings, enquiries);
            }
        } catch (SQLException | RuntimeException error) {
            System.err.println("Phase 3 failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void writeSample(SellerRepository sellers, ListingRepository listings,
                                    EnquiryRepository enquiries) throws SQLException {
        if (sellers.findById(SELLER_ID).isEmpty()) {
            sellers.save(new Seller(SELLER_ID, "Pet Finder Phase 3 Demo", "Breeder",
                "Sample Contact", "0970999001", "phase3-demo@example.com",
                "Ndola", "Town Centre", false));
        }

        if (listings.findById(LISTING_ID).isEmpty()) {
            Pet pet = new Pet("Duke", "Dog", "Doberman", "Adult", "Male", "Black", "Loyal");
            pet.addHealthRecord(new HealthRecord("Rabies", "2026-08-12"));
            listings.save(new Listing(LISTING_ID, SELLER_ID, pet,
                new BigDecimal("3500.00"), 1, "/images/doberman-adult-dog.jpeg"));
        }
        listings.updateStatus(LISTING_ID, "Reserved");

        if (enquiries.findById(ENQUIRY_ID).isEmpty()) {
            enquiries.save(new Enquiry(ENQUIRY_ID, LISTING_ID,
                "Buyer Example", "0970999002", "Can I meet Duke?",
                LocalDateTime.of(2026, 11, 3, 10, 30)));
        }

        System.out.println("WROTE: seller, animal, health record, listing, enquiry.");
        System.out.println("WROTE: listing status = Reserved.");
        System.out.println("Close this process, then run Phase3Demo read.");
    }

    private static void readSample(SellerRepository sellers, ListingRepository listings,
                                   EnquiryRepository enquiries) throws SQLException {
        Seller seller = sellers.findById(SELLER_ID)
            .orElseThrow(() -> new IllegalStateException("Seller not persisted"));
        Listing listing = listings.findById(LISTING_ID)
            .orElseThrow(() -> new IllegalStateException("Listing not persisted"));
        Enquiry enquiry = enquiries.findById(ENQUIRY_ID)
            .orElseThrow(() -> new IllegalStateException("Enquiry not persisted"));

        if (!"Reserved".equals(listing.getStatus()) ||
                listing.getAnimal().getHealthRecords().isEmpty()) {
            throw new IllegalStateException("Listing fields or health records were not persisted");
        }
        System.out.println("RELOADED: Seller = " + seller.getBusinessName());
        System.out.println("RELOADED: Animal = " + listing.getAnimal().getName()
            + " (" + listing.getAnimal().getType() + ")");
        System.out.println("RELOADED: Price = K" + listing.getPrice());
        System.out.println("RELOADED: Status = " + listing.getStatus());
        System.out.println("RELOADED: Health records = " + listing.getAnimal().getHealthRecords().size());
        System.out.println("RELOADED: Enquiry = " + enquiry.getMessage());
        System.out.println("PHASE 3 PERSISTENCE CHECK PASSED");
    }
}
