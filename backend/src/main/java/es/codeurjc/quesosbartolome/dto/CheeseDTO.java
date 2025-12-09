package es.codeurjc.quesosbartolome.dto;

import java.sql.Date;
import java.util.List;

public record CheeseDTO(
    Long id,
    String name,
    double price,
    String description,
    Date manufactureDate,
    Date expirationDate,
    String Type,
    List<Double> boxes
) {}