
package com.petfinder.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Listing {
    private final int id;
    private final int sellerId;
    private final Animal animal;
    private BigDecimal price;
    private int quantity;
    private String status;
    private final String imageUrl;

    public Listing(int id, int sellerId, Animal animal, BigDecimal price,
                   int quantity, String imageUrl) {

        if (id <= 0 || sellerId <= 0) {
            throw new IllegalArgumentException(
                "Listing and seller IDs must be positive"
            );
        }

        this.animal = Objects.requireNonNull(
            animal, "Animal is required"
        );

        if (animal.getHealthRecords().isEmpty()) {
            throw new IllegalArgumentException(
                "At least one health record is required"
            );
        }

        this.id = id;
        this.sellerId = sellerId;

        setPrice(price);
        setQuantity(quantity);

        this.status = "Available";
        this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
    }

    public int getId() { return id; }
    public int getSellerId() { return sellerId; }
    public Animal getAnimal() { return animal; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }

    public void setPrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() < 0) {
            throw new IllegalArgumentException(
                "Price cannot be negative"
            );
        }
        price = newPrice;
    }

    public void setQuantity(int newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException(
                "Quantity must be at least 1"
            );
        }
        quantity = newQuantity;
    }

    public void setStatus(String newStatus) {
        if (!"Available".equals(newStatus)
                && !"Reserved".equals(newStatus)
                && !"Rehomed".equals(newStatus)) {
            throw new IllegalArgumentException(
                "Unknown listing status: " + newStatus
            );
        }
        status = newStatus;
    }
}
