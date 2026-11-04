
package com.petfinder.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class HealthRecord {
    private final String vaccine;
    private final LocalDate date;

    public HealthRecord(String vaccine, String date) {
        this.vaccine = Animal.requireText(vaccine, "Vaccine");

        if (date == null) {
            throw new IllegalArgumentException(
                "Vaccination date is required"
            );
        }

        try {
            this.date = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Vaccination date must use a valid YYYY-MM-DD format",
                e
            );
        }
    }

    public String getVaccine() {
        return vaccine;
    }

    public String getDate() {
        return date.toString();
    }
}
