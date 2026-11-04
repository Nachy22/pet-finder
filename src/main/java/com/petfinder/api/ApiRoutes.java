package com.petfinder.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petfinder.model.Animal;
import com.petfinder.model.HealthRecord;
import com.petfinder.model.Livestock;
import com.petfinder.model.Listing;
import com.petfinder.model.Pet;
import com.petfinder.model.Seller;
import com.petfinder.repository.AccountRepository;
import com.petfinder.repository.ApiWriteRepository;
import com.petfinder.repository.EnquiryRepository;
import com.petfinder.repository.ListingRepository;
import com.petfinder.repository.SellerRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Same-origin JSON/multipart API; unauthenticated public reads, seller-only writes. */
public final class ApiRoutes {
    private final SellerRepository sellers = new SellerRepository();
    private final ListingRepository listings = new ListingRepository();
    private final EnquiryRepository enquiries = new EnquiryRepository();
    private final AccountRepository accounts = new AccountRepository();
    private final ApiWriteRepository writer = new ApiWriteRepository();
    private final ObjectMapper json = new ObjectMapper();
    private final SecureRandom random = new SecureRandom();

    public void register(Javalin app) {
        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok")));
        app.get("/hello", ctx -> ctx.result("Pet Finder is running"));

        // Public browse data: unverified sellers' listings stay private.
        app.get("/api/data", ctx -> {
            List<Seller> visibleSellers = sellers.findAll().stream().filter(Seller::isVerified).toList();
            Set<Integer> ids = visibleSellers.stream().map(Seller::getId).collect(java.util.stream.Collectors.toSet());
            List<Listing> visible = listings.findAll().stream()
                .filter(l -> ids.contains(l.getSellerId())).toList();
            ctx.json(Map.of("sellers", visibleSellers, "listings", visible));
        });
        app.get("/api/listings", ctx -> {
            Set<Integer> verified = sellers.findAll().stream().filter(Seller::isVerified)
                .map(Seller::getId).collect(java.util.stream.Collectors.toSet());
            ctx.json(Map.of("listings", listings.findAll().stream()
                .filter(l -> verified.contains(l.getSellerId())).toList()));
        });
        app.get("/api/listings/{id}", ctx -> {
            Listing listing = listingFromPath(ctx);
            Seller seller = sellers.findById(listing.getSellerId())
                .orElseThrow(() -> new ApiException(404, "Listing not found"));
            if (!seller.isVerified() && currentSellerId(ctx) != seller.getId()) {
                throw new ApiException(404, "Listing not found");
            }
            ctx.json(Map.of("listing", listing, "seller", seller,
                "imageUrls", writer.imageUrls(listing.getId())));
        });
        app.get("/api/breeds", ctx -> {
            try (InputStream stream = ApiRoutes.class.getResourceAsStream("/public/mock/breeds.json")) {
                if (stream == null) throw new ApiException(404, "Breed profiles not found");
                ctx.contentType("application/json").result(stream.readAllBytes());
            }
        });

        app.post("/api/auth/register", ctx -> {
            String business = required(ctx.formParam("businessName"), "Business name", 160);
            String sellerType = required(ctx.formParam("sellerType"), "Seller type", 30);
            if (!Set.of("Breeder", "Farmer", "Individual").contains(sellerType))
                throw new ApiException(400, "Invalid seller type");
            String contact = required(ctx.formParam("contactPerson"), "Contact person", 120);
            String phone = phone(ctx.formParam("phone"));
            String email = required(ctx.formParam("email"), "Email", 190).toLowerCase();
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                throw new ApiException(400, "Invalid email address");
            String town = required(ctx.formParam("town"), "Town", 100);
            String area = required(ctx.formParam("area"), "Area", 120);
            String hashed = Passwords.hash(ctx.formParam("password"));
            UploadedFile document = ctx.uploadedFile("document");
            String storedDocument = UploadStore.storeDocument(document);
            try {
                Seller seller = accounts.register(business, sellerType, contact, phone, email,
                    town, area, hashed, storedDocument);
                ctx.status(201).json(Map.of("seller", seller,
                    "message", "Seller account created. Verification pending."));
            } catch (SQLException | RuntimeException e) {
                UploadStore.delete(storedDocument);
                if (e instanceof SQLIntegrityConstraintViolationException)
                    throw new ApiException(409, "Email or phone is already registered");
                throw e;
            }
        });

        app.post("/api/auth/login", ctx -> {
            Map<String, Object> body = body(ctx);
            String login = required(value(body, "emailOrPhone"), "Email or phone", 190);
            if (!login.contains("@")) login = login.replaceAll("[\\s-]", "");
            String password = value(body, "password");
            var record = accounts.lookupLogin(login);
            if (record.isEmpty() || !Passwords.verify(password, record.get().passwordHash())) {
                throw new ApiException(401, "Email or phone, or password is incorrect. Please try again.");
            }
            ctx.req().getSession(true);
            ctx.req().changeSessionId();
            ctx.sessionAttribute("sellerId", record.get().sellerId());
            String csrf = token();
            ctx.sessionAttribute("csrf", csrf);
            Seller seller = sellers.findById(record.get().sellerId()).orElseThrow();
            ctx.json(Map.of("seller", seller, "csrfToken", csrf));
        });
        app.get("/api/auth/me", ctx -> {
            int sellerId = requireSeller(ctx);
            Seller seller = sellers.findById(sellerId)
                .orElseThrow(() -> new ApiException(401, "Please log in"));

            // Older or incomplete sessions may have a seller ID but no CSRF token.
            // Map.of() rejects null values and would otherwise return HTTP 500.
            String csrf = ctx.sessionAttribute("csrf");
            if (csrf == null || csrf.isBlank()) {
                csrf = token();
                ctx.sessionAttribute("csrf", csrf);
            }

            ctx.header("Cache-Control", "no-store");
            ctx.json(Map.of("seller", seller, "csrfToken", csrf));
        });
        app.post("/api/auth/logout", ctx -> {
            requireCsrf(ctx);
            ctx.req().getSession(false).invalidate();
            ctx.json(Map.of("message", "Logged out"));
        });

        app.get("/api/sellers/me/listings", ctx -> {
            ctx.header("Cache-Control", "no-store");
            ctx.json(Map.of("listings", listings.findBySellerId(requireSeller(ctx))));
        });
        app.get("/api/sellers/me/enquiries", ctx -> {
            ctx.header("Cache-Control", "no-store");
            ctx.json(Map.of("enquiries", enquiries.findBySellerId(requireSeller(ctx))));
        });

        app.post("/api/listings", ctx -> {
            int sellerId = requireCsrf(ctx);
            String payload = required(ctx.formParam("payload"), "Listing", 20_000);
            Map<String, Object> data = parseMap(payload);
            Map<String, Object> animalData = asMap(data.get("animal"), "Animal");
            String type = required(value(animalData, "type"), "Animal type", 20);
            String name = required(value(animalData, "name"), "Name", 160);
            String species = required(value(animalData, "species"), "Species", 100);
            String breed = required(value(animalData, "breed"), "Breed", 160);
            String age = required(value(animalData, "lifeStage"), "Age group", 100);
            String sex = required(value(animalData, "sex"), "Sex", 40);
            String colour = required(value(animalData, "colour"), "Colour", 160);
            Animal animal;
            if ("Pet".equals(type)) {
                animal = new Pet(name, species, breed, age, sex, colour,
                    required(value(animalData, "temperament"), "Temperament", 255));
            } else if ("Livestock".equals(type)) {
                animal = new Livestock(name, species, breed, age, sex, colour,
                    required(value(animalData, "purpose"), "Purpose", 255));
            } else throw new ApiException(400, "Animal type must be Pet or Livestock");

            Object records = animalData.get("healthRecords");
            if (!(records instanceof List<?> list) || list.isEmpty() || list.size() > 25)
                throw new ApiException(400, "Include 1 to 25 health records");
            for (Object entry : list) {
                Map<String, Object> r = asMap(entry, "Health record");
                String date = required(value(r, "date"), "Vaccination date", 10);
                HealthRecord health = new HealthRecord(
                    required(value(r, "vaccine"), "Vaccine", 180), date);
                if (LocalDate.parse(health.getDate()).isAfter(LocalDate.now()))
                    throw new ApiException(400, "Vaccination date cannot be in the future");
                animal.addHealthRecord(health);
            }
            BigDecimal price;
            try { price = new BigDecimal(required(value(data, "price"), "Price", 20)); }
            catch (NumberFormatException e) { throw new ApiException(400, "Price must be a number"); }
            if (price.scale() > 2 || price.compareTo(new BigDecimal("9999999999.99")) > 0)
                throw new ApiException(400, "Invalid price amount");
            int quantity;
            try { quantity = Integer.parseInt(required(value(data, "quantity"), "Quantity", 10)); }
            catch (NumberFormatException e) { throw new ApiException(400, "Quantity must be a whole number"); }
            Listing listing = new Listing(1, sellerId, animal, price, quantity, "");
            List<String> photos = UploadStore.storePhotos(ctx.uploadedFiles("photos"));
            try {
                int id = writer.createListing(listing, photos);
                ctx.status(201).json(listings.findById(id).orElseThrow());
            } catch (SQLException | RuntimeException error) {
                photos.forEach(UploadStore::delete);
                throw error;
            }
        });

        app.put("/api/listings/{id}", ctx -> updateListing(ctx));
        app.patch("/api/listings/{id}", ctx -> updateListing(ctx));
        app.delete("/api/listings/{id}", ctx -> {
            int ownerId = requireCsrf(ctx);
            Listing listing = listingFromPath(ctx);
            if (listing.getSellerId() != ownerId) throw new ApiException(403, "Not your listing");
            // Cascades to animal, records and enquiries. Intended for student demo only.
            listings.delete(listing.getId());
            ctx.json(Map.of("deleted", true));
        });

        app.post("/api/enquiries", ctx -> {
            Map<String, Object> data = body(ctx);
            int id = positiveInt(value(data, "listingId"), "Listing ID");
            Listing listing = listings.findById(id)
                .orElseThrow(() -> new ApiException(404, "Listing not found"));
            Seller owner = sellers.findById(listing.getSellerId()).orElseThrow();
            if (!owner.isVerified() || "Rehomed".equals(listing.getStatus()))
                throw new ApiException(400, "This listing is not accepting enquiries");
            String name = required(value(data, "buyerName"), "Buyer name", 160);
            String phone = phone(value(data, "buyerPhone"));
            String message = required(value(data, "message"), "Message", 2000);
            ctx.status(201).json(writer.createEnquiry(id, name, phone, message));
        });
        app.post("/api/reports", ctx -> {
            Map<String, Object> data = body(ctx);
            int id = positiveInt(value(data, "listingId"), "Listing ID");
            if (listings.findById(id).isEmpty()) throw new ApiException(404, "Listing not found");
            String reason = required(value(data, "reason"), "Reason", 1000);
            writer.createReport(id, reason);
            ctx.status(201).json(Map.of("message", "Report received"));
        });
        app.get("/media/{filename}", ctx -> {
            String name = ctx.pathParam("filename");
            var path = UploadStore.photoPath(name);
            String mime = name.endsWith(".png") ? "image/png" : name.endsWith(".webp")
                ? "image/webp" : "image/jpeg";
            ctx.header("X-Content-Type-Options", "nosniff");
            ctx.contentType(mime).result(Files.newInputStream(path));
        });
    }

