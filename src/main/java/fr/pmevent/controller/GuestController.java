package fr.pmevent.controller;

import fr.pmevent.dto.guest.AddGuestDto;
import fr.pmevent.dto.guest.GuestResponse;
import fr.pmevent.dto.guest.UpdateGuestDto;
import fr.pmevent.service.GuestService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/guests")
@AllArgsConstructor
public class GuestController {
    private final GuestService guestService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<GuestResponse>> getAllGuestOfOneEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(guestService.getAllGuestOfOneEvent(eventId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuestResponse> findGuest(@PathVariable long id) {
        GuestResponse guest = guestService.getGuestById(id);
        return ResponseEntity.ok(guest);
    }

    @PostMapping(
            value = "/event/{eventId}/new-guest",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<GuestResponse> addGuest(
            @PathVariable Long eventId,
            @ModelAttribute AddGuestDto guestDto
    ) {
        GuestResponse guest = guestService.addGuest(eventId, guestDto);
        return ResponseEntity.ok(guest);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<?> updateGuest(
            @PathVariable Long id,
            @ModelAttribute UpdateGuestDto guestDto
    ) {
        try {
            GuestResponse guest = guestService.updateGuest(id, guestDto);
            return ResponseEntity.ok(guest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> removeGuest(@PathVariable Long id) {
        guestService.removeGuest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/send-reminder")
    public ResponseEntity<?> sendReminder(@PathVariable Long id) {
        guestService.sendReminderEmail(id);
        return ResponseEntity.ok(Map.of("message", "Rappel envoyé"));
    }

}
