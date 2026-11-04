-- Phase 5 read-only database integrity report. This file does not change data.
USE pet_finder;

SELECT 'sellers' AS table_name, COUNT(*) AS total FROM sellers
UNION ALL SELECT 'animals', COUNT(*) FROM animals
UNION ALL SELECT 'health_records', COUNT(*) FROM health_records
UNION ALL SELECT 'listings', COUNT(*) FROM listings
UNION ALL SELECT 'enquiries', COUNT(*) FROM enquiries
UNION ALL SELECT 'seller_credentials', COUNT(*) FROM seller_credentials
UNION ALL SELECT 'listing_images', COUNT(*) FROM listing_images
UNION ALL SELECT 'reports', COUNT(*) FROM reports;

SELECT 'listings_without_seller' AS check_name, COUNT(*) AS violations
FROM listings l LEFT JOIN sellers s ON s.id = l.seller_id WHERE s.id IS NULL
UNION ALL
SELECT 'listings_without_animal', COUNT(*)
FROM listings l LEFT JOIN animals a ON a.id = l.animal_id WHERE a.id IS NULL
UNION ALL
SELECT 'health_records_without_animal', COUNT(*)
FROM health_records h LEFT JOIN animals a ON a.id = h.animal_id WHERE a.id IS NULL
UNION ALL
SELECT 'enquiries_without_listing', COUNT(*)
FROM enquiries e LEFT JOIN listings l ON l.id = e.listing_id WHERE l.id IS NULL
UNION ALL
SELECT 'credentials_without_seller', COUNT(*)
FROM seller_credentials c LEFT JOIN sellers s ON s.id = c.seller_id WHERE s.id IS NULL
UNION ALL
SELECT 'images_without_listing', COUNT(*)
FROM listing_images i LEFT JOIN listings l ON l.id = i.listing_id WHERE l.id IS NULL
UNION ALL
SELECT 'reports_without_listing', COUNT(*)
FROM reports r LEFT JOIN listings l ON l.id = r.listing_id WHERE l.id IS NULL
UNION ALL
SELECT 'listings_without_health_record', COUNT(*)
FROM listings l LEFT JOIN health_records h ON h.animal_id = l.animal_id
WHERE h.id IS NULL
UNION ALL
SELECT 'negative_price', COUNT(*) FROM listings WHERE price < 0
UNION ALL
SELECT 'invalid_quantity', COUNT(*) FROM listings WHERE quantity < 1;

SELECT status, COUNT(*) AS total FROM listings GROUP BY status;
SELECT verified, COUNT(*) AS total FROM sellers GROUP BY verified;
