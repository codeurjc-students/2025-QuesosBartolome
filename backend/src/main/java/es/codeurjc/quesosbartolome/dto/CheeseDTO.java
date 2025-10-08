package es.codeurjc.quesosbartolome.dto;

import java.sql.Blob;
import java.sql.Date;

public record CheeseDTO(
    Long id,
    String name,
    double price,
    String description,
    Date manufactureDate,
    Date expirationDate,
    String Type,
    Blob image
) {}