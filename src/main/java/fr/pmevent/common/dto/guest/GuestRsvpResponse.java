package fr.pmevent.common.dto.guest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestRsvpResponse {
    private UUID guestId;
    private String guestName;
    private String guestFirstname;
    private Integer numberPlaces;
    private Boolean currentResponse; // null si pas encore répondu

    // Informations sur l'événement
    private String eventName;
    private String eventDescription;
    private LocalDate eventDate;
    private String eventLocation;
    private String eventImageUrl;
}
