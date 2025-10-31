package fr.pmevent.dto.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String name,
        String location,
        LocalDate start_date,
        LocalDate end_date,
        String description,
        boolean publicEvent,
        LocalDateTime create_date,
        LocalDateTime update_date
) {
}

