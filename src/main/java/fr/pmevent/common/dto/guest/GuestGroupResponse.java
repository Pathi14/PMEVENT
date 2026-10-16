package fr.pmevent.common.dto.guest;

import java.util.List;
import java.util.UUID;

public record GuestGroupResponse(
        UUID id,
        String name,
        Integer maxPeople,
        long occupiedPlaces,
        List<GuestResponse> guests
) {
}
