package fr.pmevent.common.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUser {
    private String name;
    private String firstname;
    private MultipartFile photo;

    @Email
    private String email;
    private String password;
}
