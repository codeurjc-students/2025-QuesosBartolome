package es.codeurjc.quesosbartolome.dto;

import java.sql.Blob;


public record UserDTO(
    Long id,
    String name,
    String password,
    String gmail,
    String direction,
    String nif,
    Blob image
) {}