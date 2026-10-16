package fr.pmevent.controller;

import fr.pmevent.common.dto.guest.GuestGroupRequest;
import fr.pmevent.common.dto.guest.GuestGroupResponse;
import fr.pmevent.service.GuestGroupService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/guest-groups")
@AllArgsConstructor
public class GuestGroupController {

    private final GuestGroupService guestGroupService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<GuestGroupResponse>> getGroupsForEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(guestGroupService.getGroupsForEvent(eventId));
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<GuestGroupResponse> createGroup(
            @PathVariable UUID eventId,
            @Valid @RequestBody GuestGroupRequest request
    ) {
        return ResponseEntity.ok(guestGroupService.createGroup(eventId, request));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GuestGroupResponse> updateGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody GuestGroupRequest request
    ) {
        return ResponseEntity.ok(guestGroupService.updateGroup(groupId, request));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "false") boolean deleteGuests
    ) {
        guestGroupService.deleteGroup(groupId, deleteGuests);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/guests/{guestId}")
    public ResponseEntity<GuestGroupResponse> addGuest(
            @PathVariable UUID groupId,
            @PathVariable UUID guestId
    ) {
        return ResponseEntity.ok(guestGroupService.addGuest(groupId, guestId));
    }

    @DeleteMapping("/{groupId}/guests/{guestId}")
    public ResponseEntity<GuestGroupResponse> removeGuest(
            @PathVariable UUID groupId,
            @PathVariable UUID guestId
    ) {
        return ResponseEntity.ok(guestGroupService.removeGuest(groupId, guestId));
    }
}
