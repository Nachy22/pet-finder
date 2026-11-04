package com.petfinder;

import com.petfinder.api.ApiException;
import com.petfinder.api.ApiRoutes;
import com.petfinder.exception.ListingNotFoundException;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.config.SizeUnit;
import java.sql.SQLException;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.http.maxRequestSize = 50_000_000L;
            config.jetty.multipartConfig.maxFileSize(5L, SizeUnit.MB);
            config.jetty.multipartConfig.maxTotalRequestSize(45L, SizeUnit.MB);
            config.jetty.multipartConfig.maxInMemoryFileSize(1, SizeUnit.MB);
        });

        app.exception(ApiException.class,
            (error, ctx) -> ctx.status(error.status()).json(Map.of("error", error.getMessage())));
        app.exception(ListingNotFoundException.class,
            (error, ctx) -> ctx.status(404).json(Map.of("error", error.getMessage())));
        app.exception(IllegalArgumentException.class,
            (error, ctx) -> ctx.status(400).json(Map.of("error", error.getMessage())));
        app.exception(SQLException.class, (error, ctx) -> {
            error.printStackTrace();
            ctx.status(500).json(Map.of("error", "Database operation failed"));
        });
        app.exception(Exception.class, (error, ctx) -> {
            error.printStackTrace();
            ctx.status(500).json(Map.of("error", "Unexpected server error"));
        });

        new ApiRoutes().register(app);
        app.start(7070);
        System.out.println("Pet Finder started: http://localhost:7070");
    }
}
