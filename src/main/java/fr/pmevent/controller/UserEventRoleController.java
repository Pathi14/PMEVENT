package fr.pmevent.controller;

import fr.pmevent.dto.AssignRoleDto;
import fr.pmevent.service.UserEventRoleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("users-roles")
public class UserEventRoleController {
    private UserEventRoleService userEventRoleService;

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
            userEventRoleService.AssignRoleToUSer(assignRole);
            return ResponseEntity.ok("Role successfully assigned to user");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
