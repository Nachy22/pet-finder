package com.petfinder.repository;

import com.petfinder.db.Database;
import com.petfinder.model.Animal;
import com.petfinder.model.Enquiry;
import com.petfinder.model.HealthRecord;
import com.petfinder.model.Livestock;
import com.petfinder.model.Listing;
import com.petfinder.model.Pet;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Inserts with MySQL-generated IDs for HTTP clients (Phase 3 demo used fixed IDs). */
public class ApiWriteRepository {

    public int createListing(Listing listing, List<String> imageUrls) throws SQLException {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                Animal a = listing.getAnimal();
                int animalId;
                try (PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO animals
                         (animal_type,name,species,breed,life_stage,sex,colour,temperament,purpose)
                        VALUES (?,?,?,?,?,?,?,?,?)
                        """, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, a.getType());
                    ps.setString(2, a.getName());
                    ps.setString(3, a.getSpecies());
                    ps.setString(4, a.getBreed());
                    ps.setString(5, a.getLifeStage());
                    ps.setString(6, a.getSex());
                    ps.setString(7, a.getColour());
                    ps.setString(8, a instanceof Pet p ? p.getTemperament() : null);
                    ps.setString(9, a instanceof Livestock l ? l.getPurpose() : null);
                    ps.executeUpdate();
                    animalId = generatedId(ps);
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO health_records (animal_id, vaccine, vaccinated_on) VALUES (?, ?, ?)")) {
                    for (HealthRecord record : a.getHealthRecords()) {
                        ps.setInt(1, animalId);
                        ps.setString(2, record.getVaccine());
                        ps.setDate(3, Date.valueOf(record.getDate()));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                int listingId;
                try (PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO listings (seller_id,animal_id,price,quantity,status,image_url)
                        VALUES (?,?,?,?,?,?)
                        """, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, listing.getSellerId());
                    ps.setInt(2, animalId);
                    ps.setBigDecimal(3, listing.getPrice());
                    ps.setInt(4, listing.getQuantity());
                    ps.setString(5, listing.getStatus());
                    ps.setString(6, imageUrls.isEmpty() ? "" : imageUrls.get(0));
                    ps.executeUpdate();
                    listingId = generatedId(ps);
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO listing_images (listing_id,image_url) VALUES (?,?)")) {
                    for (String url : imageUrls) {
                        ps.setInt(1, listingId);
                        ps.setString(2, url);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return listingId;
            } catch (SQLException | RuntimeException error) {
                c.rollback();
                throw error;
            }
        }
    }

    public List<String> imageUrls(int listingId) throws SQLException {
        List<String> urls = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(
                "SELECT image_url FROM listing_images WHERE listing_id = ? ORDER BY id")) {
            ps.setInt(1, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) urls.add(rs.getString(1));
            }
        }
        return urls;
    }

    public Enquiry createEnquiry(int listingId, String buyerName, String buyerPhone,
                                  String message) throws SQLException {
        LocalDateTime time = LocalDateTime.now();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement("""
                INSERT INTO enquiries (listing_id,buyer_name,buyer_phone,message,sent_at)
                VALUES (?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, listingId);
            ps.setString(2, buyerName);
            ps.setString(3, buyerPhone);
            ps.setString(4, message);
            ps.setTimestamp(5, Timestamp.valueOf(time));
            ps.executeUpdate();
            return new Enquiry(generatedId(ps), listingId, buyerName, buyerPhone, message, time);
        }
    }

    public void createReport(int listingId, String reason) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO reports (listing_id,reason) VALUES (?,?)")) {
            ps.setInt(1, listingId);
            ps.setString(2, reason);
            ps.executeUpdate();
        }
    }

    private static int generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) throw new SQLException("Database did not return a generated ID");
            return rs.getInt(1);
        }
    }
}
