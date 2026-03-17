package es.codeurjc.quesosbartolome.dto;

public record PasswordChangeDTO(
        String currentPassword,
        String newPassword,
        String confirmPassword) {
}