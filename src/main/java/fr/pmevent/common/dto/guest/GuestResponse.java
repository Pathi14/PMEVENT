package fr.pmevent.common.dto.guest;

import java.util.UUID;

public record GuestResponse(
        UUID id,
        String name,
        String firstname,
        String email,
        String phone,
        Integer number_places,
        String comment,
        String photoUrl,
        Boolean present,
        String qrCodeToken
) {
}
