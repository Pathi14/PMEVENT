package fr.pmevent.dto.guest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddGuestDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String firstname;

    @Email(message = "Email is required")
    private String email;
    private String phone;

    @NotNull(message = "number_places is required")
    private Integer number_places;

    private String comment;
}
