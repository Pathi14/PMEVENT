package fr.pmevent.common.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUser {
    private String name;
    private String firstname;
    private MultipartFile photo;

    @Email(message = "L'email n'est pas valide")
    private String email;
    private String password;
}
