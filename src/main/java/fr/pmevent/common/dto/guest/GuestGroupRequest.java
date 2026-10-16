package fr.pmevent.common.dto.guest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record GuestGroupRequest(
        @NotBlank(message = "Le nom du groupe est obligatoire.")
        @Size(max = 100, message = "Le nom du groupe ne peut pas dépasser 100 caractères.")
        String name,
        @NotNull(message = "Le nombre maximum de personnes est obligatoire.")
        @Positive(message = "Le nombre maximum de personnes doit être supérieur à zéro.")
        Integer maxPeople
) {
}
