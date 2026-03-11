package es.codeurjc.quesosbartolome.dto;

public record ReviewDTO(
    Long id,
    Integer rating,
    String comment,
    UserBasicDTO user,
    CheeseBasicDTO cheese
) {}
