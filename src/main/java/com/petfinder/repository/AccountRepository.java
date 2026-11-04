package com.petfinder.repository;

import com.petfinder.db.Database;
import com.petfinder.model.Seller;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class AccountRepository {
    /** Create seller and credentials atomically; verification defaults to pending. */
    public Seller register(String businessName, String sellerType, String contactPerson,
                           String phone, String email, String town, String area,
                           String passwordHash, String verificationDocument) throws SQLException {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                int sellerId;
                String sql = """
                    INSERT INTO sellers
                      (business_name, seller_type, contact_person, phone, email, town, area, verified)
                    VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)
                    """;
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, businessName);
                    ps.setString(2, sellerType);
                    ps.setString(3, contactPerson);
                    ps.setString(4, phone);
                    ps.setString(5, email);
                    ps.setString(6, town);
                    ps.setString(7, area);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("New seller ID was not returned");
                        sellerId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO seller_credentials (seller_id, password_hash, verification_document) VALUES (?, ?, ?)")) {
                    ps.setInt(1, sellerId);
                    ps.setString(2, passwordHash);
                    ps.setString(3, verificationDocument);
                    ps.executeUpdate();
                }
                c.commit();
                return new Seller(sellerId, businessName, sellerType, contactPerson,
                    phone, email, town, area, false);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public record LoginRecord(int sellerId, String passwordHash) { }

    public Optional<LoginRecord> lookupLogin(String emailOrPhone) throws SQLException {
        String sql = """
            SELECT s.id, c.password_hash FROM sellers s
            JOIN seller_credentials c ON s.id = c.seller_id
            WHERE LOWER(s.email) = LOWER(?) OR s.phone = ?
            """;
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, emailOrPhone);
            ps.setString(2, emailOrPhone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new LoginRecord(rs.getInt(1), rs.getString(2)));
            }
        }
    }
}
