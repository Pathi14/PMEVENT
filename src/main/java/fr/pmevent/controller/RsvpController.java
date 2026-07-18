package fr.pmevent.controller;

import fr.pmevent.dto.guest.GuestRsvpResponse;
import fr.pmevent.service.GuestService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rsvp")
@AllArgsConstructor
public class RsvpController {
    private final GuestService guestService;

    @GetMapping("/{token}")
    public ResponseEntity<GuestRsvpResponse> getRsvpPage(@PathVariable String token) {
        return ResponseEntity.ok(guestService.getRsvpInfo(token));
    }

    @PostMapping("/{token}/respond")
    public ResponseEntity<?> respondToInvitation(
            @PathVariable String token,
            @RequestBody Map<String, Boolean> response
    ) {
        Boolean willAttend = response.get("willAttend");
        guestService.updateRsvpResponse(token, willAttend);
        return ResponseEntity.ok(Map.of("message", "Réponse enregistrée avec succès"));
    }
}
