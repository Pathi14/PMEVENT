package fr.pmevent.controller;

import fr.pmevent.dto.user.UpdateUser;
import fr.pmevent.entity.UserEntity;
import fr.pmevent.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody @Valid UpdateUser userDto) {
        UserEntity user = userService.updateUser(userId, userDto);
        return ResponseEntity.ok("user " + user.getName() + " successfully updated.");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.ok("user delete successfully.");
    }
}
