package fr.pmevent.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUser {
    private String name;
    private String firstname;

    @Email
    private String email;
    private String password;
}