    private void updateListing(Context ctx) throws SQLException {
        int ownerId = requireCsrf(ctx);
        Listing listing = listingFromPath(ctx);
        if (listing.getSellerId() != ownerId) throw new ApiException(403, "Not your listing");
        Map<String, Object> data = body(ctx);
        if (!data.containsKey("price") && !data.containsKey("status"))
            throw new ApiException(400, "Provide price and/or status");
        if (data.containsKey("price")) {
            try { listings.updatePrice(listing.getId(), new BigDecimal(value(data, "price"))); }
            catch (NumberFormatException e) { throw new ApiException(400, "Invalid price"); }
        }
        if (data.containsKey("status")) listings.updateStatus(listing.getId(), value(data, "status"));
        ctx.json(listings.findById(listing.getId()).orElseThrow());
    }

    private Listing listingFromPath(Context ctx) throws SQLException {
        int id = positiveInt(ctx.pathParam("id"), "Listing ID");
        return listings.findById(id).orElseThrow(() -> new ApiException(404, "Listing not found"));
    }

    private int requireSeller(Context ctx) {
        int id = currentSellerId(ctx);
        if (id <= 0) throw new ApiException(401, "Please log in");
        return id;
    }

    private int currentSellerId(Context ctx) {
        var session = ctx.req().getSession(false);
        if (session == null) return -1;
        Object value = session.getAttribute("sellerId");
        return value instanceof Integer id ? id : -1;
    }

