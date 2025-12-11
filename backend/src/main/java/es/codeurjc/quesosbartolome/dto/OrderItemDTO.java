package es.codeurjc.quesosbartolome.dto;

public record OrderItemDTO(
    CheeseBasicDTO cheese,
    Double weight,     
    Double price   
) {}