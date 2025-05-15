package fr.pmevent.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank
    String email;

    @NotBlank
    String password;
}
