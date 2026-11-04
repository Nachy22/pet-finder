
package com.petfinder.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Enquiry {
    private final int id;
    private final int listingId;
    private final String buyerName;
    private final String buyerPhone;
    private final String message;
    private final LocalDateTime sentAt;

    public Enquiry(int id, int listingId, String buyerName, String buyerPhone,
                   String message, LocalDateTime sentAt) {

        if (id <= 0 || listingId <= 0) {
            throw new IllegalArgumentException(
                "Enquiry and listing IDs must be positive"
            );
        }

        this.id = id;
        this.listingId = listingId;
        this.buyerName = Animal.requireText(
            buyerName, "Buyer name"
        );
        this.buyerPhone = Animal.requireText(
            buyerPhone, "Buyer phone"
        );
        this.message = Animal.requireText(
            message, "Message"
        );
        this.sentAt = Objects.requireNonNull(
            sentAt, "Sent time is required"
        );
    }

    public int getId() { return id; }
    public int getListingId() { return listingId; }
    public String getBuyerName() { return buyerName; }
    public String getBuyerPhone() { return buyerPhone; }
    public String getMessage() { return message; }
    public String getSentAt() { return sentAt.toString(); }
}
