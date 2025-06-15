package fr.pmevent.dto.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank
    @Email
    String email;

    @NotBlank
    String password;
}
