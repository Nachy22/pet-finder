-- Phase 4 additive migration: preserve all existing Phase 3 data.
USE pet_finder;

CREATE TABLE IF NOT EXISTS seller_credentials (
    seller_id INT NOT NULL PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    verification_document VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credentials_seller FOREIGN KEY (seller_id)
        REFERENCES sellers(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS listing_images (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    CONSTRAINT fk_image_listing FOREIGN KEY (listing_id)
        REFERENCES listings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reports (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_listing FOREIGN KEY (listing_id)
        REFERENCES listings(id) ON DELETE CASCADE
) ENGINE=InnoDB;
