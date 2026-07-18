package fr.pmevent.common.dto.guest;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateGuestDto {
    private String name;

    private String firstname;

    @Email(message = "Email is required")
    private String email;
    private String phone;

    private Integer number_places;

    private String comment;
    private MultipartFile photo;
}