    private int requireCsrf(Context ctx) {
        int sellerId = requireSeller(ctx);
        String expected = ctx.sessionAttribute("csrf");
        String actual = ctx.header("X-CSRF-Token");
        if (expected == null || actual == null || !
                java.security.MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    actual.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new ApiException(403, "Invalid request token; reload the dashboard");
        }
        return sellerId;
    }

    private String token() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Map<String, Object> body(Context ctx) {
        return parseMap(ctx.body());
    }

    private Map<String, Object> parseMap(String text) {
        try {
            Map<String, Object> result = json.readValue(text, new TypeReference<Map<String, Object>>() { });
            if (result == null) throw new ApiException(400, "Expected a JSON object");
            return result;
        }
        catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ApiException(400, "Invalid JSON request");
        }
    }

    private static Map<String, Object> asMap(Object input, String name) {
        if (!(input instanceof Map<?, ?> m)) throw new ApiException(400, name + " is required");
        Map<String, Object> result = new HashMap<>();
        m.forEach((key, value) -> { if (key instanceof String s) result.put(s, value); });
        return result;
    }

    private static String value(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? null : val.toString();
    }

    private static String required(String raw, String name, int max) {
        if (raw == null || raw.isBlank()) throw new ApiException(400, name + " is required");
        String val = raw.trim();
        if (val.length() > max) throw new ApiException(400, name + " is too long");
        return val;
    }

    private static String phone(String raw) {
        String value = required(raw, "Phone", 30).replaceAll("[\\s-]", "");
        if (!value.matches("\\+?\\d{9,13}")) throw new ApiException(400, "Invalid phone number");
        return value;
    }

    private static int positiveInt(String text, String name) {
        try {
            int number = Integer.parseInt(text);
            if (number < 1) throw new NumberFormatException();
            return number;
        } catch (Exception e) { throw new ApiException(400, name + " must be a positive number"); }
    }
}
