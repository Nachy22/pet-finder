package com.petfinder.repository;

import com.petfinder.db.Database;
import com.petfinder.exception.ListingNotFoundException;
import com.petfinder.model.Animal;
import com.petfinder.model.HealthRecord;
import com.petfinder.model.Livestock;
import com.petfinder.model.Listing;
import com.petfinder.model.Pet;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListingRepository {

    /** Saves animal, all health records and listing as one database transaction. */
    public void save(Listing listing) throws SQLException {
        try (Connection connection = Database.connect()) {
            connection.setAutoCommit(false);
            try {
                int animalId = insertAnimal(connection, listing.getAnimal());
                insertHealthRecords(connection, animalId, listing.getAnimal());
                insertListing(connection, listing, animalId);
                connection.commit();
            } catch (SQLException | RuntimeException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    error.addSuppressed(rollbackFailure);
                }
                throw error;
            }
        }
    }

    private int insertAnimal(Connection connection, Animal animal) throws SQLException {
        String sql = """
            INSERT INTO animals
              (animal_type, name, species, breed, life_stage, sex, colour, temperament, purpose)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, animal.getType());
            statement.setString(2, animal.getName());
            statement.setString(3, animal.getSpecies());
            statement.setString(4, animal.getBreed());
            statement.setString(5, animal.getLifeStage());
            statement.setString(6, animal.getSex());
            statement.setString(7, animal.getColour());
            statement.setString(8, animal instanceof Pet pet ? pet.getTemperament() : null);
            statement.setString(9, animal instanceof Livestock livestock ? livestock.getPurpose() : null);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Could not obtain inserted animal ID");
                }
                return keys.getInt(1);
            }
        }
    }

    private void insertHealthRecords(Connection connection, int animalId, Animal animal)
            throws SQLException {
        String sql = "INSERT INTO health_records (animal_id, vaccine, vaccinated_on) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (HealthRecord record : animal.getHealthRecords()) {
                statement.setInt(1, animalId);
                statement.setString(2, record.getVaccine());
                statement.setDate(3, Date.valueOf(record.getDate()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertListing(Connection connection, Listing listing, int animalId)
            throws SQLException {
        String sql = """
            INSERT INTO listings
              (id, seller_id, animal_id, price, quantity, status, image_url)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, listing.getId());
            statement.setInt(2, listing.getSellerId());
            statement.setInt(3, animalId);
            statement.setBigDecimal(4, listing.getPrice());
            statement.setInt(5, listing.getQuantity());
            statement.setString(6, listing.getStatus());
            statement.setString(7, listing.getImageUrl());
            statement.executeUpdate();
        }
    }

    public Optional<Listing> findById(int listingId) throws SQLException {
        try (Connection connection = Database.connect()) {
            return findById(connection, listingId);
        }
    }

    private Optional<Listing> findById(Connection connection, int listingId) throws SQLException {
        String sql = """
            SELECT l.id AS listing_id, l.seller_id, l.price, l.quantity,
                   l.status, l.image_url, a.id AS animal_id, a.animal_type,
                   a.name, a.species, a.breed, a.life_stage, a.sex, a.colour,
                   a.temperament, a.purpose
            FROM listings l
            JOIN animals a ON a.id = l.animal_id
            WHERE l.id = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, listingId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    return Optional.empty();
                }
                Animal animal;
                String type = row.getString("animal_type");
                if ("Pet".equals(type)) {
                    animal = new Pet(row.getString("name"), row.getString("species"),
                        row.getString("breed"), row.getString("life_stage"),
                        row.getString("sex"), row.getString("colour"),
                        row.getString("temperament"));
                } else if ("Livestock".equals(type)) {
                    animal = new Livestock(row.getString("name"), row.getString("species"),
                        row.getString("breed"), row.getString("life_stage"),
                        row.getString("sex"), row.getString("colour"),
                        row.getString("purpose"));
                } else {
                    throw new SQLException("Unknown animal type in database: " + type);
                }

                loadHealthRecords(connection, row.getInt("animal_id"), animal);

                Listing listing = new Listing(row.getInt("listing_id"),
                    row.getInt("seller_id"), animal, row.getBigDecimal("price"),
                    row.getInt("quantity"), row.getString("image_url"));
                listing.setStatus(row.getString("status"));
                return Optional.of(listing);
            }
        }
    }

    private void loadHealthRecords(Connection connection, int animalId, Animal animal)
            throws SQLException {
        String sql = "SELECT vaccine, vaccinated_on FROM health_records WHERE animal_id = ? ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, animalId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    animal.addHealthRecord(new HealthRecord(
                        rows.getString("vaccine"), rows.getDate("vaccinated_on").toString()
                    ));
                }
            }
        }
    }

    public List<Listing> findAll() throws SQLException {
        List<Listing> listings = new ArrayList<>();
        try (Connection connection = Database.connect()) {
            for (int id : listingIds(connection, null)) {
                listings.add(findById(connection, id).orElseThrow());
            }
        }
        return listings;
    }

    public List<Listing> findBySellerId(int sellerId) throws SQLException {
        List<Listing> listings = new ArrayList<>();
        try (Connection connection = Database.connect()) {
            for (int id : listingIds(connection, sellerId)) {
                listings.add(findById(connection, id).orElseThrow());
            }
        }
        return listings;
    }

    private List<Integer> listingIds(Connection connection, Integer sellerId) throws SQLException {
        String sql = sellerId == null
                ? "SELECT id FROM listings ORDER BY id"
                : "SELECT id FROM listings WHERE seller_id = ? ORDER BY id";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (sellerId != null) {
                statement.setInt(1, sellerId);
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    ids.add(rows.getInt("id"));
                }
            }
        }
        return ids;
    }

    public void updatePrice(int listingId, BigDecimal newPrice) throws SQLException {
        if (newPrice == null || newPrice.signum() < 0) {
            throw new IllegalArgumentException("Price must be zero or higher");
        }
        String sql = "UPDATE listings SET price = ? WHERE id = ?";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, newPrice);
            statement.setInt(2, listingId);
            if (statement.executeUpdate() == 0 && findById(listingId).isEmpty()) {
                throw new ListingNotFoundException(listingId);
            }
        }
    }

    public void updateStatus(int listingId, String status) throws SQLException {
        if (!"Available".equals(status) && !"Reserved".equals(status)
                && !"Rehomed".equals(status)) {
            throw new IllegalArgumentException("Unknown listing status: " + status);
        }
        String sql = "UPDATE listings SET status = ? WHERE id = ?";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, listingId);
            if (statement.executeUpdate() == 0 && findById(listingId).isEmpty()) {
                throw new ListingNotFoundException(listingId);
            }
        }
    }

    /** Deletes the animal, cascading to the listing, health records and its enquiries. */
    public void delete(int listingId) throws SQLException {
        try (Connection connection = Database.connect()) {
            connection.setAutoCommit(false);
            try {
                Integer animalId = null;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT animal_id FROM listings WHERE id = ? FOR UPDATE")) {
                    statement.setInt(1, listingId);
                    try (ResultSet row = statement.executeQuery()) {
                        if (row.next()) {
                            animalId = row.getInt(1);
                        }
                    }
                }
                if (animalId == null) {
                    throw new ListingNotFoundException(listingId);
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM animals WHERE id = ?")) {
                    statement.setInt(1, animalId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException error) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    error.addSuppressed(rollbackFailure);
                }
                throw error;
            }
        }
    }
}
