package fr.pmevent.controller;

import fr.pmevent.common.dto.guest.AddGuestDto;
import fr.pmevent.common.dto.guest.GuestResponse;
import fr.pmevent.common.dto.guest.UpdateGuestDto;
import fr.pmevent.entity.GuestEntity;
import fr.pmevent.service.GuestService;
import fr.pmevent.service.QrCodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/guests")
@AllArgsConstructor
public class GuestController {
    private final GuestService guestService;
    private final QrCodeService qrCodeService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<GuestResponse>> getAllGuestOfOneEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(guestService.getAllGuestOfOneEvent(eventId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuestResponse> findGuest(@PathVariable UUID id) {
        GuestResponse guest = guestService.getGuestById(id);
        return ResponseEntity.ok(guest);
    }

    @PostMapping(
            value = "/event/{eventId}/new-guest",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    ResponseEntity<GuestResponse> addGuest(
            @PathVariable UUID eventId,
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
            @PathVariable UUID id,
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
    ResponseEntity<Void> removeGuest(@PathVariable UUID id) {
        guestService.removeGuest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/send-reminder")
    public ResponseEntity<?> sendReminder(@PathVariable UUID id) {
        guestService.sendReminderEmail(id);
        return ResponseEntity.ok(Map.of("message", "Rappel envoyé"));
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getGuestQrCode(@PathVariable UUID id) {

        GuestEntity guest = guestService.getGuestEntity(id);
        String text = "GUEST:" + guest.getId() + ":" + guest.getQrCodeToken();

        byte[] qr = qrCodeService.generateQRCode(text);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qr);
    }

    @PostMapping("/verify-qr")
    public ResponseEntity<?> verifyQr(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");

        return ResponseEntity.ok(guestService.verifyQrCode(code));
    }

    @PostMapping("/{id}/present")
    public ResponseEntity<?> markPresent(@PathVariable UUID id) {
        guestService.markPresent(id);
        return ResponseEntity.ok().build();
    }

}
