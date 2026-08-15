package fr.pmevent.common.dto.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponseDto(
        UUID id,
        String name,
        String location,
        LocalDate start_date,
        LocalDate end_date,
        String description,
        boolean publicEvent,
        String imageUrl,
        LocalDateTime create_date,
        LocalDateTime update_date
) {
}

