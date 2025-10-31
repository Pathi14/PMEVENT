package fr.pmevent.dto.event;

import fr.pmevent.validation.HasDateRange;
import fr.pmevent.validation.ValidDateRange;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@ValidDateRange
public class CreateEventDto implements HasDateRange {
    @NotBlank(message = "Name is required")
    private String name;
    private String location;
    private LocalDate start_date;
    private LocalDate end_date;
    private String description;
    private boolean publicEvent = false;
}
