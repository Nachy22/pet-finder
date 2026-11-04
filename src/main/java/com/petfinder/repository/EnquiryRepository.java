package com.petfinder.repository;

import com.petfinder.db.Database;
import com.petfinder.model.Enquiry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnquiryRepository {

    public void save(Enquiry enquiry) throws SQLException {
        String sql = """
            INSERT INTO enquiries
              (id, listing_id, buyer_name, buyer_phone, message, sent_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, enquiry.getId());
            statement.setInt(2, enquiry.getListingId());
            statement.setString(3, enquiry.getBuyerName());
            statement.setString(4, enquiry.getBuyerPhone());
            statement.setString(5, enquiry.getMessage());
            statement.setTimestamp(6, Timestamp.valueOf(
                LocalDateTime.parse(enquiry.getSentAt())));
            statement.executeUpdate();
        }
    }

    public Optional<Enquiry> findById(int enquiryId) throws SQLException {
        String sql = "SELECT * FROM enquiries WHERE id = ?";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, enquiryId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapEnquiry(rows)) : Optional.empty();
            }
        }
    }

    public List<Enquiry> findByListingId(int listingId) throws SQLException {
        String sql = "SELECT * FROM enquiries WHERE listing_id = ? ORDER BY sent_at DESC";
        List<Enquiry> enquiries = new ArrayList<>();
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, listingId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    enquiries.add(mapEnquiry(rows));
                }
            }
        }
        return enquiries;
    }

    public List<Enquiry> findBySellerId(int sellerId) throws SQLException {
        String sql = """
            SELECT e.* FROM enquiries e
            JOIN listings l ON l.id = e.listing_id
            WHERE l.seller_id = ?
            ORDER BY e.sent_at DESC
            """;
        List<Enquiry> enquiries = new ArrayList<>();
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sellerId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    enquiries.add(mapEnquiry(rows));
                }
            }
        }
        return enquiries;
    }

    private Enquiry mapEnquiry(ResultSet row) throws SQLException {
        return new Enquiry(row.getInt("id"), row.getInt("listing_id"),
            row.getString("buyer_name"), row.getString("buyer_phone"),
            row.getString("message"), row.getTimestamp("sent_at").toLocalDateTime());
    }
}
