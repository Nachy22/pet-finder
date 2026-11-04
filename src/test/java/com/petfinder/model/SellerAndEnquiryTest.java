package com.petfinder.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SellerAndEnquiryTest {

    @Test
    void sellerCanBeVerifiedAndInformationIsAvailableThroughGetters() {
        Seller seller = new Seller(1, "Pet Haven", "Breeder", "Example Contact",
            "0970000001", "haven@example.com", "Ndola", "Town Centre", false);
        assertFalse(seller.isVerified());
        assertEquals("Pet Haven", seller.getBusinessName());
        assertEquals("Ndola", seller.getTown());
        seller.verify();
        assertTrue(seller.isVerified());
    }

    @Test
    void sellerHasValidatedRequiredFields() {
        assertThrows(IllegalArgumentException.class,
            () -> new Seller(0, "Pet Haven", "Breeder", "Example Contact", "0970000001",
                "haven@example.com", "Ndola", "Town Centre", false));
        assertThrows(IllegalArgumentException.class,
            () -> new Seller(1, " ", "Breeder", "Example Contact", "0970000001",
                "haven@example.com", "Ndola", "Town Centre", false));
    }

    @Test
    void enquiryRecordsBuyerAndTime() {
        LocalDateTime sentAt = LocalDateTime.of(2026, 11, 3, 10, 30);
        Enquiry enquiry = new Enquiry(7, 10, "Sample Buyer", "0970000002",
            "Is Duke available?", sentAt);
        assertEquals(10, enquiry.getListingId());
        assertEquals("Sample Buyer", enquiry.getBuyerName());
        assertEquals("2026-11-03T10:30", enquiry.getSentAt());
        assertEquals("Is Duke available?", enquiry.getMessage());
    }

    @Test
    void enquiryRejectsMissingMessageAndTimestamp() {
        LocalDateTime time = LocalDateTime.of(2026, 11, 3, 10, 30);
        assertThrows(IllegalArgumentException.class,
            () -> new Enquiry(1, 10, "Buyer", "0970000002", " ", time));
        assertThrows(NullPointerException.class,
            () -> new Enquiry(1, 10, "Buyer", "0970000002", "Hello", null));
        assertThrows(IllegalArgumentException.class,
            () -> new Enquiry(1, 0, "Buyer", "0970000002", "Hello", time));
    }
}
