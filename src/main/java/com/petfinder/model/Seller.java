
package com.petfinder.model;

public class Seller {
    private final int id;
    private final String businessName;
    private final String sellerType;
    private final String contactPerson;
    private final String phone;
    private final String email;
    private final String town;
    private final String area;
    private boolean verified;

    public Seller(int id, String businessName, String sellerType,
                  String contactPerson, String phone, String email,
                  String town, String area, boolean verified) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "Seller ID must be positive"
            );
        }

        this.id = id;
        this.businessName = Animal.requireText(
            businessName, "Business name"
        );
        this.sellerType = Animal.requireText(
            sellerType, "Seller type"
        );
        this.contactPerson = Animal.requireText(
            contactPerson, "Contact person"
        );
        this.phone = Animal.requireText(phone, "Phone");
        this.email = Animal.requireText(email, "Email");
        this.town = Animal.requireText(town, "Town");

        // Existing seed sellers lack an area.
        this.area = area == null ? "" : area.trim();
        this.verified = verified;
    }

    public int getId() { return id; }
    public String getBusinessName() { return businessName; }
    public String getSellerType() { return sellerType; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getTown() { return town; }
    public String getArea() { return area; }
    public boolean isVerified() { return verified; }

    // API access to this action will be restricted later.
    public void verify() {
        verified = true;
    }
}
