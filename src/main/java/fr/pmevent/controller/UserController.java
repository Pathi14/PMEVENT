package fr.pmevent.controller;

import fr.pmevent.dto.user.UpdateUser;
import fr.pmevent.dto.user.UserResponseDto;
import fr.pmevent.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    @PutMapping(
            value = "/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @ModelAttribute UpdateUser userDto
    ) {
        try {
            UserResponseDto user = userService.updateUser(userId, userDto);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/verify-password")
    public ResponseEntity<?> verifyPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String currentPassword = request.get("currentPassword");

        boolean valid = userService.verifyPassword(id, currentPassword);

        if (valid) {
            return ResponseEntity.ok(Map.of("valid", true));
        } else {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        UserResponseDto user = userService.getCurrentUserInfo();
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
