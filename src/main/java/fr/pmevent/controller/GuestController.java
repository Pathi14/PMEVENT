package fr.pmevent.controller;

import fr.pmevent.dto.guest.AddGuestDto;
import fr.pmevent.dto.guest.UpdateGuestDto;
import fr.pmevent.entity.GuestEntity;
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
    public ResponseEntity<List<GuestEntity>> getAllGuestOfOneEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(guestService.getAllGuestOfOneEvent(eventId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findGuest(@PathVariable long id) {
        GuestEntity guest = guestService.getGuestById(id);
        return ResponseEntity.ok(guest);
    }

    @PostMapping("/event/{eventId}/new-guest")
    ResponseEntity<?> addGuest(@PathVariable Long eventId, @RequestBody @Valid AddGuestDto guestDto) {
        GuestEntity guest = guestService.addGuest(eventId, guestDto);
        return ResponseEntity.ok("Guest " + guest.getName() + " successfully added.");
    }

    @PutMapping("/{id}")
    ResponseEntity<?> updateGuest(@PathVariable Long id, @RequestBody @Valid UpdateGuestDto guestDto) {
        GuestEntity guest = guestService.updateGuest(id, guestDto);
        return ResponseEntity.ok("Guest " + guest.getName() + " successfully updated.");
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> removeGuest(@PathVariable Long id) {
        guestService.removeGuest(id);
        return ResponseEntity.ok("Guest removed successfully.");
    }
}
