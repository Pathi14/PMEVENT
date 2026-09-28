package fr.pmevent.common.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmation(
        @NotBlank String token,
        @NotBlank @Size(min = 6) String password
) {
}