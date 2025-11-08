package fr.pmevent.dto.guest;

public record GuestResponse(
        Long id,
        String name,
        String firstname,
        String email,
        String phone,
        Integer number_places,
        String comment
) {
}
