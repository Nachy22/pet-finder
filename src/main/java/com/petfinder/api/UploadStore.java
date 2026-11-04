package com.petfinder.api;

import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Images are public by opaque ID; verification documents are never publicly served. */
public final class UploadStore {
    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final Path ROOT = Path.of(
        System.getenv().getOrDefault("PETFINDER_UPLOAD_DIR",
            System.getProperty("user.home") + "/.petfinder-uploads")
    ).toAbsolutePath().normalize();

    private UploadStore() { }

    public static String storeDocument(UploadedFile upload) throws IOException {
        return save(upload, true);
    }

    public static List<String> storePhotos(List<UploadedFile> uploads) throws IOException {
        if (uploads.size() > 8) throw new ApiException(400, "Maximum 8 animal photos");
        List<String> urls = new ArrayList<>();
        try {
            for (UploadedFile upload : uploads) urls.add(save(upload, false));
            return urls;
        } catch (IOException | RuntimeException failure) {
            for (String url : urls) delete(url);
            throw failure;
        }
    }

    private static String save(UploadedFile upload, boolean document) throws IOException {
        if (upload == null) throw new ApiException(400, "Upload is required");
        byte[] bytes;
        try (InputStream input = upload.content()) {
            bytes = input.readNBytes(MAX_BYTES + 1);
        }
        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new ApiException(400, "Each file must be between 1 byte and 5 MB");
        }
        String kind = detectType(bytes);
        if (kind == null || (!document && "pdf".equals(kind))) {
            throw new ApiException(400, document
                ? "Verification document must be PDF, JPG or PNG"
                : "Animal photos must be JPG, PNG or WebP");
        }
        String ext = kind.equals("jpeg") ? "jpg" : kind;
        Path folder = ROOT.resolve(document ? "private-documents" : "public-images");
        Files.createDirectories(folder);
        String filename = UUID.randomUUID() + "." + ext;
        Path target = folder.resolve(filename);
        Files.write(target, bytes);
        return document ? filename : "/media/" + filename;
    }

    public static Path photoPath(String filename) {
        if (!filename.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            throw new ApiException(404, "Photo not found");
        }
        Path path = ROOT.resolve("public-images").resolve(filename).normalize();
        if (!Files.isRegularFile(path)) throw new ApiException(404, "Photo not found");
        return path;
    }

    public static void delete(String saved) {
        if (saved == null || saved.isBlank()) return;
        try {
            String name = saved.startsWith("/media/") ? saved.substring(7) : saved;
            if (!name.matches("[0-9a-fA-F-]{36}\\.(pdf|jpg|png|webp)")) return;
            Path folder = ROOT.resolve(saved.startsWith("/media/")
                ? "public-images" : "private-documents");
            Files.deleteIfExists(folder.resolve(name));
        } catch (IOException ignored) { }
    }

    private static String detectType(byte[] b) {
        if (b.length >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F') return "pdf";
        if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255) return "jpeg";
        if (b.length >= 8 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71 && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10) return "png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        return null;
    }
}
