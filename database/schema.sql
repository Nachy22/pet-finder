-- Pet Finder / Phase 3. Safe to execute repeatedly; existing data is preserved.
-- Run with: sudo mysql < database/schema.sql
CREATE DATABASE IF NOT EXISTS pet_finder
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_finder;

CREATE TABLE IF NOT EXISTS sellers (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    business_name VARCHAR(160) NOT NULL,
    seller_type VARCHAR(30) NOT NULL,
    contact_person VARCHAR(120) NOT NULL,
    phone VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(190) NOT NULL UNIQUE,
    town VARCHAR(100) NOT NULL,
    area VARCHAR(120) NOT NULL DEFAULT '',
    verified BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS animals (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    animal_type ENUM('Pet', 'Livestock') NOT NULL,
    name VARCHAR(160) NOT NULL,
    species VARCHAR(100) NOT NULL,
    breed VARCHAR(160) NOT NULL,
    life_stage VARCHAR(100) NOT NULL,
    sex VARCHAR(40) NOT NULL,
    colour VARCHAR(160) NOT NULL,
    temperament VARCHAR(255),
    purpose VARCHAR(255),
    CONSTRAINT chk_animal_subtype CHECK (
        (animal_type = 'Pet' AND temperament IS NOT NULL AND purpose IS NULL)
        OR (animal_type = 'Livestock' AND purpose IS NOT NULL AND temperament IS NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS health_records (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    animal_id INT NOT NULL,
    vaccine VARCHAR(180) NOT NULL,
    vaccinated_on DATE NOT NULL,
    CONSTRAINT fk_health_animal FOREIGN KEY (animal_id)
        REFERENCES animals(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS listings (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    animal_id INT NOT NULL UNIQUE,
    price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    status ENUM('Available', 'Reserved', 'Rehomed') NOT NULL DEFAULT 'Available',
    image_url VARCHAR(1024) NOT NULL DEFAULT '',
    CONSTRAINT chk_listing_price CHECK (price >= 0),
    CONSTRAINT chk_listing_quantity CHECK (quantity >= 1),
    CONSTRAINT fk_listing_seller FOREIGN KEY (seller_id)
        REFERENCES sellers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_listing_animal FOREIGN KEY (animal_id)
        REFERENCES animals(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS enquiries (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    buyer_name VARCHAR(160) NOT NULL,
    buyer_phone VARCHAR(30) NOT NULL,
    message TEXT NOT NULL,
    sent_at DATETIME NOT NULL,
    CONSTRAINT fk_enquiry_listing FOREIGN KEY (listing_id)
        REFERENCES listings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

