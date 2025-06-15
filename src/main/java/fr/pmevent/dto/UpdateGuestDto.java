package fr.pmevent.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateGuestDto {
    private String name;

    private String firstname;

    @Email(message = "Email is required")
    private String email;
    private String phone;

    private Integer number_places;

    private String comment;
}
