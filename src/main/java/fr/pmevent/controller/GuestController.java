package fr.pmevent.controller;

import fr.pmevent.dto.guest.AddGuestDto;
import fr.pmevent.dto.guest.GuestResponse;
import fr.pmevent.dto.guest.UpdateGuestDto;
import fr.pmevent.service.GuestService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/event/{eventId}/new-guest")
    ResponseEntity<GuestResponse> addGuest(@PathVariable Long eventId, @RequestBody @Valid AddGuestDto guestDto) {
        GuestResponse guest = guestService.addGuest(eventId, guestDto);
        return ResponseEntity.ok(guest);
    }

    @PutMapping("/{id}")
    ResponseEntity<GuestResponse> updateGuest(@PathVariable Long id, @RequestBody @Valid UpdateGuestDto guestDto) {
        GuestResponse guest = guestService.updateGuest(id, guestDto);
        return ResponseEntity.ok(guest);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> removeGuest(@PathVariable Long id) {
        guestService.removeGuest(id);
        return ResponseEntity.noContent().build();
    }
}
