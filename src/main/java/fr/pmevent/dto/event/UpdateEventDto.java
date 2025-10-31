package fr.pmevent.dto.event;

import fr.pmevent.validation.HasDateRange;
import fr.pmevent.validation.ValidDateRange;
import lombok.Data;

import java.time.LocalDate;

@Data
@ValidDateRange
public class UpdateEventDto implements HasDateRange {

    private String name;
    private String location;
    private LocalDate start_date;
    private LocalDate end_date;
    private String description;
    private boolean publicEvent;
}
