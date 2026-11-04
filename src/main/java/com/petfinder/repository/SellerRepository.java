package com.petfinder.repository;

import com.petfinder.db.Database;
import com.petfinder.model.Seller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SellerRepository {

    public void save(Seller seller) throws SQLException {
        String sql = """
            INSERT INTO sellers
                (id, business_name, seller_type, contact_person, phone, email, town, area, verified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, seller.getId());
            statement.setString(2, seller.getBusinessName());
            statement.setString(3, seller.getSellerType());
            statement.setString(4, seller.getContactPerson());
            statement.setString(5, seller.getPhone());
            statement.setString(6, seller.getEmail());
            statement.setString(7, seller.getTown());
            statement.setString(8, seller.getArea());
            statement.setBoolean(9, seller.isVerified());
            statement.executeUpdate();
        }
    }

    public Optional<Seller> findById(int sellerId) throws SQLException {
        String sql = "SELECT * FROM sellers WHERE id = ?";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sellerId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapSeller(result)) : Optional.empty();
            }
        }
    }

    public List<Seller> findAll() throws SQLException {
        List<Seller> result = new ArrayList<>();
        String sql = "SELECT * FROM sellers ORDER BY id";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                result.add(mapSeller(rows));
            }
        }
        return result;
    }

    public boolean updateVerification(int sellerId, boolean verified) throws SQLException {
        // Phase 4 must restrict this method to authorized administrators.
        String sql = "UPDATE sellers SET verified = ? WHERE id = ?";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, verified);
            statement.setInt(2, sellerId);
            return statement.executeUpdate() == 1;
        }
    }

    private Seller mapSeller(ResultSet row) throws SQLException {
        return new Seller(
            row.getInt("id"), row.getString("business_name"),
            row.getString("seller_type"), row.getString("contact_person"),
            row.getString("phone"), row.getString("email"),
            row.getString("town"), row.getString("area"), row.getBoolean("verified")
        );
    }
}
