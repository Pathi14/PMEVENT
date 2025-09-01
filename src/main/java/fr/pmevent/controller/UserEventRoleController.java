package fr.pmevent.controller;

import fr.pmevent.dto.AssignRoleDto;
import fr.pmevent.service.UserEventRoleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("users-roles")
public class UserEventRoleController {
    private UserEventRoleService userEventRoleService;

    // Tester ce service

    @GetMapping
    ResponseEntity<?> findAllEvent() {
        return ResponseEntity.ok(userEventRoleService.findAllEvents());
    }

    @PostMapping("/assign-role")
    ResponseEntity<?> AssignRoleToUser(@RequestBody @Valid AssignRoleDto assignRole) {
        try {
            if (assignRole.getEventId() == null || assignRole.getUserId() == null || assignRole.getRole() == null) {
                return ResponseEntity.badRequest().body("Request is invalid");
            }

            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            userEventRoleService.AssignRoleToUSer(currentUserEmail, assignRole);
            return ResponseEntity.ok("Role successfully assigned to user");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove-role")
    public ResponseEntity<?> removeRoleFromUser(@RequestParam Long eventId,
                                                @RequestParam Long userId) {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            userEventRoleService.removeRoleFromUser(currentUserEmail, eventId, userId);
            return ResponseEntity.ok("Role successfully removed from user");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
