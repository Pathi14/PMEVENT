package fr.pmevent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEventDto {
    @NotBlank
    private String name;
    private String location;
    private LocalDate start_date;
    private LocalDate end_date;
    private String description;
}
